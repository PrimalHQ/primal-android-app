package net.primal.core.utils

import io.ktor.http.Url

private val urlRegexPattern = Regex(
    """(https?://)?(www\.)?[-a-zA-Z0-9@:%+.~#=]{1,256}\.[a-zA-Z0-9()]{2,63}\b([-a-zA-Z0-9()_@:%+.~#?&/=]*)""",
    RegexOption.IGNORE_CASE,
)

fun String.detectUrls(): List<String> {
    return urlRegexPattern.findAll(this).map { matchResult ->
        val url = matchResult.value
        val startIndex = matchResult.range.first
        val charBefore = this.getOrNull(startIndex - 1)

        when (charBefore) {
            '(' -> url.trimEnd(')')
            '[' -> url.trimEnd(']')
            else -> url
        }
    }.toList()
}

fun String.ensureHttpOrHttps(): String =
    if (startsWith(prefix = "http://") || startsWith(prefix = "https://")) {
        this
    } else {
        "https://$this"
    }

private val extensionToMimeType = mapOf(
    "jpeg" to "image/jpeg",
    "jpg" to "image/jpeg",
    "gif" to "image/gif",
    "png" to "image/png",
    "bmp" to "image/bmp",
    "tiff" to "image/tiff",
    "webp" to "image/webp",
    "jp2" to "image/jp2",
    "heic" to "image/heic",
    "heif" to "image/heif",
    "mp4" to "video/mp4",
    "mov" to "video/quicktime",
    "mkv" to "video/x-matroska",
    "avi" to "video/x-msvideo",
    "wmv" to "video/x-ms-wmv",
    "flw" to "video/x-flv",
    "mp3" to "audio/mpeg",
    "wav" to "audio/wav",
    "ogg" to "audio/ogg",
    "aac" to "audio/aac",
    "flac" to "audio/flac",
    "wma" to "audio/x-ms-wma",
    "midi" to "audio/midi",
    "amr" to "audio/amr",
    "opus" to "audio/opus",
    "pdf" to "application/pdf",
    "doc" to "application/msword",
    "docx" to "application/msword",
    "xls" to "application/vnd.ms-excel",
    "xlsx" to "application/vnd.ms-excel",
    "ppt" to "application/vnd.ms-powerpoint",
    "pptx" to "application/vnd.ms-powerpoint",
    "odt" to "application/vnd.oasis.opendocument.text",
    "ods" to "application/vnd.oasis.opendocument.spreadsheet",
    "zip" to "application/zip",
    "rar" to "application/x-rar-compressed",
    "xml" to "application/xml",
    "json" to "application/json",
    "txt" to "text/plain",
    "log" to "text/plain",
    "ini" to "text/plain",
    "conf" to "text/plain",
    "csv" to "text/csv",
    "rtf" to "text/rtf",
    "md" to "text/markdown",
    "yaml" to "text/yaml",
    "yml" to "text/yaml",
    "exe" to "application/x-msdownload",
    "dmg" to "application/x-apple-diskimage",
    "apk" to "application/vnd.android.package-archive",
)

fun String?.detectMimeType(): String? {
    return when {
        this == null -> null
        else -> extensionToMimeType[this.extractExtensionFromUrl().lowercase()]
    }
}

fun String.extractExtensionFromUrl(): String {
    val path = try {
        Url(this).encodedPath
    } catch (error: Exception) {
        this
    }
    return path.substringAfterLast(".", "")
}

private val tldExtractionRegex = Regex("(?:https?://)?(?:www\\.)?([\\w\\d\\-]+\\.[\\w\\d\\-.]+)")

fun String.extractTLD(): String? {
    val matchResult = tldExtractionRegex.find(this)
    val tldCandidate = matchResult?.groupValues?.get(1)
    val parts = tldCandidate?.split(".").orEmpty()
    val partsCount = parts.size
    return when {
        partsCount < 2 -> null
        partsCount == 2 -> tldCandidate
        else -> parts.drop(partsCount - 2).joinToString(".")
    }
}
