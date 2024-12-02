package net.primal.android.profile.editor

import android.net.Uri

interface ProfileEditorContract {
    data class UiState(
        val loading: Boolean = false,
        val error: EditProfileError? = null,
        val displayName: String = "",
        val username: String = "",
        val website: String = "",
        val aboutMe: String = "",
        val lightningAddress: String = "",
        val nip05Identifier: String = "",
        val localAvatarUri: Uri? = null,
        val localBannerUri: Uri? = null,
        val remoteAvatarUrl: String? = null,
        val remoteBannerUrl: String? = null,
        val showPremiumPaywallDialog: Boolean = false,
    ) {
        sealed class EditProfileError {
            data class MissingRelaysConfiguration(val cause: Throwable) : EditProfileError()
            data class FailedToPublishMetadata(val cause: Throwable) : EditProfileError()
            data class FailedToUploadImage(val cause: Throwable) : EditProfileError()
            data class InvalidLightningAddress(val lud16: String) : EditProfileError()
            data class InvalidNostrVerificationAddress(val nip05: String) : EditProfileError()
        }
    }

    sealed class UiEvent {
        data class DisplayNameChangedEvent(val displayName: String) : UiEvent()
        data class UsernameChangedEvent(val name: String) : UiEvent()
        data class WebsiteChangedEvent(val website: String) : UiEvent()
        data class AboutMeChangedEvent(val aboutMe: String) : UiEvent()
        data class LightningAddressChangedEvent(val lightningAddress: String) : UiEvent()
        data class Nip05IdentifierChangedEvent(val nip05Identifier: String) : UiEvent()
        data class AvatarUriChangedEvent(val avatarUri: Uri?) : UiEvent()
        data class BannerUriChangedEvent(val bannerUri: Uri?) : UiEvent()
        data object SaveProfileEvent : UiEvent()
        data object DismissError : UiEvent()
        data object DismissPremiumPaywallDialog : UiEvent()
    }

    sealed class SideEffect {
        data object AccountSuccessfulyEdited : SideEffect()
    }
}
