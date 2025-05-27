package net.primal.domain.nostr.utils

import io.github.aakira.napier.Napier
import net.primal.core.utils.NADDR
import net.primal.core.utils.NEVENT
import net.primal.core.utils.NOSTR
import net.primal.core.utils.NOTE
import net.primal.core.utils.NPROFILE
import net.primal.core.utils.NPUB
import net.primal.core.utils.NRELAY
import net.primal.core.utils.NSEC
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.Nip19TLV.readAsString
import net.primal.domain.nostr.cryptography.utils.bech32ToHexOrThrow
import net.primal.domain.nostr.cryptography.utils.bechToBytesOrThrow
import net.primal.domain.nostr.cryptography.utils.toHex

private val nostrUriRegexPattern = Regex(
    "($NOSTR)?@?($NSEC|$NPUB|$NEVENT|$NADDR|$NOTE|$NPROFILE|$NRELAY)([qpzry9x8gf2tvdw0s3jn54khce6mua7l]+)([\\S]*)",
    RegexOption.IGNORE_CASE,
)

fun String.parseNostrUris(): List<String> =
    nostrUriRegexPattern.findAll(this)
        .map { matchResult ->
            matchResult.groupValues[1] + matchResult.groupValues[2] + matchResult.groupValues[3]
        }
        .filter { it.nostrUriToBytes() != null }
        .toList()

fun String.nostrUriToBytes(): ByteArray? {
    val matchResult = nostrUriRegexPattern.find(this) ?: return null
    val type = matchResult.groupValues[2].takeIf { it.isNotEmpty() }?.lowercase() ?: return null
    val key = matchResult.groupValues[3].takeIf { it.isNotEmpty() }?.lowercase() ?: return null
    return try {
        (type + key).bechToBytesOrThrow()
    } catch (ignored: Exception) {
        Napier.w("", ignored)
        null
    }
}

private fun String.extract(parser: (bechPrefix: String?, key: String?) -> String?): String? {
    val matchResult = nostrUriRegexPattern.find(this) ?: return null

    val bechPrefix = matchResult.groupValues.getOrNull(2)
    val key = matchResult.groupValues.getOrNull(3)

    return try {
        parser(bechPrefix, key)
    } catch (ignored: Exception) {
        Napier.w("", ignored)
        null
    }
}

fun String.isNostrUri(): Boolean {
    val uri = lowercase()
    return uri.startsWith(NOSTR) || uri.startsWith(NPUB) || uri.startsWith(NOTE) ||
        uri.startsWith(NEVENT) || uri.startsWith(NPROFILE)
}

fun String.clearAtSignFromNostrUris(): String =
    this
        .replace("@$NOSTR", NOSTR)
        .replace("@$NPUB", NPUB)
        .replace("@$NOTE", NOTE)
        .replace("@$NEVENT", NEVENT)
        .replace("@$NADDR", NADDR)
        .replace("@$NPROFILE", NPROFILE)

fun String.isNote() = lowercase().startsWith(NOTE)

fun String.isNPub() = lowercase().startsWith(NPUB)

fun String.isNProfile() = lowercase().startsWith(NPROFILE)

fun String.isNAddr() = lowercase().startsWith(NADDR)

fun String.isNEvent() = lowercase().startsWith(NEVENT)

fun String.isNoteUri() = lowercase().startsWith(NOSTR + NOTE)

fun String.isNEventUri() = lowercase().startsWith(NOSTR + NEVENT)

fun String.isNPubUri() = lowercase().startsWith(NOSTR + NPUB)

fun String.isNProfileUri() = lowercase().startsWith(NOSTR + NPROFILE)

fun String.isNAddrUri() = lowercase().startsWith(NOSTR + NADDR)

fun String.nostrUriToNoteId() = nostrUriToBytes()?.toHex()

fun String.nostrUriToPubkey() = nostrUriToBytes()?.toHex()

private fun String.nostrUriToIdAndRelay(): Pair<String?, String?> {
    val bytes = nostrUriToBytes() ?: return null to null
    val tlv = Nip19TLV.parse(bytes)
    val id = tlv[Nip19TLV.Type.SPECIAL.id]?.firstOrNull()?.toHex()
    val relayBytes = tlv[Nip19TLV.Type.RELAY.id]?.firstOrNull()
    return id to relayBytes?.readAsString()
}

fun String.nostrUriToNoteIdAndRelay() = nostrUriToIdAndRelay()

fun String.nostrUriToPubkeyAndRelay() = nostrUriToIdAndRelay()

fun String.extractProfileId(): String? {
    return extract { bechPrefix: String?, key: String? ->
        when (bechPrefix?.lowercase()) {
            NPUB -> (bechPrefix + key).bechToBytesOrThrow().toHex()
            NPROFILE -> {
                val tlv = Nip19TLV.parse((bechPrefix + key).bechToBytesOrThrow())
                tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
            }

            else -> null
        }
    }
}

fun String.extractNoteId(): String? {
    return extract { bechPrefix: String?, key: String? ->
        when (bechPrefix?.lowercase()) {
            NOTE -> (bechPrefix + key).bechToBytesOrThrow().toHex()
            NEVENT -> {
                val tlv = Nip19TLV.parse((bechPrefix + key).bechToBytesOrThrow())
                tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
            }

            else -> null
        }
    }
}

fun String.extractEventId(): String? {
    return extract { bechPrefix: String?, key: String? ->
        when (bechPrefix?.lowercase()) {
            NEVENT -> {
                val tlv = Nip19TLV.parse((bechPrefix + key).bechToBytesOrThrow())
                tlv[Nip19TLV.Type.SPECIAL.id]?.first()?.toHex()
            }

            else -> (bechPrefix + key).bechToBytesOrThrow().toHex()
        }
    }
}

fun String.takeAsNeventOrNull(): Nevent? {
    return if (isNEvent() || isNEventUri()) {
        runCatching {
            Nip19TLV.parseUriAsNeventOrNull(this)
        }.getOrNull()
    } else {
        null
    }
}

fun String.takeAsNaddrOrNull(): Naddr? {
    return if (isNAddr() || isNAddrUri()) {
        runCatching {
            Nip19TLV.parseUriAsNaddrOrNull(this)
        }.getOrNull()
    } else {
        null
    }
}

fun String.takeAsNaddrStringOrNull(): String? {
    return if (isNAddr() || isNAddrUri()) {
        val result = runCatching {
            Nip19TLV.parseUriAsNaddrOrNull(this)
        }
        if (result.getOrNull() != null) {
            this
        } else {
            null
        }
    } else {
        null
    }
}

fun String.takeAsNoteHexIdOrNull(): String? {
    return if (isNote() || isNoteUri() || isNEventUri() || isNEvent()) {
        val result = runCatching { this.extractNoteId() }
        result.getOrNull()
    } else {
        null
    }
}

fun String.takeAsProfileHexIdOrNull(): String? {
    return if (isNPub() || isNPubUri()) {
        val result = runCatching {
            this.bech32ToHexOrThrow()
        }
        result.getOrNull()
    } else {
        null
    }
}

fun String.withNostrPrefix(): String {
    return if (this.startsWith(NOSTR)) this else "$NOSTR$this"
}
