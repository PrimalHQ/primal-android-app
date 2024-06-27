package net.primal.android.navigation.deeplinking.ext

import net.primal.android.core.utils.isValidNostrPublicKey
import net.primal.android.crypto.bech32ToHexOrThrow
import net.primal.android.navigation.deeplinking.DeepLink
import net.primal.android.user.domain.NWCParseException
import net.primal.android.user.domain.isNwcUrl
import net.primal.android.user.domain.parseNWCUrl

private const val PRIMAL_SCHEMA = "primal://"
private const val PRIMAL_NOTE_SCHEMA = "${PRIMAL_SCHEMA}e/"
private const val PRIMAL_PROFILE_SCHEMA = "${PRIMAL_SCHEMA}p/"

private const val NOSTR_WALLET_CONNECT_SCHEMA = "nostr+walletconnect://"
private const val NOSTR_WALLET_CONNECT_ALT_SCHEMA = "nostrwalletconnect://"

fun String?.handleDeeplink(): DeepLink? {
    if (this == null) return null

    return when {
        canHandlePrimalSchema() -> handlePrimalSchema()
        canHandleNostrWalletConnectSchema() -> handleNostrWalletConnectSchema()
        else -> null
    }
}

private fun String.canHandleNostrWalletConnectSchema(): Boolean {
    return (this.startsWith(NOSTR_WALLET_CONNECT_SCHEMA) || this.startsWith(NOSTR_WALLET_CONNECT_ALT_SCHEMA)) &&
        this.isNwcUrl()
}

private fun String.canHandlePrimalSchema(): Boolean = startsWith(PRIMAL_SCHEMA)

private fun String.handlePrimalSchema(): DeepLink? {
    return when {
        startsWith(PRIMAL_PROFILE_SCHEMA) -> {
            val bechEncodedPubkey = replace(PRIMAL_PROFILE_SCHEMA, "")

            if (!bechEncodedPubkey.isValidNostrPublicKey()) {
                return null
            }

            DeepLink.Profile(bechEncodedPubkey.bech32ToHexOrThrow())
        }

        startsWith(PRIMAL_NOTE_SCHEMA) -> {
            val bechEncodedNoteId = replace(PRIMAL_NOTE_SCHEMA, "")

            val noteId = bechEncodedNoteId.bech32ToHexOrThrow()

            DeepLink.Note(noteId)
        }

        else -> {
            handleNostrWalletConnectSchema()
        }
    }
}

private fun String.handleNostrWalletConnectSchema(): DeepLink? {
    return try {
        DeepLink.NostrWalletConnect(nwc = this.parseNWCUrl())
    } catch (error: NWCParseException) {
        null
    }
}
