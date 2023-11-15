@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.ImportPhotoFromCamera: ImageVector
    get() {
        if (importPhotoFromCamera != null) {
            return importPhotoFromCamera!!
        }
        importPhotoFromCamera = Builder(name = "Import photo from camera", defaultWidth =
                24.0.dp, defaultHeight = 24.0.dp, viewportWidth = 24.0f, viewportHeight =
                24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(9.1768f, 3.0f)
                lineTo(8.3674f, 4.4569f)
                curveTo(7.8383f, 5.4093f, 6.8344f, 6.0f, 5.7449f, 6.0f)
                horizontalLineTo(3.0f)
                curveTo(2.4477f, 6.0f, 2.0f, 6.4477f, 2.0f, 7.0f)
                verticalLineTo(19.0f)
                curveTo(2.0f, 19.5523f, 2.4477f, 20.0f, 3.0f, 20.0f)
                horizontalLineTo(21.0f)
                curveTo(21.5523f, 20.0f, 22.0f, 19.5523f, 22.0f, 19.0f)
                verticalLineTo(7.0f)
                curveTo(22.0f, 6.4477f, 21.5523f, 6.0f, 21.0f, 6.0f)
                horizontalLineTo(18.2551f)
                curveTo(17.1656f, 6.0f, 16.1617f, 5.4093f, 15.6326f, 4.4569f)
                lineTo(14.8232f, 3.0f)
                lineTo(16.5715f, 2.0287f)
                lineTo(17.3809f, 3.4856f)
                curveTo(17.5573f, 3.8031f, 17.8919f, 4.0f, 18.2551f, 4.0f)
                horizontalLineTo(21.0f)
                curveTo(22.6569f, 4.0f, 24.0f, 5.3432f, 24.0f, 7.0f)
                verticalLineTo(19.0f)
                curveTo(24.0f, 20.6569f, 22.6569f, 22.0f, 21.0f, 22.0f)
                horizontalLineTo(3.0f)
                curveTo(1.3432f, 22.0f, 0.0f, 20.6569f, 0.0f, 19.0f)
                verticalLineTo(7.0f)
                curveTo(0.0f, 5.3432f, 1.3432f, 4.0f, 3.0f, 4.0f)
                horizontalLineTo(5.7449f)
                curveTo(6.1081f, 4.0f, 6.4427f, 3.8031f, 6.6191f, 3.4856f)
                lineTo(7.4285f, 2.0287f)
                curveTo(7.7812f, 1.3938f, 8.4505f, 1.0f, 9.1768f, 1.0f)
                horizontalLineTo(14.8232f)
                curveTo(15.5495f, 1.0f, 16.2188f, 1.3938f, 16.5715f, 2.0287f)
                lineTo(14.8232f, 3.0f)
                lineTo(9.1768f, 3.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(6.5f, 12.5f)
                curveTo(6.5f, 9.4624f, 8.9624f, 7.0f, 12.0f, 7.0f)
                curveTo(15.0376f, 7.0f, 17.5f, 9.4624f, 17.5f, 12.5f)
                curveTo(17.5f, 15.5376f, 15.0376f, 18.0f, 12.0f, 18.0f)
                curveTo(8.9624f, 18.0f, 6.5f, 15.5376f, 6.5f, 12.5f)
                close()
                moveTo(12.0f, 9.0f)
                curveTo(10.067f, 9.0f, 8.5f, 10.567f, 8.5f, 12.5f)
                curveTo(8.5f, 14.433f, 10.067f, 16.0f, 12.0f, 16.0f)
                curveTo(13.933f, 16.0f, 15.5f, 14.433f, 15.5f, 12.5f)
                curveTo(15.5f, 10.567f, 13.933f, 9.0f, 12.0f, 9.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(19.0f, 10.0f)
                curveTo(19.5523f, 10.0f, 20.0f, 9.5523f, 20.0f, 9.0f)
                curveTo(20.0f, 8.4477f, 19.5523f, 8.0f, 19.0f, 8.0f)
                curveTo(18.4477f, 8.0f, 18.0f, 8.4477f, 18.0f, 9.0f)
                curveTo(18.0f, 9.5523f, 18.4477f, 10.0f, 19.0f, 10.0f)
                close()
            }
        }
        .build()
        return importPhotoFromCamera!!
    }

private var importPhotoFromCamera: ImageVector? = null
