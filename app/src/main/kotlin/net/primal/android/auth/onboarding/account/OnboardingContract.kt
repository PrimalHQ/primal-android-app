package net.primal.android.auth.onboarding.account

import android.net.Uri
import net.primal.android.auth.onboarding.account.ui.model.FollowGroup

interface OnboardingContract {

    data class UiState(
        val currentStep: OnboardingStep = OnboardingStep.Details,
        val profileDisplayName: String = "",
        val profileAboutYou: String = "",
        val avatarUri: Uri? = null,
        val avatarRemoteUrl: String? = null,
        val bannerUri: Uri? = null,
        val bannerRemoteUrl: String? = null,
        val accountCreated: Boolean = false,
        val working: Boolean = false,
        val allSuggestions: List<FollowGroup> = emptyList(),
        val selectedSuggestions: List<FollowGroup> = emptyList(),
        val customizeFollows: Boolean = false,
        val error: OnboardingError? = null,
    ) {
        sealed class OnboardingError {
            data class ImageUploadFailed(val cause: Throwable) : OnboardingError()
            data class CreateAccountFailed(val cause: Throwable) : OnboardingError()
        }
    }

    sealed class UiEvent {
        data class ProfileDisplayNameUpdated(val displayName: String) : UiEvent()
        data class ProfileAboutYouUpdated(val aboutYou: String) : UiEvent()
        data class ProfileAvatarUriChanged(val avatarUri: Uri?) : UiEvent()
        data class ProfileBannerUriChanged(val bannerUri: Uri?) : UiEvent()
        data class InterestSelected(val groupName: String) : UiEvent()
        data class InterestUnselected(val groupName: String) : UiEvent()
        data class SetFollowsCustomizing(val customizing: Boolean) : UiEvent()
        data class ToggleGroupFollowEvent(val groupName: String) : UiEvent()
        data class ToggleFollowEvent(val groupName: String, val userId: String) : UiEvent()
        data object KeepRecommendedFollows : UiEvent()
        data object CreateNostrProfile : UiEvent()
        data object RequestNextStep : UiEvent()
        data object RequestPreviousStep : UiEvent()
        data object DismissError : UiEvent()
    }
}
