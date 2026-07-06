package net.primal.data.local.serialization

import androidx.room3.ColumnTypeConverter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.local.dao.polls.PollOption

class PollTypeConverters {

    @ColumnTypeConverter
    fun stringToPollOptions(value: String?): List<PollOption>? {
        return value.decodeFromJsonStringOrNull<List<PollOption>>()
    }

    @ColumnTypeConverter
    fun pollOptionsToString(data: List<PollOption>?): String? {
        return data?.encodeToJsonString()
    }
}
