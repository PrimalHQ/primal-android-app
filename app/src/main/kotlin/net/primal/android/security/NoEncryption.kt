package net.primal.android.security

import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets

class NoEncryption : Encryption {

    override fun encrypt(raw: String, outputStream: OutputStream) {
        outputStream.write(raw.toByteArray())
    }

    override fun decrypt(inputStream: InputStream): String {
        return String(inputStream.readBytes(), StandardCharsets.UTF_8)
    }
}
