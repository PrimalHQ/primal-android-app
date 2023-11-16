package net.primal.android.core.files

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import javax.inject.Inject

class FileAnalyser @Inject constructor(
    private val contentResolver: ContentResolver,
) {
    fun extractImageTypeAndDimensions(uri: Uri): Pair<String?, String?> {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options)
        val width = options.outWidth
        val height = options.outHeight
        val type = options.outMimeType
        return Pair(
            first = type,
            second = if (width != -1 && height != -1) "${width}x$height" else null,
        )
    }
}
