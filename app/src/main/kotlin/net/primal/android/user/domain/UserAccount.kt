package net.primal.android.user.domain

import kotlinx.serialization.Serializable
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.nostr.model.primal.content.ContentAppSettings

@Serializable
data class UserAccount(
    val pubkey: String,
    val authorDisplayName: String,
    val userDisplayName: String,
    val avatarCdnImage: CdnImage? = null,
    val internetIdentifier: String? = null,
    val lightningAddress: String? = null,
    val followingCount: Int? = null,
    val followersCount: Int? = null,
    val notesCount: Int? = null,
    val nostrWallet: NostrWallet? = null,
    val appSettings: ContentAppSettings? = null,
    val relays: List<Relay> = emptyList(),
    val following: Set<String> = emptySet(),
    val followers: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
) {
    companion object {
        val EMPTY = UserAccount(
            pubkey = "",
            authorDisplayName = "",
            userDisplayName = "",
        )

        fun buildLocal(pubkey: String) =
            UserAccount(
                pubkey = pubkey,
                authorDisplayName = pubkey.asEllipsizedNpub(),
                userDisplayName = pubkey.asEllipsizedNpub(),
            )
    }
}
