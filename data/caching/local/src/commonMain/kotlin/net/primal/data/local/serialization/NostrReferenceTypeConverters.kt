package net.primal.data.local.serialization

import androidx.room3.ColumnTypeConverter
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.domain.links.ReferencedArticle
import net.primal.domain.links.ReferencedHighlight
import net.primal.domain.links.ReferencedNote
import net.primal.domain.links.ReferencedStream
import net.primal.domain.links.ReferencedUser
import net.primal.domain.links.ReferencedZap

class NostrReferenceTypeConverters {

    @ColumnTypeConverter
    fun stringToReferencedNote(value: String?): ReferencedNote? {
        return value.decodeFromJsonStringOrNull<ReferencedNote>()
    }

    @ColumnTypeConverter
    fun referencedNoteToString(refNote: ReferencedNote?): String? {
        return when (refNote) {
            null -> null
            else -> refNote.encodeToJsonString()
        }
    }

    @ColumnTypeConverter
    fun stringToReferencedArticle(value: String?): ReferencedArticle? {
        return value.decodeFromJsonStringOrNull<ReferencedArticle>()
    }

    @ColumnTypeConverter
    fun referencedArticleToString(refNote: ReferencedArticle?): String? {
        return when (refNote) {
            null -> null
            else -> refNote.encodeToJsonString()
        }
    }

    @ColumnTypeConverter
    fun stringToReferencedHighlight(value: String?): ReferencedHighlight? {
        return value.decodeFromJsonStringOrNull<ReferencedHighlight>()
    }

    @ColumnTypeConverter
    fun referencedHighlightToString(refNote: ReferencedHighlight?): String? {
        return when (refNote) {
            null -> null
            else -> refNote.encodeToJsonString()
        }
    }

    @ColumnTypeConverter
    fun stringToReferencedUser(value: String?): ReferencedUser? {
        return value.decodeFromJsonStringOrNull<ReferencedUser>()
    }

    @ColumnTypeConverter
    fun referencedUserToString(refNote: ReferencedUser?): String? {
        return when (refNote) {
            null -> null
            else -> refNote.encodeToJsonString()
        }
    }

    @ColumnTypeConverter
    fun stringToReferencedZap(value: String?): ReferencedZap? {
        return value.decodeFromJsonStringOrNull<ReferencedZap>()
    }

    @ColumnTypeConverter
    fun referencedZapToString(refNote: ReferencedZap?): String? {
        return when (refNote) {
            null -> null
            else -> refNote.encodeToJsonString()
        }
    }

    @ColumnTypeConverter
    fun stringToReferencedStream(value: String?): ReferencedStream? {
        return value.decodeFromJsonStringOrNull()
    }

    @ColumnTypeConverter
    fun referencedStreamToString(refStream: ReferencedStream?): String? {
        return refStream?.encodeToJsonString()
    }
}
