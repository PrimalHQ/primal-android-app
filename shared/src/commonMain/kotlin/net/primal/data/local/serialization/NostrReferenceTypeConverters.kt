package net.primal.data.local.serialization

import androidx.room.TypeConverter
import net.primal.core.utils.decodeFromStringOrNull
import net.primal.data.serialization.NostrJson
import net.primal.domain.ReferencedArticle
import net.primal.domain.ReferencedHighlight
import net.primal.domain.ReferencedNote
import net.primal.domain.ReferencedUser
import net.primal.domain.ReferencedZap

class NostrReferenceTypeConverters {

    @TypeConverter
    fun stringToReferencedNote(value: String?): ReferencedNote? {
        return NostrJson.decodeFromStringOrNull<ReferencedNote>(value)
    }

    @TypeConverter
    fun referencedNoteToString(refNote: ReferencedNote?): String? {
        return when (refNote) {
            null -> null
            else -> NostrJson.encodeToString(refNote)
        }
    }

    @TypeConverter
    fun stringToReferencedArticle(value: String?): ReferencedArticle? {
        return NostrJson.decodeFromStringOrNull<ReferencedArticle>(value)
    }

    @TypeConverter
    fun referencedArticleToString(refNote: ReferencedArticle?): String? {
        return when (refNote) {
            null -> null
            else -> NostrJson.encodeToString(refNote)
        }
    }

    @TypeConverter
    fun stringToReferencedHighlight(value: String?): ReferencedHighlight? {
        return NostrJson.decodeFromStringOrNull<ReferencedHighlight>(value)
    }

    @TypeConverter
    fun referencedHighlightToString(refNote: ReferencedHighlight?): String? {
        return when (refNote) {
            null -> null
            else -> NostrJson.encodeToString(refNote)
        }
    }

    @TypeConverter
    fun stringToReferencedUser(value: String?): ReferencedUser? {
        return NostrJson.decodeFromStringOrNull<ReferencedUser>(value)
    }

    @TypeConverter
    fun referencedUserToString(refNote: ReferencedUser?): String? {
        return when (refNote) {
            null -> null
            else -> NostrJson.encodeToString(refNote)
        }
    }

    @TypeConverter
    fun stringToReferencedZap(value: String?): ReferencedZap? {
        return NostrJson.decodeFromStringOrNull<ReferencedZap>(value)
    }

    @TypeConverter
    fun referencedZapToString(refNote: ReferencedZap?): String? {
        return when (refNote) {
            null -> null
            else -> NostrJson.encodeToString(refNote)
        }
    }
}
