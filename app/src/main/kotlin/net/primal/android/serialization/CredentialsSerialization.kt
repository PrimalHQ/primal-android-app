package net.primal.android.serialization

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.primal.android.security.Encryption
import net.primal.android.user.domain.Credential
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

class CredentialsSerialization(private val encryption: Encryption) : Serializer<List<Credential>> {

    override val defaultValue: List<Credential> = emptyList()

    override suspend fun readFrom(input: InputStream): List<Credential> {
        val decryptedJson = encryption.decrypt(input)
        return try {
            NostrJson.decodeFromString(decryptedJson)
        } catch (error: SerializationException) {
            Timber.e(error)
            throw CorruptionException("Unable to deserialize local user data.", error)
        } catch (error: IllegalArgumentException) {
            Timber.e(error)
            throw CorruptionException("Unable to read local user data.", error)
        }
    }

    override suspend fun writeTo(t: List<Credential>, output: OutputStream) {
        encryption.encrypt(NostrJson.encodeToString(t), output)
    }

}
