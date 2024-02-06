package net.primal.android.wallet.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector

data class WalletTab(
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector,
    @StringRes val labelResId: Int,
)
