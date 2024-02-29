package net.primal.android.core.crash

import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.primal.android.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class PrimalCrashReporter @Inject constructor(
    okHttpClient: OkHttpClient,
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private val okHttpClient = okHttpClient
        .newBuilder()
        .callTimeout(3.seconds.toJavaDuration())
        .build()

    fun init() {
        val defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            val errorReport = error.generateErrorReport(thread)
            scope.launch {
                uploadCrashReport(report = errorReport.prependCrashReportHeader())

                if (defaultUncaughtExceptionHandler != null) {
                    defaultUncaughtExceptionHandler.uncaughtException(thread, error)
                } else {
                    exitProcess(1)
                }
            }
        }
    }

    fun log(throwable: Throwable? = null, message: String? = null) {
        val errorReport = throwable?.generateErrorReport(thread = Thread.currentThread()) ?: ""
        scope.launch {
            uploadCrashReport(report = errorReport.prependLogReportHeader(message = message))
        }
    }

    private fun String.prependCrashReportHeader(): String {
        return "# Primal Crash Reporter\n$this"
    }

    private fun String.prependLogReportHeader(message: String? = null): String {
        return "# Primal Log Reporter\n# ${message ?: ""}\n$this"
    }

    private fun Throwable.generateErrorReport(thread: Thread): String {
        return """
            # Application: ${BuildConfig.APPLICATION_ID}
            # Platform: android
            # Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
            # Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
                
            Thread: ${thread.name}
            Exception: ${this.javaClass.simpleName}
            Message: ${this.message ?: ""}
                
            Stack Trace:
            ${this.stackTraceToString()}
            Caused by:
            ${this.cause?.causeChainToString()}
        """
            .trimIndent()
            .let { report -> report.split("\n").joinToString("\n") { it.trim() } }
    }

    private fun Throwable.causeChainToString(): String {
        val stringWriter = StringWriter()
        var cause: Throwable? = this
        while (cause != null) {
            stringWriter.append(cause.stackTraceToString())
            stringWriter.appendLine()
            stringWriter.appendLine()
            cause = cause.cause
        }
        return stringWriter.toString()
    }

    private fun uploadCrashReport(report: String) {
        okHttpClient.newCall(
            Request.Builder()
                .url("https://dev.primal.net/crash-report")
                .post(report.toRequestBody(contentType = "text/plain".toMediaType()))
                .build(),
        ).execute()
    }
}
