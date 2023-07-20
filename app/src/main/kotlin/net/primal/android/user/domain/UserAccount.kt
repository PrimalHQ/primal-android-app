package net.primal.android.user.domain

import kotlinx.serialization.Serializable
import net.primal.android.core.utils.asEllipsizedNpub

@Serializable
data class UserAccount(
    val pubkey: String,
    val displayName: String,
    val pictureUrl: String? = null,
    val internetIdentifier: String? = null,
    val followingCount: Int? = null,
    val followersCount: Int? = null,
    val notesCount: Int? = null,
    val relays: List<Relay> = emptyList(),
    val following: List<String> = emptyList(),
    val followers: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
) {
    companion object {
        val EMPTY = UserAccount(
            pubkey = "",
            displayName = "",
        )

        fun buildLocal(pubkey: String) = UserAccount(
            pubkey = pubkey,
            displayName = pubkey.asEllipsizedNpub(),
        )
    }
}
