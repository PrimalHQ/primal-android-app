package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SoundOff: ImageVector
    get() {
        if (_SoundOff != null) {
            return _SoundOff!!
        }
        _SoundOff = ImageVector.Builder(
            name = "SoundOff",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(7.53f, 6.47f)
                curveTo(7.237f, 6.177f, 6.763f, 6.177f, 6.47f, 6.47f)
                curveTo(6.177f, 6.763f, 6.177f, 7.237f, 6.47f, 7.53f)
                lineTo(20.47f, 21.53f)
                curveTo(20.763f, 21.823f, 21.237f, 21.823f, 21.53f, 21.53f)
                curveTo(21.823f, 21.237f, 21.823f, 20.763f, 21.53f, 20.47f)
                lineTo(7.53f, 6.47f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(11.127f, 8.298f)
                lineTo(14f, 11.171f)
                verticalLineTo(6.52f)
                curveTo(14f, 6.31f, 13.757f, 6.194f, 13.594f, 6.325f)
                lineTo(11.127f, 8.298f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(20.811f, 17.982f)
                lineTo(19.679f, 16.851f)
                curveTo(20.174f, 15.828f, 20.449f, 14.694f, 20.449f, 13.5f)
                curveTo(20.449f, 11.502f, 19.679f, 9.671f, 18.399f, 8.251f)
                curveTo(18.124f, 7.946f, 18.121f, 7.491f, 18.424f, 7.209f)
                curveTo(18.726f, 6.928f, 19.22f, 6.926f, 19.499f, 7.228f)
                curveTo(21.058f, 8.912f, 22f, 11.104f, 22f, 13.5f)
                curveTo(22f, 15.118f, 21.57f, 16.644f, 20.811f, 17.982f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(17.26f, 14.431f)
                lineTo(18.51f, 15.681f)
                curveTo(18.762f, 14.997f, 18.898f, 14.263f, 18.898f, 13.5f)
                curveTo(18.898f, 11.9f, 18.299f, 10.431f, 17.297f, 9.276f)
                curveTo(17.028f, 8.965f, 16.533f, 8.968f, 16.23f, 9.249f)
                curveTo(15.927f, 9.531f, 15.932f, 9.985f, 16.191f, 10.302f)
                curveTo(16.917f, 11.19f, 17.347f, 12.298f, 17.347f, 13.5f)
                curveTo(17.347f, 13.817f, 17.317f, 14.129f, 17.26f, 14.431f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(7f, 10f)
                horizontalLineTo(7.172f)
                lineTo(14f, 16.828f)
                verticalLineTo(20.48f)
                curveTo(14f, 20.689f, 13.757f, 20.806f, 13.594f, 20.675f)
                lineTo(9f, 17f)
                lineTo(7f, 17f)
                curveTo(6.448f, 17f, 6f, 16.552f, 6f, 16f)
                verticalLineTo(11f)
                curveTo(6f, 10.448f, 6.448f, 10f, 7f, 10f)
                close()
            }
        }.build()

        return _SoundOff!!
    }

@Suppress("ObjectPropertyName")
private var _SoundOff: ImageVector? = null
