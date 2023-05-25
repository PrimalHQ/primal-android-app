package net.primal.android.serialization

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray

class RoomCustomTypeConverters {

    @TypeConverter
    fun stringToJsonArray(value: String?): List<JsonArray>? {
        return when (value) {
            null -> null
            else -> NostrJson.decodeFromString<List<JsonArray>>(value)
        }
    }

    @TypeConverter
    fun jsonArrayToString(jsonArray: List<JsonArray>?): String? {
        return when (jsonArray) {
            null -> null
            else -> NostrJson.encodeToString(jsonArray)
        }
    }

    @TypeConverter
    fun stringToListOfStrings(value: String?): List<String>? {
        return when (value) {
            null -> null
            else -> NostrJson.decodeFromString<List<String>>(value)
        }
    }

    @TypeConverter
    fun listOfStringsToString(list: List<String>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }
}