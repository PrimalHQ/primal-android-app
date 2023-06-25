package net.primal.android.serialization

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.primal.android.security.Encryption
import net.primal.android.user.domain.UserAccount
import timber.log.Timber
import java.io.InputStream
import java.io.OutputStream

class UserAccountSerialization(private val encryption: Encryption) : Serializer<UserAccount> {

    override val defaultValue: UserAccount = UserAccount.EMPTY

    override suspend fun readFrom(input: InputStream): UserAccount {
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

    override suspend fun writeTo(t: UserAccount, output: OutputStream) {
        encryption.encrypt(NostrJson.encodeToString(t), output)
    }

}
