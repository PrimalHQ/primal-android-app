package net.primal.android.serialization

import androidx.datastore.core.Serializer
import net.primal.android.security.Encryption
import java.io.InputStream
import java.io.OutputStream

class StringSerializer(
    private val encryption: Encryption? = null,
) : Serializer<String> {

    override val defaultValue: String = ""

    override suspend fun readFrom(input: InputStream): String {
        return encryption?.decrypt(input) ?: String(input.readBytes())
    }

    override suspend fun writeTo(t: String, output: OutputStream) {
        encryption?.encrypt(t, output) ?: output.use {
            it.write(t.toByteArray())
        }
    }
}
