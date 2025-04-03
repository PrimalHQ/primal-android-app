package net.primal.core.utils

import io.github.aakira.napier.Napier

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

fun String.extract(parser: (bechPrefix: String?, key: String?) -> String?): String? {
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
