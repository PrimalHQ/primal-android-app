package net.primal.android.core.errors

sealed class UiError {
    data object InvalidNaddr : UiError()
    data object MissingPrivateKey : UiError()

    data object NostrSignUnauthorized : UiError()

    data class MissingLightningAddress(val cause: Throwable) : UiError()
    data class InvalidZapRequest(val cause: Throwable) : UiError()

    data class FailedToPublishZapEvent(val cause: Throwable) : UiError()
    data class FailedToPublishRepostEvent(val cause: Throwable) : UiError()
    data class FailedToPublishLikeEvent(val cause: Throwable) : UiError()
    data class FailedToPublishDeleteEvent(val cause: Throwable) : UiError()

    data class FailedToFollowUser(val cause: Throwable) : UiError()
    data class FailedToUnfollowUser(val cause: Throwable) : UiError()
    data class FailedToMuteUser(val cause: Throwable) : UiError()
    data class FailedToUnmuteUser(val cause: Throwable) : UiError()

    data class FailedToBookmarkNote(val cause: Throwable) : UiError()

    data class FailedToMuteThread(val cause: Throwable) : UiError()
    data class FailedToUnmuteThread(val cause: Throwable) : UiError()

    data class MissingRelaysConfiguration(val cause: Throwable) : UiError()

    data class FailedToAddToFeed(val cause: Throwable) : UiError()
    data class FailedToRemoveFeed(val cause: Throwable) : UiError()

    data class GenericError(val message: String? = null) : UiError()

    data class InvalidPromoCode(val cause: Throwable) : UiError()

    data class FailedToRestoreDefaultBlossomServer(val cause: Throwable?) : UiError()
    data class FailedToUpdateBlossomServer(val cause: Throwable?) : UiError()

    data class FailedToMuteHashtag(val cause: Throwable) : UiError()
    data class FailedToUnmuteHashtag(val cause: Throwable) : UiError()

    data class FailedToMuteWord(val cause: Throwable) : UiError()
    data class FailedToUnmuteWord(val cause: Throwable) : UiError()
    data class FailedToFetchMuteList(val cause: Throwable) : UiError()

    data class SignatureError(val error: SignatureUiError) : UiError()
    data class NetworkError(val cause: Throwable) : UiError()
}
