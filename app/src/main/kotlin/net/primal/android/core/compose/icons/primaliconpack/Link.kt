@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.Link: ImageVector
    get() {
        if (_link != null) {
            return _link!!
        }
        _link = Builder(name = "Link", defaultWidth = 10.0.dp, defaultHeight = 16.0.dp,
                viewportWidth = 10.0f, viewportHeight = 16.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(1.75f, 10.3529f)
                curveTo(1.3358f, 10.3529f, 1.0f, 10.669f, 1.0f, 11.0588f)
                lineTo(1.0f, 12.2353f)
                curveTo(1.0f, 14.3145f, 2.7909f, 16.0f, 5.0f, 16.0f)
                curveTo(7.2091f, 16.0f, 9.0f, 14.3145f, 9.0f, 12.2353f)
                verticalLineTo(9.4118f)
                curveTo(9.0f, 7.3326f, 7.2091f, 5.6471f, 5.0f, 5.6471f)
                curveTo(4.4488f, 5.6471f, 4.2098f, 5.752f, 3.8714f, 5.9418f)
                curveTo(3.6414f, 6.0707f, 3.5f, 6.3059f, 3.5f, 6.5579f)
                lineTo(3.5f, 6.8719f)
                curveTo(3.5f, 7.2336f, 3.9755f, 7.3541f, 4.318f, 7.1901f)
                curveTo(4.4956f, 7.1051f, 4.7108f, 7.0588f, 5.0f, 7.0588f)
                curveTo(6.3807f, 7.0588f, 7.5f, 8.1123f, 7.5f, 9.4118f)
                lineTo(7.5f, 12.2353f)
                curveTo(7.5f, 13.5348f, 6.3807f, 14.5882f, 5.0f, 14.5882f)
                curveTo(3.6193f, 14.5882f, 2.5f, 13.5348f, 2.5f, 12.2353f)
                verticalLineTo(11.0588f)
                curveTo(2.5f, 10.669f, 2.1642f, 10.3529f, 1.75f, 10.3529f)
                close()
            }
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(8.25f, 5.6471f)
                curveTo(8.6642f, 5.6471f, 9.0f, 5.331f, 9.0f, 4.9412f)
                verticalLineTo(3.7647f)
                curveTo(9.0f, 1.6855f, 7.2091f, 0.0f, 5.0f, 0.0f)
                curveTo(2.7909f, 0.0f, 1.0f, 1.6855f, 1.0f, 3.7647f)
                lineTo(1.0f, 6.5882f)
                curveTo(1.0f, 8.6674f, 2.7909f, 10.3529f, 5.0f, 10.3529f)
                curveTo(5.5512f, 10.3529f, 5.7902f, 10.248f, 6.1286f, 10.0582f)
                curveTo(6.3586f, 9.9293f, 6.5f, 9.6941f, 6.5f, 9.4421f)
                verticalLineTo(9.1281f)
                curveTo(6.5f, 8.7664f, 6.0245f, 8.6459f, 5.682f, 8.8099f)
                curveTo(5.5044f, 8.8949f, 5.2892f, 8.9412f, 5.0f, 8.9412f)
                curveTo(3.6193f, 8.9412f, 2.5f, 7.8877f, 2.5f, 6.5882f)
                lineTo(2.5f, 3.7647f)
                curveTo(2.5f, 2.4652f, 3.6193f, 1.4118f, 5.0f, 1.4118f)
                curveTo(6.3807f, 1.4118f, 7.5f, 2.4652f, 7.5f, 3.7647f)
                verticalLineTo(4.9412f)
                curveTo(7.5f, 5.331f, 7.8358f, 5.6471f, 8.25f, 5.6471f)
                close()
            }
        }
        .build()
        return _link!!
    }

private var _link: ImageVector? = null
