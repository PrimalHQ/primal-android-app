package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons
import kotlin.Suppress

val PrimalIcons.RemoveHighlight: ImageVector
    get() {
        if (_RemoveHighlight != null) {
            return _RemoveHighlight!!
        }
        _RemoveHighlight = ImageVector.Builder(
            name = "RemoveHighlight",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(15.948f, 11.999f)
                lineTo(19.253f, 7.371f)
                curveTo(20.403f, 5.761f, 20.207f, 3.575f, 18.79f, 2.186f)
                curveTo(17.373f, 0.797f, 15.142f, 0.605f, 13.5f, 1.732f)
                lineTo(8.863f, 4.914f)
                lineTo(15.948f, 11.999f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(4.67f, 7.791f)
                lineTo(3.387f, 8.671f)
                curveTo(1.621f, 9.883f, 1.4f, 12.363f, 2.923f, 13.856f)
                lineTo(6.884f, 17.738f)
                curveTo(8.408f, 19.232f, 10.938f, 19.015f, 12.174f, 17.284f)
                lineTo(13.002f, 16.124f)
                lineTo(10.766f, 13.887f)
                curveTo(9.672f, 14.465f, 8.278f, 14.301f, 7.354f, 13.395f)
                curveTo(6.416f, 12.476f, 6.258f, 11.081f, 6.881f, 10.002f)
                lineTo(4.67f, 7.791f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(5.72f, 18.199f)
                lineTo(2.453f, 14.997f)
                lineTo(0.341f, 17.067f)
                curveTo(-0.387f, 17.78f, 0.128f, 19f, 1.158f, 19f)
                lineTo(4.425f, 19f)
                curveTo(4.731f, 19f, 5.025f, 18.881f, 5.242f, 18.668f)
                lineTo(5.72f, 18.199f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(1.707f, 1.293f)
                curveTo(1.317f, 1.683f, 1.317f, 2.317f, 1.707f, 2.707f)
                lineTo(17.971f, 18.971f)
                curveTo(18.361f, 19.361f, 18.994f, 19.361f, 19.385f, 18.971f)
                curveTo(19.775f, 18.58f, 19.775f, 17.947f, 19.385f, 17.556f)
                lineTo(3.121f, 1.293f)
                curveTo(2.731f, 0.902f, 2.098f, 0.902f, 1.707f, 1.293f)
                close()
            }
        }.build()

        return _RemoveHighlight!!
    }

@Suppress("ObjectPropertyName")
private var _RemoveHighlight: ImageVector? = null
