package net.primal.android.discuss.post

interface NewPostContract {

    data class UiState(
        val preFillContent: String? = null,
        val publishing: Boolean = false,
        val error: PublishError? = null,
        val activeAccountAvatarUrl: String? = null,
    ) {
        data class PublishError(val cause: Throwable?)
    }

    sealed class UiEvent {
        data class PublishPost(val content: String) : UiEvent()
    }

    sealed class SideEffect {
        data object PostPublished : SideEffect()
    }

}
