package net.primal.android.profile.edit

import android.net.Uri

interface EditProfileContract {
    data class UiState(
        val loading: Boolean = false,
        val error: EditProfileError? = null,
        val displayName: String = "", // name
        val name: String = "", // handle
        val website: String = "",
        val aboutMe: String = "",
        val lightningAddress: String = "",
        val nip05Identifier: String = "",
        val avatarUri: Uri? = null,
        val bannerUri: Uri? = null,
    ) {
        sealed class EditProfileError {
            data class MissingRelaysConfiguration(val cause: Throwable) : EditProfileError()
            data class FailedToPublishMetadata(val cause: Throwable) : EditProfileError()
        }
    }

    sealed class UiEvent {
        data class DisplayNameChangedEvent(val displayName: String) : UiEvent()
        data class NameChangedEvent(val name: String) : UiEvent()
        data class WebsiteChangedEvent(val website: String) : UiEvent()
        data class AboutMeChangedEvent(val aboutMe: String) : UiEvent()
        data class LightningAddressChangedEvent(val lightningAddress: String) : UiEvent()
        data class Nip05IdentifierChangedEvent(val nip05Identifier: String) : UiEvent()
        data class AvatarUriChangedEvent(val avatarUri: Uri?) : UiEvent()
        data class BannerUriChangedEvent(val bannerUri: Uri?) : UiEvent()
    }
}