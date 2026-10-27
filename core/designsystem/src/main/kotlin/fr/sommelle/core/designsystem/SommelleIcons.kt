package fr.sommelle.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/** Icônes au trait, dessinées pour Sommelle. Les autres viennent de material-icons-core. */
object SommelleIcons {
    val Moon: ImageVector by lazy { stroke("Moon", "M21 12.8A9 9 0 1 1 11.2 3a7 7 0 0 0 9.8 9.8z") }
    val Chart: ImageVector by lazy { stroke("Chart", "M5 20V10", "M12 20V4", "M19 20v-7") }
    val Door: ImageVector by lazy {
        stroke("Door", "M13 4h3a2 2 0 0 1 2 2v14", "M2 20h3", "M13 20h9", "M10 12v.01", "M13 4.6v16.2a1 1 0 0 1-1.2 1L5 20V5.6a2 2 0 0 1 1.6-2L11 2.7a1.7 1.7 0 0 1 2 1.9z")
    }
    val Lamp: ImageVector by lazy {
        stroke("Lamp", "M9 18h6", "M10 22h4", "M12 2a7 7 0 0 0-4 12.7c.6.5 1 1.3 1 2.3h6c0-1 .4-1.8 1-2.3A7 7 0 0 0 12 2z")
    }
    val Bed: ImageVector by lazy { stroke("Bed", "M2 4v16", "M2 8h18a2 2 0 0 1 2 2v10", "M2 17h20", "M6 8v9") }
    val Watch: ImageVector by lazy { stroke("Watch", "M9 5h6a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H9a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2z", "M10 2h4", "M10 22h4") }
    val History: ImageVector by lazy { stroke("History", "M3 12a9 9 0 1 0 3-6.7L3 8", "M3 3v5h5") }

    private fun stroke(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            paths.forEach { d ->
                addPath(
                    pathData = PathParser().parsePathString(d).toNodes(),
                    fill = null,
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = 2f,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()
}
