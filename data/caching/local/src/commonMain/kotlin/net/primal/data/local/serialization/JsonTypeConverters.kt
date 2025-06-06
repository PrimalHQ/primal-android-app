package net.primal.data.local.serialization

import androidx.room.TypeConverter
import kotlinx.serialization.json.JsonArray
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString

class JsonTypeConverters {

    @TypeConverter
    fun stringToJsonArray(value: String?): JsonArray? {
        return value.decodeFromJsonStringOrNull<JsonArray>()
    }

    @TypeConverter
    fun jsonArrayToString(jsonArray: JsonArray?): String? {
        return when (jsonArray) {
            null -> null
            else -> jsonArray.encodeToJsonString()
        }
    }
}
