package net.primal.android.auth.create

import android.net.Uri
import net.primal.android.profile.db.ProfileMetadata

interface CreateContract {
    data class UiState(
        val currentStep: StepState = StepState.NewAccount(null),
        val error: CreateError? = null,
        val name: String = "",
        val handle: String = "",
        val website: String = "",
        val aboutMe: String = "",
        val avatarUri: Uri? = null,
        val bannerUri: Uri? = null
    ) {
        sealed class CreateError {
            data class FailedToUploadAvatar(val cause: Throwable): CreateError()
            data class FailedToUploadBanner(val cause: Throwable): CreateError()
            data class FailedToCreateMetadata(val cause: Throwable): CreateError()
            data class FailedToFollow(val cause: Throwable): CreateError()
        }
    }

    sealed class UiEvent {
        data object MetadataCreateEvent: UiEvent()
        data class AvatarUriChangedEvent(val avatarUri: Uri): UiEvent()
        data class BannerUriChangedEvent(val bannerUri: Uri): UiEvent()
        data class NameChangedEvent(val name: String): UiEvent()
        data class HandleChangedEvent(val handle: String): UiEvent()
        data class WebsiteChangedEvent(val website: String): UiEvent()
        data class AboutMeChangedEvent(val aboutMe: String): UiEvent()
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