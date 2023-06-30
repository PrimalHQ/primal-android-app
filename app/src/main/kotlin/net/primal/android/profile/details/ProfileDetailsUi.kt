package net.primal.android.profile.details

data class ProfileDetailsUi(
    val pubkey: String,
    val displayName: String,
    val pictureUrl: String?,
    val internetIdentifier: String?,
    val about: String?,
    val followingCount: Int?,
    val followersCount: Int?,
    val notesCount: Int?,
)
