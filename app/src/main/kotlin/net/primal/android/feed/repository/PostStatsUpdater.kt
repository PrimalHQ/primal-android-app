package net.primal.android.feed.repository

import androidx.room.withTransaction
import kotlin.time.Duration.Companion.milliseconds
import net.primal.android.db.PrimalDatabase
import net.primal.android.feed.db.NoteZapData
import net.primal.android.feed.db.PostStats
import net.primal.android.profile.db.PostUserStats
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc

class PostStatsUpdater(
    val postId: String,
    val userId: String,
    val postAuthorId: String,
    val database: PrimalDatabase,
) {

    private val timestamp: Long = System.currentTimeMillis().milliseconds.inWholeSeconds

    private val postStats: PostStats by lazy {
        database.postStats().find(postId = postId)
            ?: PostStats(postId = postId)
    }

    private val postUserStats: PostUserStats by lazy {
        database.postUserStats().find(postId = postId, userId = userId)
            ?: PostUserStats(postId = postId, userId = userId)
    }

    suspend fun increaseLikeStats() =
        database.withTransaction {
            database.postStats().upsert(data = postStats.copy(likes = postStats.likes + 1))
            database.postUserStats().upsert(data = postUserStats.copy(liked = true))
        }

    suspend fun increaseRepostStats() =
        database.withTransaction {
            database.postStats().upsert(data = postStats.copy(reposts = postStats.reposts + 1))
            database.postUserStats().upsert(data = postUserStats.copy(reposted = true))
        }

    suspend fun increaseZapStats(amountInSats: Int, zapComment: String) =
        database.withTransaction {
            database.postStats().upsert(
                data = postStats.copy(
                    zaps = postStats.zaps + 1,
                    satsZapped = postStats.satsZapped + amountInSats,
                ),
            )
            database.postUserStats().upsert(data = postUserStats.copy(zapped = true))

            database.noteZaps().insert(
                data = NoteZapData(
                    zapSenderId = userId,
                    zapReceiverId = postAuthorId,
                    noteId = postId,
                    zapRequestAt = timestamp,
                    zapReceiptAt = timestamp,
                    amountInBtc = amountInSats.toBtc(),
                    message = zapComment,
                ),
            )
        }

    suspend fun revertStats() =
        database.withTransaction {
            database.postStats().upsert(data = postStats)
            database.postUserStats().upsert(data = postUserStats)
            database.noteZaps().delete(
                noteId = postId,
                senderId = userId,
                receiverId = postAuthorId,
                timestamp = timestamp,
            )
        }
}
