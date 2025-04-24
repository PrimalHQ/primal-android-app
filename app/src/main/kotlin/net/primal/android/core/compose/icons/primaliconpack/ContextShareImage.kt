package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.ContextShareImage: ImageVector
    get() {
        if (_ContextShareImage != null) {
            return _ContextShareImage!!
        }
        _ContextShareImage = ImageVector.Builder(
            name = "ContextShareImage",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(6.5f, 6f)
                curveTo(7.328f, 6f, 8f, 6.672f, 8f, 7.5f)
                curveTo(8f, 8.328f, 7.328f, 9f, 6.5f, 9f)
                curveTo(5.672f, 9f, 5f, 8.328f, 5f, 7.5f)
                curveTo(5f, 6.672f, 5.672f, 6f, 6.5f, 6f)
                close()
            }
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(17.154f, 2.004f)
                curveTo(18.739f, 2.084f, 20f, 3.395f, 20f, 5f)
                verticalLineTo(15f)
                curveTo(20f, 16.605f, 18.739f, 17.916f, 17.154f, 17.996f)
                lineTo(17f, 18f)
                horizontalLineTo(3f)
                lineTo(2.846f, 17.996f)
                curveTo(1.261f, 17.916f, 0f, 16.605f, 0f, 15f)
                verticalLineTo(5f)
                curveTo(0f, 3.395f, 1.261f, 2.084f, 2.846f, 2.004f)
                lineTo(3f, 2f)
                horizontalLineTo(17f)
                lineTo(17.154f, 2.004f)
                close()
                moveTo(14.189f, 8.888f)
                curveTo(13.86f, 8.47f, 13.245f, 8.443f, 12.881f, 8.809f)
                lineTo(12.811f, 8.888f)
                lineTo(9.236f, 13.439f)
                curveTo(8.607f, 14.239f, 7.406f, 14.27f, 6.735f, 13.504f)
                lineTo(5.158f, 11.702f)
                curveTo(4.81f, 11.304f, 4.19f, 11.304f, 3.842f, 11.702f)
                lineTo(1.5f, 14.377f)
                verticalLineTo(15f)
                curveTo(1.5f, 15.828f, 2.172f, 16.5f, 3f, 16.5f)
                horizontalLineTo(17f)
                curveTo(17.828f, 16.5f, 18.5f, 15.828f, 18.5f, 15f)
                verticalLineTo(14.374f)
                lineTo(14.189f, 8.888f)
                close()
                moveTo(3f, 3.5f)
                curveTo(2.172f, 3.5f, 1.5f, 4.172f, 1.5f, 5f)
                verticalLineTo(12.479f)
                lineTo(2.9f, 10.879f)
                lineTo(3.067f, 10.708f)
                curveTo(3.933f, 9.915f, 5.306f, 9.972f, 6.1f, 10.879f)
                lineTo(7.677f, 12.681f)
                curveTo(7.831f, 12.857f, 8.109f, 12.851f, 8.254f, 12.666f)
                lineTo(11.829f, 8.115f)
                lineTo(11.997f, 7.925f)
                curveTo(12.881f, 7.036f, 14.373f, 7.1f, 15.171f, 8.115f)
                lineTo(18.5f, 12.352f)
                verticalLineTo(5f)
                curveTo(18.5f, 4.172f, 17.828f, 3.5f, 17f, 3.5f)
                horizontalLineTo(3f)
                close()
            }
        }.build()

        return _ContextShareImage!!
    }

@Suppress("ObjectPropertyName")
private var _ContextShareImage: ImageVector? = null
