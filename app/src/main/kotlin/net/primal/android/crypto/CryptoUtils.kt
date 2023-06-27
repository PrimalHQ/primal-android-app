package net.primal.android.crypto

/**
 * Copied from:
 * https://github.com/Giszmo/NostrPostr/blob/master/nostrpostrlib/src/main/java/nostr/postr/Utils.kt
 */

import fr.acinq.secp256k1.Secp256k1
import org.spongycastle.util.encoders.Base64
import org.spongycastle.util.encoders.Hex
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object CryptoUtils {
    private val sha256: MessageDigest = MessageDigest.getInstance("SHA-256")

    /**
     * Provides a 32B "private key" aka random number
     */
    fun privateKeyCreate(): ByteArray {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return bytes
    }

    fun publicKeyCreate(privateKey: ByteArray) =
        secp256k1.pubKeyCompress(secp256k1.pubkeyCreate(privateKey)).copyOfRange(1, 33)

    fun sign(data: ByteArray, privateKey: ByteArray): ByteArray =
        secp256k1.signSchnorr(data, privateKey, null)

    fun encrypt(msg: String, privateKey: ByteArray, pubKey: ByteArray): String {
        val sharedSecret = getSharedSecret(privateKey, pubKey)
        return encrypt(msg, sharedSecret)
    }

    fun encrypt(msg: String, sharedSecret: ByteArray): String {
        val iv = ByteArray(16)
        random.nextBytes(iv)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(sharedSecret, "AES"), IvParameterSpec(iv))
        val ivBase64 = Base64.toBase64String(iv)
        val encryptedMsg = cipher.doFinal(msg.toByteArray())
        val encryptedMsgBase64 = Base64.toBase64String(encryptedMsg)
        return "$encryptedMsgBase64?iv=$ivBase64"
    }

    fun decrypt(msg: String, privateKey: ByteArray, pubKey: ByteArray): String {
        val sharedSecret = getSharedSecret(privateKey, pubKey)
        return decrypt(msg, sharedSecret)
    }

    fun decrypt(msg: String, sharedSecret: ByteArray): String {
        val parts = msg.split("?iv=")
        val iv = parts[1].run { Base64.decode(this) }
        val encryptedMsg = parts.first().run { Base64.decode(this) }
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(sharedSecret, "AES"), IvParameterSpec(iv))
        return String(cipher.doFinal(encryptedMsg))
    }

    fun getSharedSecret(privateKey: ByteArray, pubKey: ByteArray): ByteArray =
        secp256k1.pubKeyTweakMul(Hex.decode("02") + pubKey, privateKey).copyOfRange(1, 33)

    fun sha256(byteArray: ByteArray): ByteArray = sha256.digest(byteArray)

    private val secp256k1 = Secp256k1.get()

    private val random = SecureRandom()
}

fun ByteArray.toHex() = String(Hex.encode(this))

fun Int.toByteArray(): ByteArray {
    val bytes = ByteArray(4)
    (0..3).forEach {
        bytes[3 - it] = ((this ushr (8 * it)) and 0xFFFF).toByte()
    }
    return bytes
}

fun ByteArray.toNsec() = Bech32.encodeBytes(hrp = "nsec", this, Bech32.Encoding.Bech32)
fun ByteArray.toNpub() = Bech32.encodeBytes(hrp = "npub", this, Bech32.Encoding.Bech32)

fun String.bechToBytes(hrp: String? = null): ByteArray {

    val decodedForm = Bech32.decodeBytes(this)
    hrp?.also {
        if (it != decodedForm.first) {
            throw IllegalArgumentException("Expected $it but obtained ${decodedForm.first}")
        }
    }
    return decodedForm.second
}

/**
 * Interpret the string as Bech32 encoded and return hrp and ByteArray as Pair
 */
fun String.bechToBytesWithHrp() = Bech32.decodeBytes(this).run { Pair(first, second) }