package net.primal.data.repository.events

import kotlinx.datetime.Clock
import net.primal.core.utils.CurrencyConversionUtils.toBtc
import net.primal.data.local.dao.events.EventStats
import net.primal.data.local.dao.events.EventUserStats
import net.primal.data.local.dao.events.EventZap
import net.primal.data.local.db.PrimalDatabase
import net.primal.data.local.db.withTransaction

internal class EventStatsUpdater(
    val eventId: String,
    val userId: String,
    val eventAuthorId: String,
    val database: PrimalDatabase,
) {

    private val timestamp: Long = Clock.System.now().epochSeconds

    private suspend fun resolveEventStats(): EventStats {
        return database.eventStats().find(eventId = eventId)
            ?: EventStats(eventId = eventId)
    }

    private suspend fun resolveEventUserStats(): EventUserStats {
        return database.eventUserStats().find(eventId = eventId, userId = userId)
            ?: EventUserStats(eventId = eventId, userId = userId)
    }

    suspend fun increaseLikeStats() {
        val eventStats = resolveEventStats()
        val eventUserStats = resolveEventUserStats()
        database.withTransaction {
            database.eventStats().upsert(data = eventStats.copy(likes = eventStats.likes + 1))
            database.eventUserStats().upsert(data = eventUserStats.copy(liked = true))
        }
    }

    suspend fun increaseRepostStats() {
        val eventStats = resolveEventStats()
        val eventUserStats = resolveEventUserStats()
        database.withTransaction {
            database.eventStats().upsert(data = eventStats.copy(reposts = eventStats.reposts + 1))
            database.eventUserStats().upsert(data = eventUserStats.copy(reposted = true))
        }
    }

    suspend fun increaseZapStats(amountInSats: Int, zapComment: String) {
        val eventStats = resolveEventStats()
        val eventUserStats = resolveEventUserStats()
        database.withTransaction {
            val zapSender = database.profiles().findProfileData(profileId = userId)
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
                    zapSenderAvatarCdnImage = zapSender?.avatarCdnImage,
                    zapSenderHandle = zapSender?.handle,
                    zapSenderDisplayName = zapSender?.displayName,
                    zapSenderInternetIdentifier = zapSender?.internetIdentifier,
                    zapSenderPrimalLegendProfile = zapSender?.primalPremiumInfo?.legendProfile,
                    amountInBtc = amountInSats.toBtc(),
                    message = zapComment,
                ),
            )
        }
    }

    suspend fun revertStats() {
        val eventStats = resolveEventStats()
        val eventUserStats = resolveEventUserStats()
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
}
