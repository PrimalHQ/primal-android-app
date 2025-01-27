package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.NwcExternalAppForeground: ImageVector
    get() {
        if (_NwcExternalAppForeground != null) {
            return _NwcExternalAppForeground!!
        }
        _NwcExternalAppForeground = ImageVector.Builder(
            name = "NwcExternalAppForeground",
            defaultWidth = 44.dp,
            defaultHeight = 44.dp,
            viewportWidth = 44f,
            viewportHeight = 44f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF666666)),
                strokeLineWidth = 2.5f
            ) {
                moveTo(28f, 1.25f)
                lineTo(40f, 1.25f)
                arcTo(2.75f, 2.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 42.75f, 4f)
                lineTo(42.75f, 16f)
                arcTo(2.75f, 2.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 40f, 18.75f)
                lineTo(28f, 18.75f)
                arcTo(2.75f, 2.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 25.25f, 16f)
                lineTo(25.25f, 4f)
                arcTo(2.75f, 2.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28f, 1.25f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666))) {
                moveTo(4f, 0f)
                lineTo(16f, 0f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 20f, 4f)
                lineTo(20f, 16f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 20f)
                lineTo(4f, 20f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 0f, 16f)
                lineTo(0f, 4f)
                arcTo(4f, 4f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 0f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF666666)),
                strokeLineWidth = 2.5f
            ) {
                moveTo(28f, 25.25f)
                lineTo(40f, 25.25f)
                arcTo(2.75f, 2.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 42.75f, 28f)
                lineTo(42.75f, 40f)
                arcTo(2.75f, 2.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 40f, 42.75f)
                lineTo(28f, 42.75f)
                arcTo(2.75f, 2.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 25.25f, 40f)
                lineTo(25.25f, 28f)
                arcTo(2.75f, 2.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 28f, 25.25f)
                close()
            }
            path(
                stroke = SolidColor(Color(0xFF666666)),
                strokeLineWidth = 2.5f
            ) {
                moveTo(4f, 25.25f)
                lineTo(16f, 25.25f)
                arcTo(2.75f, 2.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 18.75f, 28f)
                lineTo(18.75f, 40f)
                arcTo(2.75f, 2.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 16f, 42.75f)
                lineTo(4f, 42.75f)
                arcTo(2.75f, 2.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 1.25f, 40f)
                lineTo(1.25f, 28f)
                arcTo(2.75f, 2.75f, 0f, isMoreThanHalf = false, isPositiveArc = true, 4f, 25.25f)
                close()
            }
        }.build()

        return _NwcExternalAppForeground!!
    }

@Suppress("ObjectPropertyName")
private var _NwcExternalAppForeground: ImageVector? = null
