package net.primal.android.auth.create

import net.primal.android.profile.db.ProfileMetadata

interface CreateContract {
    data class UiState(
        val currentStep: StepState = StepState.NewAccount(null),
        val error: CreateError? = null
    ) {
        sealed class CreateError {
            data class FailedToUploadAvatar(val cause: Throwable): CreateError()
            data class FailedToUploadBanner(val cause: Throwable): CreateError()
            data class FailedToCreateMetadata(val cause: Throwable): CreateError()
            data class FailedToFollow(val cause: Throwable): CreateError()
        }
    }

    sealed class UiEvent {
        data class MetadataCreateEvent(val profileMetadata: ProfileMetadata): UiEvent()
        data class FollowEvent(val followedPubkeys: Set<String>): UiEvent()
    }

    sealed class SideEffect {
        data object MetadataCreated: SideEffect()
        data class AccountCreated(val pubkey: String): SideEffect()
        data object AccountsFollowed: SideEffect()
    }

    sealed class StepState {
        data class NewAccount(val profileMetadata: ProfileMetadata?): StepState()
        data class ProfilePreview(val profileMetadata: ProfileMetadata): StepState()
        data class AccountCreated(val profileMetadata: ProfileMetadata): StepState()
        data class FindWhoToFollow(val profileMetadata: ProfileMetadata, val followedPubkeys: List<String>)
    }
}