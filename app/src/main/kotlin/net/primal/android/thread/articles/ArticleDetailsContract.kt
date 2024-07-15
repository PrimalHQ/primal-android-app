package net.primal.android.thread.articles

interface ArticleDetailsContract {
    data class UiState(
        val loading: Boolean = true,
        val error: ArticleDetailsError? = null,
        val markdown: String? = null,
    ) {
        sealed class ArticleDetailsError {
            data object InvalidNaddr : ArticleDetailsError()
            data class MissingLightningAddress(val cause: Throwable) : ArticleDetailsError()
            data class InvalidZapRequest(val cause: Throwable) : ArticleDetailsError()
            data class FailedToPublishZapEvent(val cause: Throwable) : ArticleDetailsError()
            data class FailedToPublishRepostEvent(val cause: Throwable) : ArticleDetailsError()
            data class FailedToPublishLikeEvent(val cause: Throwable) : ArticleDetailsError()
            data class MissingRelaysConfiguration(val cause: Throwable) : ArticleDetailsError()
        }
    }

    sealed class UiEvent {
        data object UpdateContent : UiEvent()
        data object DismissErrors : UiEvent()
    }
}
