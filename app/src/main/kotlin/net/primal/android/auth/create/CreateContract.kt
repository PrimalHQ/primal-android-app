package net.primal.android.auth.create

import android.net.Uri
import net.primal.android.crypto.CryptoUtils

interface CreateContract {
    data class UiState(
        val currentStep: Int = 1,
        val loading: Boolean = false,
        val error: CreateError? = null,
        val name: String = "",
        val handle: String = "",
        val website: String = "",
        val aboutMe: String = "",
        val lightningAddress: String = "",
        val nip05Identifier: String = "",
        val avatarUri: Uri? = null,
        val bannerUri: Uri? = null,
        val keypair: CryptoUtils.Keypair? = null
    ) {
        sealed class CreateError {
            data class FailedToUploadAvatar(val cause: Throwable): CreateError()
            data class FailedToUploadBanner(val cause: Throwable): CreateError()
            data class FailedToCreateMetadata(val cause: Throwable): CreateError()
            data class FailedToFollow(val cause: Throwable): CreateError()
        }
    }

    sealed class UiEvent {
        data object GoToProfilePreviewStepEvent: UiEvent()
        data object GoToNostrCreatedStepEvent: UiEvent()
        data object GoBack: UiEvent()
        data object FinishEvent: UiEvent()
        data class AvatarUriChangedEvent(val avatarUri: Uri?): UiEvent()
        data class BannerUriChangedEvent(val bannerUri: Uri?): UiEvent()
        data class NameChangedEvent(val name: String): UiEvent()
        data class HandleChangedEvent(val handle: String): UiEvent()
        data class LightningAddressChangedEvent(val lightningAddress: String): UiEvent()
        data class Nip05IdentifierChangedEvent(val nip05Identifier: String): UiEvent()
        data class WebsiteChangedEvent(val website: String): UiEvent()
        data class AboutMeChangedEvent(val aboutMe: String): UiEvent()
    }

    sealed class SideEffect {
        data object MetadataCreated: SideEffect()
        data class AccountCreated(val pubkey: String): SideEffect()
        data object AccountsFollowed: SideEffect()
    }
}