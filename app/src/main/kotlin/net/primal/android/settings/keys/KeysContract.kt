package net.primal.android.settings.keys

interface KeysContract {
    data class UiState(
        val avatarUrl: String? = null,
        val nsec: String = "",
        val npub: String = "",
    )
}
