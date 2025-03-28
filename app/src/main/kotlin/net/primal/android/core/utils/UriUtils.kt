package net.primal.android.core.utils

import com.linkedin.urls.Url
import com.linkedin.urls.detection.UrlDetector
import com.linkedin.urls.detection.UrlDetectorOptions
import java.net.MalformedURLException
import java.net.URL
import java.util.regex.Pattern
import net.primal.android.nostr.ext.parseNostrUris
import timber.log.Timber

private val urlRegexPattern: Pattern = Pattern.compile(
    "https?://(www\\.)?[-a-zA-Z0-9@:%.+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()_@:%+.~#?&//=]*)",
    Pattern.CASE_INSENSITIVE,
)

fun String.parseUris(includeNostrUris: Boolean = true): List<String> {
    val urlDetector = UrlDetector(this, UrlDetectorOptions.JSON)
    val libUrls = urlDetector.detect()
        .filterInvalidTLDs()
        .map { it.originalUrl }
    val customUrls = this.detectUrls()
    val mergedUrls = mergeUrls(libUrls, customUrls, this)

    return if (includeNostrUris) {
        val nostr = this.parseNostrUris()
        nostr + mergedUrls
    } else {
        mergedUrls
    }
}

private fun String.detectUrls(): List<String> {
    val urlRegex = urlRegexPattern.toRegex()
    return urlRegex.findAll(this).map { matchResult ->
        val url = matchResult.groupValues[0]
        val startIndex = matchResult.range.first
        val charBefore = this.getOrNull(startIndex - 1)

        when (charBefore) {
            '(' -> url.trimEnd(')')
            '[' -> url.trimEnd(']')
            else -> url
        }
    }.toList()
}

private fun mergeUrls(
    libUrls: List<String>,
    customUrls: List<String>,
    content: String,
): List<String> {
    val result = mutableListOf<String>()
    val allUrls = (libUrls + customUrls).distinct()
    val visited = mutableSetOf<String>()

    for (originalUrl in allUrls) {
        val potentialDuplicateUrl = visited.find { existing -> isRelativeMatch(existing, originalUrl) }

        if (potentialDuplicateUrl != null) {
            val bothUrlExists = content.containsUrl(potentialDuplicateUrl) && content.containsUrl(originalUrl)
            if (bothUrlExists) {
                visited.add(originalUrl)
            } else if (originalUrl.length > potentialDuplicateUrl.length) {
                visited.remove(potentialDuplicateUrl)
                visited.add(originalUrl)
            }
        } else {
            visited.add(originalUrl)
        }
    }

    result.addAll(visited)
    return result
}

private fun isRelativeMatch(url1: String, url2: String): Boolean {
    return url1.startsWith(url2) || url2.startsWith(url1)
}

private fun String.containsUrl(url: String): Boolean {
    return Regex("\\b${Regex.escape(url)}\\b").containsMatchIn(this)
}

private fun List<Url>.filterInvalidTLDs() =
    filter {
        val tld = it.host.split(".").lastOrNull()
        tld != null && tld.all { char -> char.isLetter() }
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
    val file = try {
        URL(this).file
    } catch (error: MalformedURLException) {
        Timber.w(error)
        this
    }
    return file.substringAfterLast(".", "")
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
