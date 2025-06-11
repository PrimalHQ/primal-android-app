package net.primal.shared.data.local.serialization

import androidx.room.TypeConverter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.shared.data.local.encryption.CryptoManager
import net.primal.shared.data.local.encryption.EncryptableString

class EncryptableStringConvertor {

    @TypeConverter
    fun toDb(value: EncryptableString?): ByteArray? = value?.decrypted?.let(CryptoManager::encryptAsByteArray)

    @TypeConverter
    fun fromDb(bytes: ByteArray?): EncryptableString? =
        bytes?.let(CryptoManager::decryptToString)?.let { EncryptableString(decrypted = it) }

    @TypeConverter
    fun listToDb(list: List<String>?): ByteArray? =
        list?.let { CryptoManager.encryptAsByteArray(it.encodeToJsonString()) }

    @TypeConverter
    fun listFromDb(bytes: ByteArray?): List<String>? =
        bytes?.let { CryptoManager.decryptToString(it).decodeFromJsonStringOrNull() }
}
