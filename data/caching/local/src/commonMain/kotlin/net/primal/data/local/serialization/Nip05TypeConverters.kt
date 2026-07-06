package net.primal.data.local.serialization

import androidx.room3.ColumnTypeConverter
import net.primal.core.utils.runCatching
import net.primal.domain.profile.Nip05VerificationStatus

class Nip05TypeConverters {

    @ColumnTypeConverter
    fun nip05StatusToString(status: Nip05VerificationStatus?): String? {
        return status?.name
    }

    @ColumnTypeConverter
    fun stringToNip05Status(value: String?): Nip05VerificationStatus? {
        return value?.let { runCatching { Nip05VerificationStatus.valueOf(it) }.getOrNull() }
    }
}
