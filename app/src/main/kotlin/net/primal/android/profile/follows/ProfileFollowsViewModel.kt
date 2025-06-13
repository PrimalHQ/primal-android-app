package net.primal.android.profile.follows

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.profile.approvals.FollowsApproval
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.core.errors.UiError
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.navigation.followsType
import net.primal.android.navigation.profileIdOrThrow
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.profile.domain.ProfileFollowsType
import net.primal.android.profile.follows.ProfileFollowsContract.UiEvent
import net.primal.android.profile.follows.ProfileFollowsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.handler.ProfileFollowsHandler
import net.primal.android.user.handler.ProfileFollowsHandler.Companion.foldActions
import net.primal.android.user.repository.UserRepository
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.profile.ProfileRepository
import timber.log.Timber

@HiltViewModel
class ProfileFollowsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val profileFollowsHandler: ProfileFollowsHandler,
) : ViewModel() {

    private val profileId = savedStateHandle.profileIdOrThrow
    private val followsType = savedStateHandle.followsType.toFollowsTypeOrDefault()

    private val _state = MutableStateFlow(
        UiState(
            userId = activeAccountStore.activeUserId(),
            followsType = followsType,
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeProfileData()
        observeActiveAccount()
        observeFollowsResults()
        fetchFollows()
    }

    private fun String?.toFollowsTypeOrDefault(): ProfileFollowsType {
        return when (this) {
            null -> ProfileFollowsType.Followers
            else -> try {
                ProfileFollowsType.valueOf(this)
            } catch (error: IllegalArgumentException) {
                Timber.w(error)
                ProfileFollowsType.Followers
            }
        }
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.FollowProfile -> follow(profileId = it.profileId)
                    is UiEvent.UnfollowProfile -> unfollow(profileId = it.profileId)
                    is UiEvent.ApproveFollowsActions -> approveFollowsActions(actions = it.actions)
                    UiEvent.DismissError -> setState { copy(uiError = null) }
                    UiEvent.ReloadData -> fetchFollows()
                    UiEvent.DismissConfirmFollowUnfollowAlertDialog ->
                        setState { copy(shouldApproveProfileAction = null) }
                }
            }
        }

    private fun observeProfileData() =
        viewModelScope.launch {
            profileRepository.observeProfileData(profileId = profileId).collect {
                setState {
                    copy(profileName = it.usernameUiFriendly())
                }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(userFollowing = it.following)
                }
            }
        }

    private fun fetchFollows() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                val users = withContext(dispatcherProvider.io()) {
                    when (followsType) {
                        ProfileFollowsType.Following -> profileRepository.fetchFollowing(profileId)
                        ProfileFollowsType.Followers -> profileRepository.fetchFollowers(profileId)
                    }
                }
                setState { copy(users = users.map { it.mapAsUserProfileUi() }) }
            } catch (error: NetworkException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun updateStateProfileFollowAndClearApprovalFlag(profileId: String) {
        setState {
            copy(
                userFollowing = this.userFollowing.toMutableSet().apply { add(profileId) },
                shouldApproveProfileAction = null,
            )
        }
    }

    private fun updateStateProfileUnfollowAndClearApprovalFlag(profileId: String) {
        setState {
            copy(
                userFollowing = this.userFollowing.toMutableSet().apply { remove(profileId) },
                shouldApproveProfileAction = null,
            )
        }
    }

    private fun approveFollowsActions(actions: List<ProfileFollowsHandler.Action>) =
        viewModelScope.launch {
            setState {
                copy(userFollowing = userFollowing.foldActions(actions = actions), shouldApproveProfileAction = null)
            }
            profileFollowsHandler.forceUpdateList(actions = actions)
        }

    private fun follow(profileId: String) =
        viewModelScope.launch {
            updateStateProfileFollowAndClearApprovalFlag(profileId)
            profileFollowsHandler.followDelayed(userId = activeAccountStore.activeUserId(), profileId = profileId)
        }

    private fun unfollow(profileId: String) =
        viewModelScope.launch {
            updateStateProfileUnfollowAndClearApprovalFlag(profileId)
            profileFollowsHandler.unfollowDelayed(userId = activeAccountStore.activeUserId(), profileId = profileId)
        }

    private fun observeFollowsResults() =
        viewModelScope.launch {
            profileFollowsHandler.observeResults().collect {
                when (it) {
                    is ProfileFollowsHandler.FollowResult.Error -> {
                        Timber.w(it.error)
                        updateStateProfileUnfollowAndClearApprovalFlag(profileId)
                        when (it.error) {
                            is NetworkException, is SignatureException, is NostrPublishException -> {
                                setState { copy(uiError = UiError.FailedToUpdateFollowList(cause = it.error)) }
                            }

                            is UserRepository.FollowListNotFound -> {
                                setState { copy(shouldApproveProfileAction = FollowsApproval(actions = it.actions)) }
                            }

                            is MissingRelaysException -> {
                                setState { copy(uiError = UiError.MissingRelaysConfiguration(cause = it.error)) }
                            }
                        }
                    }

                    ProfileFollowsHandler.FollowResult.Success -> Unit
                }
            }
        }
}
