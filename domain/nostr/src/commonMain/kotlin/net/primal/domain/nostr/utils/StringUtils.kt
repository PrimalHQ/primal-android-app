package net.primal.domain.nostr.utils

import fr.acinq.secp256k1.Hex
import net.primal.core.utils.ellipsizeMiddle
import net.primal.core.utils.toNpub

fun String.asEllipsizedNpub(): String = Hex.decode(this).toNpub().ellipsizeMiddle(size = 8)

fun String.formatNip05Identifier(): String {
    return if (startsWith("_@")) {
        substring(2)
    } else {
        this
    }
}

fun authorNameUiFriendly(
    displayName: String?,
    name: String?,
    pubkey: String,
): String {
    return when {
        displayName?.isNotBlank() == true -> displayName
        name?.isNotBlank() == true -> name
        else -> pubkey.asEllipsizedNpub()
    }
}

fun usernameUiFriendly(
    displayName: String?,
    name: String?,
    pubkey: String,
): String {
    return when {
        name?.isNotBlank() == true -> name
        displayName?.isNotBlank() == true -> displayName
        else -> pubkey.asEllipsizedNpub()
    }
}
