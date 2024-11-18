package net.primal.android.premium.manage.nameChange

import net.primal.android.attachments.domain.CdnImage

interface PremiumChangePrimalNameContract {
    data class UiState(
        val stage: ChangePrimalNameStage = ChangePrimalNameStage.PickNew,
        val primalName: String? = null,
        val profileAvatarCdnImage: CdnImage? = null,
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
        data object GenericError : NameChangeError()
        data object PrimalNameTaken : NameChangeError()
    }

    enum class ChangePrimalNameStage {
        PickNew,
        Confirm,
    }
}
