package net.primal.android.settings.keys

interface KeysContract {
    data class UiState(
        val avatarUrl: String?,
        val nsec: String,
        val npub: String,
    )
}
