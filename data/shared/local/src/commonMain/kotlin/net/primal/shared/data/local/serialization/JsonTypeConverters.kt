package net.primal.shared.data.local.serialization

import androidx.room3.ColumnTypeConverter
import kotlinx.serialization.json.JsonArray
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString

class JsonTypeConverters {

    @ColumnTypeConverter
    fun stringToJsonArray(value: String?): JsonArray? {
        return value.decodeFromJsonStringOrNull<JsonArray>()
    }

    @ColumnTypeConverter
    fun jsonArrayToString(jsonArray: JsonArray?): String? {
        return when (jsonArray) {
            null -> null
            else -> jsonArray.encodeToJsonString()
        }
    }
}
