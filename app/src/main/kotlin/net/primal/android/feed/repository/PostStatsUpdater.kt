package net.primal.android.feed.repository

import androidx.room.withTransaction
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.PostStats
import net.primal.android.profile.db.PostUserStats

class PostStatsUpdater(
    val postId: String,
    val userId: String,
    val database: PrimalDatabase,
) {

    private val postStats: PostStats by lazy {
        database.postStats().find(postId = postId)
    }

    private val postUserStats: PostUserStats by lazy {
        database.postUserStats().find(postId = postId, userId = userId)
            ?: PostUserStats(postId = postId, userId = userId)
    }

    suspend fun increaseLikeStats() = database.withTransaction {
        database.postStats().upsert(data = postStats.copy(likes = postStats.likes + 1))
        database.postUserStats().upsert(data = postUserStats.copy(liked = true))
    }

    suspend fun increaseRepostStats() = database.withTransaction {
        database.postStats().upsert(data = postStats.copy(reposts = postStats.reposts + 1))
        database.postUserStats().upsert(data = postUserStats.copy(reposted = true))
    }

    suspend fun revertStats() = database.withTransaction {
        database.postStats().upsert(data = postStats)
        database.postUserStats().upsert(data = postUserStats)
    }

}
