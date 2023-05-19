package net.primal.android.feed

interface FeedContract {
    data class UiState(
        val eventCount: Int = 0,
    )

    sealed class UiEvent {

    }

    sealed class SideEffect {

    }

}