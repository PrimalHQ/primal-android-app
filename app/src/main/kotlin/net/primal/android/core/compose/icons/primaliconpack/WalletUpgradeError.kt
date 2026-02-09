package net.primal.android.core.compose.icons.primaliconpack

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons

val PrimalIcons.WalletUpgradeError: ImageVector
    get() {
        if (_WalletUpgradeError != null) {
            return _WalletUpgradeError!!
        }
        _WalletUpgradeError = ImageVector.Builder(
            name = "WalletUpgradeError",
            defaultWidth = 160.dp,
            defaultHeight = 160.dp,
            viewportWidth = 160f,
            viewportHeight = 160f
        ).apply {
            path(
                stroke = SolidColor(Color(0xFF757575)),
                strokeLineWidth = 10f
            ) {
                moveTo(80f, 80f)
                moveToRelative(-75f, 0f)
                arcToRelative(75f, 75f, 0f, isMoreThanHalf = true, isPositiveArc = true, 150f, 0f)
                arcToRelative(75f, 75f, 0f, isMoreThanHalf = true, isPositiveArc = true, -150f, 0f)
            }
            path(fill = SolidColor(Color(0xFF757575))) {
                moveTo(74f, 52.98f)
                curveTo(74f, 49.68f, 76.69f, 47f, 80f, 47f)
                curveTo(83.31f, 47f, 86f, 49.68f, 86f, 52.98f)
                verticalLineTo(88.88f)
                curveTo(86f, 92.18f, 83.31f, 94.86f, 80f, 94.86f)
                curveTo(76.69f, 94.86f, 74f, 92.18f, 74f, 88.88f)
                verticalLineTo(52.98f)
                close()
            }
            path(fill = SolidColor(Color(0xFF757575))) {
                moveTo(80f, 102.04f)
                curveTo(76.69f, 102.04f, 74f, 104.71f, 74f, 108.02f)
                curveTo(74f, 111.32f, 76.69f, 114f, 80f, 114f)
                curveTo(83.31f, 114f, 86f, 111.32f, 86f, 108.02f)
                curveTo(86f, 104.71f, 83.31f, 102.04f, 80f, 102.04f)
                close()
            }
        }.build()

        return _WalletUpgradeError!!
    }

@Suppress("ObjectPropertyName")
private var _WalletUpgradeError: ImageVector? = null
