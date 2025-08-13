package net.primal.shared.data.local.serialization

import androidx.room.TypeConverter
import net.primal.shared.data.local.encryption.CryptoManager
import net.primal.shared.data.local.encryption.Encryptable
import net.primal.shared.data.local.encryption.asEncryptable

object EncryptableTypeConverters {
    @TypeConverter
    fun fromLong(value: Encryptable<Long>?): ByteArray? = value?.let { CryptoManager.encrypt(value.decrypted) }

    @TypeConverter
    fun toLong(value: ByteArray?): Encryptable<Long>? =
        value?.let { CryptoManager.decrypt<Long>(value)?.asEncryptable() }

    @TypeConverter
    fun fromString(value: Encryptable<String>?): ByteArray? = value?.let { CryptoManager.encrypt(value.decrypted) }

    @TypeConverter
    fun toString(value: ByteArray?): Encryptable<String>? =
        value?.let { CryptoManager.decrypt<String>(value)?.asEncryptable() }

    @TypeConverter
    fun fromStringList(value: Encryptable<List<String>>?): ByteArray? =
        value?.let { CryptoManager.encrypt(value.decrypted) }

    @TypeConverter
    fun toStringList(value: ByteArray?): Encryptable<List<String>>? =
        value?.let { CryptoManager.decrypt<List<String>>(value)?.asEncryptable() }

    @TypeConverter
    fun fromDouble(value: Encryptable<Double>?): ByteArray? = value?.let { CryptoManager.encrypt(value.decrypted) }

    @TypeConverter
    fun toDouble(value: ByteArray?): Encryptable<Double>? =
        value?.let { CryptoManager.decrypt<Double>(value)?.asEncryptable() }

    @TypeConverter
    fun fromInt(value: Encryptable<Int>?): ByteArray? = value?.let { CryptoManager.encrypt(value.decrypted) }

    @TypeConverter
    fun toInt(value: ByteArray?): Encryptable<Int>? = value?.let { CryptoManager.decrypt<Int>(value)?.asEncryptable() }
}
