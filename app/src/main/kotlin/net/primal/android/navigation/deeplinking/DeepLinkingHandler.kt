package net.primal.android.navigation.deeplinking

import net.primal.android.crypto.bech32ToHexOrThrow
import net.primal.android.nostr.utils.Nip19TLV
import net.primal.android.settings.wallet.domain.isPrimalWalletNwcUrl
import net.primal.android.settings.wallet.domain.parseAsPrimalWalletNwc
import net.primal.android.user.domain.isNwcUrl
import net.primal.android.user.domain.parseNWCUrl

private val PRIMAL_NOTE_REGEX = Regex("https://.*primal.net/e/")

private const val NOSTR_WALLET_CONNECT_SCHEMA = "nostr+walletconnect://"
private const val NOSTR_WALLET_CONNECT_ALT_SCHEMA = "nostrwalletconnect://"

private const val PRIMAL_WALLET_NWC_CONNECT_SCHEMA = "nostrnwc://"
private const val PRIMAL_WALLET_NWC_CONNECT_PRIMAL_SCHEMA = "nostrnwc+primal://"

private val NPUB_REGEX = Regex("https://primal.net/p/(npub[a-zA-Z0-9]+)")
private val NPUB_HEX_REGEX = Regex("https://primal.net/p/([a-f0-9]{64})")
private val NPROFILE_REGEX = Regex("https://primal.net/p/(nprofile[a-zA-Z0-9]+)")
private val PRIMAL_NAME_REGEX = Regex("https://primal.net/([a-zA-Z0-9-]+)")

fun String.parseDeepLinkOrNull(): DeepLink? =
    when {
        PRIMAL_NOTE_REGEX.containsMatchIn(this) -> {
            val unknownNoteIdentifier = PRIMAL_NOTE_REGEX.replace(this, "")
            unknownNoteIdentifier.resolveNoteId()?.let { DeepLink.Note(it) }
        }

        NPUB_REGEX.containsMatchIn(this) -> {
            val npubId = NPUB_REGEX.find(this)?.groupValues?.get(1)
            npubId?.let { DeepLink.Npub(it) }
        }

        NPUB_HEX_REGEX.containsMatchIn(this) -> {
            val npubaId = NPUB_HEX_REGEX.find(this)?.groupValues?.get(1)
            npubaId?.let { DeepLink.HexNpub(it) }
        }

        NPROFILE_REGEX.containsMatchIn(this) -> {
            val nprofileId = NPROFILE_REGEX.find(this)?.groupValues?.get(1)
            nprofileId?.let { DeepLink.Nprofile(it) }
        }

        PRIMAL_NAME_REGEX.containsMatchIn(this) -> {
            val primalName = PRIMAL_NAME_REGEX.find(this)?.groupValues?.get(1)
            primalName?.let { DeepLink.PrimalName(it) }
        }

        isNostrWalletConnectSchemaAndUrl() ->
            runCatching {
                DeepLink.NostrWalletConnect(nwc = this.parseNWCUrl())
            }.getOrNull()

        isPrimalWalletNWCSchemaAndUrl() ->
            runCatching {
                DeepLink.PrimalNWC(primalWalletNwc = this.parseAsPrimalWalletNwc())
            }.getOrNull()

        else -> null
    }

private fun String.resolveNoteId(): String? =
    when {
        this.startsWith("note1") -> runCatching { bech32ToHexOrThrow() }.getOrNull()
        this.startsWith("nevent1") -> Nip19TLV.parseUriAsNeventOrNull(this)?.eventId
        else -> this
    }

private fun String.isNostrWalletConnectSchemaAndUrl(): Boolean =
    (this.startsWith(NOSTR_WALLET_CONNECT_SCHEMA) || this.startsWith(NOSTR_WALLET_CONNECT_ALT_SCHEMA)) &&
        this.isNwcUrl()

private fun String.isPrimalWalletNWCSchemaAndUrl(): Boolean =
    (this.startsWith(PRIMAL_WALLET_NWC_CONNECT_SCHEMA) || this.startsWith(PRIMAL_WALLET_NWC_CONNECT_PRIMAL_SCHEMA)) &&
        this.isPrimalWalletNwcUrl()
