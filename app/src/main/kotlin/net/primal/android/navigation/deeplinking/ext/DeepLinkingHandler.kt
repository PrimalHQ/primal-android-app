package net.primal.android.navigation.deeplinking.ext

import net.primal.android.crypto.bech32ToHexOrThrow
import net.primal.android.navigation.deeplinking.DeepLink
import net.primal.android.nostr.utils.Nip19TLV
import net.primal.android.user.domain.isNwcUrl
import net.primal.android.user.domain.parseNWCUrl

private val PRIMAL_NOTE_REGEX = Regex("https://.*primal.net/e/")

private const val NOSTR_WALLET_CONNECT_SCHEMA = "nostr+walletconnect://"
private const val NOSTR_WALLET_CONNECT_ALT_SCHEMA = "nostrwalletconnect://"

fun String.parseDeepLinkOrNull(): DeepLink? =
    when {
        PRIMAL_NOTE_REGEX.containsMatchIn(this) ->
            PRIMAL_NOTE_REGEX.replace(this, "").resolveNoteId()?.let { DeepLink.Note(it) }

        isNostrWalletConnectSchemaAndUrl() ->
            runCatching { DeepLink.NostrWalletConnect(nwc = this.parseNWCUrl()) }.getOrNull()

        else -> null
    }

private fun String.resolveNoteId(): String? = when {
    this.startsWith("note1") -> runCatching { bech32ToHexOrThrow() }.getOrNull()
    this.startsWith("nevent1") -> Nip19TLV.parseUriAsNeventOrNull(this)?.eventId
    else -> this
}

private fun String.isNostrWalletConnectSchemaAndUrl(): Boolean =
    (this.startsWith(NOSTR_WALLET_CONNECT_SCHEMA) || this.startsWith(NOSTR_WALLET_CONNECT_ALT_SCHEMA)) &&
        this.isNwcUrl()
