package net.primal.android.note.repository

import androidx.room.withTransaction
import kotlin.time.Duration.Companion.milliseconds
import net.primal.android.db.PrimalDatabase
import net.primal.android.note.db.NoteStats
import net.primal.android.note.db.NoteUserStats
import net.primal.android.note.db.NoteZapData
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc

class NoteStatsUpdater(
    val postId: String,
    val userId: String,
    val postAuthorId: String,
    val database: PrimalDatabase,
) {

    private val timestamp: Long = System.currentTimeMillis().milliseconds.inWholeSeconds

    private val noteStats: NoteStats by lazy {
        database.postStats().find(postId = postId)
            ?: NoteStats(postId = postId)
    }

    private val postUserStats: NoteUserStats by lazy {
        database.postUserStats().find(postId = postId, userId = userId)
            ?: NoteUserStats(postId = postId, userId = userId)
    }

    suspend fun increaseLikeStats() =
        database.withTransaction {
            database.postStats().upsert(data = noteStats.copy(likes = noteStats.likes + 1))
            database.postUserStats().upsert(data = postUserStats.copy(liked = true))
        }

    suspend fun increaseRepostStats() =
        database.withTransaction {
            database.postStats().upsert(data = noteStats.copy(reposts = noteStats.reposts + 1))
            database.postUserStats().upsert(data = postUserStats.copy(reposted = true))
        }

    suspend fun increaseZapStats(amountInSats: Int, zapComment: String) =
        database.withTransaction {
            database.postStats().upsert(
                data = noteStats.copy(
                    zaps = noteStats.zaps + 1,
                    satsZapped = noteStats.satsZapped + amountInSats,
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
            database.postStats().upsert(data = noteStats)
            database.postUserStats().upsert(data = postUserStats)
            database.noteZaps().delete(
                noteId = postId,
                senderId = userId,
                receiverId = postAuthorId,
                timestamp = timestamp,
            )
        }
}
