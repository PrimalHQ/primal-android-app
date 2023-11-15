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

public val PrimalIcons.Explore: ImageVector
    get() {
        if (_explore != null) {
            return _explore!!
        }
        _explore = Builder(name = "Explore", defaultWidth = 24.0.dp, defaultHeight = 24.0.dp,
                viewportWidth = 24.0f, viewportHeight = 24.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(13.4938f, 2.1108f)
                curveTo(13.798f, 2.1564f, 14.1102f, 2.0777f, 14.3436f, 1.8772f)
                lineTo(15.4764f, 0.9046f)
                curveTo(15.6237f, 0.7782f, 15.5766f, 0.5404f, 15.3904f, 0.4856f)
                curveTo(14.3153f, 0.1696f, 13.1774f, 0.0f, 12.0f, 0.0f)
                curveTo(5.3726f, 0.0f, 0.0f, 5.3726f, 0.0f, 12.0f)
                curveTo(0.0f, 15.4315f, 1.4403f, 18.5265f, 3.7494f, 20.7136f)
                curveTo(3.8881f, 20.8451f, 4.1141f, 20.7711f, 4.1541f, 20.5842f)
                lineTo(4.4545f, 19.1823f)
                curveTo(4.5217f, 18.869f, 4.432f, 18.5445f, 4.2301f, 18.2957f)
                curveTo(2.8356f, 16.5767f, 2.0f, 14.386f, 2.0f, 12.0f)
                curveTo(2.0f, 6.4771f, 6.4771f, 2.0f, 12.0f, 2.0f)
                curveTo(12.5076f, 2.0f, 13.0064f, 2.0378f, 13.4938f, 2.1108f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(10.43f, 21.8775f)
                curveTo(10.1139f, 21.8276f, 9.7886f, 21.9122f, 9.5512f, 22.1268f)
                lineTo(8.4858f, 23.0893f)
                curveTo(8.344f, 23.2174f, 8.3928f, 23.45f, 8.5759f, 23.5044f)
                curveTo(9.661f, 23.8269f, 10.8103f, 24.0f, 12.0f, 24.0f)
                curveTo(18.6274f, 24.0f, 24.0f, 18.6274f, 24.0f, 12.0f)
                curveTo(24.0f, 8.581f, 22.5701f, 5.496f, 20.2758f, 3.3102f)
                curveTo(20.1352f, 3.1763f, 19.9056f, 3.2545f, 19.8698f, 3.4452f)
                lineTo(19.5943f, 4.9102f)
                curveTo(19.5373f, 5.213f, 19.6256f, 5.5232f, 19.8179f, 5.764f)
                curveTo(21.1836f, 7.4738f, 22.0f, 9.6416f, 22.0f, 12.0f)
                curveTo(22.0f, 17.5228f, 17.5228f, 22.0f, 12.0f, 22.0f)
                curveTo(11.4659f, 22.0f, 10.9415f, 21.9581f, 10.43f, 21.8775f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(17.6959f, 1.6349f)
                curveTo(17.8844f, 1.473f, 18.1742f, 1.6404f, 18.1283f, 1.8845f)
                lineTo(16.1675f, 12.312f)
                curveTo(15.9996f, 13.205f, 15.5443f, 14.0171f, 14.8702f, 14.6261f)
                lineTo(6.309f, 22.3603f)
                curveTo(6.1198f, 22.5313f, 5.8195f, 22.3579f, 5.873f, 22.1086f)
                lineTo(8.2905f, 10.8273f)
                curveTo(8.4808f, 9.939f, 8.9565f, 9.1386f, 9.6458f, 8.5467f)
                lineTo(17.6959f, 1.6349f)
                close()
                moveTo(14.0002f, 12.0f)
                curveTo(14.0002f, 13.1046f, 13.1048f, 14.0f, 12.0002f, 14.0f)
                curveTo(10.8957f, 14.0f, 10.0002f, 13.1046f, 10.0002f, 12.0f)
                curveTo(10.0002f, 10.8955f, 10.8957f, 10.0f, 12.0002f, 10.0f)
                curveTo(13.1048f, 10.0f, 14.0002f, 10.8955f, 14.0002f, 12.0f)
                close()
            }
        }
        .build()
        return _explore!!
    }

private var _explore: ImageVector? = null
