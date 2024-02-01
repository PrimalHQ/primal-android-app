package net.primal.android.core.files

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import net.primal.android.core.files.error.UnableToSaveContent
import net.primal.android.core.files.error.UnsuccessfulFileDownload
import net.primal.android.core.utils.extractExtensionFromUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.BufferedSource
import okio.sink
import timber.log.Timber

@Singleton
class MediaDownloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentResolver: ContentResolver,
) {
    companion object {
        private const val PICTURES_PRIMAL_FOLDER = "Primal"
    }

    @Throws(UnsuccessfulFileDownload::class)
    fun downloadToMediaGallery(url: String) {
        val request = Request.Builder().url(url).build()
        val client = OkHttpClient.Builder().followRedirects(true).build()
        val response = client.newCall(request).execute()

        val remoteSource = response.body?.source() ?: throw UnsuccessfulFileDownload(message = "No remote source.")

        val fileName = generateFileName()
        val fileExtension = url.extractExtensionFromUrl()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentType = response.header("Content-Type")
                ?: throw UnsuccessfulFileDownload(message = "Unknown Content-Type.")

            saveMediaContent(
                fileName = fileName,
                contentType = contentType,
                contentSource = remoteSource,
                contentResolver = contentResolver,
            )
        } else {
            saveMediaContentSupportMode(
                fileName = "$fileName.$fileExtension",
                contentSource = remoteSource,
                context = context,
            )
        }
    }

    private fun generateFileName(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
        return "primal_${currentDateTime.format(formatter)}"
    }

    @Suppress("ThrowsCount")
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveMediaContent(
        fileName: String,
        contentType: String,
        contentSource: BufferedSource,
        contentResolver: ContentResolver,
    ) {
        val relativePath = Environment.DIRECTORY_PICTURES + File.separatorChar + PICTURES_PRIMAL_FOLDER
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, contentType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }

        val masterUri = if (contentType.startsWith("image")) {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        } else if (contentType.startsWith("video")) {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        } else {
            throw UnableToSaveContent(message = "Unsupported content type.")
        }

        val uri = contentResolver.insert(masterUri, contentValues) ?: throw UnableToSaveContent()
        val result = runCatching {
            val outputStream = contentResolver.openOutputStream(uri) ?: throw UnableToSaveContent()
            outputStream.use { contentSource.readAll(it.sink()) }
        }

        if (result.isFailure) {
            contentResolver.delete(uri, null, null)
            Timber.w(result.exceptionOrNull())
            throw UnableToSaveContent()
        }
    }

    private fun saveMediaContentSupportMode(
        fileName: String,
        contentSource: BufferedSource,
        context: Context,
    ) {
        val rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val subDir = File(rootDir, PICTURES_PRIMAL_FOLDER).apply { if (!exists()) mkdir() }

        val outputFile = File(subDir, fileName)
        outputFile.outputStream().use { contentSource.readAll(it.sink()) }

        MediaScannerConnection.scanFile(context, arrayOf(outputFile.toString()), null, null)
    }
}
