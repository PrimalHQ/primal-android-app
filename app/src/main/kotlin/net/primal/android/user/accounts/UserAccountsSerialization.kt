package net.primal.android.user.accounts

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.json.Json
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.security.Encryption
import net.primal.android.security.readDecrypted
import net.primal.android.security.writeEncrypted
import net.primal.android.user.domain.UserAccount

class UserAccountsSerialization(
    private val json: Json = NostrJson,
    private val encryption: Encryption,
) : Serializer<List<UserAccount>> {

    override val defaultValue: List<UserAccount> = emptyList()

    override suspend fun readFrom(input: InputStream): List<UserAccount> = input.readDecrypted(json, encryption)

    override suspend fun writeTo(t: List<UserAccount>, output: OutputStream) =
        output.writeEncrypted(t, json, encryption)
}
