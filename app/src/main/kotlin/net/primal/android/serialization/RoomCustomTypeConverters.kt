package net.primal.android.serialization

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonArray
import net.primal.android.attachments.domain.CdnResourceVariant
import net.primal.android.attachments.db.NoteNostrUri

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
    fun stringToListOfCdnResourceVariant(value: String?): List<CdnResourceVariant>? {
        return NostrJson.decodeFromStringOrNull<List<CdnResourceVariant>>(value)
    }

    @TypeConverter
    fun listOfCdnResourceVariantToString(list: List<CdnResourceVariant>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }

//    @TypeConverter
//    fun stringToListOfEventResourceVariant(value: String?): List<EventResourceVariant>? {
//        return NostrJson.decodeFromStringOrNull<List<EventResourceVariant>>(value)
//    }
//
//    @TypeConverter
//    fun listOfEventResourceVariantToString(list: List<EventResourceVariant>?): String? {
//        return when (list) {
//            null -> null
//            else -> NostrJson.encodeToString(list)
//        }
//    }

    @TypeConverter
    fun stringToListOfNostrResource(value: String?): List<NoteNostrUri>? {
        return NostrJson.decodeFromStringOrNull<List<NoteNostrUri>>(value)
    }

    @TypeConverter
    fun listOfNostrResourceToString(list: List<NoteNostrUri>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }

//    @TypeConverter
//    fun stringToListOfMediaResource(value: String?): List<CDNResource>? {
//        return NostrJson.decodeFromStringOrNull<List<CDNResource>>(value)
//    }
//
//    @TypeConverter
//    fun listOfMediaResourceToString(list: List<CDNResource>?): String? {
//        return when (list) {
//            null -> null
//            else -> NostrJson.encodeToString(list)
//        }
//    }
}
