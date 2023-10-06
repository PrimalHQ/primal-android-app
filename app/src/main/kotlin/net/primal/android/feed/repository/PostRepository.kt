package net.primal.android.feed.repository

import kotlinx.serialization.json.JsonArray
import net.primal.android.core.files.FileUploader
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.domain.NoteAttachment
import net.primal.android.networking.primal.api.PrimalImportApi
import net.primal.android.networking.relays.RelaysManager
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.accounts.active.ActiveAccountStore
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val activeAccountStore: ActiveAccountStore,
    private val relaysManager: RelaysManager,
    private val nostrNotary: NostrNotary,
    private val primalImportApi: PrimalImportApi,
    private val fileUploader: FileUploader,
) {

    fun findPostDataById(postId: String) = database.posts().findByPostId(postId = postId)

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
                )
            )
        } catch (error: NostrPublishException) {
            statsUpdater.revertStats()
            throw error
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun repostPost(postId: String, postAuthorId: String, postRawNostrEvent: String) {
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
                )
            )
        } catch (error: NostrPublishException) {
            statsUpdater.revertStats()
            throw error
        }
    }

    @Throws(NostrPublishException::class)
    suspend fun publishShortTextNote(
        content: String,
        tags: Set<JsonArray> = emptySet(),
    ): Boolean {
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
