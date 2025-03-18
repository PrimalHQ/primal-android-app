package net.primal.data.local.serialization

import androidx.room.TypeConverter
import kotlinx.serialization.json.JsonArray
import net.primal.core.utils.decodeFromStringOrNull
import net.primal.data.serialization.NostrJson

class JsonTypeConverters {

    @TypeConverter
    fun stringToJsonArray(value: String?): JsonArray? {
        return NostrJson.decodeFromStringOrNull<JsonArray>(value)
    }

    @TypeConverter
    fun jsonArrayToString(jsonArray: JsonArray?): String? {
        return when (jsonArray) {
            null -> null
            else -> NostrJson.encodeToString(jsonArray)
        }
    }
}
