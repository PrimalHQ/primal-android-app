package net.primal.android.multiaccount.model

data class AccountSwitcherCallbacks(
    val onActiveAccountChanged: () -> Unit,
    val onEditClick: () -> Unit,
    val onCreateNewAccountClick: () -> Unit,
    val onAddExistingAccountClick: () -> Unit,
) {
    companion object {
        val EMPTY = AccountSwitcherCallbacks(
            onActiveAccountChanged = {},
            onAddExistingAccountClick = {},
            onCreateNewAccountClick = {},
            onEditClick = {},
        )
    }
}
