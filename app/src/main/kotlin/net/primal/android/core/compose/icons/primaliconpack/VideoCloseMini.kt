package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.VideoCloseMini: ImageVector
    get() {
        if (_VideoCloseMini != null) {
            return _VideoCloseMini!!
        }
        _VideoCloseMini = ImageVector.Builder(
            name = "VideoCloseMini",
            defaultWidth = 32.dp,
            defaultHeight = 32.dp,
            viewportWidth = 32f,
            viewportHeight = 32f
        ).apply {
            path(fill = SolidColor(Color(0xFFFFFFFF))) {
                moveTo(10.317f, 9.226f)
                curveTo(10.016f, 8.925f, 9.527f, 8.925f, 9.226f, 9.226f)
                curveTo(8.925f, 9.527f, 8.925f, 10.016f, 9.226f, 10.317f)
                lineTo(14.909f, 16f)
                lineTo(9.226f, 21.683f)
                curveTo(8.925f, 21.985f, 8.925f, 22.473f, 9.226f, 22.774f)
                curveTo(9.527f, 23.075f, 10.016f, 23.075f, 10.317f, 22.774f)
                lineTo(16f, 17.091f)
                lineTo(21.683f, 22.774f)
                curveTo(21.984f, 23.075f, 22.473f, 23.075f, 22.774f, 22.774f)
                curveTo(23.075f, 22.473f, 23.075f, 21.984f, 22.774f, 21.683f)
                lineTo(17.091f, 16f)
                lineTo(22.774f, 10.317f)
                curveTo(23.075f, 10.016f, 23.075f, 9.527f, 22.774f, 9.226f)
                curveTo(22.473f, 8.925f, 21.984f, 8.925f, 21.683f, 9.226f)
                lineTo(16f, 14.909f)
                lineTo(10.317f, 9.226f)
                close()
            }
        }.build()

        return _VideoCloseMini!!
    }

@Suppress("ObjectPropertyName")
private var _VideoCloseMini: ImageVector? = null
