package net.primal.android.core.utils

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