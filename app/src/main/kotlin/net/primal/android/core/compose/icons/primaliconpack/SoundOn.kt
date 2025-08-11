package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val SoundOn: ImageVector
    get() {
        if (_SoundOn != null) {
            return _SoundOn!!
        }
        _SoundOn = ImageVector.Builder(
            name = "SoundOn",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(fill = SolidColor(Color.White)) {
                moveTo(9f, 10.23f)
                lineTo(13.594f, 6.555f)
                curveTo(13.757f, 6.424f, 14f, 6.541f, 14f, 6.75f)
                verticalLineTo(20.71f)
                curveTo(14f, 20.92f, 13.757f, 21.036f, 13.594f, 20.905f)
                lineTo(9f, 17.23f)
                lineTo(7f, 17.23f)
                curveTo(6.448f, 17.23f, 6f, 16.783f, 6f, 16.23f)
                verticalLineTo(11.23f)
                curveTo(6f, 10.678f, 6.448f, 10.23f, 7f, 10.23f)
                horizontalLineTo(9f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(18.399f, 8.482f)
                curveTo(18.124f, 8.176f, 18.121f, 7.722f, 18.424f, 7.44f)
                curveTo(18.726f, 7.158f, 19.22f, 7.157f, 19.499f, 7.458f)
                curveTo(21.058f, 9.142f, 22f, 11.334f, 22f, 13.73f)
                curveTo(22f, 16.127f, 21.058f, 18.318f, 19.499f, 20.002f)
                curveTo(19.22f, 20.304f, 18.726f, 20.302f, 18.424f, 20.021f)
                curveTo(18.121f, 19.739f, 18.124f, 19.284f, 18.399f, 18.979f)
                curveTo(19.679f, 17.559f, 20.449f, 15.728f, 20.449f, 13.73f)
                curveTo(20.449f, 11.732f, 19.679f, 9.902f, 18.399f, 8.482f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(16.191f, 10.533f)
                curveTo(15.932f, 10.216f, 15.927f, 9.762f, 16.23f, 9.48f)
                curveTo(16.533f, 9.198f, 17.028f, 9.196f, 17.297f, 9.506f)
                curveTo(18.299f, 10.661f, 18.898f, 12.13f, 18.898f, 13.73f)
                curveTo(18.898f, 15.33f, 18.299f, 16.799f, 17.297f, 17.954f)
                curveTo(17.028f, 18.264f, 16.533f, 18.262f, 16.23f, 17.98f)
                curveTo(15.927f, 17.699f, 15.932f, 17.244f, 16.191f, 16.927f)
                curveTo(16.917f, 16.039f, 17.347f, 14.932f, 17.347f, 13.73f)
                curveTo(17.347f, 12.529f, 16.917f, 11.421f, 16.191f, 10.533f)
                close()
            }
        }.build()

        return _SoundOn!!
    }

@Suppress("ObjectPropertyName")
private var _SoundOn: ImageVector? = null
