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
import net.primal.android.core.compose.profile.approvals.ProfileApproval
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.navigation.followsType
import net.primal.android.navigation.profileIdOrThrow
import net.primal.android.networking.relays.errors.MissingRelaysException
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.notary.exceptions.MissingPrivateKey
import net.primal.android.profile.domain.ProfileFollowsType
import net.primal.android.profile.follows.ProfileFollowsContract.UiEvent
import net.primal.android.profile.follows.ProfileFollowsContract.UiState
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.core.networking.sockets.errors.WssException
import timber.log.Timber

@HiltViewModel
class ProfileFollowsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
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
                    is UiEvent.FollowProfile -> follow(profileId = it.profileId, forceUpdate = it.forceUpdate)
                    is UiEvent.UnfollowProfile -> unfollow(profileId = it.profileId, forceUpdate = it.forceUpdate)
                    UiEvent.DismissError -> setState { copy(error = null) }
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
            } catch (error: WssException) {
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

    private fun follow(profileId: String, forceUpdate: Boolean) =
        viewModelScope.launch {
            updateStateProfileFollowAndClearApprovalFlag(profileId)
            try {
                userRepository.follow(
                    userId = activeAccountStore.activeUserId(),
                    followedUserId = profileId,
                    forceUpdate = forceUpdate,
                )
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = UiState.FollowsError.FailedToFollowUser(error))
                updateStateProfileUnfollowAndClearApprovalFlag(profileId)
            } catch (error: MissingPrivateKey) {
                Timber.w(error)
                setErrorState(error = UiState.FollowsError.FailedToFollowUser(error))
                updateStateProfileUnfollowAndClearApprovalFlag(profileId)
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = UiState.FollowsError.FailedToFollowUser(error))
                updateStateProfileUnfollowAndClearApprovalFlag(profileId)
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = UiState.FollowsError.MissingRelaysConfiguration(error))
                updateStateProfileUnfollowAndClearApprovalFlag(profileId)
            } catch (error: UserRepository.FollowListNotFound) {
                Timber.w(error)
                updateStateProfileUnfollowAndClearApprovalFlag(profileId)
                setState { copy(shouldApproveProfileAction = ProfileApproval.Follow(profileId = profileId)) }
            }
        }

    private fun unfollow(profileId: String, forceUpdate: Boolean) =
        viewModelScope.launch {
            updateStateProfileUnfollowAndClearApprovalFlag(profileId)
            try {
                userRepository.unfollow(
                    userId = activeAccountStore.activeUserId(),
                    unfollowedUserId = profileId,
                    forceUpdate = forceUpdate,
                )
            } catch (error: WssException) {
                Timber.w(error)
                setErrorState(error = UiState.FollowsError.FailedToUnfollowUser(error))
                updateStateProfileFollowAndClearApprovalFlag(profileId)
            } catch (error: MissingPrivateKey) {
                Timber.w(error)
                setErrorState(error = UiState.FollowsError.FailedToUnfollowUser(error))
                updateStateProfileFollowAndClearApprovalFlag(profileId)
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = UiState.FollowsError.FailedToUnfollowUser(error))
                updateStateProfileFollowAndClearApprovalFlag(profileId)
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = UiState.FollowsError.MissingRelaysConfiguration(error))
                updateStateProfileFollowAndClearApprovalFlag(profileId)
            } catch (error: UserRepository.FollowListNotFound) {
                Timber.w(error)
                updateStateProfileFollowAndClearApprovalFlag(profileId)
                setState { copy(shouldApproveProfileAction = ProfileApproval.Unfollow(profileId = profileId)) }
            }
        }

    private fun setErrorState(error: UiState.FollowsError) {
        setState { copy(error = error) }
    }
}
