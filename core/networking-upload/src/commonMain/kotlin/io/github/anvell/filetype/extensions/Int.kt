package io.github.anvell.filetype.extensions

/**
 * Parse [Int] from Big Endian [bytes].
 */
internal fun Int.Companion.fromBigEndian(bytes: ByteArray, byteOffset: Int = 0): Int =
    (
        (bytes[byteOffset].toUInt() shl 24) +
            (bytes[byteOffset + 1].toUInt() shl 16) +
            (bytes[byteOffset + 2].toUInt() shl 8) +
            bytes[byteOffset + 3].toUByte()
        ).toInt()
