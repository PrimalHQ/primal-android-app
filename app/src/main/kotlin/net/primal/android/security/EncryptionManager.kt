package net.primal.android.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class EncryptionManager(
    private val algorithm: String,
    private val blockMode: String,
    private val padding: String,
) {

    companion object {
        private const val KEY_STORE_PROVIDER = "AndroidKeyStore"
    }

    private val transformation = "$algorithm/$blockMode/$padding"

    private val keyStore by lazy {
        KeyStore.getInstance(KEY_STORE_PROVIDER).apply {
            load(null)
        }
    }

    fun getEncryptCipher(keyAlias: String): Cipher {
        return Cipher.getInstance(transformation).apply {
            init(Cipher.ENCRYPT_MODE, resolveSecretKey(keyAlias))
        }
    }

    fun getDecryptCipherForIv(keyAlias: String, iv: ByteArray): Cipher {
        return Cipher.getInstance(transformation).apply {
            init(Cipher.DECRYPT_MODE, resolveSecretKey(keyAlias), IvParameterSpec(iv))
        }
    }

    private fun resolveSecretKey(keyAlias: String): SecretKey {
        val existingKey = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createSecretKey(keyAlias)
    }

    private fun createSecretKey(keyAlias: String): SecretKey =
        KeyGenerator.getInstance(algorithm, KEY_STORE_PROVIDER)
            .apply { init(createKeyGenParameterSpec(keyAlias)) }
            .generateKey()

    private fun createKeyGenParameterSpec(keyAlias: String): KeyGenParameterSpec {
        return KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(blockMode)
            .setEncryptionPaddings(padding)
            .setUserAuthenticationRequired(false)
            .setRandomizedEncryptionRequired(true)
            .build()
    }
}
