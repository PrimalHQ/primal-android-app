package net.primal.android.navigation.deeplinking.ext

import net.primal.android.crypto.bech32ToHexOrThrow
import net.primal.android.navigation.deeplinking.DeepLink
import net.primal.android.user.domain.isNwcUrl
import net.primal.android.user.domain.parseNWCUrl

private val PRIMAL_NOTE_REGEX = Regex("https://.*primal.net/e/")

private const val NOSTR_WALLET_CONNECT_SCHEMA = "nostr+walletconnect://"
private const val NOSTR_WALLET_CONNECT_ALT_SCHEMA = "nostrwalletconnect://"

fun String.parseDeepLinkOrNull(): DeepLink? =
    when {
        PRIMAL_NOTE_REGEX.containsMatchIn(this) ->
            DeepLink.Note(PRIMAL_NOTE_REGEX.replace(this, "").bech32ToHexOrThrow())

        isNostrWalletConnectSchemaAndUrl() ->
            runCatching { DeepLink.NostrWalletConnect(nwc = this.parseNWCUrl()) }.getOrNull()

        else -> null
    }

private fun String.isNostrWalletConnectSchemaAndUrl(): Boolean =
    (this.startsWith(NOSTR_WALLET_CONNECT_SCHEMA) || this.startsWith(NOSTR_WALLET_CONNECT_ALT_SCHEMA)) &&
        this.isNwcUrl()
