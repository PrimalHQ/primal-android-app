package net.primal.android.highlights.model

import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.highlights.db.Highlight

data class HighlightUi(
    val highlightId: String,
    val author: ProfileDetailsUi?,
    val content: String,
    val context: String?,
    val alt: String?,
    val referencedEventATag: String?,
    val referencedEventAuthorId: String?,
    val createdAt: Long,
    val comments: List<CommentUi>,
)

fun Highlight.asHighlightUi() =
    HighlightUi(
        highlightId = this.data.highlightId,
        author = this.author?.asProfileDetailsUi(),
        content = this.data.content,
        context = this.data.context,
        alt = this.data.alt,
        referencedEventATag = this.data.referencedEventATag,
        referencedEventAuthorId = this.data.referencedEventAuthorId,
        createdAt = this.data.createdAt,
        comments = this.comments.map { it.toCommentUi() },
    )
