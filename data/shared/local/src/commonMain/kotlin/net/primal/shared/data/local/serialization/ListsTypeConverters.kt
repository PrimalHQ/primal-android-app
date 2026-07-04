package net.primal.shared.data.local.serialization

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.json.JsonArray
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString

class ListsTypeConverters {

    @ColumnTypeConverter
    fun stringToListOfJsonArray(value: String?): List<JsonArray>? {
        return value.decodeFromJsonStringOrNull<List<JsonArray>>()
    }

    @ColumnTypeConverter
    fun listOfJsonArrayToString(jsonArray: List<JsonArray>?): String? {
        return when (jsonArray) {
            null -> null
            else -> jsonArray.encodeToJsonString()
        }
    }

    @ColumnTypeConverter
    fun jsonStringToListOfStrings(value: String?): List<String>? {
        return value.decodeFromJsonStringOrNull<List<String>>()
    }

    @ColumnTypeConverter
    fun listOfStringsToJsonString(list: List<String>?): String? {
        return when (list) {
            null -> null
            else -> list.encodeToJsonString()
        }
    }
}
