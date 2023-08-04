package net.primal.android.nostr.ext

import net.primal.android.nostr.model.primal.PrimalEvent

fun List<PrimalEvent>.mapNotNullAsPostDataPO() =
    this.mapNotNull { it.takeContentAsNostrEventOrNull() }
        .map { it.asPost() }
