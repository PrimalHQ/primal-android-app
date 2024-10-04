package net.primal.android.explore.home.people

import net.primal.android.explore.api.model.ExplorePeopleData

interface ExplorePeopleContract {

    data class UiState(
        val loading: Boolean = true,
        val people: List<ExplorePeopleData> = emptyList(),
        val userFollowing: Set<String> = emptySet(),
        val error: Throwable? = null,
    )

    sealed class UiEvent {
        data class FollowUser(val userId: String) : UiEvent()
        data class UnfollowUser(val userId: String) : UiEvent()

        data object RefreshPeople : UiEvent()
    }
}
