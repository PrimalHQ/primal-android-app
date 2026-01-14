package net.primal.android.settings.home

interface SettingsHomeContract {
    data class UiState(
        val version: String,
        val walletNeedsBackup: Boolean = false,
    )
}
