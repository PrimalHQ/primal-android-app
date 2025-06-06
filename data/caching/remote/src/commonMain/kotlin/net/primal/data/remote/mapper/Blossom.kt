package net.primal.data.remote.mapper

import net.primal.core.utils.asMapByKey
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.getTagValueOrNull
import net.primal.domain.nostr.isServerTag

fun List<NostrEvent>.mapAsMapPubkeyToListOfBlossomServers(): Map<String, List<String>> =
    this.asMapByKey { it.pubKey }.mapValues { event ->
        event.value.tags.filter { it.isServerTag() }.mapNotNull { it.getTagValueOrNull() }
    }
