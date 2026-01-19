package net.primal.android.settings.connected.event.remote

import net.primal.domain.account.model.SessionEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

interface RemoteEventDetailsContract {
    data class UiState(
        val loading: Boolean = true,
        val sessionEvent: SessionEvent? = null,
        val parsedSignedEvent: NostrEvent? = null,
        val parsedUnsignedEvent: NostrUnsignedEvent? = null,
        val rawJson: String? = null,
        val eventNotSupported: Boolean = false,
        val requestTypeId: String? = null,
        val namingMap: Map<String, String> = emptyMap(),
    )
}
