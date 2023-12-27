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

public val PrimalIcons.Keyboard: ImageVector
    get() {
        if (_keyboard != null) {
            return _keyboard!!
        }
        _keyboard = Builder(name = "Keyboard", defaultWidth = 30.0.dp, defaultHeight = 22.0.dp,
                viewportWidth = 30.0f, viewportHeight = 22.0f).apply {
            path(fill = SolidColor(Color(0x00000000)), stroke = SolidColor(Color(0xFFAAAAAA)),
                    strokeLineWidth = 2.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f, pathFillType = NonZero) {
                moveTo(3.0f, 1.0f)
                lineTo(27.0f, 1.0f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 29.0f, 3.0f)
                lineTo(29.0f, 19.0f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 27.0f, 21.0f)
                lineTo(3.0f, 21.0f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 1.0f, 19.0f)
                lineTo(1.0f, 3.0f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 3.0f, 1.0f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(5.7691f, 7.3263f)
                curveTo(6.4063f, 7.3263f, 6.9229f, 6.7791f, 6.9229f, 6.1041f)
                curveTo(6.9229f, 5.429f, 6.4063f, 4.8818f, 5.7691f, 4.8818f)
                curveTo(5.1318f, 4.8818f, 4.6152f, 5.429f, 4.6152f, 6.1041f)
                curveTo(4.6152f, 6.7791f, 5.1318f, 7.3263f, 5.7691f, 7.3263f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(6.9229f, 10.9929f)
                curveTo(6.9229f, 11.668f, 6.4063f, 12.2152f, 5.7691f, 12.2152f)
                curveTo(5.1318f, 12.2152f, 4.6152f, 11.668f, 4.6152f, 10.9929f)
                curveTo(4.6152f, 10.3179f, 5.1318f, 9.7707f, 5.7691f, 9.7707f)
                curveTo(6.4063f, 9.7707f, 6.9229f, 10.3179f, 6.9229f, 10.9929f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(6.9229f, 15.8818f)
                curveTo(6.9229f, 16.5569f, 6.4063f, 17.1041f, 5.7691f, 17.1041f)
                curveTo(5.1318f, 17.1041f, 4.6152f, 16.5569f, 4.6152f, 15.8818f)
                curveTo(4.6152f, 15.2068f, 5.1318f, 14.6596f, 5.7691f, 14.6596f)
                curveTo(6.4063f, 14.6596f, 6.9229f, 15.2068f, 6.9229f, 15.8818f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(10.3845f, 12.2152f)
                curveTo(11.0217f, 12.2152f, 11.5383f, 11.668f, 11.5383f, 10.9929f)
                curveTo(11.5383f, 10.3179f, 11.0217f, 9.7707f, 10.3845f, 9.7707f)
                curveTo(9.7472f, 9.7707f, 9.2306f, 10.3179f, 9.2306f, 10.9929f)
                curveTo(9.2306f, 11.668f, 9.7472f, 12.2152f, 10.3845f, 12.2152f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(20.7691f, 10.9929f)
                curveTo(20.7691f, 11.668f, 20.2525f, 12.2152f, 19.6152f, 12.2152f)
                curveTo(18.978f, 12.2152f, 18.4614f, 11.668f, 18.4614f, 10.9929f)
                curveTo(18.4614f, 10.3179f, 18.978f, 9.7707f, 19.6152f, 9.7707f)
                curveTo(20.2525f, 9.7707f, 20.7691f, 10.3179f, 20.7691f, 10.9929f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(24.2306f, 12.2152f)
                curveTo(24.8679f, 12.2152f, 25.3845f, 11.668f, 25.3845f, 10.9929f)
                curveTo(25.3845f, 10.3179f, 24.8679f, 9.7707f, 24.2306f, 9.7707f)
                curveTo(23.5934f, 9.7707f, 23.0768f, 10.3179f, 23.0768f, 10.9929f)
                curveTo(23.0768f, 11.668f, 23.5934f, 12.2152f, 24.2306f, 12.2152f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(25.3845f, 15.8818f)
                curveTo(25.3845f, 16.5569f, 24.8679f, 17.1041f, 24.2306f, 17.1041f)
                curveTo(23.5934f, 17.1041f, 23.0768f, 16.5569f, 23.0768f, 15.8818f)
                curveTo(23.0768f, 15.2068f, 23.5934f, 14.6596f, 24.2306f, 14.6596f)
                curveTo(24.8679f, 14.6596f, 25.3845f, 15.2068f, 25.3845f, 15.8818f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(11.5383f, 6.1041f)
                curveTo(11.5383f, 6.7791f, 11.0217f, 7.3263f, 10.3845f, 7.3263f)
                curveTo(9.7472f, 7.3263f, 9.2306f, 6.7791f, 9.2306f, 6.1041f)
                curveTo(9.2306f, 5.429f, 9.7472f, 4.8818f, 10.3845f, 4.8818f)
                curveTo(11.0217f, 4.8818f, 11.5383f, 5.429f, 11.5383f, 6.1041f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(14.9999f, 12.2152f)
                curveTo(15.6371f, 12.2152f, 16.1537f, 11.668f, 16.1537f, 10.9929f)
                curveTo(16.1537f, 10.3179f, 15.6371f, 9.7707f, 14.9999f, 9.7707f)
                curveTo(14.3626f, 9.7707f, 13.846f, 10.3179f, 13.846f, 10.9929f)
                curveTo(13.846f, 11.668f, 14.3626f, 12.2152f, 14.9999f, 12.2152f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(16.1537f, 6.1041f)
                curveTo(16.1537f, 6.7791f, 15.6371f, 7.3263f, 14.9999f, 7.3263f)
                curveTo(14.3626f, 7.3263f, 13.846f, 6.7791f, 13.846f, 6.1041f)
                curveTo(13.846f, 5.429f, 14.3626f, 4.8818f, 14.9999f, 4.8818f)
                curveTo(15.6371f, 4.8818f, 16.1537f, 5.429f, 16.1537f, 6.1041f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(19.6152f, 7.3263f)
                curveTo(20.2525f, 7.3263f, 20.7691f, 6.7791f, 20.7691f, 6.1041f)
                curveTo(20.7691f, 5.429f, 20.2525f, 4.8818f, 19.6152f, 4.8818f)
                curveTo(18.978f, 4.8818f, 18.4614f, 5.429f, 18.4614f, 6.1041f)
                curveTo(18.4614f, 6.7791f, 18.978f, 7.3263f, 19.6152f, 7.3263f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(25.3845f, 6.1041f)
                curveTo(25.3845f, 6.7791f, 24.8679f, 7.3263f, 24.2306f, 7.3263f)
                curveTo(23.5934f, 7.3263f, 23.0768f, 6.7791f, 23.0768f, 6.1041f)
                curveTo(23.0768f, 5.429f, 23.5934f, 4.8818f, 24.2306f, 4.8818f)
                curveTo(24.8679f, 4.8818f, 25.3845f, 5.429f, 25.3845f, 6.1041f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(10.3845f, 14.6596f)
                curveTo(9.7472f, 14.6596f, 9.2306f, 15.2068f, 9.2306f, 15.8818f)
                curveTo(9.2306f, 16.5569f, 9.7472f, 17.1041f, 10.3845f, 17.1041f)
                horizontalLineTo(19.6152f)
                curveTo(20.2525f, 17.1041f, 20.7691f, 16.5569f, 20.7691f, 15.8818f)
                curveTo(20.7691f, 15.2068f, 20.2525f, 14.6596f, 19.6152f, 14.6596f)
                horizontalLineTo(10.3845f)
                close()
            }
        }
        .build()
        return _keyboard!!
    }

private var _keyboard: ImageVector? = null
