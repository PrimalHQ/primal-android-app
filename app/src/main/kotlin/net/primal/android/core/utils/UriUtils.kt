package net.primal.android.core.utils

import com.linkedin.urls.Url
import com.linkedin.urls.detection.UrlDetector
import com.linkedin.urls.detection.UrlDetectorOptions
import java.net.MalformedURLException
import java.net.URL
import net.primal.android.nostr.ext.detectUrls
import net.primal.android.nostr.ext.parseNostrUris
import timber.log.Timber

fun String.parseUris(includeNostrUris: Boolean = true): List<String> {
    val urlDetector = UrlDetector(this, UrlDetectorOptions.JSON)
    val libUrls = urlDetector.detect()
        .filterInvalidTLDs()
        .map { it.originalUrl }
    val customUrls = this.detectUrls()
    val mergedUrls = mergeUrls(emptyList(), customUrls)

    return if (includeNostrUris) {
        val nostr = this.parseNostrUris()
        nostr + mergedUrls
    } else {
        mergedUrls
    }
}

fun mergeUrls(libUrls: List<String>, customUrls: List<String>): List<String> {
    val result = mutableListOf<String>()

    val allUrls = (libUrls + customUrls).distinct()
    val visited = mutableSetOf<String>()

    for (url in allUrls) {
        if (visited.any { existing -> isRelativeMatch(existing, url) }) {
            val matchingUrl = visited.first { existing -> isRelativeMatch(existing, url) }
            if (url.length > matchingUrl.length) {
                visited.remove(matchingUrl)
                visited.add(url)
            }
        } else {
            visited.add(url)
        }
    }

    result.addAll(visited)
    return result
}

fun isRelativeMatch(url1: String, url2: String): Boolean {
    return url1.startsWith(url2) || url2.startsWith(url1)
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
