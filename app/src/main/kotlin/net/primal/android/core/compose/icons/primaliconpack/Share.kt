@file:Suppress("MagicNumber")

package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.EvenOdd
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

public val PrimalIcons.Share: ImageVector
    get() {
        if (_share != null) {
            return _share!!
        }
        _share = Builder(name = "Share user profile", defaultWidth = 20.0.dp,
                defaultHeight = 20.0.dp, viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(18.8889f, 3.3333f)
                curveTo(18.8889f, 5.1743f, 17.3965f, 6.6667f, 15.5556f, 6.6667f)
                curveTo(14.6446f, 6.6667f, 13.819f, 6.3012f, 13.2173f, 5.709f)
                lineTo(7.6417f, 9.0543f)
                curveTo(7.7303f, 9.3541f, 7.7778f, 9.6715f, 7.7778f, 10.0f)
                curveTo(7.7778f, 10.329f, 7.7301f, 10.6469f, 7.6413f, 10.9471f)
                lineTo(13.2162f, 14.2921f)
                curveTo(13.818f, 13.6992f, 14.644f, 13.3333f, 15.5556f, 13.3333f)
                curveTo(17.3965f, 13.3333f, 18.8889f, 14.8257f, 18.8889f, 16.6667f)
                curveTo(18.8889f, 18.5076f, 17.3965f, 20.0f, 15.5556f, 20.0f)
                curveTo(13.7146f, 20.0f, 12.2222f, 18.5076f, 12.2222f, 16.6667f)
                curveTo(12.2222f, 16.3382f, 12.2697f, 16.0208f, 12.3583f, 15.721f)
                lineTo(6.7827f, 12.3756f)
                curveTo(6.181f, 12.9679f, 5.3554f, 13.3333f, 4.4444f, 13.3333f)
                curveTo(2.6035f, 13.3333f, 1.1111f, 11.841f, 1.1111f, 10.0f)
                curveTo(1.1111f, 8.159f, 2.6035f, 6.6667f, 4.4444f, 6.6667f)
                curveTo(5.356f, 6.6667f, 6.182f, 7.0325f, 6.7838f, 7.6254f)
                lineTo(12.3587f, 4.2805f)
                curveTo(12.2699f, 3.9802f, 12.2222f, 3.6624f, 12.2222f, 3.3333f)
                curveTo(12.2222f, 1.4924f, 13.7146f, 0.0f, 15.5556f, 0.0f)
                curveTo(17.3965f, 0.0f, 18.8889f, 1.4924f, 18.8889f, 3.3333f)
                close()
                moveTo(17.2222f, 3.3333f)
                curveTo(17.2222f, 4.2538f, 16.476f, 5.0f, 15.5556f, 5.0f)
                curveTo(14.6351f, 5.0f, 13.8889f, 4.2538f, 13.8889f, 3.3333f)
                curveTo(13.8889f, 2.4129f, 14.6351f, 1.6667f, 15.5556f, 1.6667f)
                curveTo(16.476f, 1.6667f, 17.2222f, 2.4129f, 17.2222f, 3.3333f)
                close()
                moveTo(17.2222f, 16.6667f)
                curveTo(17.2222f, 17.5871f, 16.476f, 18.3333f, 15.5556f, 18.3333f)
                curveTo(14.6351f, 18.3333f, 13.8889f, 17.5871f, 13.8889f, 16.6667f)
                curveTo(13.8889f, 15.7462f, 14.6351f, 15.0f, 15.5556f, 15.0f)
                curveTo(16.476f, 15.0f, 17.2222f, 15.7462f, 17.2222f, 16.6667f)
                close()
                moveTo(4.4444f, 11.6667f)
                curveTo(5.3649f, 11.6667f, 6.1111f, 10.9205f, 6.1111f, 10.0f)
                curveTo(6.1111f, 9.0795f, 5.3649f, 8.3333f, 4.4444f, 8.3333f)
                curveTo(3.524f, 8.3333f, 2.7778f, 9.0795f, 2.7778f, 10.0f)
                curveTo(2.7778f, 10.9205f, 3.524f, 11.6667f, 4.4444f, 11.6667f)
                close()
            }
        }
        .build()
        return _share!!
    }

private var _share: ImageVector? = null
