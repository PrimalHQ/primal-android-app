@file:Suppress("MagicNumber")

package net.primal.android.crypto

/**
 * Originally copied from:
 * https://github.com/Giszmo/NostrPostr/blob/master/nostrpostrlib/src/main/java/nostr/postr/Utils.kt
 */

import fr.acinq.secp256k1.Secp256k1
import java.security.GeneralSecurityException
import java.security.InvalidAlgorithmParameterException
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import org.spongycastle.util.encoders.Base64
import org.spongycastle.util.encoders.DecoderException
import org.spongycastle.util.encoders.Hex

object CryptoUtils {

    data class Keypair(val privateKey: String, val pubKey: String)

    private val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")

    fun generateHexEncodedKeypair(): Keypair {
        val privateKeyByteArray = privateKeyCreate()
        val pubkeyByteArray = publicKeyCreate(privateKey = privateKeyByteArray)
        return Keypair(privateKey = privateKeyByteArray.toHex(), pubKey = pubkeyByteArray.toHex())
    }

    fun privateKeyCreate(): ByteArray {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes
    }

    fun publicKeyCreate(privateKey: ByteArray) =
        secp256k1.pubKeyCompress(secp256k1.pubkeyCreate(privateKey)).copyOfRange(1, 33)

    fun sign(data: ByteArray, privateKey: ByteArray): ByteArray =
        secp256k1.signSchnorr(
            data,
            privateKey,
            null,
        )

    fun encrypt(
        msg: String,
        privateKey: ByteArray,
        pubKey: ByteArray,
    ): String {
        val sharedSecret = getSharedSecret(privateKey, pubKey)
        return encrypt(msg, sharedSecret)
    }

    fun encrypt(msg: String, sharedSecret: ByteArray): String {
        val iv = ByteArray(16)
        random.nextBytes(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(sharedSecret, "AES"), IvParameterSpec(iv))
        val ivBase64 = Base64.toBase64String(iv)
        val encryptedMsg = cipher.doFinal(msg.toByteArray())
        val encryptedMsgBase64 = Base64.toBase64String(encryptedMsg)
        return "$encryptedMsgBase64?iv=$ivBase64"
    }

    @Throws(GeneralSecurityException::class, DecoderException::class)
    fun decrypt(
        message: String,
        privateKey: ByteArray,
        pubKey: ByteArray,
    ): String {
        val sharedSecret = getSharedSecret(privateKey, pubKey)
        return decrypt(message, sharedSecret)
    }

    @Throws(GeneralSecurityException::class, DecoderException::class)
    private fun decrypt(message: String, sharedSecret: ByteArray): String {
        val parts = message.split("?iv=")
        if (parts.size != 2 || sharedSecret.isEmpty()) throw InvalidAlgorithmParameterException()

        val encryptedMsg = Base64.decode(parts[0])
        val iv = Base64.decode(parts[1])

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(sharedSecret, "AES"), IvParameterSpec(iv))
        return String(cipher.doFinal(encryptedMsg))
    }

    private fun getSharedSecret(privateKey: ByteArray, pubKey: ByteArray): ByteArray =
        secp256k1.pubKeyTweakMul(Hex.decode("02") + pubKey, privateKey).copyOfRange(1, 33)

    fun sha256(byteArray: ByteArray): ByteArray = sha256.digest(byteArray)

    private val secp256k1 = Secp256k1.get()

    private val random = SecureRandom.getInstanceStrong()
}
