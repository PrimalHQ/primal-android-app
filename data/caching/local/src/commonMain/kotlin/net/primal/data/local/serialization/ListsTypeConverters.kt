package net.primal.data.local.serialization

import androidx.room.TypeConverter
import kotlinx.serialization.json.JsonArray
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString

class ListsTypeConverters {

    @TypeConverter
    fun stringToListOfJsonArray(value: String?): List<JsonArray>? {
        return value.decodeFromJsonStringOrNull<List<JsonArray>>()
    }

    @TypeConverter
    fun listOfJsonArrayToString(jsonArray: List<JsonArray>?): String? {
        return when (jsonArray) {
            null -> null
            else -> jsonArray.encodeToJsonString()
        }
    }

    @TypeConverter
    fun jsonStringToListOfStrings(value: String?): List<String>? {
        return value.decodeFromJsonStringOrNull<List<String>>()
    }

    @TypeConverter
    fun listOfStringsToJsonString(list: List<String>?): String? {
        return when (list) {
            null -> null
            else -> list.encodeToJsonString()
        }
    }
}
