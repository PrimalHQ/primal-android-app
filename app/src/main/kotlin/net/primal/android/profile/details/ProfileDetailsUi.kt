package net.primal.android.profile.details

data class ProfileDetailsUi(
    val pubkey: String,
    val displayName: String,
    val coverUrl: String?,
    val avatarUrl: String?,
    val internetIdentifier: String?,
    val about: String?,
    val website: String?,
)
