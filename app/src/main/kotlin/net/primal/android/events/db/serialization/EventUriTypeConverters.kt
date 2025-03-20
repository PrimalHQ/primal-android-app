package net.primal.android.events.db.serialization

import androidx.room.TypeConverter
import net.primal.android.events.db.EventUri
import net.primal.android.events.db.EventUriNostr
import net.primal.android.events.domain.CdnImage
import net.primal.android.events.domain.CdnResourceVariant
import net.primal.core.utils.serialization.CommonJson
import net.primal.core.utils.serialization.decodeFromStringOrNull

class EventUriTypeConverters {

    @TypeConverter
    fun stringToListOfNoteNostrUri(value: String?): List<EventUriNostr>? {
        return CommonJson.decodeFromStringOrNull<List<EventUriNostr>>(value)
    }

    @TypeConverter
    fun listOfNoteNostrUriToString(list: List<EventUriNostr>?): String? {
        return when (list) {
            null -> null
            else -> CommonJson.encodeToString(list)
        }
    }

    @TypeConverter
    fun stringToListOfNoteAttachment(value: String?): List<EventUri>? {
        return CommonJson.decodeFromStringOrNull<List<EventUri>>(value)
    }

    @TypeConverter
    fun listOfNoteAttachmentToString(list: List<EventUri>?): String? {
        return when (list) {
            null -> null
            else -> CommonJson.encodeToString(list)
        }
    }

    @TypeConverter
    fun stringToCdnImage(value: String?): CdnImage? {
        return CommonJson.decodeFromStringOrNull<CdnImage>(value)
    }

    @TypeConverter
    fun cdnImageToString(data: CdnImage?): String? {
        return when (data) {
            null -> null
            else -> CommonJson.encodeToString(data)
        }
    }

    @TypeConverter
    fun stringToListOfCdnResourceVariant(value: String?): List<CdnResourceVariant>? {
        return CommonJson.decodeFromStringOrNull<List<CdnResourceVariant>>(value)
    }

    @TypeConverter
    fun listOfCdnResourceVariantToString(list: List<CdnResourceVariant>?): String? {
        return when (list) {
            null -> null
            else -> CommonJson.encodeToString(list)
        }
    }
}
