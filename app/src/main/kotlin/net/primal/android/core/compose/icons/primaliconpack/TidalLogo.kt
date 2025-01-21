package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.TidalLogo: ImageVector
    get() {
        if (_TidalLogo != null) {
            return _TidalLogo!!
        }
        _TidalLogo = ImageVector.Builder(
            name = "TidalLogo",
            defaultWidth = 18.dp,
            defaultHeight = 18.dp,
            viewportWidth = 18f,
            viewportHeight = 18f
        ).apply {
            path(fill = SolidColor(Color(0xFFD9D9D9))) {
                moveTo(3f, 4.5f)
                lineTo(0f, 7.5f)
                lineTo(3f, 10.5f)
                lineTo(6f, 7.5f)
                lineTo(3f, 4.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFD9D9D9))) {
                moveTo(9f, 4.5f)
                lineTo(6f, 7.5f)
                lineTo(9f, 10.5f)
                lineTo(12f, 7.5f)
                lineTo(9f, 4.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFD9D9D9))) {
                moveTo(15f, 4.5f)
                lineTo(12f, 7.5f)
                lineTo(15f, 10.5f)
                lineTo(18f, 7.5f)
                lineTo(15f, 4.5f)
                close()
            }
            path(fill = SolidColor(Color(0xFFD9D9D9))) {
                moveTo(9f, 10.5f)
                lineTo(6f, 13.5f)
                lineTo(9f, 16.5f)
                lineTo(12f, 13.5f)
                lineTo(9f, 10.5f)
                close()
            }
        }.build()

        return _TidalLogo!!
    }

@Suppress("ObjectPropertyName")
private var _TidalLogo: ImageVector? = null
