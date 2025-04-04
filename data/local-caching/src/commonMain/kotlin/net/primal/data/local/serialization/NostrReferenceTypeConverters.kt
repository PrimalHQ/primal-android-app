package net.primal.data.local.serialization

import androidx.room.TypeConverter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.ReferencedArticle
import net.primal.domain.ReferencedHighlight
import net.primal.domain.ReferencedNote
import net.primal.domain.ReferencedUser
import net.primal.domain.ReferencedZap

class NostrReferenceTypeConverters {

    @TypeConverter
    fun stringToReferencedNote(value: String?): ReferencedNote? {
        return value.decodeFromJsonStringOrNull<ReferencedNote>()
    }

    @TypeConverter
    fun referencedNoteToString(refNote: ReferencedNote?): String? {
        return when (refNote) {
            null -> null
            else -> refNote.encodeToJsonString()
        }
    }

    @TypeConverter
    fun stringToReferencedArticle(value: String?): ReferencedArticle? {
        return value.decodeFromJsonStringOrNull<ReferencedArticle>()
    }

    @TypeConverter
    fun referencedArticleToString(refNote: ReferencedArticle?): String? {
        return when (refNote) {
            null -> null
            else -> refNote.encodeToJsonString()
        }
    }

    @TypeConverter
    fun stringToReferencedHighlight(value: String?): ReferencedHighlight? {
        return value.decodeFromJsonStringOrNull<ReferencedHighlight>()
    }

    @TypeConverter
    fun referencedHighlightToString(refNote: ReferencedHighlight?): String? {
        return when (refNote) {
            null -> null
            else -> refNote.encodeToJsonString()
        }
    }

    @TypeConverter
    fun stringToReferencedUser(value: String?): ReferencedUser? {
        return value.decodeFromJsonStringOrNull<ReferencedUser>()
    }

    @TypeConverter
    fun referencedUserToString(refNote: ReferencedUser?): String? {
        return when (refNote) {
            null -> null
            else -> refNote.encodeToJsonString()
        }
    }

    @TypeConverter
    fun stringToReferencedZap(value: String?): ReferencedZap? {
        return value.decodeFromJsonStringOrNull<ReferencedZap>()
    }

    @TypeConverter
    fun referencedZapToString(refNote: ReferencedZap?): String? {
        return when (refNote) {
            null -> null
            else -> refNote.encodeToJsonString()
        }
    }
}
