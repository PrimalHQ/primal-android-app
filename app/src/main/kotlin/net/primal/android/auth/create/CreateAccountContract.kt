package net.primal.android.auth.create

import android.net.Uri
import net.primal.android.auth.create.ui.RecommendedFollow

interface CreateAccountContract {
    data class UiState(
        val currentStep: CreateAccountStep = CreateAccountStep.NEW_ACCOUNT,
        val loading: Boolean = false,
        val error: CreateError? = null,
        val displayName: String = "",
        val username: String = "",
        val website: String = "",
        val aboutMe: String = "",
        val lightningAddress: String = "",
        val nip05Identifier: String = "",
        val avatarUri: Uri? = null,
        val bannerUri: Uri? = null,
        val userId: String? = null,
        val recommendedFollows: List<RecommendedFollow> = listOf(),
    ) {
        sealed class CreateError {
            data class FailedToUploadImage(val cause: Throwable) : CreateError()
            data class FailedToCreateMetadata(val cause: Throwable) : CreateError()
            data class FailedToFetchRecommendedFollows(val cause: Throwable) : CreateError()
            data class FailedToFollow(val cause: Throwable) : CreateError()
        }

        enum class CreateAccountStep(val step: Int) {
            NEW_ACCOUNT(1),
            PROFILE_PREVIEW(2),
            ACCOUNT_CREATED(3),
            FOLLOW_RECOMMENDED_ACCOUNTS(4),
            ;

            companion object {
                operator fun invoke(step: Int): CreateAccountStep? =
                    CreateAccountStep.values().firstOrNull { it.step == step }
            }
        }
    }

    sealed class UiEvent {
        data object GoToProfilePreviewStepEvent : UiEvent()
        data object GoToNostrCreatedStepEvent : UiEvent()
        data object GoToFollowContactsStepEvent : UiEvent()
        data object GoBack : UiEvent()
        data object FinishEvent : UiEvent()
        data class AvatarUriChangedEvent(val avatarUri: Uri?) : UiEvent()
        data class BannerUriChangedEvent(val bannerUri: Uri?) : UiEvent()
        data class DisplayNameChangedEvent(val name: String) : UiEvent()
        data class UsernameChangedEvent(val handle: String) : UiEvent()
        data class LightningAddressChangedEvent(val lightningAddress: String) : UiEvent()
        data class Nip05IdentifierChangedEvent(val nip05Identifier: String) : UiEvent()
        data class WebsiteChangedEvent(val website: String) : UiEvent()
        data class AboutMeChangedEvent(val aboutMe: String) : UiEvent()
        data class ToggleFollowEvent(val groupName: String, val pubkey: String) : UiEvent()
        data class ToggleGroupFollowEvent(val groupName: String) : UiEvent()
    }

    sealed class SideEffect {
        data class AccountCreatedAndPersisted(val pubkey: String) : SideEffect()
    }
}
