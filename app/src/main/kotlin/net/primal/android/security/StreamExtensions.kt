package net.primal.android.security

import androidx.datastore.core.CorruptionException
import io.github.aakira.napier.Napier
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

inline fun <reified T> InputStream.readDecrypted(json: Json, encryption: Encryption): T {
    val decryptedJson = encryption.decrypt(this)
    return try {
        json.decodeFromString(decryptedJson)
    } catch (error: SerializationException) {
        Napier.w(throwable = error) { "Unable to deserialize decrypted value." }
        throw CorruptionException("Unable to deserialize decrypted value.", error)
    } catch (error: IllegalArgumentException) {
        Napier.w(throwable = error) { "Unable to deserialize decrypted value." }
        throw CorruptionException("Unable to deserialize decrypted value.", error)
    }
}

inline fun <reified T> OutputStream.writeEncrypted(
    value: T,
    json: Json,
    encryption: Encryption,
) {
    encryption.encrypt(json.encodeToString(value), this)
}
