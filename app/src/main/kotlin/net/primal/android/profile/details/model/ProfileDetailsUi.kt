package net.primal.android.profile.details.model

data class ProfileDetailsUi(
    val pubkey: String,
    val displayName: String,
    val coverUrl: String?,
    val avatarUrl: String?,
    val internetIdentifier: String?,
    val about: String?,
    val website: String?,
)
