@file:Suppress("MagicNumber")

package net.primal.domain.nostr.cryptography.utils

/**
 * Originally copied from:
 * https://github.com/Giszmo/NostrPostr/blob/master/nostrpostrlib/src/main/java/nostr/postr/Utils.kt
 */

import fr.acinq.secp256k1.Hex
import fr.acinq.secp256k1.Secp256k1
import io.ktor.utils.io.charsets.Charsets
import io.ktor.utils.io.core.toByteArray
import korlibs.crypto.AES.Companion.decryptAesCbc
import korlibs.crypto.AES.Companion.encryptAesCbc
import korlibs.crypto.CipherPadding
import korlibs.crypto.fillRandomBytes
import korlibs.crypto.sha256
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import net.primal.domain.nostr.cryptography.NostrKeyPair

object CryptoUtils {

    private const val AES_BLOCK_SIZE = 16
    private const val PRIVATE_KEY_BLOCK_SIZE = 32

    fun generateHexEncodedKeypair(): NostrKeyPair {
        val privateKeyByteArray = privateKeyCreate()
        val pubkeyByteArray = publicKeyCreate(privateKey = privateKeyByteArray)
        return NostrKeyPair(
            privateKey = privateKeyByteArray.toHex(),
            pubKey = pubkeyByteArray.toHex(),
        )
    }

    private fun privateKeyCreate(): ByteArray = generateSecureRandomBytes(PRIVATE_KEY_BLOCK_SIZE)

    fun publicKeyCreate(privateKey: ByteArray): ByteArray =
        secp256k1.pubKeyCompress(secp256k1.pubkeyCreate(privateKey)).copyOfRange(1, 33)

    fun sign(data: ByteArray, privateKey: ByteArray): ByteArray = secp256k1.signSchnorr(data, privateKey, null)

    @ExperimentalEncodingApi
    fun encrypt(
        msg: String,
        privateKey: ByteArray,
        pubKey: ByteArray,
    ): String {
        val sharedSecret = getSharedSecret(privateKey, pubKey)
        return encrypt(msg, sharedSecret)
    }

    @ExperimentalEncodingApi
    private fun encrypt(msg: String, sharedSecret: ByteArray): String {
        val iv = generateSecureRandomBytes(AES_BLOCK_SIZE)

        val plaintextBytes = msg.toByteArray(Charsets.UTF_8)

        val encryptedBytes = encryptAesCbc(
            data = plaintextBytes,
            key = sharedSecret,
            iv = iv,
            padding = CipherPadding.PKCS7Padding,
        )

        val ivBase64 = Base64.encode(iv)
        val encryptedMsgBase64 = Base64.encode(encryptedBytes)

        return "$encryptedMsgBase64?iv=$ivBase64"
    }

    @ExperimentalEncodingApi
    @Throws(IllegalArgumentException::class)
    fun decrypt(
        message: String,
        privateKey: ByteArray,
        pubKey: ByteArray,
    ): String {
        val sharedSecret = getSharedSecret(privateKey, pubKey)
        return decrypt(message, sharedSecret)
    }

    @ExperimentalEncodingApi
    @Throws(IllegalArgumentException::class)
    private fun decrypt(message: String, sharedSecret: ByteArray): String {
        val parts = message.split("?iv=")
        if (parts.size != 2 || sharedSecret.isEmpty()) throw IllegalArgumentException()

        val encryptedMsg = Base64.decode(parts[0])
        val iv = Base64.decode(parts[1])

        return decryptAesCbc(
            data = encryptedMsg,
            key = sharedSecret,
            iv = iv,
            padding = CipherPadding.PKCS7Padding,
        ).decodeToString()
    }

    private fun getSharedSecret(privateKey: ByteArray, pubKey: ByteArray): ByteArray =
        secp256k1.pubKeyTweakMul(Hex.decode("02") + pubKey, privateKey).copyOfRange(1, 33)

    fun sha256(byteArray: ByteArray): ByteArray = byteArray.sha256().bytes

    private val secp256k1 = Secp256k1.get()

    private fun generateSecureRandomBytes(size: Int) = ByteArray(size).also { fillRandomBytes(it) }
}
