@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.ContextMuteUser: ImageVector
    get() {
        if (_contextMuteUser != null) {
            return _contextMuteUser!!
        }
        _contextMuteUser = Builder(name = "ContextMuteUser", defaultWidth = 20.0.dp, defaultHeight =
                20.0.dp, viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFFFA3C3C)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(14.2266f, 2.7868f)
                    curveTo(13.5409f, 1.151f, 11.9067f, 0.0f, 9.9999f, 0.0f)
                    curveTo(7.4752f, 0.0f, 5.4285f, 2.0179f, 5.4285f, 4.507f)
                    curveTo(5.4285f, 6.387f, 6.596f, 7.9981f, 8.2551f, 8.6742f)
                    lineTo(9.6463f, 7.3026f)
                    curveTo(8.2352f, 7.1308f, 7.1428f, 5.9447f, 7.1428f, 4.507f)
                    curveTo(7.1428f, 2.9513f, 8.422f, 1.6901f, 9.9999f, 1.6901f)
                    curveTo(11.4581f, 1.6901f, 12.6612f, 2.7671f, 12.8354f, 4.1583f)
                    lineTo(14.2266f, 2.7868f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFFA3C3C)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(0.9029f, 15.9228f)
                    curveTo(1.2609f, 12.9206f, 3.6765f, 10.539f, 6.7216f, 10.186f)
                    lineTo(0.9029f, 15.9228f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFFA3C3C)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(16.6493f, 19.1549f)
                    horizontalLineTo(4.0896f)
                    lineTo(5.8039f, 17.4648f)
                    horizontalLineTo(16.6493f)
                    curveTo(17.0796f, 17.4648f, 17.4285f, 17.1208f, 17.4285f, 16.6965f)
                    curveTo(17.4285f, 14.0094f, 15.219f, 11.831f, 12.4934f, 11.831f)
                    horizontalLineTo(11.5181f)
                    lineTo(13.1958f, 10.177f)
                    curveTo(16.538f, 10.5229f, 19.1428f, 13.3098f, 19.1428f, 16.6965f)
                    curveTo(19.1428f, 18.0543f, 18.0264f, 19.1549f, 16.6493f, 19.1549f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFFA3C3C)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(19.749f, 1.7243f)
                    curveTo(20.0837f, 1.3943f, 20.0837f, 0.8592f, 19.749f, 0.5292f)
                    curveTo(19.4142f, 0.1992f, 18.8715f, 0.1992f, 18.5368f, 0.5292f)
                    lineTo(0.2511f, 18.5574f)
                    curveTo(-0.0837f, 18.8874f, -0.0837f, 19.4225f, 0.2511f, 19.7525f)
                    curveTo(0.5858f, 20.0825f, 1.1285f, 20.0825f, 1.4632f, 19.7525f)
                    lineTo(19.749f, 1.7243f)
                    close()
                }
            }
        }
        .build()
        return _contextMuteUser!!
    }

private var _contextMuteUser: ImageVector? = null
