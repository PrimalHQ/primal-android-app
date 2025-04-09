package net.primal.domain.nostr.zaps

import kotlinx.serialization.json.JsonArray
import net.primal.domain.nostr.asEventIdTag
import net.primal.domain.nostr.asPubkeyTag
import net.primal.domain.nostr.asReplaceableEventTag

sealed class ZapTarget {
    data class Profile(
        val profileId: String,
        val profileLnUrlDecoded: String,
    ) : ZapTarget()

    data class Event(
        val eventId: String,
        val eventAuthorId: String,
        val eventAuthorLnUrlDecoded: String,
    ) : ZapTarget()

    data class ReplaceableEvent(
        val kind: Int,
        val identifier: String,
        val eventId: String,
        val eventAuthorId: String,
        val eventAuthorLnUrlDecoded: String,
    ) : ZapTarget()
}

fun ZapTarget.userId(): String {
    return when (this) {
        is ZapTarget.Event -> this.eventAuthorId
        is ZapTarget.Profile -> this.profileId
        is ZapTarget.ReplaceableEvent -> this.eventAuthorId
    }
}

fun ZapTarget.lnUrlDecoded(): String {
    return when (this) {
        is ZapTarget.Event -> this.eventAuthorLnUrlDecoded
        is ZapTarget.Profile -> this.profileLnUrlDecoded
        is ZapTarget.ReplaceableEvent -> this.eventAuthorLnUrlDecoded
    }
}

fun ZapTarget.toTags(): List<JsonArray> {
    val tags = mutableListOf<JsonArray>()

    when (this) {
        is ZapTarget.Profile -> tags.add(profileId.asPubkeyTag())

        is ZapTarget.Event -> {
            tags.add(eventId.asEventIdTag())
            tags.add(eventAuthorId.asPubkeyTag())
        }

        is ZapTarget.ReplaceableEvent -> {
            tags.add(eventId.asEventIdTag())
            tags.add(eventAuthorId.asPubkeyTag())
            tags.add("$kind:$eventAuthorId:$identifier".asReplaceableEventTag())
        }
    }

    return tags
}
