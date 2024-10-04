package net.primal.android.note.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import java.util.*
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
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
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.note.api.EventStatsApi
import net.primal.android.note.api.model.EventZapsRequestBody
import net.primal.android.note.db.EventZap
import net.primal.android.note.reactions.mediator.EventZapsMediator
import timber.log.Timber

class NoteRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val nostrPublisher: NostrPublisher,
    private val eventStatsApi: EventStatsApi,
    private val database: PrimalDatabase,
) {

    @Throws(NostrPublishException::class)
    suspend fun likeEvent(
        userId: String,
        eventId: String,
        eventAuthorId: String,
        optionalTags: List<JsonArray> = emptyList(),
    ) = withContext(dispatcherProvider.io()) {
        val statsUpdater = EventStatsUpdater(
            eventId = eventId,
            userId = userId,
            eventAuthorId = eventAuthorId,
            database = database,
        )

        try {
            statsUpdater.increaseLikeStats()
            nostrPublisher.signAndPublishNostrEvent(
                userId = userId,
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = NostrEventKind.Reaction.value,
                    tags = listOf(eventId.asEventIdTag(), eventAuthorId.asPubkeyTag()) + optionalTags,
                    content = "+",
                ),
            )
        } catch (error: NostrPublishException) {
            Timber.w(error)
            statsUpdater.revertStats()
            throw error
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun repostEvent(
        userId: String,
        eventId: String,
        eventKind: NostrEventKind,
        eventAuthorId: String,
        eventRawNostrEvent: String,
        optionalTags: List<JsonArray> = emptyList(),
    ) = withContext(dispatcherProvider.io()) {
        val statsUpdater = EventStatsUpdater(
            eventId = eventId,
            userId = userId,
            eventAuthorId = eventAuthorId,
            database = database,
        )

        try {
            statsUpdater.increaseRepostStats()
            nostrPublisher.signAndPublishNostrEvent(
                userId = userId,
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = if (eventKind == NostrEventKind.ShortTextNote) {
                        NostrEventKind.ShortTextNoteRepost.value
                    } else {
                        NostrEventKind.GenericRepost.value
                    },
                    tags = listOf(eventId.asEventIdTag(), eventAuthorId.asPubkeyTag()) + optionalTags,
                    content = eventRawNostrEvent,
                ),
            )
        } catch (error: NostrPublishException) {
            Timber.w(error)
            statsUpdater.revertStats()
            throw error
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun publishShortTextNote(
        userId: String,
        content: String,
        attachments: List<NoteAttachment> = emptyList(),
        rootArticleEventId: String? = null,
        rootArticleId: String? = null,
        rootArticleAuthorId: String? = null,
        rootPostId: String? = null,
        replyToPostId: String? = null,
        replyToAuthorId: String? = null,
    ): Boolean {
        if (rootArticleId != null && rootPostId != null) {
            throw IllegalStateException("You can not have both article and post as root events.")
        }

        val replyPostData = replyToPostId?.let {
            withContext(dispatcherProvider.io()) {
                database.posts().findByPostId(postId = it)
            }
        }

        /* Article tag */
        val rootArticleTags = if (rootArticleId != null && rootArticleAuthorId != null && rootArticleEventId != null) {
            val tagContent = "${NostrEventKind.LongFormContent.value}:$rootArticleAuthorId:$rootArticleId"
            listOf(
                rootArticleEventId.asEventIdTag(marker = "root"),
                tagContent.asReplaceableEventTag(marker = "root"),
            )
        } else {
            null
        }

        /* Note tags */
        val mentionEventTags = content.parseEventTags(marker = "mention")
        val rootPostTag = rootPostId?.asEventIdTag(marker = "root")
        val replyEventTag = if (rootPostId != replyToPostId) {
            replyToPostId?.asEventIdTag(marker = "reply")
        } else {
            null
        }
        val rootEventTags = rootArticleTags ?: listOf(rootPostTag)
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
        val existingPubkeyTags = replyPostData?.tags?.filter { it.isPubKeyTag() }?.toSet() ?: setOf()
        val replyAuthorPubkeyTag = replyToAuthorId?.asPubkeyTag()
        val rootArticleAuthorPubkeyTag = rootArticleAuthorId?.asPubkeyTag()
        val mentionPubkeyTags = content.parsePubkeyTags(marker = "mention").toSet()
        val pubkeyTags = existingPubkeyTags + mentionPubkeyTags +
            setOfNotNull(replyAuthorPubkeyTag, rootArticleAuthorPubkeyTag)

        /* Hashtag tags */
        val hashtagTags = content.parseHashtagTags().toSet()

        /* iMeta tags */
        val attachmentUrls = attachments.mapNotNull { it.remoteUrl }
        val iMetaTags = attachments.filter { it.isImageAttachment }.map { it.asIMetaTag() }

        /* Content */
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

    suspend fun fetchTopNoteZaps(userId: String, eventId: String) {
        val response = eventStatsApi.getEventZaps(EventZapsRequestBody(eventId = eventId, userId = userId, limit = 15))
        withContext(dispatcherProvider.io()) {
            response.persistToDatabaseAsTransaction(database = database)
        }
    }

    fun pagedEventZaps(userId: String, eventId: String): Flow<PagingData<EventZap>> {
        return createPager(userId = userId, eventId = eventId) {
            database.eventZaps().pagedEventZaps(eventId = eventId)
        }.flow
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(
        userId: String,
        eventId: String,
        pagingSourceFactory: () -> PagingSource<Int, EventZap>,
    ) = Pager(
        config = PagingConfig(
            pageSize = 50,
            prefetchDistance = 50,
            initialLoadSize = 150,
            enablePlaceholders = true,
        ),
        remoteMediator = EventZapsMediator(
            eventId = eventId,
            userId = userId,
            dispatcherProvider = dispatcherProvider,
            eventStatsApi = eventStatsApi,
            database = database,
        ),
        pagingSourceFactory = pagingSourceFactory,
    )
}
