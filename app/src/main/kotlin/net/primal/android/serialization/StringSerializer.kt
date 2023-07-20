package net.primal.android.serialization

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream

class StringSerializer : Serializer<String> {

    override val defaultValue: String = ""

    override suspend fun readFrom(input: InputStream): String {
        return String(input.readBytes())
    }

    override suspend fun writeTo(t: String, output: OutputStream) {
        output.use {
            it.write(t.toByteArray())
        }
    }
}
