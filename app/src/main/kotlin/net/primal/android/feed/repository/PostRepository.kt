package net.primal.android.feed.repository

import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import net.primal.android.core.files.FileUploader
import net.primal.android.db.PrimalDatabase
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.networking.primal.api.PrimalImportApi
import net.primal.android.networking.relays.RelaysManager
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asImageTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.isPubKeyTag
import net.primal.android.nostr.ext.parseEventTags
import net.primal.android.nostr.ext.parseHashtagTags
import net.primal.android.nostr.ext.parsePubkeyTags
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.accounts.active.ActiveAccountStore

class PostRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val activeAccountStore: ActiveAccountStore,
    private val relaysManager: RelaysManager,
    private val nostrNotary: NostrNotary,
    private val primalImportApi: PrimalImportApi,
    private val fileUploader: FileUploader,
) {

    @Throws(NostrPublishException::class)
    suspend fun likePost(postId: String, postAuthorId: String) {
        val userId = activeAccountStore.activeUserId()
        val statsUpdater = PostStatsUpdater(postId = postId, userId = userId, database = database)

        try {
            statsUpdater.increaseLikeStats()
            relaysManager.publishEvent(
                nostrEvent = nostrNotary.signLikeReactionNostrEvent(
                    userId = userId,
                    postId = postId,
                    postAuthorId = postAuthorId,
                ),
            )
        } catch (error: NostrPublishException) {
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
            relaysManager.publishEvent(
                nostrEvent = nostrNotary.signRepostNostrEvent(
                    userId = userId,
                    postId = postId,
                    postAuthorId = postAuthorId,
                    postRawNostrEvent = postRawNostrEvent,
                ),
            )
        } catch (error: NostrPublishException) {
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

        val existingPubkeyTags = replyPostData?.tags?.filter { it.isPubKeyTag() }?.toSet() ?: setOf()
        val replyAuthorPubkeyTag = replyToAuthorId?.asPubkeyTag()
        val mentionPubkeyTags = content.parsePubkeyTags(marker = "mention").toSet()
        val pubkeyTags = existingPubkeyTags + setOfNotNull(replyAuthorPubkeyTag) + mentionPubkeyTags

        val rootEventTag = rootPostId?.asEventIdTag(marker = "root")
        val replyEventTag = if (rootPostId != replyToPostId) {
            replyToPostId?.asEventIdTag(marker = "reply")
        } else {
            null
        }
        val mentionEventTags = content.parseEventTags(marker = "mention")
        val eventTags = setOfNotNull(rootEventTag, replyEventTag) + mentionEventTags

        val hashtagTags = content.parseHashtagTags().toSet()

        val attachmentUrls = attachments.mapNotNull { it.remoteUrl }
        val imageTags = attachments.filter { it.isImageAttachment }.map { it.asImageTag() }

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

        return publishShortTextNote(
            content = refinedContent,
            tags = pubkeyTags + eventTags + hashtagTags + imageTags,
        )
    }

    private suspend fun publishShortTextNote(content: String, tags: Set<JsonArray> = emptySet()): Boolean {
        val noteEvent = nostrNotary.signShortTextNoteEvent(
            userId = activeAccountStore.activeUserId(),
            tags = tags.toList(),
            noteContent = content,
        )
        relaysManager.publishEvent(nostrEvent = noteEvent)
        return try {
            primalImportApi.importEvents(events = listOf(noteEvent))
        } catch (error: WssException) {
            false
        }
    }

    suspend fun uploadPostAttachment(attachment: NoteAttachment): String {
        val userId = activeAccountStore.activeUserId()
        return fileUploader.uploadFile(userId = userId, attachment.localUri)
    }
}
