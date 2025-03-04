package net.primal.android.core.errors

sealed class UiError {
    data object InvalidNaddr : UiError()

    data class MissingLightningAddress(val cause: Throwable) : UiError()
    data class InvalidZapRequest(val cause: Throwable) : UiError()

    data class FailedToPublishZapEvent(val cause: Throwable) : UiError()
    data class FailedToPublishRepostEvent(val cause: Throwable) : UiError()
    data class FailedToPublishLikeEvent(val cause: Throwable) : UiError()

    data class FailedToFollowUser(val cause: Throwable) : UiError()
    data class FailedToUnfollowUser(val cause: Throwable) : UiError()
    data class FailedToMuteUser(val cause: Throwable) : UiError()
    data class FailedToUnmuteUser(val cause: Throwable) : UiError()

    data class MissingRelaysConfiguration(val cause: Throwable) : UiError()

    data class FailedToAddToFeed(val cause: Throwable) : UiError()
    data class FailedToRemoveFeed(val cause: Throwable) : UiError()

    data class GenericError(val message: String? = null) : UiError()
}
