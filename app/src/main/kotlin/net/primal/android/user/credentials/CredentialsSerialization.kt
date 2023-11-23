package net.primal.android.user.credentials

import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import kotlinx.serialization.json.Json
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.security.Encryption
import net.primal.android.security.readDecrypted
import net.primal.android.security.writeEncrypted
import net.primal.android.user.domain.Credential

class CredentialsSerialization(
    private val json: Json = NostrJson,
    private val encryption: Encryption,
) : Serializer<List<Credential>> {

    override val defaultValue: List<Credential> = emptyList()

    override suspend fun readFrom(input: InputStream): List<Credential> = input.readDecrypted(json, encryption)

    override suspend fun writeTo(t: List<Credential>, output: OutputStream) = output.writeEncrypted(t, json, encryption)
}
