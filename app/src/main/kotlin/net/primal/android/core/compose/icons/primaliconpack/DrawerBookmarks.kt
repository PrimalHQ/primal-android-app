package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.DrawerBookmarks: ImageVector
    get() {
        if (_DrawerBookmarks != null) {
            return _DrawerBookmarks!!
        }
        _DrawerBookmarks = ImageVector.Builder(
            name = "DrawerBookmarks",
            defaultWidth = 20.dp,
            defaultHeight = 20.dp,
            viewportWidth = 20f,
            viewportHeight = 20f
        ).apply {
            path(
                fill = SolidColor(Color(0xFFAAAAAA)),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(3.75f, 3.199f)
                curveTo(3.75f, 1.977f, 4.69f, 1f, 5.833f, 1f)
                horizontalLineTo(14.167f)
                curveTo(15.31f, 1f, 16.25f, 1.977f, 16.25f, 3.199f)
                verticalLineTo(18.395f)
                curveTo(16.25f, 18.899f, 15.722f, 19.163f, 15.357f, 18.892f)
                lineTo(10.221f, 15.078f)
                curveTo(10.089f, 14.98f, 9.911f, 14.98f, 9.779f, 15.078f)
                lineTo(4.643f, 18.892f)
                curveTo(4.278f, 19.163f, 3.75f, 18.899f, 3.75f, 18.395f)
                verticalLineTo(3.199f)
                close()
                moveTo(5.833f, 2.211f)
                curveTo(5.303f, 2.211f, 4.886f, 2.66f, 4.886f, 3.199f)
                verticalLineTo(17.235f)
                lineTo(9.129f, 14.085f)
                curveTo(9.652f, 13.697f, 10.348f, 13.697f, 10.871f, 14.085f)
                lineTo(15.114f, 17.235f)
                verticalLineTo(3.199f)
                curveTo(15.114f, 2.66f, 14.697f, 2.211f, 14.167f, 2.211f)
                horizontalLineTo(5.833f)
                close()
            }
        }.build()

        return _DrawerBookmarks!!
    }

@Suppress("ObjectPropertyName")
private var _DrawerBookmarks: ImageVector? = null
