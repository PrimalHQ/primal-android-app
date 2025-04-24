package net.primal.domain.events

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.JsonArray
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.NostrPublishException
import net.primal.domain.nostr.zaps.ZapException
import net.primal.domain.nostr.zaps.ZapTarget
import net.primal.domain.publisher.PrimalPublishResult

interface EventInteractionRepository {

    private companion object {
        const val DEFAULT_DELETION_CONTENT =
            "This is a deletion request created in https://primal.net Android application"
    }

    @Throws(
        SignatureException::class,
        NostrPublishException::class,
        CancellationException::class,
    )
    suspend fun likeEvent(
        userId: String,
        eventId: String,
        eventAuthorId: String,
        optionalTags: List<JsonArray> = emptyList(),
    ): PrimalPublishResult

    @Throws(
        SignatureException::class,
        NostrPublishException::class,
        CancellationException::class,
    )
    suspend fun repostEvent(
        userId: String,
        eventId: String,
        eventKind: Int,
        eventAuthorId: String,
        eventRawNostrEvent: String,
        optionalTags: List<JsonArray> = emptyList(),
    ): PrimalPublishResult

    @Throws(
        SignatureException::class,
        NostrPublishException::class,
        CancellationException::class,
    )
    suspend fun deleteEvent(
        userId: String,
        eventIdentifier: String,
        eventKind: NostrEventKind,
        content: String = DEFAULT_DELETION_CONTENT,
        relayHint: String? = null,
    ): PrimalPublishResult

    @Throws(
        ZapException::class,
        CancellationException::class,
    )
    suspend fun zapEvent(
        userId: String,
        amountInSats: ULong,
        comment: String,
        target: ZapTarget,
        zapRequestEvent: NostrEvent,
    )
}
