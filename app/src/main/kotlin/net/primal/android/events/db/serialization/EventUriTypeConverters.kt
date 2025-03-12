package net.primal.android.events.db.serialization

import androidx.room.TypeConverter
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull
import net.primal.android.events.db.EventUri
import net.primal.android.events.db.EventUriNostr
import net.primal.android.events.domain.CdnImage
import net.primal.android.events.domain.CdnResourceVariant

class EventUriTypeConverters {

    @TypeConverter
    fun stringToListOfNoteNostrUri(value: String?): List<EventUriNostr>? {
        return NostrJson.decodeFromStringOrNull<List<EventUriNostr>>(value)
    }

    @TypeConverter
    fun listOfNoteNostrUriToString(list: List<EventUriNostr>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }

    @TypeConverter
    fun stringToListOfNoteAttachment(value: String?): List<EventUri>? {
        return NostrJson.decodeFromStringOrNull<List<EventUri>>(value)
    }

    @TypeConverter
    fun listOfNoteAttachmentToString(list: List<EventUri>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }

    @TypeConverter
    fun stringToCdnImage(value: String?): CdnImage? {
        return NostrJson.decodeFromStringOrNull<CdnImage>(value)
    }

    @TypeConverter
    fun cdnImageToString(data: CdnImage?): String? {
        return when (data) {
            null -> null
            else -> NostrJson.encodeToString(data)
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
}
