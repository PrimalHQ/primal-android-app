package net.primal.android.core.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
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
) {
    try {
        val file = withContext(Dispatchers.IO) {
            val clipboardDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            val fileExtension = when (format ?: getBitmapFormat(bitmap)) {
                Bitmap.CompressFormat.PNG -> "png"
                Bitmap.CompressFormat.JPEG -> "jpg"
                Bitmap.CompressFormat.WEBP -> "webp"
                else -> "jpg"
            }

            val imageFile = File(clipboardDir, "copied_image.$fileExtension")

            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(format ?: getBitmapFormat(bitmap), IMAGE_COMPRESSION_QUALITY, outputStream)
            }
            imageFile
        }

        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newUri(context.contentResolver, "Image", uri)

        clipboard.setPrimaryClip(clipData)
    } catch (e: IOException) {
        showToast(context, "Failed to copy image: ${e.message}")
    } catch (e: SecurityException) {
        showToast(context, "Permission issue: ${e.message}")
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
