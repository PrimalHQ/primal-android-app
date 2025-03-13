package net.primal.android.core.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun copyText(
    text: String,
    context: Context,
    label: String = "",
) {
    val clipboard = context.getSystemService(ClipboardManager::class.java)
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
}

const val IMAGE_COMPRESSION_QUALITY = 100

suspend fun copyBitmapToClipboard(
    context: Context,
    bitmap: Bitmap,
    format: Bitmap.CompressFormat? = null,
    errorMessage: String? = null,
) {
    val result = runCatching {
        val file = withContext(Dispatchers.IO) {
            val clipboardDir = context.externalCacheDir
            val fileExtension = when (format ?: getBitmapFormat(bitmap)) {
                Bitmap.CompressFormat.PNG -> "png"
                Bitmap.CompressFormat.JPEG -> "jpg"
                Bitmap.CompressFormat.WEBP -> "webp"
                else -> "jpg"
            }

            val imageFile = File(clipboardDir, "PrimalCopyImage.$fileExtension")

            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(format ?: getBitmapFormat(bitmap), IMAGE_COMPRESSION_QUALITY, outputStream)
            }
            imageFile
        }

        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newUri(context.contentResolver, "Image", uri)

        clipboard.setPrimaryClip(clipData)
        file.deleteOnExit()
    }

    if (result.isFailure) {
        showToast(context, errorMessage ?: "Failed to copy image. Please try again.")
    }
}

private fun getBitmapFormat(bitmap: Bitmap): Bitmap.CompressFormat {
    return when (bitmap.config) {
        Bitmap.Config.ALPHA_8 -> Bitmap.CompressFormat.PNG
        Bitmap.Config.RGB_565, Bitmap.Config.ARGB_8888 -> Bitmap.CompressFormat.JPEG
        Bitmap.Config.RGBA_F16, Bitmap.Config.HARDWARE -> Bitmap.CompressFormat.PNG
        else -> Bitmap.CompressFormat.JPEG
    }
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
