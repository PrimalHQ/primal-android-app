package net.primal.android.nostr.ext

import net.primal.android.core.ext.asMapByKey
import net.primal.domain.nostr.NostrEvent

fun List<NostrEvent>.mapAsMapPubkeyToListOfBlossomServers() =
    this.asMapByKey { it.pubKey }.mapValues { event ->
        event.value.tags.filter { it.isServerTag() }.mapNotNull { it.getTagValueOrNull() }
    }
