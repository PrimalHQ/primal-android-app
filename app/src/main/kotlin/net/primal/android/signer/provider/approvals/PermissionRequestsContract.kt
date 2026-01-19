package net.primal.android.signer.provider.approvals

import net.primal.data.account.signer.local.model.LocalSignerMethodResponse
import net.primal.domain.account.model.SessionEvent
import net.primal.domain.nostr.NostrEvent
import net.primal.domain.nostr.NostrUnsignedEvent

interface PermissionRequestsContract {
    data class UiState(
        val callingPackage: String,
        val appName: String? = null,
        val requestQueue: List<SessionEvent> = emptyList(),
        val responding: Boolean = false,
        val permissionsMap: Map<String, String> = emptyMap(),
        val eventDetailsSessionEvent: SessionEvent? = null,
        val parsedSignedEvent: NostrEvent? = null,
        val parsedUnsignedEvent: NostrUnsignedEvent? = null,
    )

    sealed class UiEvent {
        data class Allow(val eventIds: List<String>, val alwaysAllow: Boolean) : UiEvent()
        data class Reject(val eventIds: List<String>, val alwaysReject: Boolean) : UiEvent()
        data object RejectAll : UiEvent()
        data class OpenEventDetails(val eventId: String) : UiEvent()
        data object CloseEventDetails : UiEvent()
    }

    sealed class SideEffect {
        data class RequestsCompleted(
            val approved: List<LocalSignerMethodResponse>,
            val rejected: List<LocalSignerMethodResponse>,
        ) : SideEffect()

        data object InvalidRequest : SideEffect()
    }
}
