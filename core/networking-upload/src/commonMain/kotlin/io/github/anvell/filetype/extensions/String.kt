package io.github.anvell.filetype.extensions

internal val String.bytes: ByteArray
    get() = toCharArray()
        .map(Char::b)
        .toByteArray()
