package net.primal.android.user.domain

import kotlinx.serialization.Serializable

@Serializable
data class UserAccount(
    val pubkey: String,
    val displayName: String,
    val pictureUrl: String? = null,
    val internetIdentifier: String? = null,
    val followingCount: Int? = null,
    val followersCount: Int? = null,
    val notesCount: Int? = null,
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
