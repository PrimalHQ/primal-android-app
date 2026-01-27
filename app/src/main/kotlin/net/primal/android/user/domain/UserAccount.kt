package net.primal.android.user.domain

import java.time.Instant
import kotlinx.serialization.Serializable
import net.primal.android.premium.domain.PremiumMembership
import net.primal.android.settings.wallet.domain.WalletPreference
import net.primal.domain.global.ContentAppSettings
import net.primal.domain.links.CdnImage
import net.primal.domain.membership.PrimalLegendProfile
import net.primal.domain.nostr.utils.asEllipsizedNpub
import net.primal.domain.wallet.NostrWalletConnect
import net.primal.domain.wallet.WalletSettings
import net.primal.domain.wallet.WalletState

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
    @Deprecated("Please use WalletDatabase for wallet information.")
    val nostrWallet: NostrWalletConnect? = null,
    @Deprecated("Please use WalletDatabase for wallet information.")
    val primalWallet: PrimalWallet? = null,
    @Deprecated("Please use WalletDatabase for wallet information.")
    val primalWalletState: WalletState = WalletState(),
    @Deprecated("Please use WalletDatabase for wallet information.")
    val primalWalletSettings: WalletSettings = WalletSettings(),
    @Deprecated("Please use WalletDatabase for wallet information.")
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
    val pushNotificationsEnabled: Boolean = false,
    val shouldShowStreamControlPopup: Boolean = true,
    val shouldShowUpgradeWalletSheet: Boolean = true,
) {
    companion object {
        val EMPTY = UserAccount(
            pubkey = "",
            authorDisplayName = "",
            userDisplayName = "",
            shouldShowUpgradeWalletSheet = false,
        )

        fun buildLocal(pubkey: String) =
            UserAccount(
                pubkey = pubkey,
                authorDisplayName = pubkey.asEllipsizedNpub(),
                userDisplayName = pubkey.asEllipsizedNpub(),
                shouldShowUpgradeWalletSheet = false,
            )
    }
}
