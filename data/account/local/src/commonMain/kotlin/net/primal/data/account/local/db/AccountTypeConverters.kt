package net.primal.data.account.local.db

import androidx.room.TypeConverter
import net.primal.domain.account.model.SignerMethodType

class AccountTypeConverters {
    @TypeConverter
    fun fromSignerMethodType(type: SignerMethodType): String = type.name

    @TypeConverter
    fun toSignerMethodType(name: String): SignerMethodType = SignerMethodType.valueOf(name)
}
