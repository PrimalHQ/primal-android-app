package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons
import kotlin.Suppress

val PrimalIcons.Check: ImageVector
    get() {
        if (_Check != null) {
            return _Check!!
        }
        _Check = ImageVector.Builder(
            name = "Check",
            defaultWidth = 17.dp,
            defaultHeight = 13.dp,
            viewportWidth = 17f,
            viewportHeight = 13f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(5.267f, 12.835f)
                lineTo(0.839f, 7.523f)
                curveTo(0.388f, 7.078f, 0.387f, 6.378f, 0.835f, 5.931f)
                curveTo(1.232f, 5.536f, 1.851f, 5.464f, 2.329f, 5.716f)
                curveTo(2.539f, 5.827f, 2.688f, 6.02f, 2.832f, 6.208f)
                lineTo(5.48f, 9.678f)
                curveTo(5.487f, 9.687f, 5.495f, 9.696f, 5.504f, 9.704f)
                curveTo(5.607f, 9.792f, 5.768f, 9.788f, 5.865f, 9.692f)
                lineTo(14.384f, 0.357f)
                curveTo(14.87f, -0.121f, 15.682f, -0.119f, 16.165f, 0.362f)
                curveTo(16.613f, 0.809f, 16.611f, 1.508f, 16.161f, 1.953f)
                lineTo(6.089f, 12.835f)
                curveTo(5.865f, 13.055f, 5.491f, 13.055f, 5.267f, 12.835f)
                close()
            }
        }.build()

        return _Check!!
    }

@Suppress("ObjectPropertyName")
private var _Check: ImageVector? = null
