package net.primal.shared.data.local.encryption

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString

internal object CryptoManager {

    private val rawKey: ByteArray = createPlatformKeyStore().getOrCreateKey()

    private val gcmKey by lazy {
        CryptographyProvider.Default
            .get(AES.GCM)
            .keyDecoder()
            .decodeFromByteArrayBlocking(AES.Key.Format.RAW, rawKey)
    }

    inline fun <reified T> encrypt(value: T?): ByteArray? =
        value?.let { encryptAsByteArray(text = value.encodeToJsonString()) }

    inline fun <reified T> decrypt(value: ByteArray?): T? =
        value?.let { decryptToString(blob = value) }?.decodeFromJsonStringOrNull()

    private fun encryptAsByteArray(bytes: ByteArray): ByteArray = gcmKey.cipher().encryptBlocking(bytes)

    private fun decryptToByteArray(blob: ByteArray): ByteArray = gcmKey.cipher().decryptBlocking(blob)

    private fun encryptAsByteArray(text: String): ByteArray = encryptAsByteArray(text.encodeToByteArray())

    private fun decryptToString(blob: ByteArray): String = decryptToByteArray(blob).decodeToString()
}
