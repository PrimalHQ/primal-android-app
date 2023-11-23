package net.primal.android.config.store

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.json.Json
import net.primal.android.config.domain.AppConfig
import net.primal.android.config.domain.DEFAULT_APP_CONFIG
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.security.Encryption
import net.primal.android.security.readDecrypted
import net.primal.android.security.writeEncrypted

class AppConfigSerialization(
    private val json: Json = NostrJson,
    private val encryption: Encryption,
) : Serializer<AppConfig> {

    override val defaultValue: AppConfig = DEFAULT_APP_CONFIG

    override suspend fun readFrom(input: InputStream): AppConfig = input.readDecrypted(json, encryption)

    override suspend fun writeTo(t: AppConfig, output: OutputStream) = output.writeEncrypted(t, json, encryption)
}
