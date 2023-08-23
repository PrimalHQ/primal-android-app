package net.primal.android.discuss.post

interface NewPostContract {

    data class UiState(
        val preFillContent: String? = null,
        val publishing: Boolean = false,
        val error: NewPostError? = null,
        val activeAccountAvatarUrl: String? = null,
    ) {
        sealed class NewPostError {
            data class PublishError(val cause: Throwable?) : NewPostError()
            data class MissingRelaysConfiguration(val cause: Throwable) : NewPostError()
        }

    }

    sealed class UiEvent {
        data class PublishPost(val content: String) : UiEvent()
    }

    sealed class SideEffect {
        data object PostPublished : SideEffect()
    }

}
