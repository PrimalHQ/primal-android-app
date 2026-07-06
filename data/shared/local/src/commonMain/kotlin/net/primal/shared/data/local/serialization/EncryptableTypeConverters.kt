package net.primal.shared.data.local.serialization

import androidx.room3.ColumnTypeConverter
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

    @ColumnTypeConverter
    fun fromLong(value: Encryptable<Long>?): String? =
        value?.let { CryptoManager.encrypt(value.decrypted, encryptionType) }

    @ColumnTypeConverter
    fun toLong(value: String?): Encryptable<Long>? =
        value?.let { CryptoManager.decrypt<Long>(value, encryptionType)?.asEncryptable() }

    @ColumnTypeConverter
    fun fromString(value: Encryptable<String>?): String? =
        value?.let { CryptoManager.encrypt(value.decrypted, encryptionType) }

    @ColumnTypeConverter
    fun toString(value: String?): Encryptable<String>? =
        value?.let { CryptoManager.decrypt<String>(value, encryptionType)?.asEncryptable() }

    @ColumnTypeConverter
    fun fromStringList(value: Encryptable<List<String>>?): String? =
        value?.let { CryptoManager.encrypt(value.decrypted, encryptionType) }

    @ColumnTypeConverter
    fun toStringList(value: String?): Encryptable<List<String>>? =
        value?.let { CryptoManager.decrypt<List<String>>(value, encryptionType)?.asEncryptable() }

    @ColumnTypeConverter
    fun fromDouble(value: Encryptable<Double>?): String? =
        value?.let { CryptoManager.encrypt(value.decrypted, encryptionType) }

    @ColumnTypeConverter
    fun toDouble(value: String?): Encryptable<Double>? =
        value?.let { CryptoManager.decrypt<Double>(value, encryptionType)?.asEncryptable() }

    @ColumnTypeConverter
    fun fromInt(value: Encryptable<Int>?): String? =
        value?.let { CryptoManager.encrypt(value.decrypted, encryptionType) }

    @ColumnTypeConverter
    fun toInt(value: String?): Encryptable<Int>? =
        value?.let { CryptoManager.decrypt<Int>(value, encryptionType)?.asEncryptable() }

    @ColumnTypeConverter
    fun fromBoolean(value: Encryptable<Boolean>?): String? =
        value?.let { CryptoManager.encrypt(value.decrypted, encryptionType) }

    @ColumnTypeConverter
    fun toBoolean(value: String?): Encryptable<Boolean>? =
        value?.let { CryptoManager.decrypt<Boolean>(value, encryptionType)?.asEncryptable() }
}
