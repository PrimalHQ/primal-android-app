package net.primal.android.core.push.api

import net.primal.domain.nostr.NostrEvent

interface PrimalPushMessagesApi {

    suspend fun updateNotificationsToken(authorizationEvents: List<NostrEvent>, token: String)
}
