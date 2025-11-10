package net.primal.data.account.local.db

import androidx.room.TypeConverter
import net.primal.data.account.local.dao.RequestState
import net.primal.data.account.local.dao.SignerMethodType

class AccountTypeConverters {
    @TypeConverter
    fun fromSignerMethodType(type: SignerMethodType): String = type.name

    @TypeConverter
    fun toSignerMethodType(name: String): SignerMethodType = SignerMethodType.valueOf(name)

    @TypeConverter
    fun fromRequestState(state: RequestState): String = state.name

    @TypeConverter
    fun toRequestState(name: String): RequestState = RequestState.valueOf(name)
}
