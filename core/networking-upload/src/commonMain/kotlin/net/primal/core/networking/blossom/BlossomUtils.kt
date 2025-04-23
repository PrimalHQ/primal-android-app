package net.primal.core.networking.blossom

fun resolveBlossomUrls(originalUrl: String?, blossoms: List<String>): List<String> {
    if (originalUrl.isNullOrEmpty()) return emptyList()

    val fileName = originalUrl.extractFileHashNameFromUrl()
    return blossoms.map { blossomUrl ->
        "${blossomUrl.ensureEndsWithSlash()}$fileName"
    }
}

private fun String.extractFileHashNameFromUrl(): String? {
    val hashStartIndex = this.lastIndexOf('/') + 1
    return if (hashStartIndex != -1 && hashStartIndex < this.length) {
        this.substring(hashStartIndex)
    } else {
        null
    }
}

private fun String.ensureEndsWithSlash(): String {
    return if (this.endsWith("/")) this else "$this/"
}
