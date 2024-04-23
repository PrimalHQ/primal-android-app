package net.primal.android.feed.repository

import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.core.files.FileUploader
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.db.PrimalDatabase
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.db.eventHintsUpserter
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asImageTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.isPubKeyTag
import net.primal.android.nostr.ext.parseEventTags
import net.primal.android.nostr.ext.parseHashtagTags
import net.primal.android.nostr.ext.parsePubkeyTags
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.PublicBookmark
import timber.log.Timber

class PostRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val activeAccountStore: ActiveAccountStore,
    private val fileUploader: FileUploader,
    private val nostrPublisher: NostrPublisher,
    private val profileRepository: ProfileRepository,
) {

    @Throws(NostrPublishException::class)
    suspend fun likePost(postId: String, postAuthorId: String) {
        val userId = activeAccountStore.activeUserId()
        val statsUpdater = PostStatsUpdater(postId = postId, userId = userId, database = database)

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
        val statsUpdater = PostStatsUpdater(postId = postId, userId = userId, database = database)

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
            withContext(Dispatchers.IO) {
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

        val relayHintsMap = withContext(Dispatchers.IO) {
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

        /* Image tags */
        val attachmentUrls = attachments.mapNotNull { it.remoteUrl }
        val imageTags = attachments.filter { it.isImageAttachment }.map { it.asImageTag() }

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
            tags = pubkeyTags + noteTags + hashtagTags + imageTags,
            outboxRelays = outboxRelays,
        )
    }

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun uploadPostAttachment(attachment: NoteAttachment): String {
        val userId = activeAccountStore.activeUserId()
        return fileUploader.uploadFile(userId = userId, attachment.localUri)
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

    fun observeTopZappers(postId: String) = database.noteZaps().observeTopZappers(noteId = postId)
}
