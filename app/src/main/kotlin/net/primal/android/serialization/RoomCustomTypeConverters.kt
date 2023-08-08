package net.primal.android.serialization

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import net.primal.android.feed.db.MediaResource
import net.primal.android.feed.db.NostrResource
import net.primal.android.nostr.model.primal.PrimalResourceVariant

class RoomCustomTypeConverters {

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

    @TypeConverter
    fun stringToListOfPrimalResourceVariant(value: String?): List<PrimalResourceVariant>? {
        return NostrJson.decodeFromStringOrNull<List<PrimalResourceVariant>>(value)
    }

    @TypeConverter
    fun listOfPrimalResourceVariantToString(list: List<PrimalResourceVariant>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }

    @TypeConverter
    fun stringToListOfNostrResource(value: String?): List<NostrResource>? {
        return NostrJson.decodeFromStringOrNull<List<NostrResource>>(value)
    }

    @TypeConverter
    fun listOfNostrResourceToString(list: List<NostrResource>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }

    @TypeConverter
    fun stringToListOfMediaResource(value: String?): List<MediaResource>? {
        return NostrJson.decodeFromStringOrNull<List<MediaResource>>(value)
    }

    @TypeConverter
    fun listOfMediaResourceToString(list: List<MediaResource>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }
}