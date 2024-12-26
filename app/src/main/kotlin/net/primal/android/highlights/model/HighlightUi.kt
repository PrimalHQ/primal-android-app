package net.primal.android.highlights.model

import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.highlights.db.Highlight
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.utils.Nevent

data class HighlightUi(
    val highlightId: String,
    val authorId: String,
    val author: ProfileDetailsUi?,
    val content: String,
    val context: String?,
    val alt: String?,
    val referencedEventATag: String?,
    val referencedEventAuthorId: String?,
    val createdAt: Long,
    val comments: List<CommentUi>,
)

data class JoinedHighlightsUi(
    val highlightId: String,
    val authors: Set<ProfileDetailsUi>,
    val content: String,
    val comments: List<CommentUi>,
    val referencedEventATag: String?,
    val referencedEventAuthorId: String?,
    val context: String?,
)

fun Highlight.asHighlightUi() =
    HighlightUi(
        highlightId = this.data.highlightId,
        authorId = this.data.authorId,
        author = this.author?.asProfileDetailsUi(),
        content = this.data.content,
        context = this.data.context,
        alt = this.data.alt,
        referencedEventATag = this.data.referencedEventATag,
        referencedEventAuthorId = this.data.referencedEventAuthorId,
        createdAt = this.data.createdAt,
        comments = this.comments.map { it.toCommentUi() },
    )

operator fun JoinedHighlightsUi.plus(element: JoinedHighlightsUi): JoinedHighlightsUi =
    JoinedHighlightsUi(
        highlightId = this.highlightId,
        authors = this.authors + element.authors,
        content = this.content,
        comments = this.comments + element.comments,
        referencedEventATag = this.referencedEventATag ?: element.referencedEventATag,
        referencedEventAuthorId = this.referencedEventAuthorId ?: element.referencedEventAuthorId,
        context = this.context ?: element.context,
    )

fun List<Highlight>.joinOnContent(): List<JoinedHighlightsUi> = this.groupBy { it.data.content }.map { it.value.sum() }

fun List<Highlight>.sum() =
    this.map { it.asJoinedHighlightsUi() }.reduce { acc, joinedHighlightsUi -> acc + joinedHighlightsUi }

fun Highlight.asJoinedHighlightsUi() =
    JoinedHighlightsUi(
        highlightId = this.data.highlightId,
        authors = setOfNotNull(this.author?.asProfileDetailsUi()),
        content = this.data.content,
        comments = this.comments.map { it.toCommentUi() },
        referencedEventATag = this.data.referencedEventATag,
        referencedEventAuthorId = this.data.referencedEventAuthorId,
        context = this.data.context,
    )

fun HighlightUi.generateNevent() =
    Nevent(
        kind = NostrEventKind.Highlight.value,
        eventId = this.highlightId,
        userId = this.authorId,
    )
