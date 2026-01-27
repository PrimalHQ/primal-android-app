package net.primal.core.utils.security

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Securely zeros a ByteArray to prevent sensitive data from lingering in memory.
 */
fun ByteArray.zeroOut() {
    for (i in indices) {
        this[i] = 0
    }
}

/**
 * Securely zeros a CharArray to prevent sensitive data from lingering in memory.
 */
fun CharArray.zeroOut() {
    for (i in indices) {
        this[i] = '\u0000'
    }
}

/**
 * A wrapper for sensitive character data that ensures zeroing on close.
 *
 * Use this for handling sensitive data like seed phrases, passwords, etc.
 * The underlying CharArray is zeroed when [close] is called or when using [use].
 *
 * Example:
 * ```
 * SensitiveCharArray.wrap(seedPhrase.toCharArray()).use { chars ->
 *     // Use chars safely
 *     sdk.connect(String(chars))
 * }
 * // chars are automatically zeroed here
 * ```
 */
class SensitiveCharArray private constructor(
    private var data: CharArray?,
) : AutoCloseable {

    /**
     * Returns true if the data has been zeroed and is no longer accessible.
     */
    val isCleared: Boolean
        get() = data == null

    /**
     * Provides access to the underlying CharArray.
     * @throws IllegalStateException if already cleared
     */
    fun getChars(): CharArray {
        return data ?: error("SensitiveCharArray has been cleared")
    }

    /**
     * Converts to String. Use sparingly - prefer working with CharArray.
     * @throws IllegalStateException if already cleared
     */
    fun asString(): String {
        return getChars().concatToString()
    }

    /**
     * Zeros the underlying data and marks as cleared.
     */
    override fun close() {
        data?.zeroOut()
        data = null
    }

    companion object {
        /**
         * Wraps an existing CharArray. The array will be zeroed when close() is called.
         * Note: The original array reference is used, not copied.
         */
        fun wrap(chars: CharArray): SensitiveCharArray {
            return SensitiveCharArray(chars)
        }

        /**
         * Creates from a String by copying to CharArray.
         * Note: The original String cannot be zeroed (immutable), but the CharArray copy can be.
         */
        fun fromString(str: String): SensitiveCharArray {
            return SensitiveCharArray(str.toCharArray())
        }

        /**
         * Creates from a list of words joined by a separator.
         * Note: The original Strings cannot be zeroed, but the joined CharArray can be.
         */
        fun fromWords(words: List<String>, separator: Char = ' '): SensitiveCharArray {
            if (words.isEmpty()) return SensitiveCharArray(CharArray(0))

            val totalLength = words.sumOf { it.length } + (words.size - 1)
            val chars = CharArray(totalLength)
            var position = 0

            words.forEachIndexed { index, word ->
                word.toCharArray().copyInto(chars, position)
                position += word.length
                if (index < words.size - 1) {
                    chars[position] = separator
                    position++
                }
            }

            return SensitiveCharArray(chars)
        }
    }
}

/**
 * Executes [block] with the sensitive data, then automatically zeros it.
 *
 * @param block The code to execute with access to the CharArray
 * @return The result of [block]
 */
@OptIn(ExperimentalContracts::class)
inline fun <R> SensitiveCharArray.use(block: (CharArray) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    try {
        return block(getChars())
    } finally {
        close()
    }
}

/**
 * Executes [block] with the sensitive data as String, then automatically zeros the CharArray.
 * Use sparingly - prefer [use] with CharArray when possible.
 *
 * @param block The code to execute with the String value
 * @return The result of [block]
 */
@OptIn(ExperimentalContracts::class)
inline fun <R> SensitiveCharArray.useAsString(block: (String) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    try {
        return block(asString())
    } finally {
        close()
    }
}
