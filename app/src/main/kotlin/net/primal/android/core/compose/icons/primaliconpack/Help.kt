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

public val PrimalIcons.Help: ImageVector
    get() {
        if (_help != null) {
            return _help!!
        }
        _help = Builder(name = "Help", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(10.627f, 14.836f)
                curveTo(10.5311f, 14.836f, 10.4535f, 14.7584f, 10.4535f, 14.6625f)
                curveTo(10.4581f, 13.8573f, 10.5372f, 13.2167f, 10.6908f, 12.7407f)
                curveTo(10.8491f, 12.2647f, 11.0725f, 11.8821f, 11.3611f, 11.5929f)
                curveTo(11.6497f, 11.2993f, 12.0012f, 11.0302f, 12.4154f, 10.7855f)
                curveTo(12.6994f, 10.6165f, 12.9531f, 10.4318f, 13.1765f, 10.2316f)
                curveTo(13.4046f, 10.027f, 13.5838f, 9.8001f, 13.7141f, 9.551f)
                curveTo(13.8445f, 9.2974f, 13.9096f, 9.0149f, 13.9096f, 8.7035f)
                curveTo(13.9096f, 8.3521f, 13.8235f, 8.0473f, 13.6513f, 7.7893f)
                curveTo(13.4791f, 7.5313f, 13.2463f, 7.3311f, 12.9531f, 7.1887f)
                curveTo(12.6645f, 7.0464f, 12.341f, 6.9752f, 11.9825f, 6.9752f)
                curveTo(11.6521f, 6.9752f, 11.3379f, 7.0442f, 11.04f, 7.1821f)
                curveTo(10.7467f, 7.3155f, 10.5023f, 7.5202f, 10.3068f, 7.796f)
                curveTo(10.153f, 8.0147f, 10.0537f, 8.281f, 10.0087f, 8.595f)
                curveTo(9.9899f, 8.7263f, 9.8819f, 8.8303f, 9.7492f, 8.8303f)
                horizontalLineTo(7.757f)
                curveTo(7.6162f, 8.8303f, 7.5025f, 8.714f, 7.5133f, 8.5736f)
                curveTo(7.5694f, 7.8409f, 7.7791f, 7.2191f, 8.1424f, 6.7083f)
                curveTo(8.552f, 6.1389f, 9.0919f, 5.7118f, 9.7622f, 5.4271f)
                curveTo(10.4372f, 5.1424f, 11.1819f, 5.0f, 11.9965f, 5.0f)
                curveTo(12.8856f, 5.0f, 13.6676f, 5.149f, 14.3425f, 5.4471f)
                curveTo(15.0221f, 5.7452f, 15.5504f, 6.17f, 15.9275f, 6.7216f)
                curveTo(16.3092f, 7.2688f, 16.5f, 7.9183f, 16.5f, 8.6702f)
                curveTo(16.5f, 9.1773f, 16.4139f, 9.6311f, 16.2417f, 10.0315f)
                curveTo(16.0741f, 10.4318f, 15.8344f, 10.7877f, 15.5225f, 11.0991f)
                curveTo(15.2106f, 11.4106f, 14.8406f, 11.6886f, 14.4123f, 11.9333f)
                curveTo(14.0353f, 12.1557f, 13.7258f, 12.387f, 13.4837f, 12.6273f)
                curveTo(13.2463f, 12.8675f, 13.0694f, 13.15f, 12.9531f, 13.4747f)
                curveTo(12.8413f, 13.795f, 12.7832f, 14.191f, 12.7785f, 14.6625f)
                curveTo(12.7785f, 14.7584f, 12.7008f, 14.836f, 12.605f, 14.836f)
                horizontalLineTo(10.627f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(11.6683f, 19.0f)
                curveTo(11.2494f, 19.0f, 10.8887f, 18.8576f, 10.5861f, 18.5729f)
                curveTo(10.2836f, 18.2882f, 10.1323f, 17.9412f, 10.1323f, 17.5319f)
                curveTo(10.1323f, 17.1316f, 10.2836f, 16.789f, 10.5861f, 16.5043f)
                curveTo(10.8887f, 16.2196f, 11.2494f, 16.0772f, 11.6683f, 16.0772f)
                curveTo(12.0826f, 16.0772f, 12.441f, 16.2196f, 12.7436f, 16.5043f)
                curveTo(13.0508f, 16.789f, 13.2044f, 17.1316f, 13.2044f, 17.5319f)
                curveTo(13.2044f, 17.8033f, 13.1323f, 18.0502f, 12.988f, 18.2726f)
                curveTo(12.8483f, 18.4951f, 12.6621f, 18.673f, 12.4294f, 18.8065f)
                curveTo(12.2013f, 18.9355f, 11.9476f, 19.0f, 11.6683f, 19.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(12.0f, 24.0f)
                curveTo(18.6274f, 24.0f, 24.0f, 18.6274f, 24.0f, 12.0f)
                curveTo(24.0f, 5.3726f, 18.6274f, 0.0f, 12.0f, 0.0f)
                curveTo(5.3726f, 0.0f, 0.0f, 5.3726f, 0.0f, 12.0f)
                curveTo(0.0f, 18.6274f, 5.3726f, 24.0f, 12.0f, 24.0f)
                close()
                moveTo(12.0f, 22.0f)
                curveTo(17.5228f, 22.0f, 22.0f, 17.5228f, 22.0f, 12.0f)
                curveTo(22.0f, 6.4771f, 17.5228f, 2.0f, 12.0f, 2.0f)
                curveTo(6.4771f, 2.0f, 2.0f, 6.4771f, 2.0f, 12.0f)
                curveTo(2.0f, 17.5228f, 6.4771f, 22.0f, 12.0f, 22.0f)
                close()
            }
        }
        .build()
        return _help!!
    }

private var _help: ImageVector? = null
