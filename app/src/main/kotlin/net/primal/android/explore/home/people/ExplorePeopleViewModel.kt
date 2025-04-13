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
import net.primal.android.core.compose.profile.approvals.ProfileApproval
import net.primal.android.core.errors.UiError
import net.primal.android.explore.home.people.ExplorePeopleContract.UiEvent
import net.primal.android.explore.home.people.ExplorePeopleContract.UiState
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.explore.ExploreRepository
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.profile.ProfileRepository
import timber.log.Timber

@HiltViewModel
class ExplorePeopleViewModel @Inject constructor(
    private val exploreRepository: ExploreRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
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
            try {
                profileRepository.fetchFollowing(profileId = activeAccountStore.activeUserId())
            } catch (error: WssException) {
                Timber.w(error)
            }
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
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.FollowUser -> follow(profileId = it.userId, forceUpdate = it.forceUpdate)
                    is UiEvent.UnfollowUser -> unfollow(profileId = it.userId, forceUpdate = it.forceUpdate)
                    UiEvent.RefreshPeople -> fetchExplorePeople()
                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.DismissConfirmFollowUnfollowAlertDialog ->
                        setState { copy(shouldApproveProfileAction = null) }
                }
            }
        }

    private fun follow(profileId: String, forceUpdate: Boolean) =
        viewModelScope.launch {
            updateStateProfileFollowAndClearApprovalFlag(profileId)

            val followResult = runCatching {
                userRepository.follow(
                    userId = activeAccountStore.activeUserId(),
                    followedUserId = profileId,
                    forceUpdate = forceUpdate,
                )
            }

            if (followResult.isFailure) {
                followResult.exceptionOrNull()?.let { error ->
                    Timber.w(error)
                    updateStateProfileUnfollowAndClearApprovalFlag(profileId)
                    when (error) {
                        is SigningKeyNotFoundException -> setState { copy(error = UiError.MissingPrivateKey) }

                        is SigningRejectedException -> setState { copy(error = UiError.NostrSignUnauthorized) }

                        is WssException, is NostrPublishException ->
                            setState { copy(error = UiError.FailedToFollowUser(error)) }

                        is UserRepository.FollowListNotFound -> setState {
                            copy(shouldApproveProfileAction = ProfileApproval.Follow(profileId = profileId))
                        }

                        is MissingRelaysException ->
                            setState { copy(error = UiError.MissingRelaysConfiguration(error)) }

                        else -> setState { copy(error = UiError.GenericError()) }
                    }
                }
            }
        }

    private fun unfollow(profileId: String, forceUpdate: Boolean) =
        viewModelScope.launch {
            updateStateProfileUnfollowAndClearApprovalFlag(profileId)

            val unfollowResult = runCatching {
                userRepository.unfollow(
                    userId = activeAccountStore.activeUserId(),
                    unfollowedUserId = profileId,
                    forceUpdate = forceUpdate,
                )
            }

            if (unfollowResult.isFailure) {
                updateStateProfileFollowAndClearApprovalFlag(profileId)
                unfollowResult.exceptionOrNull()?.let { error ->
                    Timber.w(error)
                    when (error) {
                        is WssException, is NostrPublishException, is SignatureException ->
                            setState { copy(error = UiError.FailedToUnfollowUser(error)) }

                        is UserRepository.FollowListNotFound -> setState {
                            copy(shouldApproveProfileAction = ProfileApproval.Unfollow(profileId = profileId))
                        }

                        is MissingRelaysException ->
                            setState { copy(error = UiError.MissingRelaysConfiguration(error)) }

                        else -> setState { copy(error = UiError.GenericError()) }
                    }
                }
            }
        }

    private fun updateStateProfileUnfollowAndClearApprovalFlag(profileId: String) =
        setState {
            copy(
                userFollowing = userFollowing - profileId,
                shouldApproveProfileAction = null,
            )
        }

    private fun updateStateProfileFollowAndClearApprovalFlag(profileId: String) =
        setState {
            copy(
                userFollowing = userFollowing + profileId,
                shouldApproveProfileAction = null,
            )
        }
}
