package net.primal.android.thread.blogs

interface LongFormThreadContract {
    data class UiState(
        val loading: Boolean = true,
        val error: LongFormThreadError? = null,
    ) {
        sealed class LongFormThreadError {
            data object InvalidNaddr : LongFormThreadError()
            data class MissingLightningAddress(val cause: Throwable) : LongFormThreadError()
            data class InvalidZapRequest(val cause: Throwable) : LongFormThreadError()
            data class FailedToPublishZapEvent(val cause: Throwable) : LongFormThreadError()
            data class FailedToPublishRepostEvent(val cause: Throwable) : LongFormThreadError()
            data class FailedToPublishLikeEvent(val cause: Throwable) : LongFormThreadError()
            data class MissingRelaysConfiguration(val cause: Throwable) : LongFormThreadError()
        }
    }

    sealed class UiEvent {
        data object UpdateContent : UiEvent()
        data object DismissErrors : UiEvent()
    }
}
