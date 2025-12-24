package net.primal.android.signer.provider.approvals

import net.primal.domain.account.model.LocalSignerMethodResponse
import net.primal.domain.account.model.SessionEvent

interface PermissionRequestsContract {
    data class UiState(
        val requestQueue: List<SessionEvent> = emptyList(),
    )

    sealed class UiEvent {
        data object ApproveSelectedMethods : UiEvent()
    }

    sealed class SideEffect {
        data class ApprovalSuccess(val approvedMethods: List<LocalSignerMethodResponse>) : SideEffect()
    }
}
