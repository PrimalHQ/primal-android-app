package net.primal.android.feed

interface FeedContract {
    data class UiState(
        val loading: Boolean = false,
    )

    sealed class UiEvent {

    }

    sealed class SideEffect {

    }

}