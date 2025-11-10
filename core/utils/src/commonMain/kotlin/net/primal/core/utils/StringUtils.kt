package net.primal.core.utils

fun String.ellipsizeMiddle(size: Int): String {
    return if (length <= size * 2) {
        this
    } else {
        val firstEight = substring(0, size)
        val lastEight = substring(length - size, length)
        "$firstEight...$lastEight"
    }
}

/**
 * Validates that the string is in an email format.
 * Regex used is the one from Javas `Patterns.EMAIL_ADDRESS`.
 */
fun String.isEmailAddress() =
    Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
            "@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+",
    ).matches(this)

operator fun Regex.contains(text: CharSequence): Boolean = this.matches(text)
