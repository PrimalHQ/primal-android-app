package net.primal.data.local.serialization

import androidx.room.TypeConverter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.local.dao.polls.PollOption

class PollTypeConverters {

    @TypeConverter
    fun stringToPollOptions(value: String?): List<PollOption>? {
        return value.decodeFromJsonStringOrNull<List<PollOption>>()
    }

    @TypeConverter
    fun pollOptionsToString(data: List<PollOption>?): String? {
        return data?.encodeToJsonString()
    }
}
