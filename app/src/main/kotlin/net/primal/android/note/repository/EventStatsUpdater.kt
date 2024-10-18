package net.primal.android.note.repository

import androidx.room.withTransaction
import kotlin.time.Duration.Companion.milliseconds
import net.primal.android.db.PrimalDatabase
import net.primal.android.note.db.EventStats
import net.primal.android.note.db.EventUserStats
import net.primal.android.note.db.EventZap
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc

class EventStatsUpdater(
    val eventId: String,
    val userId: String,
    val eventAuthorId: String,
    val database: PrimalDatabase,
) {

    private val timestamp: Long = System.currentTimeMillis().milliseconds.inWholeSeconds

    private val eventStats: EventStats by lazy {
        database.eventStats().find(eventId = eventId)
            ?: EventStats(eventId = eventId)
    }

    private val eventUserStats: EventUserStats by lazy {
        database.eventUserStats().find(eventId = eventId, userId = userId)
            ?: EventUserStats(eventId = eventId, userId = userId)
    }

    suspend fun increaseLikeStats() =
        database.withTransaction {
            database.eventStats().upsert(data = eventStats.copy(likes = eventStats.likes + 1))
            database.eventUserStats().upsert(data = eventUserStats.copy(liked = true))
        }

    suspend fun increaseRepostStats() =
        database.withTransaction {
            database.eventStats().upsert(data = eventStats.copy(reposts = eventStats.reposts + 1))
            database.eventUserStats().upsert(data = eventUserStats.copy(reposted = true))
        }

    suspend fun increaseZapStats(amountInSats: Int, zapComment: String) =
        database.withTransaction {
            database.eventStats().upsert(
                data = eventStats.copy(
                    zaps = eventStats.zaps + 1,
                    satsZapped = eventStats.satsZapped + amountInSats,
                ),
            )
            database.eventUserStats().upsert(data = eventUserStats.copy(zapped = true))

            database.eventZaps().insert(
                data = EventZap(
                    zapSenderId = userId,
                    zapReceiverId = eventAuthorId,
                    eventId = eventId,
                    zapRequestAt = timestamp,
                    zapReceiptAt = timestamp,
                    amountInBtc = amountInSats.toBtc(),
                    message = zapComment,
                ),
            )
        }

    suspend fun revertStats() =
        database.withTransaction {
            database.eventStats().upsert(data = eventStats)
            database.eventUserStats().upsert(data = eventUserStats)
            database.eventZaps().delete(
                noteId = eventId,
                senderId = userId,
                receiverId = eventAuthorId,
                timestamp = timestamp,
            )
        }
}
