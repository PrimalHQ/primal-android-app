@file:Suppress("SpellCheckingInspection")

package io.github.anvell.filetype.matchers

import io.github.anvell.filetype.FileType
import io.github.anvell.filetype.extensions.matchSignature

private val Pdf = FileType.Matcher { buf ->
    buf.matchSignature(0x25, 0x50, 0x44, 0x46)
}

private val Rtf = FileType.Matcher { buf ->
    buf.matchSignature(0x7B, 0x5C, 0x72, 0x74, 0x66, 0x31)
}

internal val DocumentMatchers = mapOf(
    FileType.Document.Pdf to Pdf,
    FileType.Document.Rtf to Rtf,
)
