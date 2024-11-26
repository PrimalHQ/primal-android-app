package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.CheckCircleOutline: ImageVector
    get() {
        if (_CheckCircleOutline != null) {
            return _CheckCircleOutline!!
        }
        _CheckCircleOutline = ImageVector.Builder(
            name = "CheckCircleOutline",
            defaultWidth = 44.dp,
            defaultHeight = 44.dp,
            viewportWidth = 44f,
            viewportHeight = 44f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                fillAlpha = 0.8f
            ) {
                moveTo(12.86f, 24.228f)
                lineTo(18.409f, 30.042f)
                curveTo(18.518f, 30.155f, 18.699f, 30.155f, 18.807f, 30.042f)
                lineTo(31.141f, 17.122f)
                curveTo(31.72f, 16.515f, 31.722f, 15.56f, 31.145f, 14.951f)
                curveTo(30.526f, 14.298f, 29.486f, 14.295f, 28.863f, 14.946f)
                lineTo(18.807f, 25.465f)
                curveTo(18.699f, 25.579f, 18.518f, 25.579f, 18.41f, 25.466f)
                lineTo(15.137f, 22.05f)
                curveTo(14.514f, 21.4f, 13.475f, 21.403f, 12.856f, 22.056f)
                curveTo(12.278f, 22.666f, 12.28f, 23.621f, 12.86f, 24.228f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                fillAlpha = 0.8f,
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(44f, 22f)
                curveTo(44f, 34.15f, 34.15f, 44f, 22f, 44f)
                curveTo(9.85f, 44f, 0f, 34.15f, 0f, 22f)
                curveTo(0f, 9.85f, 9.85f, 0f, 22f, 0f)
                curveTo(34.15f, 0f, 44f, 9.85f, 44f, 22f)
                close()
                moveTo(41.25f, 22f)
                curveTo(41.25f, 32.632f, 32.632f, 41.25f, 22f, 41.25f)
                curveTo(11.368f, 41.25f, 2.75f, 32.632f, 2.75f, 22f)
                curveTo(2.75f, 11.368f, 11.368f, 2.75f, 22f, 2.75f)
                curveTo(32.632f, 2.75f, 41.25f, 11.368f, 41.25f, 22f)
                close()
            }
        }.build()

        return _CheckCircleOutline!!
    }

@Suppress("ObjectPropertyName")
private var _CheckCircleOutline: ImageVector? = null
