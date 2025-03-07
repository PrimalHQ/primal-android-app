package net.primal.serialization.room

import androidx.room.TypeConverter
import kotlinx.serialization.json.JsonArray
import net.primal.serialization.json.NostrJson
import net.primal.serialization.json.decodeFromStringOrNull

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
