package net.primal.android.editor

import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.db.PrimalDatabase
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.ext.asEventTag
import net.primal.android.nostr.ext.asIMetaTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.asReplaceableEventTag
import net.primal.android.nostr.ext.isPubKeyTag
import net.primal.android.nostr.ext.parseEventTags
import net.primal.android.nostr.ext.parseHashtagTags
import net.primal.android.nostr.ext.parsePubkeyTags
import net.primal.android.nostr.ext.parseReplaceableEventTags
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.nostr.publish.PublishResult
import net.primal.android.nostr.utils.Naddr
import net.primal.android.nostr.utils.Nevent

class NotePublishHandler @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val nostrPublisher: NostrPublisher,
    private val database: PrimalDatabase,
) {

    @Throws(NostrPublishException::class)
    suspend fun publishShortTextNote(
        userId: String,
        content: String,
        attachments: List<NoteAttachment> = emptyList(),
        rootNoteNevent: Nevent? = null,
        rootArticleNaddr: Naddr? = null,
        rootHighlightNevent: Nevent? = null,
        replyToNoteNevent: Nevent? = null,
    ): PublishResult {
        /* Event mentions from content */
        val mentionEventTags = content.parseEventTags(marker = "mention")
        val mentionReplaceableEventTags = content.parseReplaceableEventTags(marker = "mention")
        val allMentionedEventTags = mentionEventTags + mentionReplaceableEventTags

        /* Pubkey mentions from content and referenced pub keys. */
        val mentionPubkeyTags = content.parsePubkeyTags(marker = "mention").toSet()
        val referencedPubkeyTags = resolveReferencedPubkeyTags(
            rootNoteNevent = rootNoteNevent,
            rootArticleNaddr = rootArticleNaddr,
            rootHighlightNevent = rootHighlightNevent,
            replyToNoteNevent = replyToNoteNevent,
        )
        val allPubkeyTags = mentionPubkeyTags + referencedPubkeyTags

        /* Reply & Root tags */
        val replyEventTag = resolveReplyEventTag(
            rootNote = rootNoteNevent,
            replyTo = replyToNoteNevent,
            rootHighlight = rootHighlightNevent,
            rootArticle = rootArticleNaddr,
        )
        val rootTag = resolveRootEventTag(
            highlight = rootHighlightNevent,
            article = rootArticleNaddr,
            note = rootNoteNevent,
        )

        /* Hashtag tags */
        val hashtagTags = content.parseHashtagTags().toSet()

        /* iMeta tags */
        val iMetaTags = attachments.filter { it.isImageAttachment }.map { it.asIMetaTag() }

        /* Relay Hints */
        val allReferenceTags = (allMentionedEventTags + allPubkeyTags + replyEventTag + rootTag).filterNotNull().toSet()
        val relayHintsMap = withContext(dispatcherProvider.io()) {
            val tagNoteIds = allReferenceTags.map { it.get(index = 1).jsonPrimitive.content }
            val hints = database.eventHints().findById(eventIds = tagNoteIds)
            hints.associate { it.eventId to it.relays.first() }
        }
        val allReferenceTagsWithRelays = allReferenceTags.insertRelayHints(relayHintsMap = relayHintsMap)

        /* Content */
        val refinedContent = content.appendAttachmentUrls(attachments)

        return withContext(dispatcherProvider.io()) {
            val outboxRelays = replyToNoteNevent?.eventId?.let { noteId ->
                relayHintsMap[noteId]?.let { relayUrl -> listOf(relayUrl) }
            } ?: emptyList()

            nostrPublisher.signPublishImportNostrEvent(
                userId = userId,
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = NostrEventKind.ShortTextNote.value,
                    tags = (allReferenceTagsWithRelays + hashtagTags + iMetaTags).toList(),
                    content = refinedContent,
                ),
                outboxRelays = outboxRelays,
            )
        }
    }

    private fun resolveRootEventTag(
        highlight: Nevent?,
        article: Naddr?,
        note: Nevent?,
    ) = when {
        highlight != null -> highlight.asEventTag(marker = "root")
        article != null -> article.asReplaceableEventTag(marker = "root")
        note != null -> note.asEventTag(marker = "root")
        else -> null
    }

    private fun resolveReplyEventTag(
        rootNote: Nevent?,
        rootHighlight: Nevent?,
        rootArticle: Naddr?,
        replyTo: Nevent?,
    ): JsonArray? {
        return when {
            rootArticle != null || rootHighlight != null -> {
                replyTo?.asEventTag(marker = "reply")
            }

            replyTo?.eventId != rootNote?.eventId -> {
                replyTo?.asEventTag(marker = "reply")
            }

            else -> null
        }
    }

    private suspend fun resolveReferencedPubkeyTags(
        rootNoteNevent: Nevent?,
        rootArticleNaddr: Naddr?,
        rootHighlightNevent: Nevent?,
        replyToNoteNevent: Nevent?,
    ): Set<JsonArray> {
        val replyToPostData = replyToNoteNevent?.let {
            withContext(dispatcherProvider.io()) {
                database.posts().findByPostId(postId = it.eventId)
            }
        }

        val replyToExistingPubkeyTags = replyToPostData?.tags?.filter { it.isPubKeyTag() }?.toSet() ?: emptySet()
        val replyToAuthorPubkeyTag = replyToNoteNevent?.asPubkeyTag()
        val rootNoteAuthorPubkeyTag = rootNoteNevent?.asPubkeyTag()
        val rootHighlightAuthorPubkeyTag = rootHighlightNevent?.asPubkeyTag()
        val rootArticleAuthorPubkeyTag = rootArticleNaddr?.asPubkeyTag()

        return sequenceOf(
            replyToExistingPubkeyTags,
            listOfNotNull(
                replyToAuthorPubkeyTag,
                rootNoteAuthorPubkeyTag,
                rootHighlightAuthorPubkeyTag,
                rootArticleAuthorPubkeyTag,
            ),
        ).flatten().toSet()
    }

    private fun String.appendAttachmentUrls(attachments: List<NoteAttachment>): String {
        val attachmentUrls = attachments.mapNotNull { it.remoteUrl }
        val refinedContent = if (attachmentUrls.isEmpty()) {
            this
        } else {
            StringBuilder().apply {
                append(this@appendAttachmentUrls)
                appendLine()
                appendLine()
                attachmentUrls.forEachIndexed { index, url ->
                    append(url)
                    if (index < attachmentUrls.size - 1) appendLine()
                }
            }.toString()
        }
        return refinedContent
    }

    private fun Set<JsonArray>.insertRelayHints(relayHintsMap: Map<String, String>) =
        this.map {
            val noteId = it[1].jsonPrimitive.content
            val relayHint = relayHintsMap.getOrDefault(key = noteId, defaultValue = "")
            JsonArray(
                it.toMutableList().apply {
                    if (this.size > 2) {
                        this[2] = JsonPrimitive(relayHint)
                    } else {
                        add(index = 2, JsonPrimitive(relayHint))
                    }
                },
            )
        }
}
