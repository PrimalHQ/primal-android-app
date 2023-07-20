package net.primal.android.serialization

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.primal.android.security.Encryption
import net.primal.android.user.domain.UserAccount
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

class UserAccountsSerialization(
    private val json: Json = NostrJson,
    private val encryption: Encryption,
) : Serializer<List<UserAccount>> {

    override val defaultValue: List<UserAccount> = emptyList()

    override suspend fun readFrom(input: InputStream): List<UserAccount> {
        val decryptedJson = encryption.decrypt(input)
        return try {
            json.decodeFromString(decryptedJson)
        } catch (error: SerializationException) {
            Timber.e(error)
            throw CorruptionException("Unable to deserialize local user data.", error)
        } catch (error: IllegalArgumentException) {
            Timber.e(error)
            throw CorruptionException("Unable to read local user data.", error)
        }
    }

    override suspend fun writeTo(t: List<UserAccount>, output: OutputStream) {
        encryption.encrypt(json.encodeToString(t), output)
    }

}
