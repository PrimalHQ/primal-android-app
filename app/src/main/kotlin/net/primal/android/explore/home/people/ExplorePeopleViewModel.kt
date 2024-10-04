package net.primal.android.explore.home.people

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.explore.home.people.ExplorePeopleContract.UiEvent
import net.primal.android.explore.home.people.ExplorePeopleContract.UiState
import net.primal.android.explore.repository.ExploreRepository
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import timber.log.Timber

@HiltViewModel
class ExplorePeopleViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchExplorePeople()
        fetchFollowing()
        observeActiveAccount()
        observeEvents()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState { copy(userFollowing = it.following) }
            }
        }

    private fun fetchFollowing() =
        viewModelScope.launch {
            profileRepository.fetchFollowing(userId = activeAccountStore.activeUserId())
        }

    private fun fetchExplorePeople() =
        viewModelScope.launch {
            setState { copy(loading = true, error = null) }
            try {
                val explorePeople = exploreRepository.fetchTrendingPeople(
                    userId = activeAccountStore.activeUserId(),
                )
                setState { copy(people = explorePeople) }
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = error) }
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.FollowUser -> follow(profileId = it.userId)
                    is UiEvent.UnfollowUser -> unfollow(profileId = it.userId)
                    UiEvent.RefreshPeople -> fetchExplorePeople()
                }
            }
        }

    private fun follow(profileId: String) =
        viewModelScope.launch {
            updateStateProfileFollow(profileId)
            try {
                profileRepository.follow(
                    userId = activeAccountStore.activeUserId(),
                    followedUserId = profileId,
                )
            } catch (error: Exception) {
                Timber.w(error)
                updateStateProfileUnfollow(profileId)
            }
        }

    private fun unfollow(profileId: String) =
        viewModelScope.launch {
            updateStateProfileUnfollow(profileId)
            try {
                profileRepository.unfollow(
                    userId = activeAccountStore.activeUserId(),
                    unfollowedUserId = profileId,
                )
            } catch (error: Exception) {
                Timber.w(error)
                updateStateProfileFollow(profileId)
            }
        }

    private fun updateStateProfileUnfollow(profileId: String) =
        setState { copy(userFollowing = userFollowing - profileId) }

    private fun updateStateProfileFollow(profileId: String) =
        setState { copy(userFollowing = userFollowing + profileId) }
}
