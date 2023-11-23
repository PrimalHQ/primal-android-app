package net.primal.android.core.serialization.room

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull

class ListsTypeConverters {

    @TypeConverter
    fun stringToListOfJsonArray(value: String?): List<JsonArray>? {
        return NostrJson.decodeFromStringOrNull<List<JsonArray>>(value)
    }

    @TypeConverter
    fun listOfJsonArrayToString(jsonArray: List<JsonArray>?): String? {
        return when (jsonArray) {
            null -> null
            else -> NostrJson.encodeToString(jsonArray)
        }
    }

    @TypeConverter
    fun jsonStringToListOfStrings(value: String?): List<String>? {
        return NostrJson.decodeFromStringOrNull<List<String>>(value)
    }

    @TypeConverter
    fun listOfStringsToJsonString(list: List<String>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }
}
