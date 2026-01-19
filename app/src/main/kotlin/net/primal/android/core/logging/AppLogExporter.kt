package net.primal.android.core.logging

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.datetime.Clock
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.primal.android.BuildConfig

@Singleton
class AppLogExporter @Inject constructor(
    private val recorder: AppLogRecorder,
) {
    private val json = Json { prettyPrint = true }

    fun exportLogsAsZip(cacheDir: File): File? {
        val logFiles = recorder.getLogFiles()
        if (logFiles.isEmpty()) return null

        cleanupPreviousExports(cacheDir)

        val timestamp = System.currentTimeMillis()
        val zipFile = File(cacheDir, "primal_logs_$timestamp.zip")

        val zipResult = runCatching {
            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                logFiles.forEach { logFile ->
                    zos.putNextEntry(ZipEntry(logFile.name))
                    logFile.inputStream().use { input ->
                        input.copyTo(zos)
                    }
                    zos.closeEntry()
                }

                zos.putNextEntry(ZipEntry("metadata.json"))
                val metadata = buildMetadata()
                zos.write(metadata.toByteArray())
                zos.closeEntry()
            }
        }

        return zipResult.fold(
            onSuccess = { zipFile },
            onFailure = {
                zipFile.delete()
                null
            },
        )
    }

    private fun buildMetadata(): String {
        val exportTimestamp = Clock.System.now().format(AppLogRecorder.ISO_TIMESTAMP_FORMAT, UtcOffset.ZERO)
        val jsonObject = buildJsonObject {
            put("app_version", BuildConfig.VERSION_NAME)
            put("app_version_code", BuildConfig.VERSION_CODE)
            put("device_manufacturer", Build.MANUFACTURER)
            put("device_model", Build.MODEL)
            put("android_version", Build.VERSION.RELEASE)
            put("android_sdk", Build.VERSION.SDK_INT)
            put("export_timestamp", exportTimestamp)
            put("log_file_count", recorder.getLogFileCount())
            put("total_log_size_bytes", recorder.getTotalLogSize())
        }
        return json.encodeToString(jsonObject)
    }

    private fun cleanupPreviousExports(cacheDir: File) {
        cacheDir.listFiles { file ->
            file.name.startsWith("primal_logs_") && file.name.endsWith(".zip")
        }?.forEach { it.delete() }
    }

    companion object {
        fun shareLogs(context: Context, uri: Uri) {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "application/zip"
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Logs"))
        }
    }
}
