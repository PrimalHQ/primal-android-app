package net.primal.android.auth.onboarding.account

import android.net.Uri
import net.primal.android.auth.onboarding.account.api.Suggestion

interface OnboardingContract {

    data class UiState(
        val currentStep: OnboardingStep = OnboardingStep.Details,
        val profileDisplayName: String = "",
        val profileAboutYou: String = "",
        val avatarUri: Uri? = null,
        val bannerUri: Uri? = null,
        val userId: String? = null,
        val working: Boolean = false,
        val allSuggestions: List<Suggestion> = emptyList(),
        val suggestions: List<Suggestion> = emptyList(),
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
        data class InterestSelected(val suggestion: Suggestion) : UiEvent()
        data class InterestUnselected(val suggestion: Suggestion) : UiEvent()
        data object CreateNostrProfile : UiEvent()
        data object RequestNextStep : UiEvent()
        data object RequestPreviousStep : UiEvent()
        data object DismissError : UiEvent()
    }
}
