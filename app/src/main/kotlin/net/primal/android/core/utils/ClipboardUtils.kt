package net.primal.android.core.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL
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

suspend fun copyImageToClipboard(context: Context, imageUrl: String) {
    try {
        val file = withContext(Dispatchers.IO) {
            val url = try {
                URL(imageUrl)
            } catch (e: MalformedURLException) {
                throw IllegalArgumentException("Invalid URL: $imageUrl", e)
            }

            val connection = url.openConnection()
            connection.connect()

            val inputStream = connection.getInputStream()
            val file = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "copied_image.png",
            )

            FileOutputStream(file).use { outputStream ->
                inputStream.use { it.copyTo(outputStream) }
            }
            file
        }

        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newUri(context.contentResolver, "Image", uri)
        clipboard.setPrimaryClip(clipData)
    } catch (e: IllegalArgumentException) {
        showToast(context, "Invalid image URL, ${e.message}")
    } catch (e: IOException) {
        showToast(context, "Network or file error: ${e.message}")
    } catch (e: SecurityException) {
        showToast(context, "Permission issue: ${e.message}")
    }
}

private fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
