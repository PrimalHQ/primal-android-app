package net.primal.android.core.utils

import net.primal.android.crypto.toNpub
import org.spongycastle.util.encoders.Hex

fun String.ellipsizeMiddle(size: Int): String {
    return if (length <= size * 2) {
        this
    } else {
        val firstEight = substring(0, size)
        val lastEight = substring(length - size, length)
        "$firstEight...$lastEight"
    }
}

fun String?.isPrimalIdentifier() = this?.contains("primal.net") == true

fun String.asEllipsizedNpub(): String = Hex.decode(this).toNpub().ellipsizeMiddle(size = 8)

fun String.formatNip05Identifier(): String {
    return if (startsWith("_@")) {
        substring(2)
    } else {
        this
    }
}
