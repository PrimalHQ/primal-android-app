package net.primal.android.user.domain

import java.time.Instant
import kotlinx.serialization.Serializable
import net.primal.android.premium.domain.PremiumMembership
import net.primal.android.wallet.domain.WalletSettings
import net.primal.android.wallet.domain.WalletState
import net.primal.domain.global.ContentAppSettings
import net.primal.domain.links.CdnImage
import net.primal.domain.nostr.utils.asEllipsizedNpub
import net.primal.domain.premium.PrimalLegendProfile

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
    val contentDisplaySettings: ContentDisplaySettings = ContentDisplaySettings(),
    val following: Set<String> = emptySet(),
    val interests: List<String> = emptyList(),
    val followListEventContent: String? = null,
    val cachingProxyEnabled: Boolean = false,
    val premiumMembership: PremiumMembership? = null,
    val lastBuyPremiumTimestampInMillis: Long? = null,
    val primalLegendProfile: PrimalLegendProfile? = null,
    val lastAccessedAt: Long = Instant.now().epochSecond,
    val blossomServers: List<String> = emptyList(),
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
