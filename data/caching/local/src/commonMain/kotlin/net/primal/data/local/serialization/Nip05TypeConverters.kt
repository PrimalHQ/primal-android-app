package net.primal.data.local.serialization

import androidx.room.TypeConverter
import net.primal.core.utils.runCatching
import net.primal.domain.profile.Nip05VerificationStatus

class Nip05TypeConverters {

    @TypeConverter
    fun nip05StatusToString(status: Nip05VerificationStatus?): String? {
        return status?.name
    }

    @TypeConverter
    fun stringToNip05Status(value: String?): Nip05VerificationStatus? {
        return value?.let { runCatching { Nip05VerificationStatus.valueOf(it) }.getOrNull() }
    }
}
