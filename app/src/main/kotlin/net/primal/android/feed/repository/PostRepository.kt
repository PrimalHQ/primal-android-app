package net.primal.android.feed.repository

import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.primal.android.core.files.FileUploader
import net.primal.android.core.files.error.UnsuccessfulFileUpload
import net.primal.android.db.PrimalDatabase
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.ext.asEventIdTag
import net.primal.android.nostr.ext.asImageTag
import net.primal.android.nostr.ext.asPubkeyTag
import net.primal.android.nostr.ext.isPubKeyTag
import net.primal.android.nostr.ext.parseEventTags
import net.primal.android.nostr.ext.parseHashtagTags
import net.primal.android.nostr.ext.parsePubkeyTags
import net.primal.android.nostr.publish.NostrPublisher
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

class PostRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val activeAccountStore: ActiveAccountStore,
    private val fileUploader: FileUploader,
    private val nostrPublisher: NostrPublisher,
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

        return nostrPublisher.publishShortTextNote(
            userId = activeAccountStore.activeUserId(),
            content = refinedContent,
            tags = pubkeyTags + eventTags + hashtagTags + imageTags,
        )
    }

    @Throws(UnsuccessfulFileUpload::class)
    suspend fun uploadPostAttachment(attachment: NoteAttachment): String {
        val userId = activeAccountStore.activeUserId()
        return fileUploader.uploadFile(userId = userId, attachment.localUri)
    }
}
