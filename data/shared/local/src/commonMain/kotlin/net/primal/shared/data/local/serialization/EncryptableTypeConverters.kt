package net.primal.shared.data.local.serialization

import androidx.room.TypeConverter
import net.primal.shared.data.local.encryption.CryptoManager
import net.primal.shared.data.local.encryption.EncryptableLong
import net.primal.shared.data.local.encryption.asEncryptable

object EncryptableTypeConverters {
    @TypeConverter
    fun fromLong(value: EncryptableLong?): ByteArray? =
        value?.let { CryptoManager.encryptAsByteArray(value.decrypted.toString()) }
//    fun fromLong(value: EncryptableLong?): ByteArray? = value?.let { CryptoManager.encrypt(value.decrypted) }

    @TypeConverter
    fun toLong(value: ByteArray?): EncryptableLong? =
        value?.let { CryptoManager.decryptToString(value).toLong().asEncryptable() }
//    fun toLong(value: ByteArray?): EncryptableLong? = value?.let { CryptoManager.decrypt<Long>(value)?.asEncryptable() }
}
