package net.primal.android.signer.provider.approvals

import net.primal.domain.account.model.LocalSignerMethodResponse

interface PermissionRequestsContract {
    sealed class SideEffect {
        data class RespondToIntent(val result: LocalSignerMethodResponse) : SideEffect()
    }
}
