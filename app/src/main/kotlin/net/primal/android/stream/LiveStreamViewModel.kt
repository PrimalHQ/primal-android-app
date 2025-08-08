package net.primal.android.stream

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.approvals.FollowsApproval
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.core.compose.profile.model.asProfileStatsUi
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.asSignatureUiError
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.events.ui.asEventZapUiModel
import net.primal.android.navigation.naddr
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.stream.LiveStreamContract.SideEffect
import net.primal.android.stream.LiveStreamContract.StreamInfoUi
import net.primal.android.stream.LiveStreamContract.UiEvent
import net.primal.android.stream.LiveStreamContract.UiState
import net.primal.android.stream.ui.ChatMessageUi
import net.primal.android.stream.ui.StreamChatItem
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.handler.ProfileFollowsHandler
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.android.wallet.zaps.hasWallet
import net.primal.domain.bookmarks.BookmarkType
import net.primal.domain.bookmarks.PublicBookmarksRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.events.EventInteractionRepository
import net.primal.domain.events.EventRelayHintsRepository
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.PublicBookmarksNotFoundException
import net.primal.domain.nostr.ReportType
import net.primal.domain.nostr.asATagValue
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.nostr.publisher.NostrPublishException as DomainNostrPublishException
import net.primal.domain.nostr.zaps.ZapError
import net.primal.domain.nostr.zaps.ZapResult
import net.primal.domain.nostr.zaps.ZapTarget
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.streams.StreamRepository
import net.primal.domain.streams.chat.ChatMessage
import net.primal.domain.streams.chat.LiveStreamChatRepository
import timber.log.Timber

@HiltViewModel
class LiveStreamViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val streamRepository: StreamRepository,
    private val liveStreamChatRepository: LiveStreamChatRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val profileFollowsHandler: ProfileFollowsHandler,
    private val zapHandler: ZapHandler,
    private val mutedItemRepository: MutedItemRepository,
    private val bookmarksRepository: PublicBookmarksRepository,
    private val eventInteractionRepository: EventInteractionRepository,
    private val relayHintsRepository: EventRelayHintsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(UiState(activeUserId = activeAccountStore.activeUserId()))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _sideEffects = MutableSharedFlow<SideEffect>()
    fun observeSideEffects() = _sideEffects

    private fun setSideEffect(effect: SideEffect) = viewModelScope.launch { _sideEffects.emit(effect) }

    private var liveStreamSubscriptionJob: Job? = null

    private var zaps: List<StreamChatItem.ZapMessageItem> = emptyList()
    private var chatMessages: List<StreamChatItem.ChatMessageItem> = emptyList()

    init {
        resolveNaddr()
        observeEvents()
        observeFollowsResults()
    }

    private fun resolveNaddr() =
        viewModelScope.launch {
            setState { copy(loading = true) }
            val naddr = savedStateHandle.naddr?.let {
                Nip19TLV.parseUriAsNaddrOrNull(it)
            }
            if (naddr != null) {
                val authorId = naddr.userId
                setState { copy(profileId = authorId, naddr = naddr) }
                liveStreamSubscriptionJob = startLiveStreamSubscription(naddr)
                initializeObservers(naddr = naddr, authorId = authorId)
            } else {
                Timber.w("Unable to resolve naddr.")
                setState { copy(loading = false) }
            }
        }

    private fun startLiveStreamSubscription(naddr: Naddr) =
        viewModelScope.launch {
            streamRepository.startLiveStreamSubscription(
                naddr = naddr,
                userId = activeAccountStore.activeUserId(),
            )
        }

    private fun initializeObservers(naddr: Naddr, authorId: String) {
        observeStreamInfo(naddr)
        observeAuthorProfile(authorId)
        observeAuthorProfileStats(authorId)
        observeFollowState(authorId)
        observeActiveAccount()
        observeChatMessages(naddr)
        observeZaps(naddr)
        observeMuteState(authorId)
    }

    private fun observeMuteState(authorId: String) =
        viewModelScope.launch {
            mutedItemRepository.observeIsUserMutedByOwnerId(
                pubkey = authorId,
                ownerId = activeAccountStore.activeUserId(),
            ).collect {
                setState { copy(isMuted = it) }
            }
        }

    private fun observeZaps(naddr: Naddr) =
        viewModelScope.launch {
            streamRepository.observeStream(aTag = naddr.asATagValue())
                .filterNotNull()
                .map { it.eventZaps.map { zap -> StreamChatItem.ZapMessageItem(zap.asEventZapUiModel()) } }
                .collect {
                    zaps = it
                    updateChatItems()
                }
        }

    private fun observeChatMessages(naddr: Naddr) =
        viewModelScope.launch {
            liveStreamChatRepository.observeMessages(streamATag = naddr.asATagValue())
                .map { chatList -> chatList.map { it.toChatMessageItem() } }
                .collect {
                    chatMessages = it
                    updateChatItems()
                }
        }

    private fun updateChatItems() {
        val combinedAndSorted = (zaps + chatMessages)
            .sortedByDescending { it.timestamp }
        setState { copy(chatItems = combinedAndSorted) }
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.OnPlayerStateUpdate -> {
                        setState {
                            copy(
                                playerState = playerState.copy(
                                    isPlaying = it.isPlaying ?: playerState.isPlaying,
                                    isBuffering = it.isBuffering ?: playerState.isBuffering,
                                    atLiveEdge = it.atLiveEdge ?: playerState.atLiveEdge,
                                    currentTime = it.currentTime ?: playerState.currentTime,
                                    totalDuration = it.totalDuration ?: playerState.totalDuration,
                                ),
                            )
                        }
                    }

                    is UiEvent.OnSeekStarted -> setState { copy(playerState = playerState.copy(isSeeking = true)) }
                    is UiEvent.OnSeek -> {
                        setState {
                            copy(playerState = playerState.copy(isSeeking = false, currentTime = it.positionMs))
                        }
                    }

                    is UiEvent.FollowAction -> follow(it.profileId)
                    is UiEvent.UnfollowAction -> unfollow(it.profileId)
                    is UiEvent.ApproveFollowsActions -> approveFollowsActions(it.actions)
                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.DismissConfirmFollowUnfollowAlertDialog -> setState {
                        copy(shouldApproveProfileAction = null)
                    }

                    is UiEvent.ZapStream -> zapStream(zapAction = it)
                    is UiEvent.OnCommentValueChanged -> setState { copy(comment = it.value) }
                    is UiEvent.SendMessage -> sendMessage(text = it.text)
                    is UiEvent.MuteAction -> mute(it.profileId)
                    is UiEvent.UnmuteAction -> unmute(it.profileId)
                    is UiEvent.ReportAbuse -> reportAbuse(it.reportType)
                    UiEvent.RequestDeleteStream -> requestDeleteStream()
                    is UiEvent.BookmarkStream -> bookmarkStream(it)
                    is UiEvent.QuoteStream -> setSideEffect(SideEffect.NavigateToQuote(it.naddr))
                    UiEvent.DismissBookmarkConfirmation -> dismissBookmarkConfirmation()
                }
            }
        }

    private fun sendMessage(text: String) {
        val streamInfo = state.value.streamInfo ?: return
        viewModelScope.launch {
            setState { copy(sendingMessage = true) }
            try {
                liveStreamChatRepository.sendMessage(
                    userId = activeAccountStore.activeUserId(),
                    streamATag = streamInfo.atag,
                    content = text,
                )
                setState { copy(comment = TextFieldValue()) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToPublishZapEvent(error)) }
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                setState { copy(error = UiError.SignatureError(error.asSignatureUiError())) }
            } finally {
                setState { copy(sendingMessage = false) }
            }
        }
    }

    private fun observeStreamInfo(naddr: Naddr) =
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
                    val isBookmarked = bookmarksRepository.isBookmarked(tagValue = stream.aTag)
                    setState {
                        copy(
                            loading = false,
                            isBookmarked = isBookmarked,
                            playerState = playerState.copy(isLive = isLive, atLiveEdge = isLive),
                            streamInfo = StreamInfoUi(
                                atag = stream.aTag,
                                eventId = stream.eventId,
                                title = stream.title ?: "Live Stream",
                                streamUrl = streamingUrl,
                                viewers = stream.currentParticipants ?: 0,
                                startedAt = stream.startsAt,
                                description = stream.summary,
                                rawNostrEventJson = stream.rawNostrEventJson,
                                authorId = stream.authorId,
                            ),
                            zaps = stream.eventZaps
                                .map { it.asEventZapUiModel() }
                                .sortedWith(EventZapUiModel.DefaultComparator),
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

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeAccountState
                .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
                .collect {
                    setState {
                        copy(
                            zappingState = this.zappingState.copy(
                                walletConnected = it.data.hasWallet(),
                                walletPreference = it.data.walletPreference,
                                zapDefault = it.data.appSettings?.zapDefault ?: this.zappingState.zapDefault,
                                zapsConfig = it.data.appSettings?.zapsConfig ?: this.zappingState.zapsConfig,
                                walletBalanceInBtc = it.data.primalWalletState.balanceInBtc,
                            ),
                        )
                    }
                }
        }

    private fun zapStream(zapAction: UiEvent.ZapStream) {
        val naddr = savedStateHandle.naddr?.let { Nip19TLV.parseUriAsNaddrOrNull(it) } ?: return
        val streamInfo = state.value.streamInfo ?: return
        val authorProfile = _state.value.authorProfile ?: return

        viewModelScope.launch {
            val postAuthorProfileData = profileRepository.findProfileDataOrNull(profileId = authorProfile.pubkey)
            val lnUrlDecoded = postAuthorProfileData?.lnUrlDecoded
            if (lnUrlDecoded == null) {
                setState { copy(error = UiError.MissingLightningAddress(IllegalStateException("Missing ln url"))) }
                return@launch
            }

            val result = zapHandler.zap(
                userId = activeAccountStore.activeUserId(),
                comment = zapAction.zapDescription,
                amountInSats = zapAction.zapAmount,
                target = ZapTarget.ReplaceableEvent(
                    kind = NostrEventKind.LiveActivity.value,
                    identifier = naddr.identifier,
                    eventId = streamInfo.eventId,
                    eventAuthorId = naddr.userId,
                    eventAuthorLnUrlDecoded = lnUrlDecoded,
                ),
            )

            if (result is ZapResult.Failure) {
                when (result.error) {
                    is ZapError.InvalidZap, is ZapError.FailedToFetchZapPayRequest,
                    is ZapError.FailedToFetchZapInvoice,
                    -> setState { copy(error = UiError.InvalidZapRequest()) }

                    ZapError.FailedToPublishEvent, ZapError.FailedToSignEvent -> {
                        setState { copy(error = UiError.FailedToPublishZapEvent()) }
                    }

                    is ZapError.Unknown -> {
                        setState { copy(error = UiError.GenericError()) }
                    }
                }
            }
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

    private fun mute(profileId: String) =
        viewModelScope.launch {
            setState { copy(isMuted = true) }
            try {
                mutedItemRepository.muteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    mutedUserId = profileId,
                )
            } catch (error: DomainNostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToMuteUser(error), isMuted = false) }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error), isMuted = false) }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = UiError.SignatureError(error.asSignatureUiError()), isMuted = false) }
            }
        }

    private fun unmute(profileId: String) =
        viewModelScope.launch {
            setState { copy(isMuted = false) }
            try {
                mutedItemRepository.unmuteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    unmutedUserId = profileId,
                )
            } catch (error: DomainNostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToUnmuteUser(error), isMuted = true) }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error), isMuted = true) }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = UiError.SignatureError(error.asSignatureUiError()), isMuted = true) }
            }
        }

    private fun reportAbuse(reportType: ReportType) =
        viewModelScope.launch {
            val streamInfo = state.value.streamInfo ?: return@launch
            try {
                profileRepository.reportAbuse(
                    userId = activeAccountStore.activeUserId(),
                    reportType = reportType,
                    profileId = streamInfo.authorId,
                    eventId = streamInfo.eventId,
                    articleId = streamInfo.atag,
                )
            } catch (error: SigningKeyNotFoundException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingPrivateKey) }
            } catch (error: SigningRejectedException) {
                Timber.w(error)
                setState { copy(error = UiError.NostrSignUnauthorized) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
            }
        }

    private fun requestDeleteStream() =
        viewModelScope.launch {
            val streamInfo = state.value.streamInfo
            val activeUserId = state.value.activeUserId

            if (streamInfo == null || activeUserId == null || streamInfo.authorId != activeUserId) {
                return@launch
            }

            try {
                val relayHint = relayHintsRepository
                    .findRelaysByIds(listOf(streamInfo.eventId))
                    .flatMap { it.relays }
                    .firstOrNull()

                eventInteractionRepository.deleteEvent(
                    userId = activeUserId,
                    eventIdentifier = streamInfo.atag,
                    eventKind = NostrEventKind.LiveActivity,
                    relayHint = relayHint,
                )

                setSideEffect(SideEffect.StreamDeleted)
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToPublishDeleteEvent(error)) }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: SigningKeyNotFoundException) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Timber.w(error)
            } catch (error: SigningRejectedException) {
                setState { copy(error = UiError.NostrSignUnauthorized) }
                Timber.w(error)
            }
        }

    private fun bookmarkStream(event: UiEvent.BookmarkStream) =
        viewModelScope.launch {
            val streamInfo = state.value.streamInfo ?: return@launch
            val userId = activeAccountStore.activeUserId()
            val isBookmarked = state.value.isBookmarked

            setState { copy(isBookmarked = !isBookmarked, shouldApproveBookmark = false) }

            try {
                if (isBookmarked) {
                    bookmarksRepository.removeFromBookmarks(
                        userId = userId,
                        forceUpdate = event.forceUpdate,
                        bookmarkType = BookmarkType.Stream,
                        tagValue = streamInfo.atag,
                    )
                } else {
                    bookmarksRepository.addToBookmarks(
                        userId = userId,
                        forceUpdate = event.forceUpdate,
                        bookmarkType = BookmarkType.Stream,
                        tagValue = streamInfo.atag,
                    )
                }
            } catch (error: NostrPublishException) {
                setState { copy(error = UiError.FailedToBookmarkNote(error)) }
                Timber.w(error)
            } catch (error: PublicBookmarksNotFoundException) {
                Timber.w(error)
                setState { copy(shouldApproveBookmark = true) }
            } catch (error: SigningKeyNotFoundException) {
                setState { copy(error = UiError.MissingPrivateKey) }
                Timber.w(error)
            } catch (error: SigningRejectedException) {
                setState { copy(error = UiError.NostrSignUnauthorized) }
                Timber.w(error)
            } catch (error: NetworkException) {
                setState { copy(error = UiError.FailedToBookmarkNote(error)) }
                Timber.w(error)
            }
        }

    private fun dismissBookmarkConfirmation() =
        viewModelScope.launch {
            setState { copy(shouldApproveBookmark = false) }
        }

    private fun ChatMessage.toChatMessageItem(): StreamChatItem.ChatMessageItem =
        StreamChatItem.ChatMessageItem(
            ChatMessageUi(
                messageId = this.messageId,
                authorProfile = this.author.asProfileDetailsUi(),
                content = this.content,
                timestamp = this.createdAt,
            ),
        )
}
