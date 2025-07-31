package net.primal.android.stream

import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.streams.Stream

fun Stream.toNaddrString(): String? =
    Naddr(
        kind = NostrEventKind.LiveActivity.value,
        userId = this.authorId,
        identifier = this.dTag,
    ).toNaddrString()
