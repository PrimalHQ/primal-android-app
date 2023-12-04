package net.primal.android.navigation.deeplinking

sealed class DeepLink {
    data class NostrWalletConnect(val nwc: net.primal.android.user.domain.NostrWalletConnect) : DeepLink()
    data class Profile(val pubkey: String) : DeepLink()
    data class Note(val noteId: String) : DeepLink()
}
