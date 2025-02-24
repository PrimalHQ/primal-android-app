package net.primal.android.navigation.deeplinking

import net.primal.android.crypto.bech32ToHexOrThrow
import net.primal.android.nostr.utils.Nip19TLV
import net.primal.android.settings.wallet.domain.isPrimalWalletNwcUrl
import net.primal.android.settings.wallet.domain.parseAsPrimalWalletNwc
import net.primal.android.user.domain.isNwcUrl
import net.primal.android.user.domain.parseNWCUrl

private val PRIMAL_NOTE_REGEX = Regex("https://.*primal.net/e/")
private val PRIMAL_ARTICLE_REGEX = Regex("https://.*primal.net/a/")
private val PRIMAL_PROFILE_REGEX = Regex("https://.*primal.net/p/")
private val PRIMAL_ADVANCED_SEARCH = Regex("https://.*primal.net/asearch/")

private const val NOSTR_WALLET_CONNECT_SCHEMA = "nostr+walletconnect://"
private const val NOSTR_WALLET_CONNECT_ALT_SCHEMA = "nostrwalletconnect://"

private const val PRIMAL_WALLET_NWC_CONNECT_SCHEMA = "nostrnwc://"
private const val PRIMAL_WALLET_NWC_CONNECT_PRIMAL_SCHEMA = "nostrnwc+primal://"

fun String.parseDeepLinkOrNull(): DeepLink? =
    when {
        PRIMAL_NOTE_REGEX.containsMatchIn(this) -> {
            val unknownNoteIdentifier = PRIMAL_NOTE_REGEX.replace(this, "")
            unknownNoteIdentifier.resolveNoteId()?.let { DeepLink.Note(it) }
        }

        PRIMAL_PROFILE_REGEX.containsMatchIn(this) -> {
            val unknownProfileIdentifier = PRIMAL_PROFILE_REGEX.replace(this, "")
            unknownProfileIdentifier.resolveProfileId()?.let { DeepLink.Profile(it) }
        }

        PRIMAL_ARTICLE_REGEX.containsMatchIn(this) -> {
            val unknownArticleIdentifier = PRIMAL_ARTICLE_REGEX.replace(this, "")
            unknownArticleIdentifier.let { DeepLink.Article(it) }
        }

        PRIMAL_ADVANCED_SEARCH.containsMatchIn(this) -> {
            val unknownArticleIdentifier = PRIMAL_ADVANCED_SEARCH.replace(this, "")
            unknownArticleIdentifier.let { DeepLink.AdvancedSearch(it) }
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

private fun String.resolveProfileId(): String? =
    when {
        this.startsWith("npub") -> runCatching { bech32ToHexOrThrow() }.getOrNull()
        this.startsWith("nprofile1") -> {
            val pubkey = Nip19TLV.parseUriAsNprofileOrNull(this)?.pubkey
            runCatching { pubkey?.bech32ToHexOrThrow() }.getOrNull()
        }
        else -> this
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
