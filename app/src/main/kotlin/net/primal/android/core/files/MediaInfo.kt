package net.primal.android.core.files

data class MediaInfo(
    val mimeType: String? = null,
    val dimensionInPixels: String? = null,
    val durationInSeconds: Double? = null,
    val bitrateInBitsPerSec: Long? = null,
) {
    val hasAnyData: Boolean
        get() = mimeType != null || dimensionInPixels != null ||
            durationInSeconds != null || bitrateInBitsPerSec != null
}
