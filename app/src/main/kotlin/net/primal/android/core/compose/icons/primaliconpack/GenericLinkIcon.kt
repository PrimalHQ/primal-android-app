package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.GenericLinkIcon: ImageVector
    get() {
        if (_GenericLinkIcon != null) {
            return _GenericLinkIcon!!
        }
        _GenericLinkIcon = ImageVector.Builder(
            name = "GenericLinkIcon",
            defaultWidth = 35.dp,
            defaultHeight = 35.dp,
            viewportWidth = 35f,
            viewportHeight = 35f
        ).apply {
            path(fill = SolidColor(Color(0xFF666666))) {
                moveTo(6.398f, 14.201f)
                curveTo(6.925f, 13.674f, 7.781f, 13.672f, 8.308f, 14.2f)
                curveTo(8.836f, 14.727f, 8.836f, 15.583f, 8.308f, 16.11f)
                lineTo(5.31f, 19.109f)
                curveTo(2.498f, 21.921f, 2.498f, 26.481f, 5.31f, 29.292f)
                curveTo(8.121f, 32.103f, 12.679f, 32.103f, 15.491f, 29.292f)
                lineTo(18.49f, 26.292f)
                curveTo(19.017f, 25.764f, 19.871f, 25.765f, 20.399f, 26.292f)
                curveTo(20.926f, 26.819f, 20.927f, 27.673f, 20.4f, 28.201f)
                lineTo(17.4f, 31.201f)
                lineTo(17.031f, 31.553f)
                curveTo(13.144f, 35.063f, 7.146f, 34.946f, 3.401f, 31.201f)
                curveTo(-0.465f, 27.335f, -0.465f, 21.066f, 3.401f, 17.2f)
                lineTo(6.398f, 14.201f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666))) {
                moveTo(21.546f, 10.645f)
                curveTo(22.074f, 10.119f, 22.928f, 10.118f, 23.455f, 10.645f)
                curveTo(23.982f, 11.172f, 23.982f, 12.027f, 23.455f, 12.554f)
                lineTo(12.655f, 23.354f)
                curveTo(12.128f, 23.881f, 11.274f, 23.881f, 10.746f, 23.354f)
                curveTo(10.219f, 22.827f, 10.219f, 21.972f, 10.746f, 21.445f)
                lineTo(21.546f, 10.645f)
                close()
            }
            path(fill = SolidColor(Color(0xFF666666))) {
                moveTo(17.4f, 3.199f)
                curveTo(21.266f, -0.667f, 27.535f, -0.666f, 31.401f, 3.199f)
                curveTo(35.267f, 7.065f, 35.267f, 13.334f, 31.401f, 17.2f)
                lineTo(28.673f, 19.928f)
                curveTo(28.145f, 20.455f, 27.291f, 20.455f, 26.764f, 19.928f)
                curveTo(26.237f, 19.401f, 26.237f, 18.546f, 26.764f, 18.019f)
                lineTo(29.492f, 15.291f)
                curveTo(32.304f, 12.479f, 32.304f, 7.92f, 29.492f, 5.108f)
                curveTo(26.68f, 2.297f, 22.122f, 2.297f, 19.31f, 5.108f)
                lineTo(16.581f, 7.836f)
                curveTo(16.053f, 8.363f, 15.199f, 8.363f, 14.672f, 7.836f)
                curveTo(14.145f, 7.309f, 14.145f, 6.455f, 14.672f, 5.927f)
                lineTo(17.4f, 3.199f)
                close()
            }
        }.build()

        return _GenericLinkIcon!!
    }

@Suppress("ObjectPropertyName")
private var _GenericLinkIcon: ImageVector? = null
