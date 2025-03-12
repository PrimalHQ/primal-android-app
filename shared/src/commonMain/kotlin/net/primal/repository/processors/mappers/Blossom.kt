package net.primal.repository.processors.mappers

import net.primal.core.utils.asMapByKey
import net.primal.networking.model.NostrEvent
import net.primal.repository.getTagValueOrNull
import net.primal.repository.isServerTag

fun List<NostrEvent>.mapAsMapPubkeyToListOfBlossomServers() =
    this.asMapByKey { it.pubKey }.mapValues { event ->
        event.value.tags.filter { it.isServerTag() }.mapNotNull { it.getTagValueOrNull() }
    }
