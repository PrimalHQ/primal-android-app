package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.VideoPauseMini: ImageVector
    get() {
        if (_VideoPauseMini != null) {
            return _VideoPauseMini!!
        }
        _VideoPauseMini = ImageVector.Builder(
            name = "VideoPauseMini",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(11.938f, 9f)
                curveTo(12.455f, 9f, 12.875f, 9.448f, 12.875f, 10f)
                verticalLineTo(22f)
                curveTo(12.875f, 22.552f, 12.455f, 23f, 11.938f, 23f)
                curveTo(11.42f, 23f, 11f, 22.552f, 11f, 22f)
                verticalLineTo(10f)
                curveTo(11f, 9.448f, 11.42f, 9f, 11.938f, 9f)
                close()
            }
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(20.063f, 9f)
                curveTo(20.58f, 9f, 21f, 9.448f, 21f, 10f)
                verticalLineTo(22f)
                curveTo(21f, 22.552f, 20.58f, 23f, 20.063f, 23f)
                curveTo(19.545f, 23f, 19.125f, 22.552f, 19.125f, 22f)
                verticalLineTo(10f)
                curveTo(19.125f, 9.448f, 19.545f, 9f, 20.063f, 9f)
                close()
            }
        }.build()

        return _VideoPauseMini!!
    }

@Suppress("ObjectPropertyName")
private var _VideoPauseMini: ImageVector? = null
