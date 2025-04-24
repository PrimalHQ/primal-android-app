package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.ContextCopyNoteLink: ImageVector
    get() {
        if (_ContextCopyNoteLink != null) {
            return _ContextCopyNoteLink!!
        }
        _ContextCopyNoteLink = ImageVector.Builder(
            name = "ContextCopyNoteLink",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            group(
                clipPathData = PathData {
                    moveTo(0f, 0f)
                    horizontalLineToRelative(20f)
                    verticalLineToRelative(20f)
                    horizontalLineToRelative(-20f)
                    close()
                }
            ) {
                path(fill = SolidColor(Color(0xFFFFFFFF))) {
                    moveTo(3.554f, 8.445f)
                    curveTo(3.847f, 8.152f, 4.323f, 8.151f, 4.616f, 8.444f)
                    curveTo(4.909f, 8.737f, 4.909f, 9.213f, 4.616f, 9.506f)
                    lineTo(2.95f, 11.172f)
                    curveTo(1.388f, 12.734f, 1.388f, 15.267f, 2.95f, 16.829f)
                    curveTo(4.512f, 18.391f, 7.044f, 18.39f, 8.606f, 16.829f)
                    lineTo(10.272f, 15.162f)
                    curveTo(10.565f, 14.869f, 11.04f, 14.869f, 11.333f, 15.162f)
                    curveTo(11.625f, 15.455f, 11.626f, 15.93f, 11.333f, 16.222f)
                    lineTo(9.666f, 17.889f)
                    lineTo(9.461f, 18.085f)
                    curveTo(7.302f, 20.035f, 3.97f, 19.97f, 1.889f, 17.889f)
                    curveTo(-0.259f, 15.742f, -0.258f, 12.259f, 1.889f, 10.111f)
                    lineTo(3.554f, 8.445f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFFFFFFF))) {
                    moveTo(11.97f, 6.47f)
                    curveTo(12.263f, 6.177f, 12.738f, 6.177f, 13.031f, 6.47f)
                    curveTo(13.323f, 6.762f, 13.323f, 7.237f, 13.031f, 7.53f)
                    lineTo(7.031f, 13.53f)
                    curveTo(6.738f, 13.823f, 6.263f, 13.823f, 5.97f, 13.53f)
                    curveTo(5.677f, 13.237f, 5.677f, 12.762f, 5.97f, 12.469f)
                    lineTo(11.97f, 6.47f)
                    close()
                }
                path(fill = SolidColor(Color(0xFFFFFFFF))) {
                    moveTo(9.666f, 2.333f)
                    curveTo(11.814f, 0.185f, 15.297f, 0.185f, 17.445f, 2.333f)
                    curveTo(19.593f, 4.481f, 19.593f, 7.963f, 17.445f, 10.111f)
                    lineTo(15.929f, 11.627f)
                    curveTo(15.636f, 11.919f, 15.161f, 11.92f, 14.869f, 11.627f)
                    curveTo(14.576f, 11.334f, 14.576f, 10.859f, 14.869f, 10.566f)
                    lineTo(16.384f, 9.051f)
                    curveTo(17.946f, 7.488f, 17.946f, 4.955f, 16.384f, 3.393f)
                    curveTo(14.822f, 1.832f, 12.29f, 1.832f, 10.728f, 3.393f)
                    lineTo(9.211f, 4.909f)
                    curveTo(8.918f, 5.201f, 8.444f, 5.202f, 8.151f, 4.909f)
                    curveTo(7.858f, 4.616f, 7.858f, 4.141f, 8.151f, 3.848f)
                    lineTo(9.666f, 2.333f)
                    close()
                }
            }
        }.build()

        return _ContextCopyNoteLink!!
    }

@Suppress("ObjectPropertyName")
private var _ContextCopyNoteLink: ImageVector? = null
