package net.primal.android.core.crash

import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
        Thread.setDefaultUncaughtExceptionHandler { t, e ->
            val errorReport = generateErrorReport(t, e)
            scope.launch {
                uploadCrashReport(report = errorReport)

                if (defaultUncaughtExceptionHandler != null) {
                    defaultUncaughtExceptionHandler.uncaughtException(t, e)
                } else {
                    exitProcess(1)
                }
            }
        }
    }

    private fun generateErrorReport(thread: Thread, throwable: Throwable): String {
        return """
            # Primal Crash Reporter
            # Application: ${BuildConfig.APPLICATION_ID}
            # Platform: android
            # Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})
            # Date: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}
                
            Thread: ${thread.name}
            Exception: ${throwable.javaClass.simpleName}
            Message: ${throwable.message ?: ""}
                
            Stack Trace:
            ${throwable.stackTraceToString()}
            Caused by:
            ${throwable.cause?.causeChainToString()}
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
