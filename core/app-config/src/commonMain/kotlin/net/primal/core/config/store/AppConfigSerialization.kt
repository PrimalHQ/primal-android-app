package net.primal.core.config.store

import androidx.datastore.core.okio.OkioSerializer
import kotlinx.serialization.json.Json
import net.primal.core.config.DEFAULT_APP_CONFIG
import net.primal.domain.global.AppConfig
import okio.BufferedSink
import okio.BufferedSource
import okio.use

object AppConfigSerialization : OkioSerializer<AppConfig> {

    private val jsonSerializer = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    override val defaultValue: AppConfig = DEFAULT_APP_CONFIG

    override suspend fun readFrom(source: BufferedSource): AppConfig {
        return jsonSerializer.decodeFromString<AppConfig>(source.readUtf8())
    }

    override suspend fun writeTo(t: AppConfig, sink: BufferedSink) {
        sink.use {
            it.writeUtf8(jsonSerializer.encodeToString(AppConfig.serializer(), t))
        }
    }
}
