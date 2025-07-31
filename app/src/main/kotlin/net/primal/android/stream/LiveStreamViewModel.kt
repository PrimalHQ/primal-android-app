package net.primal.android.stream

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.approvals.FollowsApproval
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.core.compose.profile.model.asProfileStatsUi
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.asSignatureUiError
import net.primal.android.navigation.naddr
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.stream.LiveStreamContract.StreamInfoUi
import net.primal.android.stream.LiveStreamContract.UiEvent
import net.primal.android.stream.LiveStreamContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.handler.ProfileFollowsHandler
import net.primal.android.user.repository.UserRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.asATagValue
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.streams.StreamRepository
import timber.log.Timber

@HiltViewModel
class LiveStreamViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val streamRepository: StreamRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val profileFollowsHandler: ProfileFollowsHandler,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        resolveNaddr()
        observeEvents()
        observeFollowsResults()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.OnPlayerStateUpdate -> {
                        setState {
                            copy(
                                isPlaying = it.isPlaying ?: this.isPlaying,
                                isBuffering = it.isBuffering ?: this.isBuffering,
                                atLiveEdge = it.atLiveEdge ?: this.atLiveEdge,
                                currentTime = it.currentTime ?: this.currentTime,
                                totalDuration = it.totalDuration ?: this.totalDuration,
                            )
                        }
                    }
                    is UiEvent.OnSeekStarted -> setState { copy(isSeeking = true) }
                    is UiEvent.OnSeek -> setState { copy(isSeeking = false, currentTime = it.positionMs) }
                    is UiEvent.FollowAction -> follow(it.profileId)
                    is UiEvent.UnfollowAction -> unfollow(it.profileId)
                    is UiEvent.ApproveFollowsActions -> approveFollowsActions(it.actions)
                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.DismissConfirmFollowUnfollowAlertDialog -> setState {
                        copy(
                            shouldApproveProfileAction = null,
                        )
                    }
                }
            }
        }

    private fun resolveNaddr() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            val naddr = parseAndResolveNaddr()
            if (naddr != null) {
                val authorId = naddr.userId
                setState { copy(profileId = authorId) }
                initializeObservers(naddr = naddr, authorId = authorId)
            } else {
                Timber.w("Unable to resolve naddr.")
                setState { copy(loading = false) }
            }
        }

    private fun parseAndResolveNaddr(): Naddr? {
        return savedStateHandle.naddr?.let {
            Nip19TLV.parseUriAsNaddrOrNull(it)
        }
    }

    private fun initializeObservers(naddr: Naddr, authorId: String) {
        observeLiveStream(naddr)
        observeAuthorProfile(authorId)
        observeAuthorProfileStats(authorId)
        observeFollowState(authorId)
    }

    private fun observeLiveStream(naddr: Naddr) =
        viewModelScope.launch {
            streamRepository.observeStream(aTag = naddr.asATagValue())
                .filterNotNull()
                .collect { stream ->
                    val streamingUrl = stream.streamingUrl
                    if (streamingUrl == null) {
                        setState { copy(loading = false) }
                        return@collect
                    }
                    val isLive = stream.isLive()
                    setState {
                        copy(
                            loading = false,
                            isLive = isLive,
                            atLiveEdge = isLive,
                            streamInfo = StreamInfoUi(
                                title = stream.title ?: "Live Stream",
                                streamUrl = streamingUrl,
                                viewers = stream.currentParticipants ?: 0,
                                startedAt = stream.startsAt,
                            ),
                            comment = TextFieldValue(text = streamingUrl),
                        )
                    }
                }
        }

    private fun observeAuthorProfile(authorId: String) =
        viewModelScope.launch {
            profileRepository.observeProfileData(profileId = authorId)
                .collect {
                    setState { copy(authorProfile = it.asProfileDetailsUi()) }
                }
        }

    private fun observeAuthorProfileStats(authorId: String) =
        viewModelScope.launch {
            profileRepository.observeProfileStats(profileId = authorId)
                .collect {
                    setState { copy(profileStats = it?.asProfileStatsUi()) }
                }
        }

    private fun observeFollowState(authorId: String) =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount
                .map { authorId in it.following }
                .distinctUntilChanged()
                .collect {
                    setState { copy(isFollowed = it) }
                }
        }

    private fun follow(profileId: String) =
        viewModelScope.launch {
            setState { copy(isFollowed = true, shouldApproveProfileAction = null) }
            profileFollowsHandler.follow(
                userId = activeAccountStore.activeUserId(),
                profileId = profileId,
            )
        }

    private fun unfollow(profileId: String) =
        viewModelScope.launch {
            setState { copy(isFollowed = false, shouldApproveProfileAction = null) }
            profileFollowsHandler.unfollow(
                userId = activeAccountStore.activeUserId(),
                profileId = profileId,
            )
        }

    private fun approveFollowsActions(actions: List<ProfileFollowsHandler.Action>) =
        viewModelScope.launch {
            setState {
                copy(
                    shouldApproveProfileAction = null,
                    isFollowed = actions.firstOrNull() is ProfileFollowsHandler.Action.Follow,
                )
            }
            profileFollowsHandler.forceUpdateList(actions = actions)
        }

    private fun observeFollowsResults() =
        viewModelScope.launch {
            profileFollowsHandler.observeResults().collect {
                when (it) {
                    is ProfileFollowsHandler.ActionResult.Error -> {
                        setState { copy(isFollowed = !isFollowed) }
                        when (it.error) {
                            is NetworkException, is NostrPublishException -> {
                                setState { copy(error = UiError.FailedToUpdateFollowList(cause = it.error)) }
                            }
                            is SigningRejectedException, is SigningKeyNotFoundException -> {
                                setState { copy(error = UiError.SignatureError(it.error.asSignatureUiError())) }
                            }
                            is MissingRelaysException -> {
                                setState { copy(error = UiError.MissingRelaysConfiguration(cause = it.error)) }
                            }
                            is UserRepository.FollowListNotFound -> {
                                setState { copy(shouldApproveProfileAction = FollowsApproval(it.actions)) }
                            }
                        }
                    }
                    ProfileFollowsHandler.ActionResult.Success -> Unit
                }
            }
        }
}
