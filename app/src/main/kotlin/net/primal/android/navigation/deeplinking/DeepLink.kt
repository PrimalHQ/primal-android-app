package net.primal.android.navigation.deeplinking

import net.primal.android.user.domain.NostrWallet

sealed class DeepLink {
    data class NostrWalletConnect(val wallet: NostrWallet) : DeepLink()
    data class Profile(val pubkey: String) : DeepLink()
    data class Note(val noteId: String) : DeepLink()
}
