package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.LegendaryProfileNoCustomization: ImageVector
    get() {
        if (_NoCustomization != null) {
            return _NoCustomization!!
        }
        _NoCustomization = ImageVector.Builder(
            name = "NoCustomization",
            defaultWidth = 35.dp,
            defaultHeight = 35.dp,
            viewportWidth = 35f,
            viewportHeight = 35f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF666666)),
                strokeLineWidth = 3f
            ) {
                moveTo(17.5f, 17.5f)
                moveToRelative(-16f, 0f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, 32f, 0f)
                arcToRelative(16f, 16f, 0f, isMoreThanHalf = true, isPositiveArc = true, -32f, 0f)
            }
            path(fill = SolidColor(Color(0xFF666666))) {
                moveTo(28.107f, 4.772f)
                lineToRelative(2.121f, 2.121f)
                lineToRelative(-23.335f, 23.335f)
                lineToRelative(-2.121f, -2.121f)
                close()
            }
        }.build()

        return _NoCustomization!!
    }

@Suppress("ObjectPropertyName")
private var _NoCustomization: ImageVector? = null
