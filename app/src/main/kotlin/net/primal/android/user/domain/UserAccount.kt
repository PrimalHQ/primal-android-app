package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    val pubkey: String,
    val displayName: String,
    val pictureUrl: String?,
    val internetIdentifier: String?,
    val followingCount: Int?,
    val followersCount: Int?,
    val notesCount: Int?,
) {
    companion object {
        val EMPTY = UserAccount(
            pubkey = "",
            displayName = "",
            internetIdentifier = null,
            pictureUrl = null,
            followingCount = null,
            followersCount = null,
            notesCount = null,
        )
    }
}
