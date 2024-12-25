package net.primal.android.editor

import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.assertOnlyOneNotNull
import net.primal.android.db.PrimalDatabase
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asIMetaTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.asReplaceableEventTag
import net.primal.android.nostr.ext.isPubKeyTag
import net.primal.android.nostr.ext.parseEventTags
import net.primal.android.nostr.ext.parseHashtagTags
import net.primal.android.nostr.ext.parsePubkeyTags
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.notes.db.PostData

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
        rootArticleEventId: String? = null,
        rootArticleId: String? = null,
        rootArticleAuthorId: String? = null,
        rootHighlightId: String? = null,
        rootHighlightAuthorId: String? = null,
        rootPostId: String? = null,
        replyToPostId: String? = null,
        replyToAuthorId: String? = null,
    ): Boolean {
        assertOnlyOneNotNull(rootArticleId, rootPostId) { "You can have only one root event." }

        val replyPostData = replyToPostId?.let {
            withContext(dispatcherProvider.io()) {
                database.posts().findByPostId(postId = it)
            }
        }

        /* Note tags */
        val mentionEventTags = content.parseEventTags(marker = "mention")
        val rootPostTag = rootPostId?.asEventIdTag(marker = "root")
        val replyEventTag = if (rootPostId != replyToPostId) {
            replyToPostId?.asEventIdTag(marker = "reply")
        } else {
            null
        }

        val rootEventTags = if (rootHighlightId != null && rootHighlightAuthorId != null) {
            listOf(rootHighlightId.asEventIdTag(marker = "root"))
        } else if (rootArticleId != null && rootArticleAuthorId != null && rootArticleEventId != null) {
            val tagContent = "${NostrEventKind.LongFormContent.value}:$rootArticleAuthorId:$rootArticleId"
            listOf(
                rootArticleEventId.asEventIdTag(marker = "root"),
                tagContent.asReplaceableEventTag(marker = "root"),
            )
        } else {
            listOf(rootPostTag)
        }

        val eventTags = setOfNotNull(*rootEventTags.toTypedArray(), replyEventTag) + mentionEventTags

        val relayHintsMap = withContext(dispatcherProvider.io()) {
            val tagNoteIds = eventTags.map { it.get(index = 1).jsonPrimitive.content }
            val hints = database.eventHints().findById(eventIds = tagNoteIds)
            hints.associate { it.eventId to it.relays.first() }
        }
        val noteTags = eventTags.map {
            val noteId = it[1].jsonPrimitive.content
            val relayHint = relayHintsMap.getOrDefault(key = noteId, defaultValue = "")
            JsonArray(it.toMutableList().apply { this[2] = JsonPrimitive(relayHint) })
        }

        /* Pubkey tags */
        val pubkeyTags = constructPubkeyTags(
            replyPostData = replyPostData,
            replyToAuthorId = replyToAuthorId,
            rootHighlightAuthorId = rootHighlightAuthorId,
            rootArticleAuthorId = rootArticleAuthorId,
            content = content,
        )

        /* Hashtag tags */
        val hashtagTags = content.parseHashtagTags().toSet()

        /* iMeta tags */
        val attachmentUrls = attachments.mapNotNull { it.remoteUrl }
        val iMetaTags = attachments.filter { it.isImageAttachment }.map { it.asIMetaTag() }

        /* Content */
        val refinedContent = buildRefinedContent(attachmentUrls, content)

        val outboxRelays = replyToPostId?.let { noteId ->
            relayHintsMap[noteId]?.let { relayUrl -> listOf(relayUrl) }
        } ?: emptyList()

        return withContext(dispatcherProvider.io()) {
            nostrPublisher.publishShortTextNote(
                userId = userId,
                content = refinedContent,
                tags = pubkeyTags + noteTags + hashtagTags + iMetaTags,
                outboxRelays = outboxRelays,
            )
        }
    }

    private fun constructPubkeyTags(
        replyPostData: PostData?,
        replyToAuthorId: String?,
        rootHighlightAuthorId: String?,
        rootArticleAuthorId: String?,
        content: String,
    ): Set<JsonArray> {
        val existingPubkeyTags = replyPostData?.tags?.filter { it.isPubKeyTag() }?.toSet() ?: emptySet()
        val replyAuthorPubkeyTag = replyToAuthorId?.asPubkeyTag()
        val replyHighlightAuthorPubkeyTag = rootHighlightAuthorId?.asPubkeyTag()
        val rootArticleAuthorPubkeyTag = rootArticleAuthorId?.asPubkeyTag()
        val mentionPubkeyTags = content.parsePubkeyTags(marker = "mention").toSet()

        return sequenceOf(
            existingPubkeyTags,
            mentionPubkeyTags,
            listOf(
                replyAuthorPubkeyTag,
                replyHighlightAuthorPubkeyTag ?: rootArticleAuthorPubkeyTag,
            ),
        ).flatten().filterNotNull().toSet()
    }

    private fun buildRefinedContent(attachmentUrls: List<String>, content: String): String {
        val refinedContent = if (attachmentUrls.isEmpty()) {
            content
        } else {
            StringBuilder().apply {
                append(content)
                appendLine()
                appendLine()
                attachmentUrls.forEach {
                    append(it)
                    appendLine()
                }
            }.toString()
        }
        return refinedContent
    }
}
