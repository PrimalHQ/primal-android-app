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
import net.primal.android.nostr.db.eventHintsUpserter
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asIMetaTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.isPubKeyTag
import net.primal.android.nostr.ext.parseEventTags
import net.primal.android.nostr.ext.parseHashtagTags
import net.primal.android.nostr.ext.parsePubkeyTags
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.note.api.EventStatsApi
import net.primal.android.note.api.model.EventZapsRequestBody
import net.primal.android.note.db.EventZap
import net.primal.android.note.reactions.mediator.EventZapsMediator
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.PublicBookmark
import timber.log.Timber

class NoteRepository @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val nostrPublisher: NostrPublisher,
    private val profileRepository: ProfileRepository,
    private val eventStatsApi: EventStatsApi,
    private val database: PrimalDatabase,
) {

    @Throws(NostrPublishException::class)
    suspend fun likePost(postId: String, postAuthorId: String) {
        val userId = activeAccountStore.activeUserId()
        val statsUpdater = EventStatsUpdater(
            eventId = postId,
            userId = userId,
            eventAuthorId = postAuthorId,
            database = database,
        )

        try {
            statsUpdater.increaseLikeStats()
            nostrPublisher.publishLikeNote(userId = userId, postId = postId, postAuthorId = postAuthorId)
        } catch (error: NostrPublishException) {
            Timber.w(error)
            statsUpdater.revertStats()
            throw error
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun repostPost(
        postId: String,
        postAuthorId: String,
        postRawNostrEvent: String,
    ) {
        val userId = activeAccountStore.activeUserId()
        val statsUpdater = EventStatsUpdater(
            eventId = postId,
            userId = userId,
            eventAuthorId = postAuthorId,
            database = database,
        )

        try {
            statsUpdater.increaseRepostStats()
            nostrPublisher.publishRepostNote(
                userId = userId,
                postId = postId,
                postAuthorId = postAuthorId,
                postRawNostrEvent = postRawNostrEvent,
            )
        } catch (error: NostrPublishException) {
            Timber.w(error)
            statsUpdater.revertStats()
            throw error
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun publishShortTextNote(
        content: String,
        attachments: List<NoteAttachment> = emptyList(),
        rootPostId: String? = null,
        replyToPostId: String? = null,
        replyToAuthorId: String? = null,
    ): Boolean {
        val replyPostData = replyToPostId?.let {
            withContext(dispatcherProvider.io()) {
                database.posts().findByPostId(postId = it)
            }
        }

        /* Note tags */
        val mentionEventTags = content.parseEventTags(marker = "mention")
        val rootEventTag = rootPostId?.asEventIdTag(marker = "root")
        val replyEventTag = if (rootPostId != replyToPostId) {
            replyToPostId?.asEventIdTag(marker = "reply")
        } else {
            null
        }
        val eventTags = setOfNotNull(rootEventTag, replyEventTag) + mentionEventTags

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
        val mentionPubkeyTags = content.parsePubkeyTags(marker = "mention").toSet()
        val pubkeyTags = existingPubkeyTags + setOfNotNull(replyAuthorPubkeyTag) + mentionPubkeyTags

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
        return nostrPublisher.publishShortTextNote(
            userId = activeAccountStore.activeUserId(),
            content = refinedContent,
            tags = pubkeyTags + noteTags + hashtagTags + iMetaTags,
            outboxRelays = outboxRelays,
        )
    }

    suspend fun isBookmarked(noteId: String): Boolean {
        return database.eventHints().findById(eventId = noteId)?.isBookmarked == true
    }

    @Throws(ProfileRepository.BookmarksListNotFound::class, NostrPublishException::class)
    suspend fun addToBookmarks(
        userId: String,
        noteId: String,
        forceUpdate: Boolean,
    ) {
        profileRepository.addBookmark(
            userId = userId,
            bookmark = PublicBookmark(type = "e", value = noteId),
            forceUpdate = forceUpdate,
        )
        eventHintsUpserter(dao = database.eventHints(), eventId = noteId) {
            copy(isBookmarked = true)
        }
    }

    @Throws(ProfileRepository.BookmarksListNotFound::class, NostrPublishException::class)
    suspend fun removeFromBookmarks(
        userId: String,
        noteId: String,
        forceUpdate: Boolean,
    ) {
        profileRepository.removeBookmark(
            userId = userId,
            bookmark = PublicBookmark(type = "e", value = noteId),
            forceUpdate = forceUpdate,
        )
        eventHintsUpserter(dao = database.eventHints(), eventId = noteId) {
            copy(isBookmarked = false)
        }
    }

    fun observeTopZappers(eventId: String) = database.eventZaps().observeTopZaps(noteId = eventId)

    suspend fun fetchTopNoteZaps(eventId: String) {
        val userId = activeAccountStore.activeUserId()
        val response = eventStatsApi.getEventZaps(EventZapsRequestBody(eventId = eventId, userId = userId, limit = 15))
        withContext(dispatcherProvider.io()) {
            response.persistToDatabaseAsTransaction(database = database)
        }
    }

    fun pagedEventZaps(eventId: String): Flow<PagingData<EventZap>> {
        return createPager(eventId = eventId) {
            database.eventZaps().pagedNoteZaps(noteId = eventId)
        }.flow
    }

    @OptIn(ExperimentalPagingApi::class)
    private fun createPager(eventId: String, pagingSourceFactory: () -> PagingSource<Int, EventZap>) =
        Pager(
            config = PagingConfig(
                pageSize = 50,
                prefetchDistance = 50,
                initialLoadSize = 150,
                enablePlaceholders = true,
            ),
            remoteMediator = EventZapsMediator(
                eventId = eventId,
                userId = activeAccountStore.activeUserId(),
                dispatcherProvider = dispatcherProvider,
                eventStatsApi = eventStatsApi,
                database = database,
            ),
            pagingSourceFactory = pagingSourceFactory,
        )
}
