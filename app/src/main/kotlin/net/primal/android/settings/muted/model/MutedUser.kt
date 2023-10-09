package net.primal.android.settings.muted.model

data class MutedUser(
    val name: String,
    val pubkey: String,
    val avatarUrl: String? = null,
    val nip05: String? = null
)