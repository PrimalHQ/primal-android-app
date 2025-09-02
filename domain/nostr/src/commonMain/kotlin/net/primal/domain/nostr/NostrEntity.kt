package net.primal.domain.nostr

import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.Nip19TLV.toNeventString
import net.primal.domain.nostr.Nip19TLV.toNprofileString

sealed interface NostrEntity

fun NostrEntity.toNostrString() =
    when (this) {
        is Naddr -> this.toNaddrString()
        is Nevent -> this.toNeventString()
        is Nprofile -> this.toNprofileString()
    }
