package net.primal.android.core.logging

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.aakira.napier.LogLevel
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.primal.core.utils.coroutines.DispatcherProvider

@Singleton
class AppLogRecorder @Inject constructor(
    @ApplicationContext context: Context,
    dispatcherProvider: DispatcherProvider,
) {
    private val logsDirectory: File = File(context.filesDir, LOGS_DIRECTORY).apply {
        if (!exists()) mkdirs()
    }

    private val scope = CoroutineScope(SupervisorJob() + dispatcherProvider.io())
    private val writeMutex = Mutex()

    private var currentWriter: BufferedWriter? = null
    private var currentFile: File? = null
    private var currentFileSize: Long = 0L

    private val json = Json { encodeDefaults = true }

    fun writeLog(
        level: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ) {
        scope.launch {
            writeMutex.withLock {
                runCatching {
                    ensureWriterOpen()
                    val logEntry = buildLogEntry(level, tag, throwable, message)
                    currentWriter?.appendLine(logEntry)
                    currentWriter?.flush()
                    currentFileSize += logEntry.toByteArray(Charsets.UTF_8).size + 1

                    if (currentFileSize >= MAX_FILE_SIZE_BYTES) {
                        rotateFile()
                    }
                }
            }
        }
    }

    private fun buildLogEntry(
        level: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?,
    ): String {
        val timestamp = Clock.System.now().format(ISO_TIMESTAMP_FORMAT, UtcOffset.ZERO)
        val jsonObject = buildJsonObject {
            put("ts", timestamp)
            put("level", level.name.first().toString())
            tag?.let { put("tag", it) }
            message?.let { put("msg", it) }
            throwable?.let { put("error", it.stackTraceToString()) }
        }
        return json.encodeToString(jsonObject)
    }

    private fun ensureWriterOpen() {
        if (currentWriter == null || currentFile == null) {
            val newFile = createNewLogFile()
            currentFile = newFile
            currentWriter = BufferedWriter(FileWriter(newFile, true))
            currentFileSize = newFile.length()
        }
    }

    private fun createNewLogFile(): File {
        val timestamp = System.currentTimeMillis()
        return File(logsDirectory, "app_log_$timestamp.jsonl")
    }

    private fun rotateFile() {
        currentWriter?.close()
        currentWriter = null
        currentFile = null
        currentFileSize = 0L

        cleanupOldFiles()
    }

    private fun cleanupOldFiles() {
        val logFiles = getLogFiles()
        if (logFiles.size > MAX_FILES) {
            logFiles.sortedBy { it.lastModified() }
                .take(logFiles.size - MAX_FILES)
                .forEach { it.delete() }
        }
    }

    fun getLogFiles(): List<File> {
        return logsDirectory.listFiles { file ->
            file.name.startsWith("app_log_") && file.name.endsWith(".jsonl")
        }?.toList() ?: emptyList()
    }

    fun getTotalLogSize(): Long {
        return getLogFiles().sumOf { it.length() }
    }

    fun getLogFileCount(): Int {
        return getLogFiles().size
    }

    suspend fun clearLogs() {
        writeMutex.withLock {
            currentWriter?.close()
            currentWriter = null
            currentFile = null
            currentFileSize = 0L

            getLogFiles().forEach { it.delete() }
        }
    }

    fun getLogsDirectory(): File = logsDirectory

    companion object {
        private const val LOGS_DIRECTORY = "app_logs"
        private const val MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L // 10MB
        private const val MAX_FILES = 10

        val ISO_TIMESTAMP_FORMAT = DateTimeComponents.Format {
            year()
            char('-')
            monthNumber(Padding.ZERO)
            char('-')
            dayOfMonth(Padding.ZERO)
            char('T')
            hour(Padding.ZERO)
            char(':')
            minute(Padding.ZERO)
            char(':')
            second(Padding.ZERO)
            char('.')
            secondFraction(3)
            char('Z')
        }
    }
}
