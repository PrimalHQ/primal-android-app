package net.primal.android.settings.connected.event

import net.primal.domain.nostr.NostrEvent

interface EventDetailsContract {
    data class UiState(
        val loading: Boolean = true,
        val event: NostrEvent? = null,
        val rawJson: String? = null,
        val eventNotSupported: Boolean = false,
        val requestTypeId: String? = null,
        val namingMap: Map<String, String> = emptyMap(),
    )
}
