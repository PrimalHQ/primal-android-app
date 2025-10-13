package net.primal.domain.nostr.zaps

import kotlinx.serialization.json.JsonArray
import net.primal.domain.nostr.asEventIdTag
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.asReplaceableEventTag

sealed class ZapTarget(
    open val recipientUserId: String,
    open val recipientLnUrlDecoded: String,
) {
    data class Profile(
        override val recipientUserId: String,
        override val recipientLnUrlDecoded: String,
    ) : ZapTarget(
        recipientUserId = recipientUserId,
        recipientLnUrlDecoded = recipientLnUrlDecoded,
    )

    data class Event(
        val eventId: String,
        override val recipientUserId: String,
        override val recipientLnUrlDecoded: String,
    ) : ZapTarget(
        recipientUserId = recipientUserId,
        recipientLnUrlDecoded = recipientLnUrlDecoded,
    )

    data class ReplaceableEvent(
        val aTag: String,
        val eventId: String,
        override val recipientUserId: String,
        override val recipientLnUrlDecoded: String,
    ) : ZapTarget(
        recipientUserId = recipientUserId,
        recipientLnUrlDecoded = recipientLnUrlDecoded,
    )
}

fun ZapTarget.toTags(): List<JsonArray> {
    val tags = mutableListOf<JsonArray>()
    tags.add(recipientUserId.asPubkeyTag())
    when (this) {
        is ZapTarget.Profile -> Unit

        is ZapTarget.Event -> {
            tags.add(eventId.asEventIdTag())
        }

        is ZapTarget.ReplaceableEvent -> {
            tags.add(aTag.asReplaceableEventTag())
            tags.add(eventId.asEventIdTag())
        }
    }

    return tags
}
