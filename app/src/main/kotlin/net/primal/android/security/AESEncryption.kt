package net.primal.android.security

import android.security.keystore.KeyProperties
import java.io.InputStream
import java.io.OutputStream

class AESEncryption(
    private val keyAlias: String,
) : Encryption {

    private val encryptionManager = EncryptionManager(
        algorithm = KeyProperties.KEY_ALGORITHM_AES,
        blockMode = KeyProperties.BLOCK_MODE_CBC,
        padding = KeyProperties.ENCRYPTION_PADDING_PKCS7,
    )

    override fun encrypt(raw: String, outputStream: OutputStream) {
        val encryptCipher = encryptionManager.getEncryptCipher(keyAlias = keyAlias)
        val encryptedBytes = encryptCipher.doFinal(raw.toByteArray())
        outputStream.use {
            it.write(encryptCipher.iv.size)
            it.write(encryptCipher.iv)
            it.write(encryptedBytes)
        }
    }

    override fun decrypt(inputStream: InputStream): String {
        val decryptedBytes = inputStream.use {
            val ivSize = it.read()
            val iv = ByteArray(ivSize)
            it.read(iv)

            val encryptedData = it.readBytes()
            val cipher = encryptionManager.getDecryptCipherForIv(keyAlias, iv)
            cipher.doFinal(encryptedData)
        }
        return String(decryptedBytes, Charsets.UTF_8)
    }
}
