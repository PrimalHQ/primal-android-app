package net.primal.data.local.encryption

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.random.Random

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object PlatformKeyStore {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val PREFS_NAME = "caching_database_preferences"
    private const val KEY_ALIAS = "wrapped_aes_key"
    private const val ENCRYPTED_KEY = "encrypted_key"
    private const val ENCRYPTED_IV = "encrypted_iv"

    private lateinit var appContext: Context

    fun init(appContext: Context) {
        this.appContext = appContext.applicationContext
    }

    actual fun getOrCreateKey(): ByteArray {
        val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encryptedKey = prefs.getString(ENCRYPTED_KEY, null)
        val encodedIv = prefs.getString(ENCRYPTED_IV, null)

        val secretKey = getOrCreateSecretKey()

        return if (encryptedKey != null && encodedIv != null) {
            decryptRawKey(secretKey, encryptedKey, encodedIv)
        } else {
            val rawKey = ByteArray(32).also { Random.Default.nextBytes(it) }
            val (encKey, iv) = encryptRawKey(secretKey, rawKey)

            prefs.edit {
                putString(ENCRYPTED_KEY, encKey)
                putString(ENCRYPTED_IV, iv)
            }

            rawKey
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        keyStore.getKey(KEY_ALIAS, null)?.let { return it as SecretKey }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val builder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(256)
            setUserAuthenticationRequired(false)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                if (appContext.hasStrongBox()) builder.setIsStrongBoxBacked(true)
                keyGenerator.init(builder.build())
            } catch (_: Exception) {
                builder.setIsStrongBoxBacked(false)
                keyGenerator.init(builder.build())
            }
        } else {
            keyGenerator.init(builder.build())
        }

        return keyGenerator.generateKey()
    }

    private fun encryptRawKey(secretKey: SecretKey, rawKey: ByteArray): Pair<String, String> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(rawKey)
        return Base64.encodeToString(encrypted, Base64.DEFAULT) to
            Base64.encodeToString(iv, Base64.DEFAULT)
    }

    private fun decryptRawKey(
        secretKey: SecretKey,
        encryptedBase64: String,
        ivBase64: String,
    ): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = Base64.decode(ivBase64, Base64.DEFAULT)
        val encrypted = Base64.decode(encryptedBase64, Base64.DEFAULT)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        return cipher.doFinal(encrypted)
    }

    fun Context.hasStrongBox(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
            packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
}
