package net.primal.android.drawer.multiaccount.events

data class AccountSwitcherCallbacks(
    val onActiveAccountChanged: () -> Unit,
    val onCreateNewAccountClick: () -> Unit,
    val onAddExistingAccountClick: () -> Unit,
)
