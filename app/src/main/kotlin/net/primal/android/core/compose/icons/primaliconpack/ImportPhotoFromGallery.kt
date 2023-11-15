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

public val PrimalIcons.ImportPhotoFromGallery: ImageVector
    get() {
        if (importPhotoFromGallery != null) {
            return importPhotoFromGallery!!
        }
        importPhotoFromGallery = Builder(name = "Import photo from gallery", defaultWidth =
                24.0.dp, defaultHeight = 24.0.dp, viewportWidth = 24.0f, viewportHeight =
                24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(8.0f, 11.0f)
                curveTo(9.1046f, 11.0f, 10.0f, 10.1046f, 10.0f, 9.0f)
                curveTo(10.0f, 7.8954f, 9.1046f, 7.0f, 8.0f, 7.0f)
                curveTo(6.8954f, 7.0f, 6.0f, 7.8954f, 6.0f, 9.0f)
                curveTo(6.0f, 10.1046f, 6.8954f, 11.0f, 8.0f, 11.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(3.0f, 2.0f)
                curveTo(1.3432f, 2.0f, 0.0f, 3.3431f, 0.0f, 5.0f)
                verticalLineTo(19.0f)
                curveTo(0.0f, 20.6569f, 1.3432f, 22.0f, 3.0f, 22.0f)
                horizontalLineTo(21.0f)
                curveTo(22.6569f, 22.0f, 24.0f, 20.6569f, 24.0f, 19.0f)
                verticalLineTo(5.0f)
                curveTo(24.0f, 3.3431f, 22.6569f, 2.0f, 21.0f, 2.0f)
                horizontalLineTo(3.0f)
                close()
                moveTo(21.0f, 4.0f)
                horizontalLineTo(3.0f)
                curveTo(2.4477f, 4.0f, 2.0f, 4.4477f, 2.0f, 5.0f)
                verticalLineTo(16.7714f)
                lineTo(4.6763f, 14.2481f)
                curveTo(5.4267f, 13.5405f, 6.5922f, 13.5197f, 7.3675f, 14.2f)
                lineTo(8.9576f, 15.5955f)
                curveTo(9.3954f, 15.9797f, 10.0677f, 15.9098f, 10.4172f, 15.4438f)
                lineTo(14.4f, 10.1334f)
                curveTo(15.2f, 9.0667f, 16.8f, 9.0667f, 17.6f, 10.1334f)
                lineTo(22.0f, 16.0f)
                verticalLineTo(5.0f)
                curveTo(22.0f, 4.4477f, 21.5523f, 4.0f, 21.0f, 4.0f)
                close()
            }
        }
        .build()
        return importPhotoFromGallery!!
    }

private var importPhotoFromGallery: ImageVector? = null
