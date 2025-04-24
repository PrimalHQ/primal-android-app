package net.primal.data.local.encryption

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES

internal object CryptoManager {

    private val rawKey: ByteArray = PlatformKeyStore.getOrCreateKey()

    private val gcmKey by lazy {
        CryptographyProvider.Default
            .get(AES.GCM)
            .keyDecoder()
            .decodeFromByteArrayBlocking(AES.Key.Format.RAW, rawKey)
    }

    fun encryptAsByteArray(bytes: ByteArray): ByteArray = gcmKey.cipher().encryptBlocking(bytes)

    fun decryptToByteArray(blob: ByteArray): ByteArray = gcmKey.cipher().decryptBlocking(blob)

    fun encryptAsByteArray(text: String): ByteArray = encryptAsByteArray(text.encodeToByteArray())

    fun decryptToString(blob: ByteArray): String = decryptToByteArray(blob).decodeToString()
}
