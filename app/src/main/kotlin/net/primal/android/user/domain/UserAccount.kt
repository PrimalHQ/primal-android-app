package net.primal.android.user.domain

import kotlinx.serialization.Serializable
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.nostr.model.primal.content.ContentAppSettings
import net.primal.android.wallet.domain.WalletSettings
import net.primal.android.wallet.domain.WalletState

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
    val repliesCount: Int? = null,
    val nostrWallet: NostrWalletConnect? = null,
    val primalWallet: PrimalWallet? = null,
    val primalWalletState: WalletState = WalletState(),
    val primalWalletSettings: WalletSettings = WalletSettings(),
    val walletPreference: WalletPreference = when {
        primalWallet != null -> WalletPreference.PrimalWallet
        nostrWallet != null -> WalletPreference.NostrWalletConnect
        else -> WalletPreference.Undefined
    },
    val appSettings: ContentAppSettings? = null,
    val following: Set<String> = emptySet(),
    val interests: List<String> = emptyList(),
    val followListEventContent: String? = null,
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
