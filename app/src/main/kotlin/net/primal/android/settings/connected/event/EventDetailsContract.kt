package net.primal.android.settings.connected.event

import net.primal.domain.nostr.NostrEvent

interface EventDetailsContract {
    data class UiState(
        val loading: Boolean = true,
        val event: NostrEvent? = null,
        val rawJson: String? = null,
        val eventNotSupported: Boolean = false,
    )

    sealed class UiEvent {
        data class CopyToClipboard(val text: String, val label: String) : UiEvent()
    }

    sealed class SideEffect {
        data class TextCopied(val label: String) : SideEffect()
    }
}
