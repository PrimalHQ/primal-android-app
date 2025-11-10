package net.primal.data.account.local.db

import androidx.room.TypeConverter
import net.primal.data.account.local.dao.RemoteSignerMethodDataType

class AccountTypeConverters {
    @TypeConverter
    fun fromRemoteSignerMethodDataType(type: RemoteSignerMethodDataType): String = type.name

    @TypeConverter
    fun toRemoteSignerMethodDataType(name: String): RemoteSignerMethodDataType =
        RemoteSignerMethodDataType.valueOf(name)
}
