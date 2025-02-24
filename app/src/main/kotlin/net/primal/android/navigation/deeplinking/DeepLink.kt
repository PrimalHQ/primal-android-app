package net.primal.android.navigation.deeplinking

import net.primal.android.settings.wallet.domain.PrimalWalletNwc

sealed class DeepLink {
    data class NostrWalletConnect(val nwc: net.primal.android.user.domain.NostrWalletConnect) : DeepLink()
    data class Profile(val pubkey: String) : DeepLink()
    data class Note(val noteId: String) : DeepLink()
    data class Article(val naddr: String) : DeepLink()
    data class PrimalNWC(val primalWalletNwc: PrimalWalletNwc) : DeepLink()
    data class AdvancedSearch(val feedSpec: String) : DeepLink()
}
