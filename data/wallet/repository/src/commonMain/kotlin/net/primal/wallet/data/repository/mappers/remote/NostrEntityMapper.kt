package net.primal.wallet.data.repository.mappers.remote

import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nevent
import net.primal.domain.nostr.NostrEntity
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.Nprofile
import net.primal.domain.nostr.findFirstATag
import net.primal.domain.nostr.findFirstEventId
import net.primal.domain.nostr.findFirstKindTag
import net.primal.domain.nostr.findFirstProfileId
import net.primal.domain.nostr.isATag
import net.primal.domain.nostr.isEventIdTag
import net.primal.domain.nostr.isPubKeyTag

fun NostrEvent.toNostrEntity(): NostrEntity? =
    when {
        this.tags.any { it.isATag() } -> {
            this.tags.findFirstATag()?.split(":")?.let { (kind, userId, identifier) ->
                Naddr(
                    kind = kind.toInt(),
                    userId = userId,
                    identifier = identifier,
                )
            }
        }

        this.tags.any { it.isEventIdTag() } -> {
            this.tags.findFirstEventId()?.let { eventId ->
                Nevent(
                    eventId = eventId,
                    kind = this.tags.findFirstKindTag()?.toInt() ?: 1,
                    userId = this.tags.findFirstProfileId(),
                )
            }
        }

        this.tags.any { it.isPubKeyTag() } -> {
            this.tags.findFirstProfileId()?.let {
                Nprofile(pubkey = it)
            }
        }

        else -> null
    }
