package net.primal.android.nostr.ext

import net.primal.android.messages.domain.MessagesUnreadCount
import net.primal.domain.common.PrimalEvent

fun PrimalEvent.asMessagesTotalCount(): MessagesUnreadCount? {
    return this.content.toIntOrNull()?.let {
        MessagesUnreadCount(count = it)
    }
}
