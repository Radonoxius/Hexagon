package ix.radon.hexagon.ui.fonts

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import ix.radon.hexagon.R

object Expansiva {
    val fonts: FontFamily = FontFamily(
        Font(
            R.font.expansiva,
            FontWeight.Normal
        ),
        Font(
            R.font.expansiva_bold,
            FontWeight.Bold
        ),
        Font(
            R.font.expansiva_italic,
            FontWeight.Normal,
            FontStyle.Italic
        ),
        Font(
            R.font.expansiva_bold_italic,
            FontWeight.Bold,
            FontStyle.Italic
        )
    )
}