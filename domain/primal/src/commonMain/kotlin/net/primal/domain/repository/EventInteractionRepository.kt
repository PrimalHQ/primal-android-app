package net.primal.domain.repository

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.serialization.json.JsonArray
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.NostrPublishException
import net.primal.domain.nostr.zaps.ZapException
import net.primal.domain.nostr.zaps.ZapTarget
import net.primal.domain.publisher.PrimalPublishResult

interface EventInteractionRepository {

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
