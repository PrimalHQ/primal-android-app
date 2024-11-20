package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.DrawerSettings: ImageVector
    get() {
        if (_DrawerSettings != null) {
            return _DrawerSettings!!
        }
        _DrawerSettings = ImageVector.Builder(
            name = "DrawerSettings",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(6.729f, 10f)
                curveTo(6.729f, 8.216f, 8.194f, 6.77f, 10f, 6.77f)
                curveTo(11.807f, 6.77f, 13.271f, 8.216f, 13.271f, 10f)
                curveTo(13.271f, 11.784f, 11.807f, 13.23f, 10f, 13.23f)
                curveTo(8.194f, 13.23f, 6.729f, 11.784f, 6.729f, 10f)
                close()
                moveTo(10f, 8.014f)
                curveTo(8.89f, 8.014f, 7.989f, 8.903f, 7.989f, 10f)
                curveTo(7.989f, 11.097f, 8.89f, 11.986f, 10f, 11.986f)
                curveTo(11.111f, 11.986f, 12.011f, 11.097f, 12.011f, 10f)
                curveTo(12.011f, 8.903f, 11.111f, 8.014f, 10f, 8.014f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(7.344f, 1.141f)
                curveTo(7.418f, 0.63f, 7.861f, 0.25f, 8.384f, 0.25f)
                horizontalLineTo(11.617f)
                curveTo(12.139f, 0.25f, 12.583f, 0.63f, 12.656f, 1.141f)
                lineTo(12.997f, 3.497f)
                lineTo(13.099f, 3.545f)
                curveTo(13.453f, 3.711f, 13.791f, 3.904f, 14.111f, 4.123f)
                lineTo(14.203f, 4.186f)
                lineTo(16.442f, 3.299f)
                curveTo(16.927f, 3.106f, 17.482f, 3.296f, 17.743f, 3.743f)
                lineTo(19.359f, 6.507f)
                curveTo(19.621f, 6.954f, 19.509f, 7.523f, 19.098f, 7.842f)
                lineTo(17.201f, 9.312f)
                lineTo(17.21f, 9.423f)
                curveTo(17.225f, 9.614f, 17.233f, 9.806f, 17.233f, 10f)
                curveTo(17.233f, 10.194f, 17.225f, 10.386f, 17.21f, 10.577f)
                lineTo(17.201f, 10.688f)
                lineTo(19.098f, 12.158f)
                curveTo(19.509f, 12.477f, 19.621f, 13.046f, 19.359f, 13.493f)
                lineTo(17.743f, 16.257f)
                curveTo(17.482f, 16.704f, 16.927f, 16.893f, 16.442f, 16.701f)
                lineTo(14.203f, 15.814f)
                lineTo(14.111f, 15.877f)
                curveTo(13.791f, 16.096f, 13.453f, 16.289f, 13.099f, 16.455f)
                lineTo(12.997f, 16.503f)
                lineTo(12.656f, 18.86f)
                curveTo(12.583f, 19.371f, 12.139f, 19.75f, 11.617f, 19.75f)
                horizontalLineTo(8.384f)
                curveTo(7.861f, 19.75f, 7.418f, 19.371f, 7.344f, 18.86f)
                lineTo(7.003f, 16.503f)
                lineTo(6.901f, 16.455f)
                curveTo(6.547f, 16.289f, 6.209f, 16.096f, 5.89f, 15.877f)
                lineTo(5.797f, 15.814f)
                lineTo(3.558f, 16.701f)
                curveTo(3.073f, 16.893f, 2.519f, 16.704f, 2.257f, 16.257f)
                lineTo(0.641f, 13.493f)
                curveTo(0.379f, 13.046f, 0.491f, 12.477f, 0.902f, 12.158f)
                lineTo(2.799f, 10.687f)
                lineTo(2.79f, 10.576f)
                curveTo(2.775f, 10.386f, 2.767f, 10.194f, 2.767f, 10f)
                curveTo(2.767f, 9.806f, 2.775f, 9.614f, 2.79f, 9.424f)
                lineTo(2.799f, 9.313f)
                lineTo(0.902f, 7.842f)
                curveTo(0.491f, 7.523f, 0.379f, 6.954f, 0.641f, 6.507f)
                lineTo(2.257f, 3.743f)
                curveTo(2.519f, 3.296f, 3.073f, 3.106f, 3.558f, 3.299f)
                lineTo(5.797f, 4.186f)
                lineTo(5.89f, 4.122f)
                curveTo(6.209f, 3.904f, 6.547f, 3.711f, 6.901f, 3.545f)
                lineTo(7.003f, 3.497f)
                lineTo(7.344f, 1.141f)
                close()
                moveTo(11.434f, 1.495f)
                horizontalLineTo(8.566f)
                lineTo(8.166f, 4.26f)
                curveTo(8.155f, 4.334f, 8.105f, 4.396f, 8.035f, 4.424f)
                lineTo(7.808f, 4.512f)
                curveTo(7.256f, 4.728f, 6.743f, 5.022f, 6.286f, 5.382f)
                lineTo(6.094f, 5.532f)
                curveTo(6.035f, 5.578f, 5.955f, 5.59f, 5.885f, 5.562f)
                lineTo(3.258f, 4.521f)
                lineTo(1.824f, 6.974f)
                lineTo(4.049f, 8.699f)
                curveTo(4.109f, 8.745f, 4.139f, 8.819f, 4.128f, 8.893f)
                lineTo(4.092f, 9.131f)
                curveTo(4.05f, 9.414f, 4.028f, 9.704f, 4.028f, 10f)
                curveTo(4.028f, 10.296f, 4.05f, 10.586f, 4.092f, 10.869f)
                lineTo(4.128f, 11.107f)
                curveTo(4.139f, 11.181f, 4.109f, 11.255f, 4.049f, 11.301f)
                lineTo(1.824f, 13.026f)
                lineTo(3.258f, 15.479f)
                lineTo(5.885f, 14.438f)
                curveTo(5.955f, 14.41f, 6.035f, 14.421f, 6.094f, 14.468f)
                lineTo(6.285f, 14.618f)
                curveTo(6.743f, 14.978f, 7.256f, 15.272f, 7.808f, 15.488f)
                lineTo(8.035f, 15.576f)
                curveTo(8.105f, 15.604f, 8.155f, 15.666f, 8.166f, 15.74f)
                lineTo(8.566f, 18.505f)
                horizontalLineTo(11.434f)
                lineTo(11.835f, 15.74f)
                curveTo(11.845f, 15.666f, 11.895f, 15.604f, 11.965f, 15.576f)
                lineTo(12.193f, 15.488f)
                curveTo(12.745f, 15.272f, 13.257f, 14.978f, 13.715f, 14.618f)
                lineTo(13.906f, 14.468f)
                curveTo(13.965f, 14.422f, 14.045f, 14.41f, 14.115f, 14.438f)
                lineTo(16.742f, 15.479f)
                lineTo(18.177f, 13.026f)
                lineTo(15.951f, 11.301f)
                curveTo(15.891f, 11.255f, 15.861f, 11.181f, 15.873f, 11.108f)
                lineTo(15.908f, 10.869f)
                curveTo(15.95f, 10.586f, 15.972f, 10.295f, 15.972f, 10f)
                curveTo(15.972f, 9.705f, 15.95f, 9.414f, 15.908f, 9.13f)
                lineTo(15.873f, 8.892f)
                curveTo(15.861f, 8.819f, 15.891f, 8.745f, 15.951f, 8.699f)
                lineTo(18.177f, 6.974f)
                lineTo(16.742f, 4.521f)
                lineTo(14.115f, 5.562f)
                curveTo(14.045f, 5.59f, 13.965f, 5.578f, 13.906f, 5.532f)
                lineTo(13.715f, 5.382f)
                curveTo(13.257f, 5.022f, 12.745f, 4.728f, 12.193f, 4.512f)
                lineTo(11.965f, 4.424f)
                curveTo(11.895f, 4.396f, 11.845f, 4.334f, 11.835f, 4.26f)
                lineTo(11.434f, 1.495f)
                close()
            }
        }.build()

        return _DrawerSettings!!
    }

@Suppress("ObjectPropertyName")
private var _DrawerSettings: ImageVector? = null
