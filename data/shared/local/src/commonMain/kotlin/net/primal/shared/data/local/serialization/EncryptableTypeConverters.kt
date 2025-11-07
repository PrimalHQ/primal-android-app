package net.primal.shared.data.local.serialization

import androidx.room.TypeConverter
import kotlin.io.encoding.ExperimentalEncodingApi
import net.primal.shared.data.local.encryption.CryptoManager
import net.primal.shared.data.local.encryption.Encryptable
import net.primal.shared.data.local.encryption.EncryptionType
import net.primal.shared.data.local.encryption.asEncryptable

@OptIn(ExperimentalEncodingApi::class)
object EncryptableTypeConverters {
    var enableEncryption = true

    private val encryptionType: EncryptionType
        get() = if (enableEncryption) {
            EncryptionType.AES
        } else {
            EncryptionType.PlainText
        }

    @TypeConverter
    fun fromLong(value: Encryptable<Long>?): String? =
        value?.let { CryptoManager.encrypt(value.decrypted, encryptionType) }

    @TypeConverter
    fun toLong(value: String?): Encryptable<Long>? =
        value?.let { CryptoManager.decrypt<Long>(value, encryptionType)?.asEncryptable() }

    @TypeConverter
    fun fromString(value: Encryptable<String>?): String? =
        value?.let { CryptoManager.encrypt(value.decrypted, encryptionType) }

    @TypeConverter
    fun toString(value: String?): Encryptable<String>? =
        value?.let { CryptoManager.decrypt<String>(value, encryptionType)?.asEncryptable() }

    @TypeConverter
    fun fromStringList(value: Encryptable<List<String>>?): String? =
        value?.let { CryptoManager.encrypt(value.decrypted, encryptionType) }

    @TypeConverter
    fun toStringList(value: String?): Encryptable<List<String>>? =
        value?.let { CryptoManager.decrypt<List<String>>(value, encryptionType)?.asEncryptable() }

    @TypeConverter
    fun fromDouble(value: Encryptable<Double>?): String? =
        value?.let { CryptoManager.encrypt(value.decrypted, encryptionType) }

    @TypeConverter
    fun toDouble(value: String?): Encryptable<Double>? =
        value?.let { CryptoManager.decrypt<Double>(value, encryptionType)?.asEncryptable() }

    @TypeConverter
    fun fromInt(value: Encryptable<Int>?): String? =
        value?.let { CryptoManager.encrypt(value.decrypted, encryptionType) }

    @TypeConverter
    fun toInt(value: String?): Encryptable<Int>? =
        value?.let { CryptoManager.decrypt<Int>(value, encryptionType)?.asEncryptable() }

    @TypeConverter
    fun fromBoolean(value: Encryptable<Boolean>?): String? =
        value?.let { CryptoManager.encrypt(value.decrypted, encryptionType) }

    @TypeConverter
    fun toBoolean(value: String?): Encryptable<Boolean>? =
        value?.let { CryptoManager.decrypt<Boolean>(value, encryptionType)?.asEncryptable() }
}
