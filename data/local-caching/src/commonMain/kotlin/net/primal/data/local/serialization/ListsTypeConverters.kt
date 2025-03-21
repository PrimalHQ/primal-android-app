package net.primal.data.local.serialization

import androidx.room.TypeConverter
import kotlinx.serialization.json.JsonArray
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull

class ListsTypeConverters {

    @TypeConverter
    fun stringToListOfJsonArray(value: String?): List<JsonArray>? {
        return CommonJson.decodeFromStringOrNull<List<JsonArray>>(value)
    }

    @TypeConverter
    fun listOfJsonArrayToString(jsonArray: List<JsonArray>?): String? {
        return when (jsonArray) {
            null -> null
            else -> CommonJson.encodeToString(jsonArray)
        }
    }

    @TypeConverter
    fun jsonStringToListOfStrings(value: String?): List<String>? {
        return CommonJson.decodeFromStringOrNull<List<String>>(value)
    }

    @TypeConverter
    fun listOfStringsToJsonString(list: List<String>?): String? {
        return when (list) {
            null -> null
            else -> CommonJson.encodeToString(list)
        }
    }
}
