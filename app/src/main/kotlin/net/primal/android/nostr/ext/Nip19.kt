package net.primal.android.nostr.ext

import androidx.compose.ui.text.toLowerCase
import java.util.regex.Pattern

val NOSTR = "nostr:"
val NPUB = "npub1"
val NSEC = "nsec1"
val NEVENT = "nevent1"
val NADDR = "naddr1"
val NOTE = "note1"
val NRELAY = "nrelay1"
val NPROFILE = "nprofile1"

val NIP19_REGEXP = Pattern.compile(
    "($NOSTR)?@?($NSEC|$NPUB|$NEVENT|$NADDR|$NOTE|$NPROFILE|$NRELAY)([qpzry9x8gf2tvdw0s3jn54khce6mua7l]+)([\\S]*)",
    Pattern.CASE_INSENSITIVE
)

fun String.isNostrUri(): Boolean {
    return this.lowercase().startsWith("$NOSTR")
}

fun String.isNpubOrNprofile() : Boolean {
    val loweredCase = this.lowercase()
    return loweredCase.startsWith(NOSTR + NPUB) || loweredCase.startsWith(NOSTR + NPROFILE)
}

fun String.isNote() : Boolean {
    val loweredCase = this.lowercase()
    return loweredCase.startsWith(NOSTR + NOTE)
}

fun String.parseNip19(): List<String> {
    return `NIP19_REGEXP`.toRegex().findAll(this).map { matchResult ->
        val link =
            matchResult.groupValues[1] + matchResult.groupValues[2] + matchResult.groupValues[3]
        link
    }.toList()
}