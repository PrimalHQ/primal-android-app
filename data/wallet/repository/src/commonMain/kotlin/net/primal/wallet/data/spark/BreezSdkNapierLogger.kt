package net.primal.wallet.data.spark

import breez_sdk_spark.LogEntry
import breez_sdk_spark.Logger
import breez_sdk_spark.initLogging
import io.github.aakira.napier.Napier

internal object BreezSdkNapierLogger : Logger {

    private const val TAG = "BreezSdk"

    init {
        initLogging(logDir = null, appLogger = this, logFilter = null)
    }

    fun ensureInitialized() = Unit

    override fun log(l: LogEntry) {
        when (l.level.lowercase()) {
            "error" -> Napier.e(tag = TAG) { l.line }
            "warn" -> Napier.w(tag = TAG) { l.line }
            "info" -> Napier.i(tag = TAG) { l.line }
            "debug" -> Napier.d(tag = TAG) { l.line }
            "trace" -> Napier.v(tag = TAG) { l.line }
            else -> Napier.d(tag = TAG) { "[${l.level}] ${l.line}" }
        }
    }
}
