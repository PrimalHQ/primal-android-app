package net.primal.networking.config.store

import androidx.datastore.core.okio.OkioSerializer
import net.primal.data.serialization.NostrJson
import net.primal.networking.config.domain.AppConfig
import net.primal.networking.config.domain.DEFAULT_APP_CONFIG
import okio.BufferedSink
import okio.BufferedSource
import okio.use

object AppConfigSerialization : OkioSerializer<AppConfig> {

    override val defaultValue: AppConfig = DEFAULT_APP_CONFIG

    override suspend fun readFrom(source: BufferedSource): AppConfig {
        return NostrJson.decodeFromString<AppConfig>(source.readUtf8())
    }

    override suspend fun writeTo(t: AppConfig, sink: BufferedSink) {
        sink.use {
            it.writeUtf8(NostrJson.encodeToString(AppConfig.serializer(), t))
        }
    }
}
