package net.primal.android.core.utils

import com.linkedin.urls.detection.UrlDetector
import com.linkedin.urls.detection.UrlDetectorOptions
import net.primal.android.nostr.ext.parseNip19

fun String.parseUris(): List<String> {
    val urlDetector = UrlDetector(this, UrlDetectorOptions.JSON)
    val urls = urlDetector.detect().map { it.originalUrl }
    val nostr = this.parseNip19()
    return urls + nostr
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
