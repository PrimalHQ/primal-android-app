package net.primal.android.feed.repository

import androidx.room.withTransaction
import net.primal.android.db.PrimalDatabase
import net.primal.android.networking.relays.RelayPool
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.profile.db.PostUserStats
import net.primal.android.user.active.ActiveAccountStore
import javax.inject.Inject

class PostRepository @Inject constructor(
    private val database: PrimalDatabase,
    private val activeAccountStore: ActiveAccountStore,
    private val relayPool: RelayPool,
    private val nostrNotary: NostrNotary,
) {

    suspend fun likePost(postId: String, postAuthorId: String) {
        val userId = activeAccountStore.activeUserId()
        val nostrEvent = nostrNotary.signLikeReactionNostrEvent(
            userId = userId,
            postId = postId,
            postPubkey = postAuthorId,
        )

        val previousStats = database.withTransaction {
            val postStats = database.postStats().find(postId = postId)
            database.postStats().upsert(data = postStats.copy(likes = postStats.likes + 1))

            val postUserStats = database.postUserStats().find(postId = postId, userId = userId)
                ?: PostUserStats(postId = postId, userId = userId)
            database.postUserStats().upsert(data = postUserStats.copy(liked = true))

            Pair(postStats, postUserStats)
        }

        try {
            relayPool.publishEvent(nostrEvent)
        } catch (error: NostrPublishException) {
            database.withTransaction {
                database.postStats().upsert(data = previousStats.first)
                database.postUserStats().upsert(data = previousStats.second)
            }
            throw FailedToPublishLikeEvent()
        }
    }

    inner class FailedToPublishLikeEvent : RuntimeException()
}
