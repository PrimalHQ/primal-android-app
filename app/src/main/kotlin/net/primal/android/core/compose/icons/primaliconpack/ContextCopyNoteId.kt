package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.ContextCopyNoteId: ImageVector
    get() {
        if (_ContextCopyNoteId != null) {
            return _ContextCopyNoteId!!
        }
        _ContextCopyNoteId = ImageVector.Builder(
            name = "ContextCopyNoteId",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFFFFFFF)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(16.847f, 14.398f)
                horizontalLineTo(13.744f)
                lineTo(12.999f, 19.001f)
                curveTo(12.929f, 19.433f, 12.556f, 19.75f, 12.119f, 19.75f)
                curveTo(11.569f, 19.75f, 11.151f, 19.258f, 11.238f, 18.716f)
                lineTo(11.938f, 14.398f)
                horizontalLineTo(6.804f)
                lineTo(6.059f, 19.001f)
                curveTo(5.989f, 19.433f, 5.616f, 19.75f, 5.178f, 19.75f)
                curveTo(4.629f, 19.75f, 4.21f, 19.258f, 4.298f, 18.716f)
                lineTo(4.997f, 14.398f)
                horizontalLineTo(2.172f)
                curveTo(1.605f, 14.398f, 1.172f, 13.891f, 1.262f, 13.331f)
                curveTo(1.334f, 12.884f, 1.719f, 12.555f, 2.172f, 12.555f)
                horizontalLineTo(5.296f)
                lineTo(6.123f, 7.445f)
                horizontalLineTo(3.319f)
                curveTo(2.752f, 7.445f, 2.319f, 6.938f, 2.409f, 6.377f)
                curveTo(2.481f, 5.93f, 2.866f, 5.602f, 3.319f, 5.602f)
                horizontalLineTo(6.422f)
                lineTo(7.167f, 0.999f)
                curveTo(7.237f, 0.567f, 7.61f, 0.25f, 8.047f, 0.25f)
                curveTo(8.597f, 0.25f, 9.016f, 0.742f, 8.928f, 1.284f)
                lineTo(8.229f, 5.602f)
                horizontalLineTo(13.362f)
                lineTo(14.107f, 0.999f)
                curveTo(14.177f, 0.567f, 14.55f, 0.25f, 14.988f, 0.25f)
                curveTo(15.537f, 0.25f, 15.956f, 0.742f, 15.868f, 1.284f)
                lineTo(15.169f, 5.602f)
                horizontalLineTo(17.994f)
                curveTo(18.561f, 5.602f, 18.994f, 6.109f, 18.904f, 6.669f)
                curveTo(18.833f, 7.116f, 18.447f, 7.445f, 17.994f, 7.445f)
                horizontalLineTo(14.87f)
                lineTo(14.043f, 12.555f)
                horizontalLineTo(16.847f)
                curveTo(17.414f, 12.555f, 17.847f, 13.062f, 17.757f, 13.623f)
                curveTo(17.685f, 14.07f, 17.3f, 14.398f, 16.847f, 14.398f)
                close()
                moveTo(7.103f, 12.555f)
                horizontalLineTo(12.236f)
                lineTo(13.064f, 7.445f)
                horizontalLineTo(7.93f)
                lineTo(7.103f, 12.555f)
                close()
            }
        }.build()

        return _ContextCopyNoteId!!
    }

@Suppress("ObjectPropertyName")
private var _ContextCopyNoteId: ImageVector? = null
