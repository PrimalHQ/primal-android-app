package net.primal.android.signer

import net.primal.domain.account.model.LocalSignerMethodResponse

interface SignerContract {
    sealed class SideEffect {
        data class RespondToIntent(val result: LocalSignerMethodResponse) : SideEffect()
    }
}
