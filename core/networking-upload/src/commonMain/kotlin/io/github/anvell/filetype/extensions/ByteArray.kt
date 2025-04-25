package io.github.anvell.filetype.extensions

/**
 * Matches [ByteArray] against [signature].
 */
internal fun ByteArray.matchSignature(offset: Int, signature: ByteArray): Boolean =
    size >= signature.size + offset &&
        sliceArray(offset..offset + signature.lastIndex).contentEquals(signature)

internal fun ByteArray.matchSignature(vararg signature: Byte): Boolean =
    matchSignature(
        offset = 0,
        signature = signature,
    )
