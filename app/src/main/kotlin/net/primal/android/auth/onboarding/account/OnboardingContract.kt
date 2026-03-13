package net.primal.android.auth.onboarding.account

import android.net.Uri
import net.primal.android.auth.onboarding.account.ui.model.OnboardingFollowPack

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
        val accountCreationStep: AccountCreationStep = AccountCreationStep.AccountPreview,
        val working: Boolean = false,
        val followPacks: List<OnboardingFollowPack> = emptyList(),
        val expandedPackNames: Set<String> = emptySet(),
        val followedUserIds: Set<String> = emptySet(),
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
        data class TogglePackExpanded(val packName: String) : UiEvent()
        data class ToggleFollowUser(val userId: String) : UiEvent()
        data class ToggleFollowAllInPack(val packName: String) : UiEvent()
        data object CreateNostrProfile : UiEvent()
        data object RequestNextStep : UiEvent()
        data object RequestPreviousStep : UiEvent()
        data object DismissError : UiEvent()
    }

    data class ScreenCallbacks(
        val onClose: () -> Unit,
        val onOnboarded: () -> Unit,
    )
}
