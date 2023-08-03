package net.primal.android.core.utils

import com.linkedin.urls.Url
import com.linkedin.urls.detection.UrlDetector
import com.linkedin.urls.detection.UrlDetectorOptions
import net.primal.android.nostr.ext.parseNostrUris

fun String.parseUris(): List<String> {
    val urlDetector = UrlDetector(this, UrlDetectorOptions.JSON)
    val urls = urlDetector.detect()
        .filterInvalidTLDs()
        .map { it.originalUrl }
    val nostr = this.parseNostrUris()
    return urls + nostr
}

private fun List<Url>.filterInvalidTLDs() = filter {
    val tld = it.host.split(".").lastOrNull()
    tld != null && tld.all { char -> char.isLetter() }
}

fun String?.detectContentType(): String? {
    return when {
        this == null -> null
        endsWith(".jpeg") || endsWith(".jpg") -> "image/jpeg"
        endsWith(".gif") -> "image/gif"
        endsWith(".png") -> "image/png"
        endsWith(".bmp") -> "image/bmp"
        endsWith(".tiff") -> "image/tiff"
        endsWith(".webp") -> "image/webp"
        endsWith(".jp2") -> "image/jp2"
        endsWith(".heic") || endsWith("heif") -> "image/heif"
        else -> null
    }
}
