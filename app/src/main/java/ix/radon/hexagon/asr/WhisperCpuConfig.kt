package ix.radon.hexagon.asr

import android.util.Log
import java.io.BufferedReader
import java.io.FileReader

object WhisperCpuConfig {
    val preferredThreadCount: Int
        //The preferred thread count is 2 or more threads. Minimum is 2
        //Refer to the `companion object` at the bottom of `CpuInfo`
        get() = CpuInfo.getHighPerfCpuCount().coerceAtLeast(2)
}

private class CpuInfo(private val lines: List<String>) {
    //Calls the functions below
    private fun getHighPerfCpuCount(): Int = try {
        getHighPerfCpuCountByFrequencies()
    } catch (e: Exception) {
        Log.d(LOG_TAG, "Couldn't read CPU frequencies", e)
        getHighPerfCpuCountByVariant()
    }

    //Gets the (Total cores - # of cores in the lowest (max) clock freq cluster)
    private fun getHighPerfCpuCountByFrequencies(): Int =
        getCpuValues(property = "processor") { getMaxCpuFrequency(it.toInt()) }
            .also { Log.d(LOG_TAG, "Binned cpu frequencies (frequency, count): ${it.binnedValues()}") }
            .countDroppingMin()

    //Gets the (Total cores - # of cores in the lowest (max) clock freq cluster)
    private fun getHighPerfCpuCountByVariant(): Int =
        getCpuValues(property = "CPU variant") { it.substringAfter("0x").toInt(radix = 16) }
            .also { Log.d(LOG_TAG, "Binned cpu variants (variant, count): ${it.binnedValues()}") }
            .countDroppingMin()

    //Count the number of elements which have the same value
    private fun List<Int>.binnedValues() = groupingBy { it }.eachCount()

    //String processing magic. Finally, sorts ints in ascending order and returns it as a list
    private fun getCpuValues(property: String, mapper: (String) -> Int) = lines
        .asSequence()
        .filter { it.startsWith(property) }
        .map { mapper(it.substringAfter(':').trim()) }
        .sorted()
        .toList()

    //Get the no. of elements in the given list which are
    //greater than the `minimum of the list`
    private fun List<Int>.countDroppingMin(): Int {
        val min = min()
        return count { it > min }
    }

    companion object {
        private const val LOG_TAG = "WhisperCpuConfig"

        //Tries to get the count of high-perf cores.
        fun getHighPerfCpuCount(): Int = try {
            readCpuInfo().getHighPerfCpuCount()
        } catch (e: Exception) {
            Log.d(LOG_TAG, "Couldn't read CPU info", e)
            //If all else fails, our best guess -- just return the # of cores - 4.
            (Runtime.getRuntime().availableProcessors() - 4).coerceAtLeast(0)
        }

        //Reads `cpuinfo` and gives us a `CpuInfo`, which contains the file as a list of lines/strings
        private fun readCpuInfo() = CpuInfo(
            BufferedReader(FileReader("/proc/cpuinfo"))
                .useLines { it.toList() }
        )

        //Reads the `sys file` of the specified core (`cpuIndex`) and
        //returns its maximum-attainable freq in KHz
        private fun getMaxCpuFrequency(cpuIndex: Int): Int {
            val path = "/sys/devices/system/cpu/cpu${cpuIndex}/cpufreq/cpuinfo_max_freq"
            val maxFreq = BufferedReader(FileReader(path)).use { it.readLine() }
            return maxFreq.toInt()
        }
    }
}
