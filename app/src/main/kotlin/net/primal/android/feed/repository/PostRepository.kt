package net.primal.android.feed.repository

import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.relays.RelayPool
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.user.active.ActiveAccountStore
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val activeAccountStore: ActiveAccountStore,
    private val relayPool: RelayPool,
    private val nostrNotary: NostrNotary,
) {

    @Throws(NostrPublishException::class)
    suspend fun likePost(postId: String, postAuthorId: String) {
        val userId = activeAccountStore.activeUserId()
        val statsUpdater = PostStatsUpdater(postId = postId, userId = userId, database = database)

        try {
            statsUpdater.increaseLikeStats()
            relayPool.publishEvent(
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
            relayPool.publishEvent(
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
    suspend fun publishShortTextNote(content: String) {
        relayPool.publishEvent(
            nostrEvent = nostrNotary.signShortTextNoteEvent(
                userId = activeAccountStore.activeUserId(),
                eventTags = emptyList(),
                pubkeyTags = emptyList(),
                noteContent = content,
            )
        )
    }
}
