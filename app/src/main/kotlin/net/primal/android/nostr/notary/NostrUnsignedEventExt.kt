package net.primal.android.nostr.notary

import kotlinx.serialization.json.add
import kotlinx.serialization.json.addJsonArray
import kotlinx.serialization.json.buildJsonArray
import net.primal.android.crypto.Bech32
import net.primal.android.crypto.CryptoUtils
import net.primal.android.crypto.toHex
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

fun NostrUnsignedEvent.signOrThrow(nsec: String): NostrEvent {
    val hexPrivateKey = Bech32.decodeBytes(nsec).second
    return this.signOrThrow(hexPrivateKey)
}

fun NostrUnsignedEvent.signOrThrow(hexPrivateKey: ByteArray): NostrEvent {
    val eventId = this.calculateEventId()
    return NostrEvent(
        id = eventId.toHex(),
        pubKey = this.pubKey,
        createdAt = this.createdAt,
        kind = this.kind,
        tags = this.tags,
        content = this.content,
        sig = CryptoUtils.sign(
            data = eventId,
            privateKey = hexPrivateKey,
        ).toHex(),
    )
}

fun NostrUnsignedEvent.calculateEventId(): ByteArray {
    val json = buildJsonArray {
        add(0)
        add(pubKey)
        add(createdAt)
        add(kind)
        addJsonArray { tags.forEach { add(it) } }
        add(content)
    }
    val rawEventJson = CommonJson.encodeToString(json)
    return CryptoUtils.sha256(rawEventJson.toByteArray())
}
