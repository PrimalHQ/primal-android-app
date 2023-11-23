package net.primal.android.attachments.db.serialization

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import net.primal.android.attachments.db.NoteAttachment
import net.primal.android.attachments.db.NoteNostrUri
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.attachments.domain.CdnResourceVariant
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.core.serialization.json.decodeFromStringOrNull

class AttachmentTypeConverters {

    @TypeConverter
    fun stringToListOfNoteNostrUri(value: String?): List<NoteNostrUri>? {
        return NostrJson.decodeFromStringOrNull<List<NoteNostrUri>>(value)
    }

    @TypeConverter
    fun listOfNoteNostrUriToString(list: List<NoteNostrUri>?): String? {
        return when (list) {
            null -> null
            else -> NostrJson.encodeToString(list)
        }
    }

    @TypeConverter
    fun stringToListOfNoteAttachment(value: String?): List<NoteAttachment>? {
        return NostrJson.decodeFromStringOrNull<List<NoteAttachment>>(value)
    }

    @TypeConverter
    fun listOfNoteAttachmentToString(list: List<NoteAttachment>?): String? {
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
