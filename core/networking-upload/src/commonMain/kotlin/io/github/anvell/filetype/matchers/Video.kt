@file:Suppress("SpellCheckingInspection")

package io.github.anvell.filetype.matchers

import io.github.anvell.filetype.FileType
import io.github.anvell.filetype.FileType.Video
import io.github.anvell.filetype.extensions.b
import io.github.anvell.filetype.extensions.bytes
import io.github.anvell.filetype.extensions.matchSignature

private val Avi = FileType.Matcher { buf ->
    buf.matchSignature(0x52.b, 0x49.b, 0x46.b, 0x46.b) &&
        buf.matchSignature(8, byteArrayOf(0x41.b, 0x56.b, 0x49.b))
}

private val Flv = FileType.Matcher { buf ->
    buf.matchSignature(0x46.b, 0x4C.b, 0x56.b, 0x01.b)
}

private val M4v = FileType.Matcher { buf ->
    buf.matchSignature(
        offset = 4,
        signature = byteArrayOf(0x66.b, 0x74.b, 0x79.b, 0x70.b, 0x4D.b, 0x34.b, 0x56.b),
    )
}

private val Mkv = FileType.Matcher { buf ->
    buf.matchSignature(
        0x1A.b, 0x45.b, 0xDF.b, 0xA3.b, 0x93.b, 0x42.b, 0x82.b, 0x88.b,
        0x6D.b, 0x61.b, 0x74.b, 0x72.b, 0x6F.b, 0x73.b, 0x6B.b, 0x61.b,
    ) || buf.matchSignature(
        offset = 31,
        byteArrayOf(
            0x6D.b, 0x61.b, 0x74.b, 0x72.b,
            0x6f.b, 0x73.b, 0x6B.b, 0x61.b,
        ),
    )
}

private val Mov = FileType.Matcher { buf ->
    (buf.matchSignature(4, "ftyp".bytes) && buf.matchSignature(8, "qt  ".bytes)) ||
        buf.matchSignature(4, byteArrayOf(0x6d.b, 0x6f.b, 0x6f.b, 0x76.b)) ||
        buf.matchSignature(4, byteArrayOf(0x6d.b, 0x64.b, 0x61.b, 0x74.b)) ||
        buf.matchSignature(12, byteArrayOf(0x6d.b, 0x64.b, 0x61.b, 0x74.b))
}

private val Mp4 = FileType.Matcher { buf ->
    val type = buf.matchSignature(4, "ftyp".bytes)
    val subtype = listOf(
        "avc1", "dash", "iso2", "iso3",
        "iso4", "iso5", "iso6", "isom",
        "mmp4", "mp41", "mp42", "mp4v",
        "mp71", "MSNV", "NDAS", "NDSC",
        "NSDC", "NDSH", "NDSM", "NDSP",
        "NDSS", "NDXC", "NDXH", "NDXM",
        "NDXP", "NDXS", "F4V ", "F4P ",
    ).map(String::bytes)

    type && subtype.any { buf.matchSignature(8, it) }
}

private val Mpeg = FileType.Matcher { buf ->
    buf.size > 3 &&
        buf[0] == 0x0.b &&
        buf[1] == 0x0.b &&
        buf[2] == 0x1.b &&
        buf[3] >= 0xb0.b &&
        buf[3] <= 0xbf.b
}

private val Webm = FileType.Matcher { buf ->
    buf.matchSignature(0x1A.b, 0x45.b, 0xDF.b, 0xA3.b)
}

private val Wmv = FileType.Matcher { buf ->
    buf.matchSignature(
        0x30.b, 0x26.b, 0xB2.b, 0x75.b, 0x8E.b,
        0x66.b, 0xCF.b, 0x11.b, 0xA6.b, 0xD9.b,
    )
}

internal val VideoMatchers = mapOf(
    Video.Avi to Avi,
    Video.Flv to Flv,
    Video.M4v to M4v,
    Video.Mkv to Mkv,
    Video.Mov to Mov,
    Video.Mp4 to Mp4,
    Video.Mpeg to Mpeg,
    Video.Webm to Webm,
    Video.Wmv to Wmv,
)
