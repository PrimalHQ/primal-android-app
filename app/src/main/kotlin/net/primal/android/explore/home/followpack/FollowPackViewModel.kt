package net.primal.android.explore.home.followpack

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.approvals.FollowsApproval
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.core.errors.UiError
import net.primal.android.explore.home.followpack.FollowPackContract.UiEvent
import net.primal.android.explore.home.followpack.FollowPackContract.UiState
import net.primal.android.explore.home.people.model.asFollowPackUi
import net.primal.android.navigation.followPackIdOrThrow
import net.primal.android.navigation.profileIdOrThrow
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.handler.ProfileFollowsHandler
import net.primal.android.user.handler.ProfileFollowsHandler.Companion.foldActions
import net.primal.android.user.repository.UserRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.explore.ExploreRepository
import net.primal.domain.feeds.buildAdvancedSearchNotesFeedSpec
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV.toNaddrString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.publisher.MissingRelaysException
import timber.log.Timber

@HiltViewModel
@OptIn(FlowPreview::class)
class FollowPackViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val exploreRepository: ExploreRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val profileFollowsHandler: ProfileFollowsHandler,
) : ViewModel() {

    private val profileId = savedStateHandle.profileIdOrThrow
    private val identifier = savedStateHandle.followPackIdOrThrow

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        fetchFollowPack()
        observeFollowPack()
        observeActiveAccount()
        observeFollowsResults()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.FollowUser -> follow(profileId = it.userId)
                    is UiEvent.UnfollowUser -> unfollow(profileId = it.userId)
                    UiEvent.DismissConfirmFollowUnfollowAlertDialog ->
                        setState { copy(shouldApproveFollowsAction = null) }

                    UiEvent.DismissError -> setState { copy(uiError = null) }
                    is UiEvent.FollowAll -> followAll(profileIds = it.userIds)
                    UiEvent.RefreshFollowPack -> fetchFollowPack()
                    is UiEvent.ApproveFollowsActions -> approveFollowsActions(it.actions)
                }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect { userAccount ->
                setState {
                    copy(
                        followPack = followPack?.copy(
                            profiles = followPack.profiles.resolveIsFollowing(following = userAccount.following),
                        ),
                        following = userAccount.following,
                    )
                }
            }
        }

    private fun observeFollowPack() =
        viewModelScope.launch {
            exploreRepository.observeFollowList(profileId = profileId, identifier = identifier)
                .collect { observedFollowPack ->
                    val followPackUi = observedFollowPack?.asFollowPackUi()
                    setState {
                        copy(
                            followPack = followPackUi?.copy(
                                profiles = followPackUi.profiles.sortedByDescending { it.followersCount }
                                    .resolveIsFollowing(following = state.value.following),
                            ),
                            feedSpec = observedFollowPack?.let {
                                buildAdvancedSearchNotesFeedSpec(
                                    query = "from:" + Naddr(
                                        kind = NostrEventKind.StarterPack.value,
                                        userId = it.authorId,
                                        identifier = it.identifier,
                                    ).toNaddrString(),
                                )
                            },
                            feedDescription = followPackUi?.let { "Created by " + it.authorProfileData?.displayName },
                        )
                    }
                }
        }

    private fun fetchFollowPack() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            try {
                exploreRepository.fetchFollowList(profileId = profileId, identifier = identifier)
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(uiError = UiError.NetworkError(error)) }
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun approveFollowsActions(actions: List<ProfileFollowsHandler.Action>) =
        viewModelScope.launch {
            setState {
                val following = following.foldActions(actions = actions)
                copy(
                    following = following,
                    shouldApproveFollowsAction = null,
                    followPack = followPack?.copy(
                        profiles = followPack.profiles.resolveIsFollowing(following),
                    ),
                )
            }
            profileFollowsHandler.forceUpdateList(actions = actions)
        }

    private fun followAll(profileIds: List<String>) =
        viewModelScope.launch {
            updateStateProfileFollowAllAndClearApprovalFlag(profileIds)
            profileIds.forEach {
                profileFollowsHandler.followDelayed(userId = activeAccountStore.activeUserId(), profileId = it)
            }
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
            profileFollowsHandler.observeResults()
                .collect { result ->
                    when (result) {
                        is ProfileFollowsHandler.ActionResult.Error -> {
                            setState {
                                val following = following
                                    .foldActions(actions = result.actions.map { it.flip() }.reversed())
                                copy(
                                    following = following,
                                    followPack = followPack?.copy(
                                        profiles = followPack.profiles.resolveIsFollowing(
                                            following,
                                        ),
                                    ),
                                )
                            }

                            when (result.error) {
                                is SigningKeyNotFoundException -> setState { copy(uiError = UiError.MissingPrivateKey) }

                                is SigningRejectedException -> setState {
                                    copy(
                                        uiError = UiError.NostrSignUnauthorized,
                                    )
                                }

                                is NetworkException, is NostrPublishException ->
                                    setState { copy(uiError = UiError.FailedToUpdateFollowList(result.error)) }

                                is UserRepository.FollowListNotFound -> setState {
                                    copy(shouldApproveFollowsAction = FollowsApproval(actions = result.actions))
                                }

                                is MissingRelaysException ->
                                    setState { copy(uiError = UiError.MissingRelaysConfiguration(result.error)) }

                                else -> setState { copy(uiError = UiError.GenericError()) }
                            }
                        }

                        ProfileFollowsHandler.ActionResult.Success -> Unit
                    }
                }
        }

    private fun updateStateProfileUnfollowAndClearApprovalFlag(profileId: String) =
        setState {
            copy(
                following = following - profileId,
                shouldApproveFollowsAction = null,
                followPack = followPack?.copy(profiles = followPack.profiles.resolveIsFollowing(following - profileId)),
            )
        }

    private fun updateStateProfileFollowAndClearApprovalFlag(profileId: String) =
        setState {
            copy(
                following = following + profileId,
                shouldApproveFollowsAction = null,
                followPack = followPack?.copy(profiles = followPack.profiles.resolveIsFollowing(following + profileId)),
            )
        }

    private fun updateStateProfileFollowAllAndClearApprovalFlag(profileIds: List<String>) =
        setState {
            copy(
                following = following + profileIds,
                shouldApproveFollowsAction = null,
                followPack = followPack?.copy(
                    profiles = followPack.profiles.resolveIsFollowing(following + profileIds),
                ),
            )
        }

    private fun List<UserProfileItemUi>.resolveIsFollowing(following: Set<String>) =
        map { it.copy(isFollowed = following.contains(it.profileId)) }
}
