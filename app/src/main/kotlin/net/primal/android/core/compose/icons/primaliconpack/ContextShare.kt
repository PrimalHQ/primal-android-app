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

public val PrimalIcons.ContextShare: ImageVector
    get() {
        if (_contextShare != null) {
            return _contextShare!!
        }
        _contextShare = Builder(name = "ContextShare", defaultWidth = 20.0.dp, defaultHeight =
                20.0.dp, viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            group {
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(10.0f, 0.0034f)
                    lineTo(5.5061f, 3.9356f)
                    curveTo(5.1944f, 4.2083f, 5.1628f, 4.6821f, 5.4356f, 4.9939f)
                    curveTo(5.7083f, 5.3056f, 6.1821f, 5.3372f, 6.4939f, 5.0644f)
                    lineTo(9.25f, 2.6528f)
                    verticalLineTo(14.0f)
                    curveTo(9.25f, 14.4142f, 9.5858f, 14.75f, 10.0f, 14.75f)
                    curveTo(10.4142f, 14.75f, 10.75f, 14.4142f, 10.75f, 14.0f)
                    verticalLineTo(2.6528f)
                    lineTo(13.5061f, 5.0644f)
                    curveTo(13.8178f, 5.3372f, 14.2917f, 5.3056f, 14.5644f, 4.9939f)
                    curveTo(14.8372f, 4.6821f, 14.8056f, 4.2083f, 14.4939f, 3.9356f)
                    lineTo(10.0f, 0.0034f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFffffff)), stroke = null, strokeLineWidth = 0.0f,
                        strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                        pathFillType = NonZero) {
                    moveTo(6.75f, 7.0f)
                    curveTo(7.1642f, 7.0f, 7.5f, 7.3358f, 7.5f, 7.75f)
                    curveTo(7.5f, 8.1642f, 7.1642f, 8.5f, 6.75f, 8.5f)
                    horizontalLineTo(4.0f)
                    curveTo(3.7239f, 8.5f, 3.5f, 8.7239f, 3.5f, 9.0f)
                    verticalLineTo(18.0f)
                    curveTo(3.5f, 18.2761f, 3.7239f, 18.5f, 4.0f, 18.5f)
                    horizontalLineTo(16.0f)
                    curveTo(16.2761f, 18.5f, 16.5f, 18.2761f, 16.5f, 18.0f)
                    verticalLineTo(9.0f)
                    curveTo(16.5f, 8.7239f, 16.2761f, 8.5f, 16.0f, 8.5f)
                    horizontalLineTo(13.25f)
                    curveTo(12.8358f, 8.5f, 12.5f, 8.1642f, 12.5f, 7.75f)
                    curveTo(12.5f, 7.3358f, 12.8358f, 7.0f, 13.25f, 7.0f)
                    horizontalLineTo(16.0f)
                    curveTo(17.1046f, 7.0f, 18.0f, 7.8954f, 18.0f, 9.0f)
                    verticalLineTo(18.0f)
                    curveTo(18.0f, 19.1046f, 17.1046f, 20.0f, 16.0f, 20.0f)
                    horizontalLineTo(4.0f)
                    curveTo(2.8954f, 20.0f, 2.0f, 19.1046f, 2.0f, 18.0f)
                    verticalLineTo(9.0f)
                    curveTo(2.0f, 7.8954f, 2.8954f, 7.0f, 4.0f, 7.0f)
                    horizontalLineTo(6.75f)
                    close()
                }
            }
        }
        .build()
        return _contextShare!!
    }

private var _contextShare: ImageVector? = null
