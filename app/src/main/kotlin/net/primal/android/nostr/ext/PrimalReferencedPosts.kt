package net.primal.android.nostr.ext

import net.primal.android.nostr.model.NostrEvent
import net.primal.android.nostr.model.primal.PrimalEvent

fun List<PrimalEvent>.mapNotNullAsPostDataPO() =
    this.mapNotNull { it.takeContentOrNull<NostrEvent>() }
        .map { it.asPost() }
