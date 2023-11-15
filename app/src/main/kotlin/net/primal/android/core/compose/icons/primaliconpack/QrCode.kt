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

public val PrimalIcons.QrCode: ImageVector
    get() {
        if (_qrCode != null) {
            return _qrCode!!
        }
        _qrCode = Builder(name = "QrCode", defaultWidth = 20.0.dp, defaultHeight = 20.0.dp,
                viewportWidth = 20.0f, viewportHeight = 20.0f).apply {
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(3.1818f, 3.6818f)
                curveTo(3.1818f, 3.4056f, 3.4057f, 3.1818f, 3.6818f, 3.1818f)
                horizontalLineTo(5.4091f)
                curveTo(5.6852f, 3.1818f, 5.9091f, 3.4056f, 5.9091f, 3.6818f)
                verticalLineTo(5.409f)
                curveTo(5.9091f, 5.6852f, 5.6852f, 5.909f, 5.4091f, 5.909f)
                horizontalLineTo(3.6818f)
                curveTo(3.4057f, 5.909f, 3.1818f, 5.6852f, 3.1818f, 5.409f)
                verticalLineTo(3.6818f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(1.8182f, 0.0f)
                curveTo(0.814f, 0.0f, 0.0f, 0.814f, 0.0f, 1.8182f)
                verticalLineTo(7.2727f)
                curveTo(0.0f, 8.2769f, 0.814f, 9.0909f, 1.8182f, 9.0909f)
                horizontalLineTo(7.2727f)
                curveTo(8.2769f, 9.0909f, 9.0909f, 8.2769f, 9.0909f, 7.2727f)
                verticalLineTo(1.8182f)
                curveTo(9.0909f, 0.814f, 8.2769f, 0.0f, 7.2727f, 0.0f)
                horizontalLineTo(1.8182f)
                close()
                moveTo(7.2727f, 1.3636f)
                horizontalLineTo(1.8182f)
                curveTo(1.5671f, 1.3636f, 1.3636f, 1.5671f, 1.3636f, 1.8182f)
                verticalLineTo(7.2727f)
                curveTo(1.3636f, 7.5238f, 1.5671f, 7.7273f, 1.8182f, 7.7273f)
                horizontalLineTo(7.2727f)
                curveTo(7.5238f, 7.7273f, 7.7273f, 7.5238f, 7.7273f, 7.2727f)
                verticalLineTo(1.8182f)
                curveTo(7.7273f, 1.5671f, 7.5238f, 1.3636f, 7.2727f, 1.3636f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(14.0909f, 3.6818f)
                curveTo(14.0909f, 3.4056f, 14.3148f, 3.1818f, 14.5909f, 3.1818f)
                horizontalLineTo(16.3182f)
                curveTo(16.5943f, 3.1818f, 16.8182f, 3.4056f, 16.8182f, 3.6818f)
                verticalLineTo(5.409f)
                curveTo(16.8182f, 5.6852f, 16.5943f, 5.909f, 16.3182f, 5.909f)
                horizontalLineTo(14.5909f)
                curveTo(14.3148f, 5.909f, 14.0909f, 5.6852f, 14.0909f, 5.409f)
                verticalLineTo(3.6818f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(10.9091f, 1.8182f)
                curveTo(10.9091f, 0.814f, 11.7231f, 0.0f, 12.7273f, 0.0f)
                horizontalLineTo(18.1818f)
                curveTo(19.186f, 0.0f, 20.0f, 0.814f, 20.0f, 1.8182f)
                verticalLineTo(7.2727f)
                curveTo(20.0f, 8.2769f, 19.186f, 9.0909f, 18.1818f, 9.0909f)
                horizontalLineTo(12.7273f)
                curveTo(11.7231f, 9.0909f, 10.9091f, 8.2769f, 10.9091f, 7.2727f)
                verticalLineTo(1.8182f)
                close()
                moveTo(12.7273f, 1.3636f)
                horizontalLineTo(18.1818f)
                curveTo(18.4329f, 1.3636f, 18.6364f, 1.5671f, 18.6364f, 1.8182f)
                verticalLineTo(7.2727f)
                curveTo(18.6364f, 7.5238f, 18.4329f, 7.7273f, 18.1818f, 7.7273f)
                horizontalLineTo(12.7273f)
                curveTo(12.4762f, 7.7273f, 12.2727f, 7.5238f, 12.2727f, 7.2727f)
                verticalLineTo(1.8182f)
                curveTo(12.2727f, 1.5671f, 12.4762f, 1.3636f, 12.7273f, 1.3636f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(3.1818f, 14.5909f)
                curveTo(3.1818f, 14.3148f, 3.4057f, 14.0909f, 3.6818f, 14.0909f)
                horizontalLineTo(5.4091f)
                curveTo(5.6852f, 14.0909f, 5.9091f, 14.3148f, 5.9091f, 14.5909f)
                verticalLineTo(16.3182f)
                curveTo(5.9091f, 16.5944f, 5.6852f, 16.8182f, 5.4091f, 16.8182f)
                horizontalLineTo(3.6818f)
                curveTo(3.4057f, 16.8182f, 3.1818f, 16.5944f, 3.1818f, 16.3182f)
                verticalLineTo(14.5909f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = EvenOdd) {
                moveTo(0.0f, 12.7272f)
                curveTo(0.0f, 11.7231f, 0.814f, 10.9091f, 1.8182f, 10.9091f)
                horizontalLineTo(7.2727f)
                curveTo(8.2769f, 10.9091f, 9.0909f, 11.7231f, 9.0909f, 12.7272f)
                verticalLineTo(18.1818f)
                curveTo(9.0909f, 19.1859f, 8.2769f, 20.0f, 7.2727f, 20.0f)
                horizontalLineTo(1.8182f)
                curveTo(0.814f, 20.0f, 0.0f, 19.1859f, 0.0f, 18.1818f)
                verticalLineTo(12.7272f)
                close()
                moveTo(1.8182f, 12.2727f)
                horizontalLineTo(7.2727f)
                curveTo(7.5238f, 12.2727f, 7.7273f, 12.4762f, 7.7273f, 12.7272f)
                verticalLineTo(18.1818f)
                curveTo(7.7273f, 18.4328f, 7.5238f, 18.6363f, 7.2727f, 18.6363f)
                horizontalLineTo(1.8182f)
                curveTo(1.5671f, 18.6363f, 1.3636f, 18.4328f, 1.3636f, 18.1818f)
                verticalLineTo(12.7272f)
                curveTo(1.3636f, 12.4762f, 1.5671f, 12.2727f, 1.8182f, 12.2727f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(11.4091f, 10.9091f)
                curveTo(11.1329f, 10.9091f, 10.9091f, 11.1329f, 10.9091f, 11.4091f)
                verticalLineTo(13.1363f)
                curveTo(10.9091f, 13.4125f, 11.1329f, 13.6363f, 11.4091f, 13.6363f)
                horizontalLineTo(13.1364f)
                curveTo(13.4125f, 13.6363f, 13.6364f, 13.4125f, 13.6364f, 13.1363f)
                verticalLineTo(11.4091f)
                curveTo(13.6364f, 11.1329f, 13.4125f, 10.9091f, 13.1364f, 10.9091f)
                horizontalLineTo(11.4091f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(14.0909f, 14.5909f)
                curveTo(14.0909f, 14.3148f, 14.3148f, 14.0909f, 14.5909f, 14.0909f)
                horizontalLineTo(16.3182f)
                curveTo(16.5943f, 14.0909f, 16.8182f, 14.3148f, 16.8182f, 14.5909f)
                verticalLineTo(16.3182f)
                curveTo(16.8182f, 16.5944f, 16.5943f, 16.8182f, 16.3182f, 16.8182f)
                horizontalLineTo(14.5909f)
                curveTo(14.3148f, 16.8182f, 14.0909f, 16.5944f, 14.0909f, 16.3182f)
                verticalLineTo(14.5909f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(17.7727f, 17.2727f)
                curveTo(17.4966f, 17.2727f, 17.2727f, 17.4966f, 17.2727f, 17.7727f)
                verticalLineTo(19.5f)
                curveTo(17.2727f, 19.7761f, 17.4966f, 20.0f, 17.7727f, 20.0f)
                horizontalLineTo(19.5f)
                curveTo(19.7761f, 20.0f, 20.0f, 19.7761f, 20.0f, 19.5f)
                verticalLineTo(17.7727f)
                curveTo(20.0f, 17.4966f, 19.7761f, 17.2727f, 19.5f, 17.2727f)
                horizontalLineTo(17.7727f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(10.9091f, 17.7727f)
                curveTo(10.9091f, 17.4966f, 11.1329f, 17.2727f, 11.4091f, 17.2727f)
                horizontalLineTo(13.1364f)
                curveTo(13.4125f, 17.2727f, 13.6364f, 17.4966f, 13.6364f, 17.7727f)
                verticalLineTo(19.5f)
                curveTo(13.6364f, 19.7761f, 13.4125f, 20.0f, 13.1364f, 20.0f)
                horizontalLineTo(11.4091f)
                curveTo(11.1329f, 20.0f, 10.9091f, 19.7761f, 10.9091f, 19.5f)
                verticalLineTo(17.7727f)
                close()
            }
            path(fill = SolidColor(Color(0xFFAAAAAA)), stroke = null, strokeLineWidth = 0.0f,
                    strokeLineCap = Butt, strokeLineJoin = Miter, strokeLineMiter = 4.0f,
                    pathFillType = NonZero) {
                moveTo(17.7727f, 10.9091f)
                curveTo(17.4966f, 10.9091f, 17.2727f, 11.1329f, 17.2727f, 11.4091f)
                verticalLineTo(13.1363f)
                curveTo(17.2727f, 13.4125f, 17.4966f, 13.6363f, 17.7727f, 13.6363f)
                horizontalLineTo(19.5f)
                curveTo(19.7761f, 13.6363f, 20.0f, 13.4125f, 20.0f, 13.1363f)
                verticalLineTo(11.4091f)
                curveTo(20.0f, 11.1329f, 19.7761f, 10.9091f, 19.5f, 10.9091f)
                horizontalLineTo(17.7727f)
                close()
            }
        }
        .build()
        return _qrCode!!
    }

private var _qrCode: ImageVector? = null
