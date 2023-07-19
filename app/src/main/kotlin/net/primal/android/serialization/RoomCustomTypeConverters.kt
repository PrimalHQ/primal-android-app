package net.primal.android.serialization

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import net.primal.android.nostr.model.primal.PrimalResourceVariant

class RoomCustomTypeConverters {

    @TypeConverter
    fun stringToListOfJsonArray(value: String?): List<JsonArray>? {
        return when (value) {
            null -> null
            else -> try {
                NostrJson.decodeFromString<List<JsonArray>>(value)
            } catch (error: IllegalArgumentException) {
                null
            }
        }
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
        return when (value) {
            null -> null
            else -> try {
                NostrJson.decodeFromString<List<String>>(value)
            } catch (error: IllegalArgumentException) {
                null
            }
        }
    }

    @TypeConverter
    fun listOfStringsToJsonString(list: List<String>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }

    @TypeConverter
    fun stringToListOfPrimalResourceVariant(value: String?): List<PrimalResourceVariant>? {
        return when (value) {
            null -> null
            else -> try {
                NostrJson.decodeFromString<List<PrimalResourceVariant>>(value)
            } catch (error: IllegalArgumentException) {
                null
            }
        }
    }

    @TypeConverter
    fun listOfPrimalResourceVariantToString(list: List<PrimalResourceVariant>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }
}