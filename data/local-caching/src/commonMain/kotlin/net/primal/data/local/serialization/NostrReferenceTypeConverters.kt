package net.primal.data.local.serialization

import androidx.room.TypeConverter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.links.ReferencedArticle
import net.primal.domain.links.ReferencedHighlight
import net.primal.domain.links.ReferencedNote
import net.primal.domain.links.ReferencedUser
import net.primal.domain.links.ReferencedZap

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
