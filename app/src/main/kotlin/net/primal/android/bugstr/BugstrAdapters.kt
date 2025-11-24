package net.primal.android.bugstr

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import org.jetbrains.annotations.VisibleForTesting
import net.primal.android.networking.relays.RelaysSocketManager
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.cryptography.signOrThrow
import net.primal.domain.nostr.publisher.NostrPublishException
import fr.acinq.secp256k1.Hex
import com.bugstr.nostr.crypto.NostrEventPublisher
import com.bugstr.nostr.crypto.NostrEventSigner
import com.bugstr.nostr.crypto.SignedGiftWrap
import com.bugstr.nostr.crypto.SignedNostrEvent
import com.bugstr.nostr.crypto.UnsignedNostrEvent

private fun List<List<String>>.toJsonTags(): List<JsonArray> =
    map { tag ->
        buildJsonArray {
            tag.forEach { add(JsonPrimitive(it)) }
        }
    }

private fun List<JsonArray>.toStringTags(): List<List<String>> =
    map { tag -> tag.mapNotNull { (it as? JsonPrimitive)?.content } }

private fun UnsignedNostrEvent.toDomain(): NostrUnsignedEvent =
    NostrUnsignedEvent(
        pubKey = pubKey,
        createdAt = createdAt,
        kind = kind,
        tags = tags.toJsonTags(),
        content = content,
    )

@VisibleForTesting
internal fun NostrEvent.toBugstr(): SignedNostrEvent =
    SignedNostrEvent(
        id = id,
        pubKey = pubKey,
        createdAt = createdAt,
        kind = kind,
        tags = tags.toStringTags(),
        content = content,
        sig = sig,
    )

/**
 * Signs Bugstr unsigned events with the provided private key hex.
 * Uses existing domain signing helpers to keep ID/sig calculation consistent with Primal.
 */
@Singleton
class BugstrNostrSigner @Inject constructor() : NostrEventSigner {
    override fun sign(event: UnsignedNostrEvent, privateKeyHex: String): Result<SignedNostrEvent> {
        if (privateKeyHex.isBlank()) return Result.failure(IllegalArgumentException("Missing private key"))
        val hexKeyBytes = runCatching { Hex.decode(privateKeyHex) }.getOrElse { return Result.failure(it) }
        return runCatching { event.toDomain().signOrThrow(hexPrivateKey = hexKeyBytes) }.map { it.toBugstr() }
    }
}

/**
 * Publishes gift wraps to relays using the existing socket manager. Only the gift-wrap event is broadcast.
 */
@Singleton
class BugstrGiftWrapPublisher @Inject constructor(
    private val relaysSocketManager: RelaysSocketManager,
) : NostrEventPublisher {
    override suspend fun publishGiftWraps(wraps: List<SignedGiftWrap>): Result<Unit> {
        if (wraps.isEmpty()) return Result.success(Unit)
        wraps.forEach { wrap ->
            val gift = wrap.giftWrap
            val publishResult =
                runCatching {
                    relaysSocketManager.publishEvent(
                        nostrEvent =
                            NostrEvent(
                                id = gift.id,
                                pubKey = gift.pubKey,
                                createdAt = gift.createdAt,
                                kind = gift.kind,
                                tags = gift.tags.toJsonTags(),
                                content = gift.content,
                                sig = gift.sig,
                            ),
                    )
                }
            if (publishResult.isFailure) {
                val cause = publishResult.exceptionOrNull() ?: NostrPublishException("Failed to publish gift wrap")
                return Result.failure(cause)
            }
        }
        return Result.success(Unit)
    }
}
