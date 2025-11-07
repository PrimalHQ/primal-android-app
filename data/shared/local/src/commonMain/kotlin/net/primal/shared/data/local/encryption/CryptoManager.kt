package net.primal.shared.data.local.encryption

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString

@ExperimentalEncodingApi
internal object CryptoManager {

    private val rawKey: ByteArray by lazy {
        createPlatformKeyStore().getOrCreateKey()
    }

    private val gcmKey by lazy {
        CryptographyProvider.Default
            .get(AES.GCM)
            .keyDecoder()
            .decodeFromByteArrayBlocking(AES.Key.Format.RAW, rawKey)
    }

    inline fun <reified T> encrypt(value: T?, encryptionType: EncryptionType): String? =
        when (encryptionType) {
            EncryptionType.PlainText -> value?.encodeToJsonString()
            EncryptionType.AES -> value?.let { Base64.encode(encryptAsByteArray(text = value.encodeToJsonString())) }
        }

    inline fun <reified T> decrypt(value: String?, encryptionType: EncryptionType): T? =
        when (encryptionType) {
            EncryptionType.PlainText -> value?.decodeFromJsonStringOrNull()
            EncryptionType.AES -> value?.let { decryptToString(blob = Base64.decode(value)) }
                ?.decodeFromJsonStringOrNull()
        }

    private fun encryptAsByteArray(bytes: ByteArray): ByteArray = gcmKey.cipher().encryptBlocking(bytes)

    private fun decryptToByteArray(blob: ByteArray): ByteArray = gcmKey.cipher().decryptBlocking(blob)

    private fun encryptAsByteArray(text: String): ByteArray = encryptAsByteArray(text.encodeToByteArray())

    private fun decryptToString(blob: ByteArray): String = decryptToByteArray(blob).decodeToString()
}
