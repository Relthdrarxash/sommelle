package fr.sommelle.core.designsystem

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalTime

/** Palette de documentation/design.md, section 9. Contrastes de texte vérifiés à 4,5:1 minimum. */
object SommelleColors {
    val Night = Color(0xFF2F3A6B)
    val Cream = Color(0xFFF6F3EE)
    val Paper = Color(0xFFFFFDF9)
    val Ink = Color(0xFF1E2233)
    val InkMuted = Color(0xFF4A4F63)
    val Lavender = Color(0xFFE6E4F2)
    val LavenderOnNight = Color(0xFFD9DCEB)
    val Line = Color(0xFFE2DDD3)
    val ChipBorder = Color(0xFFCFC8BA)
    val Amber = Color(0xFFE8A33D)
    val Moon = Color(0xFFF2C27B)

    // Thème de nuit : sombre et chaud, sans bleu.
    val Ember = Color(0xFF120D0A)
    val EmberSurface = Color(0xFF1E1611)
    val EmberContainer = Color(0xFF2A2019)
    val Sand = Color(0xFFC9A98A)
    val SandMuted = Color(0xFF9C8169)
    val EmberOutline = Color(0xFF6B5747)
    val AmberDim = Color(0xFFB9853A)
}

private val DayColors: ColorScheme = lightColorScheme(
    primary = SommelleColors.Night,
    onPrimary = SommelleColors.Cream,
    primaryContainer = SommelleColors.Lavender,
    onPrimaryContainer = SommelleColors.Night,
    secondary = SommelleColors.Amber,
    onSecondary = SommelleColors.Ink,
    background = SommelleColors.Cream,
    onBackground = SommelleColors.Ink,
    surface = SommelleColors.Paper,
    onSurface = SommelleColors.Ink,
    surfaceVariant = SommelleColors.Lavender,
    onSurfaceVariant = SommelleColors.InkMuted,
    outline = SommelleColors.ChipBorder,
    outlineVariant = SommelleColors.Line,
)

private val NightColors: ColorScheme = darkColorScheme(
    primary = SommelleColors.Sand,
    onPrimary = SommelleColors.Ember,
    primaryContainer = SommelleColors.EmberContainer,
    onPrimaryContainer = SommelleColors.Sand,
    secondary = SommelleColors.AmberDim,
    onSecondary = SommelleColors.Ember,
    background = SommelleColors.Ember,
    onBackground = SommelleColors.Sand,
    surface = SommelleColors.EmberSurface,
    onSurface = SommelleColors.Sand,
    surfaceVariant = SommelleColors.EmberContainer,
    onSurfaceVariant = SommelleColors.SandMuted,
    outline = SommelleColors.EmberOutline,
    outlineVariant = SommelleColors.EmberContainer,
)

/** Atkinson Hyperlegible Next (Braille Institute, licence SIL OFL 1.1, voir FONT-LICENSE.txt). */
val Atkinson = FontFamily(
    Font(R.font.atkinson_regular, FontWeight.Normal),
    Font(R.font.atkinson_semibold, FontWeight.SemiBold),
    Font(R.font.atkinson_bold, FontWeight.Bold),
    Font(R.font.atkinson_extrabold, FontWeight.ExtraBold),
)

private fun style(size: Int, weight: FontWeight, lineHeight: Int = (size * 1.3).toInt()) = TextStyle(
    fontFamily = Atkinson,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
)

/** Corps de texte à 18 sp par défaut, jamais sous 15 sp (documentation/design.md, section 9). */
private val SommelleTypography = Typography(
    displayLarge = style(64, FontWeight.ExtraBold, 70),
    displayMedium = style(44, FontWeight.ExtraBold),
    displaySmall = style(36, FontWeight.ExtraBold),
    headlineLarge = style(34, FontWeight.ExtraBold),
    headlineMedium = style(30, FontWeight.ExtraBold),
    headlineSmall = style(24, FontWeight.Bold),
    titleLarge = style(21, FontWeight.Bold),
    titleMedium = style(19, FontWeight.Bold),
    titleSmall = style(17, FontWeight.Bold),
    bodyLarge = style(19, FontWeight.Normal, 28),
    bodyMedium = style(18, FontWeight.Normal, 26),
    bodySmall = style(16, FontWeight.Normal, 23),
    labelLarge = style(20, FontWeight.ExtraBold),
    labelMedium = style(16, FontWeight.SemiBold),
    labelSmall = style(15, FontWeight.SemiBold),
)

private val SommelleShapes = Shapes(
    extraSmall = RoundedCornerShape(12.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

/** Le thème de nuit s'applique automatiquement entre 21 h et 7 h, ou si le téléphone est en mode sombre. */
fun isNightHours(time: LocalTime): Boolean = time >= LocalTime.of(21, 0) || time < LocalTime.of(7, 0)

@Composable
fun SommelleTheme(
    night: Boolean = isSystemInDarkTheme() || isNightHours(LocalTime.now()),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (night) NightColors else DayColors,
        typography = SommelleTypography,
        shapes = SommelleShapes,
        content = content,
    )
}
