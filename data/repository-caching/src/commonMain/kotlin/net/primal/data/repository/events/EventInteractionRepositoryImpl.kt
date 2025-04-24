package net.primal.data.repository.events

import io.github.aakira.napier.Napier
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.data.local.db.PrimalDatabase
import net.primal.domain.events.EventInteractionRepository
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asEventIdTag
import net.primal.domain.nostr.asKindTag
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.asReplaceableEventTag
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.NostrPublishException
import net.primal.domain.nostr.zaps.NostrZapperFactory
import net.primal.domain.nostr.zaps.ZapFailureException
import net.primal.domain.nostr.zaps.ZapRequestData
import net.primal.domain.nostr.zaps.ZapRequestException
import net.primal.domain.nostr.zaps.ZapTarget
import net.primal.domain.nostr.zaps.lnUrlDecoded
import net.primal.domain.nostr.zaps.userId
import net.primal.domain.publisher.PrimalPublisher

class EventInteractionRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val primalPublisher: PrimalPublisher,
    private val nostrZapperFactory: NostrZapperFactory,
    private val database: PrimalDatabase,
) : EventInteractionRepository {

    override suspend fun likeEvent(
        userId: String,
        eventId: String,
        eventAuthorId: String,
        optionalTags: List<JsonArray>,
    ) = withContext(dispatcherProvider.io()) {
        val statsUpdater = EventStatsUpdater(
            eventId = eventId,
            userId = userId,
            eventAuthorId = eventAuthorId,
            database = database,
        )

        try {
            statsUpdater.increaseLikeStats()
            primalPublisher.signPublishImportNostrEvent(
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = NostrEventKind.Reaction.value,
                    tags = listOf(eventId.asEventIdTag(), eventAuthorId.asPubkeyTag()) + optionalTags,
                    content = "+",
                ),
            )
        } catch (error: NostrPublishException) {
            Napier.w(error) { "Failed to publish event." }
            statsUpdater.revertStats()
            throw error
        } catch (error: SignatureException) {
            Napier.w(error) { "Unable to sign event." }
            statsUpdater.revertStats()
            throw error
        }
    }

    override suspend fun repostEvent(
        userId: String,
        eventId: String,
        eventKind: Int,
        eventAuthorId: String,
        eventRawNostrEvent: String,
        optionalTags: List<JsonArray>,
    ) = withContext(dispatcherProvider.io()) {
        val statsUpdater = EventStatsUpdater(
            eventId = eventId,
            userId = userId,
            eventAuthorId = eventAuthorId,
            database = database,
        )

        try {
            statsUpdater.increaseRepostStats()
            primalPublisher.signPublishImportNostrEvent(
                unsignedNostrEvent = NostrUnsignedEvent(
                    pubKey = userId,
                    kind = if (eventKind == NostrEventKind.ShortTextNote.value) {
                        NostrEventKind.ShortTextNoteRepost.value
                    } else {
                        NostrEventKind.GenericRepost.value
                    },
                    tags = listOf(eventId.asEventIdTag(), eventAuthorId.asPubkeyTag()) + optionalTags,
                    content = eventRawNostrEvent,
                ),
            )
        } catch (error: NostrPublishException) {
            Napier.w(error) { "Failed to publish event." }
            statsUpdater.revertStats()
            throw error
        } catch (error: SignatureException) {
            Napier.w(error) { "Unable to sign event." }
            statsUpdater.revertStats()
            throw error
        }
    }

    override suspend fun deleteEvent(
        userId: String,
        eventIdentifier: String,
        eventKind: NostrEventKind,
        content: String,
        relayHint: String?,
    ) = withContext(dispatcherProvider.io()) {
        primalPublisher.signPublishImportNostrEvent(
            unsignedNostrEvent = NostrUnsignedEvent(
                pubKey = userId,
                kind = NostrEventKind.EventDeletion.value,
                tags = listOf(
                    if (eventKind == NostrEventKind.LongFormContent) {
                        eventIdentifier.asReplaceableEventTag(relayHint = relayHint)
                    } else {
                        eventIdentifier.asEventIdTag(authorPubkey = userId, relayHint = relayHint)
                    },
                    eventKind.asKindTag(),
                ),
                content = content,
            ),
        )
    }

    override suspend fun zapEvent(
        userId: String,
        amountInSats: ULong,
        comment: String,
        target: ZapTarget,
        zapRequestEvent: NostrEvent,
    ) {
        val targetLnUrlDecoded = target.lnUrlDecoded()
        val statsUpdater = target.buildPostStatsUpdaterIfApplicable(userId = userId)

        statsUpdater?.increaseZapStats(
            amountInSats = amountInSats.toInt(),
            zapComment = comment,
        )

        try {
            val nostrZapper = nostrZapperFactory.createOrNull(userId = userId)
                ?: throw ZapRequestException(message = "Unable to create nostr zapper.")

            nostrZapper.zap(
                data = ZapRequestData(
                    zapperUserId = userId,
                    targetUserId = target.userId(),
                    lnUrlDecoded = targetLnUrlDecoded,
                    zapAmountInSats = amountInSats,
                    zapComment = comment,
                    userZapRequestEvent = zapRequestEvent,
                ),
            )
        } catch (error: ZapFailureException) {
            statsUpdater?.revertStats()
            throw error
        }
    }

    private fun ZapTarget.buildPostStatsUpdaterIfApplicable(userId: String): EventStatsUpdater? =
        when (this) {
            is ZapTarget.Event -> EventStatsUpdater(
                userId = userId,
                eventId = this.eventId,
                eventAuthorId = this.eventAuthorId,
                database = database,
            )

            is ZapTarget.ReplaceableEvent -> EventStatsUpdater(
                userId = userId,
                eventId = this.eventId,
                eventAuthorId = this.eventAuthorId,
                database = database,
            )

            is ZapTarget.Profile -> null
        }
}
