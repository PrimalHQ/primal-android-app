package net.primal.android.editor

import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.assertNotNullCount
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
        assertNotNullCount(rootArticleId, rootPostId, atMost = 1) { "You can have at most one root event." }

        val replyPostData = replyToPostId?.let {
            withContext(dispatcherProvider.io()) {
                database.posts().findByPostId(postId = it)
            }
        }

        val mentionEventTags = content.parseEventTags(marker = "mention")
        val mentionPubkeyTags = content.parsePubkeyTags(marker = "mention").toSet()

        val rootEventTags = constructRootTags(
            rootHighlightId = rootHighlightId,
            rootPostId = rootPostId,
            rootArticleId = rootArticleId,
            rootArticleAuthorId = rootArticleAuthorId,
            rootArticleEventId = rootArticleEventId,
        )

        val replyEventTag = replyToPostId?.constructReplyTags(rootPostId)

        val eventTags = (mentionEventTags + mentionPubkeyTags + rootEventTags + replyEventTag).filterNotNull().toSet()

        val relayHintsMap = withContext(dispatcherProvider.io()) {
            val tagNoteIds = eventTags.map { it.get(index = 1).jsonPrimitive.content }
            val hints = database.eventHints().findById(eventIds = tagNoteIds)
            hints.associate { it.eventId to it.relays.first() }
        }

        val noteTags = eventTags.addRelayHints(relayHintsMap = relayHintsMap)

        /* Pubkey tags */
        val pubkeyTags = constructPubkeyTags(
            replyPostData = replyPostData,
            replyToAuthorId = replyToAuthorId,
            rootHighlightAuthorId = rootHighlightAuthorId,
            rootArticleAuthorId = rootArticleAuthorId,
        )

        /* Hashtag tags */
        val hashtagTags = content.parseHashtagTags().toSet()

        /* iMeta tags */
        val iMetaTags = attachments.filter { it.isImageAttachment }.map { it.asIMetaTag() }

        /* Content */
        val refinedContent = buildRefinedContent(attachments, content)

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

    /*
        Highlights and articles take precedence.
        Highlights and articles cannot appear as nested reply chain (e.g. reply to article with article).
        Therefore the non-null value of article or highlight references means that they are root events
        and should be treated as such.
     */
    private fun constructRootTags(
        rootHighlightId: String?,
        rootArticleId: String?,
        rootArticleEventId: String?,
        rootArticleAuthorId: String?,
        rootPostId: String?,
    ): List<JsonArray> =
        when {
            (rootHighlightId != null) -> listOf(rootHighlightId.asEventIdTag(marker = "root"))
            (rootArticleId != null && rootArticleAuthorId != null && rootArticleEventId != null) -> {
                val tagContent = "${NostrEventKind.LongFormContent.value}:$rootArticleAuthorId:$rootArticleId"
                listOf(
                    rootArticleEventId.asEventIdTag(marker = "root"),
                    tagContent.asReplaceableEventTag(marker = "root"),
                )
            }

            else -> listOfNotNull(rootPostId?.asEventIdTag(marker = "root"))
        }

    private fun constructPubkeyTags(
        replyPostData: PostData?,
        replyToAuthorId: String?,
        rootHighlightAuthorId: String?,
        rootArticleAuthorId: String?,
    ): Set<JsonArray> {
        val existingPubkeyTags = replyPostData?.tags?.filter { it.isPubKeyTag() }?.toSet() ?: emptySet()
        val replyAuthorPubkeyTag = replyToAuthorId?.asPubkeyTag()
        val replyHighlightAuthorPubkeyTag = rootHighlightAuthorId?.asPubkeyTag()
        val rootArticleAuthorPubkeyTag = rootArticleAuthorId?.asPubkeyTag()

        return sequenceOf(
            existingPubkeyTags,
            listOf(
                replyAuthorPubkeyTag,
                replyHighlightAuthorPubkeyTag ?: rootArticleAuthorPubkeyTag,
            ),
        ).flatten().filterNotNull().toSet()
    }

    private fun buildRefinedContent(attachments: List<NoteAttachment>, content: String): String {
        val attachmentUrls = attachments.mapNotNull { it.remoteUrl }
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

    private fun Set<JsonArray>.addRelayHints(relayHintsMap: Map<String, String>) =
        this.map {
            val noteId = it[1].jsonPrimitive.content
            val relayHint = relayHintsMap.getOrDefault(key = noteId, defaultValue = "")
            JsonArray(it.toMutableList().apply { this[2] = JsonPrimitive(relayHint) })
        }

    private fun String?.constructReplyTags(rootPostId: String?) =
        if (rootPostId != this) {
            this?.asEventIdTag(marker = "reply")
        } else {
            null
        }
}
