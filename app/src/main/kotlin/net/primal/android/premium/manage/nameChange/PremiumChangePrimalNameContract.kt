package net.primal.android.premium.manage.nameChange

import net.primal.android.attachments.domain.CdnImage

interface PremiumChangePrimalNameContract {
    data class UiState(
        val stage: ChangePrimalNameStage = ChangePrimalNameStage.PickNew,
        val primalName: String? = null,
        val profileAvatarCdnImage: CdnImage? = null,
        val profileDisplayName: String? = null,
        val changingName: Boolean = false,
        val error: NameChangeError? = null,
    )

    sealed class UiEvent {
        data class SetPrimalName(val primalName: String?) : UiEvent()
        data class SetStage(val stage: ChangePrimalNameStage) : UiEvent()

        data object ConfirmPrimalNameChange : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object PrimalNameChanged : SideEffect()
    }

    sealed class NameChangeError {
        // TODO: consider handling more specific errors here, e.g. PrimalNameTaken (someone snatched it)
        data object GenericError : NameChangeError()
    }

    enum class ChangePrimalNameStage {
        PickNew,
        Confirm,
    }
}
