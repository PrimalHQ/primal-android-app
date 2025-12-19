package net.primal.android.signer.provider

import net.primal.domain.account.model.LocalSignerMethodResponse

interface SignerContract {
    sealed class SideEffect {
        data class RespondToIntent(val result: LocalSignerMethodResponse) : SideEffect()
    }
}
