package net.primal.android.core.files

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_BITRATE
import android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
import android.media.MediaMetadataRetriever.METADATA_KEY_MIMETYPE
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
import android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
import android.net.Uri
import io.github.aakira.napier.Napier
import javax.inject.Inject

class FileAnalyser @Inject constructor(
    private val contentResolver: ContentResolver,
) {
    fun extractMediaInfo(uri: Uri): MediaInfo {
        val imageInfo = tryExtractImageInfo(uri)
        if (imageInfo.mimeType?.startsWith("image") == true) {
            return imageInfo
        }

        return tryExtractVideoAudioInfo(uri)
    }

    private fun tryExtractImageInfo(uri: Uri): MediaInfo {
        return runCatching {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream, null, options)
            }

            val width = options.outWidth
            val height = options.outHeight
            val type = options.outMimeType

            MediaInfo(
                mimeType = type,
                dimensionInPixels = if (width != -1 && height != -1) "${width}x$height" else null,
            )
        }.getOrElse {
            Napier.w(message = "Failed to extract image metadata from $uri", throwable = it)
            MediaInfo()
        }
    }

    @Suppress("MagicNumber")
    private fun tryExtractVideoAudioInfo(uri: Uri): MediaInfo {
        return runCatching {
            MediaMetadataRetriever().useCompat { retriever ->
                contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    retriever.setDataSource(pfd.fileDescriptor)

                    val mimeType = retriever.extractMetadata(METADATA_KEY_MIMETYPE)
                    val width = retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull()
                    val height = retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull()
                    val durationMs = retriever.extractMetadata(METADATA_KEY_DURATION)?.toLongOrNull()
                    val bitrate = retriever.extractMetadata(METADATA_KEY_BITRATE)?.toLongOrNull()

                    MediaInfo(
                        mimeType = mimeType,
                        dimensionInPixels = if (width != null && height != null) "${width}x$height" else null,
                        durationInSeconds = durationMs?.let { (it / 1000.0).roundToTwoDecimals() },
                        bitrateInBitsPerSec = bitrate,
                    )
                } ?: MediaInfo()
            }
        }.getOrElse {
            Napier.w(message = "Failed to extract video/audio metadata from $uri", throwable = it)
            MediaInfo()
        }
    }

    private inline fun <R> MediaMetadataRetriever.useCompat(block: (MediaMetadataRetriever) -> R): R {
        return try {
            block(this)
        } finally {
            runCatching { release() }
        }
    }

    @Suppress("MagicNumber")
    private fun Double.roundToTwoDecimals(): Double = (this * 100).toLong() / 100.0
}
