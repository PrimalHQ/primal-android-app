package net.primal.android.stream

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.approvals.FollowsApproval
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.core.compose.profile.model.asProfileStatsUi
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.asSignatureUiError
import net.primal.android.editor.domain.NoteTaggedUser
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.events.ui.asEventZapUiModel
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.notes.feed.model.NoteNostrUriUi
import net.primal.android.profile.mention.UserMentionHandler
import net.primal.android.profile.mention.appendUserTagAtSignAtCursorPosition
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
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.bookmarks.BookmarkType
import net.primal.domain.bookmarks.PublicBookmarksRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.events.EventInteractionRepository
import net.primal.domain.events.EventRelayHintsRepository
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.ReferencedUser
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.Nip19TLV
import net.primal.domain.nostr.Nip19TLV.toNprofileString
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.Nprofile
import net.primal.domain.nostr.PublicBookmarksNotFoundException
import net.primal.domain.nostr.ReportType
import net.primal.domain.nostr.asATagValue
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.cryptography.SigningKeyNotFoundException
import net.primal.domain.nostr.cryptography.SigningRejectedException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.nostr.publisher.NostrPublishException as DomainNostrPublishException
import net.primal.domain.nostr.utils.parseNostrUris
import net.primal.domain.nostr.zaps.ZapError
import net.primal.domain.nostr.zaps.ZapResult
import net.primal.domain.nostr.zaps.ZapTarget
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.streams.StreamRepository
import net.primal.domain.streams.chat.ChatMessage
import net.primal.domain.streams.chat.LiveStreamChatRepository
import net.primal.domain.utils.isConfigured
import timber.log.Timber

@Suppress("LargeClass")
@HiltViewModel
class LiveStreamViewModel @Inject constructor(
    userMentionHandlerFactory: UserMentionHandler.Factory,
    private val profileRepository: ProfileRepository,
    private val streamRepository: StreamRepository,
    private val liveStreamChatRepository: LiveStreamChatRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val profileFollowsHandler: ProfileFollowsHandler,
    private val zapHandler: ZapHandler,
    private val walletAccountRepository: WalletAccountRepository,
    private val mutedItemRepository: MutedItemRepository,
    private val bookmarksRepository: PublicBookmarksRepository,
    private val eventInteractionRepository: EventInteractionRepository,
    private val relayHintsRepository: EventRelayHintsRepository,
) : ViewModel() {

    private val userMentionHandler = userMentionHandlerFactory.create(
        scope = viewModelScope,
        userId = activeAccountStore.activeUserId(),
    )
    private val _state = MutableStateFlow(UiState(activeUserId = activeAccountStore.activeUserId()))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    private var liveStreamSubscriptionJob: Job? = null
    private var authorObserversJob: Job? = null

    private var zaps: List<StreamChatItem.ZapMessageItem> = emptyList()
    private var chatMessages: List<StreamChatItem.ChatMessageItem> = emptyList()

    private fun stopSubscriptions() {
        liveStreamSubscriptionJob?.cancel()
        liveStreamSubscriptionJob = null
        authorObserversJob?.cancel()
        authorObserversJob = null
        setState {
            copy(
                naddr = null,
                streamInfo = null,
                zaps = emptyList(),
                chatItems = emptyList(),
            )
        }
    }

    init {
        observeEvents()
        observeFollowsResults()
        observeUserTaggingState()
    }

    private fun resolveNaddr(naddrUri: String) =
        viewModelScope.launch {
            setState { copy(loading = true) }
            val naddr = Nip19TLV.parseUriAsNaddrOrNull(naddrUri)
            if (naddr != null) {
                setState { copy(naddr = naddr) }
                liveStreamSubscriptionJob = startLiveStreamSubscription(naddr)
                initializeObservers(naddr = naddr)
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

    private fun initializeObservers(naddr: Naddr) {
        observeStreamInfo(naddr)
        observeActiveWallet()
        observeActiveAccount()
        observeChatMessages(naddr)
        observeZaps(naddr)
    }

    private fun observeUserTaggingState() {
        viewModelScope.launch {
            userMentionHandler.state.collect { taggingState ->
                setState { copy(userTaggingState = taggingState) }
            }
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
                    is UiEvent.QuoteStream -> setEffect(SideEffect.NavigateToQuote(it.naddr))
                    UiEvent.DismissBookmarkConfirmation -> dismissBookmarkConfirmation()
                    UiEvent.ToggleMute -> setState {
                        copy(playerState = playerState.copy(isMuted = !playerState.isMuted))
                    }

                    is UiEvent.StartStream -> {
                        val newNaddr = Nip19TLV.parseUriAsNaddrOrNull(it.naddr)
                        if (state.value.naddr != newNaddr) {
                            stopSubscriptions()
                            resolveNaddr(naddrUri = it.naddr)
                        }
                    }

                    UiEvent.StopStream -> stopSubscriptions()

                    is UiEvent.SearchUsers -> userMentionHandler.search(it.query)
                    is UiEvent.ToggleSearchUsers -> userMentionHandler.toggleSearch(it.enabled)
                    is UiEvent.TagUser -> {
                        setState {
                            copy(taggedUsers = this.taggedUsers.toMutableList().apply { add(it.taggedUser) })
                        }
                        userMentionHandler.markUserAsMentioned(it.taggedUser.userId)
                    }

                    UiEvent.AppendUserTagAtSign -> setState {
                        copy(comment = this.comment.appendUserTagAtSignAtCursorPosition())
                    }
                }
            }
        }

    private fun sendMessage(text: String) {
        val streamInfo = state.value.streamInfo ?: return
        viewModelScope.launch {
            setState { copy(sendingMessage = true) }
            try {
                val content = text.replaceUserMentionsWithNostrUris(users = state.value.taggedUsers)
                liveStreamChatRepository.sendMessage(
                    userId = activeAccountStore.activeUserId(),
                    streamATag = streamInfo.atag,
                    content = content,
                )
                setState { copy(comment = TextFieldValue(), taggedUsers = emptyList()) }
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

    private fun String.replaceUserMentionsWithNostrUris(users: List<NoteTaggedUser>): String {
        var content = this
        users.forEach { user ->
            val nprofile = Nprofile(pubkey = user.userId, relays = emptyList())
            content = content.replace(
                oldValue = user.displayUsername,
                newValue = "nostr:${nprofile.toNprofileString()}",
            )
        }
        return content
    }

    private fun observeStreamInfo(naddr: Naddr) =
        viewModelScope.launch {
            streamRepository.observeStream(aTag = naddr.asATagValue())
                .filterNotNull()
                .collect { stream ->
                    val isLive = stream.isLive()
                    val streamUrlToPlay = if (isLive) stream.streamingUrl else stream.recordingUrl

                    if (streamUrlToPlay == null) {
                        setState { copy(loading = false) }
                        return@collect
                    }

                    if (authorObserversJob == null || state.value.streamInfo?.mainHostId != stream.authorId) {
                        initializeMainHostObservers(mainHostId = stream.authorId)
                    }

                    val isBookmarked = bookmarksRepository.isBookmarked(tagValue = stream.aTag)
                    setState {
                        copy(
                            loading = false,
                            isBookmarked = isBookmarked,
                            playerState = playerState.copy(isLive = isLive, atLiveEdge = isLive),
                            streamInfo = this.streamInfo?.copy(
                                atag = stream.aTag,
                                eventId = stream.eventId,
                                title = stream.title ?: "Live Stream",
                                streamUrl = streamUrlToPlay,
                                viewers = stream.currentParticipants ?: 0,
                                startedAt = stream.startsAt,
                                description = stream.summary,
                                rawNostrEventJson = stream.rawNostrEventJson,
                                mainHostId = stream.authorId,
                            ) ?: StreamInfoUi(
                                atag = stream.aTag,
                                eventId = stream.eventId,
                                title = stream.title ?: "Live Stream",
                                streamUrl = streamUrlToPlay,
                                viewers = stream.currentParticipants ?: 0,
                                startedAt = stream.startsAt,
                                description = stream.summary,
                                rawNostrEventJson = stream.rawNostrEventJson,
                                mainHostId = stream.authorId,
                            ),
                            zaps = stream.eventZaps
                                .map { it.asEventZapUiModel() }
                                .sortedWith(EventZapUiModel.DefaultComparator),
                        )
                    }
                }
        }

    private fun initializeMainHostObservers(mainHostId: String) {
        authorObserversJob?.cancel()
        authorObserversJob = viewModelScope.launch {
            observeAuthorProfile(mainHostId)
            observeAuthorProfileStats(mainHostId)
            observeFollowState(mainHostId)
            observeMuteState(mainHostId)
        }
    }

    private fun CoroutineScope.observeAuthorProfile(mainHostId: String) =
        launch {
            profileRepository.observeProfileData(profileId = mainHostId)
                .collect { profileData ->
                    setState {
                        copy(
                            streamInfo = this.streamInfo?.copy(
                                mainHostProfile = profileData.asProfileDetailsUi(),
                            ),
                        )
                    }
                }
        }

    private fun CoroutineScope.observeAuthorProfileStats(mainHostId: String) =
        launch {
            profileRepository.observeProfileStats(profileId = mainHostId)
                .collect { stats ->
                    setState {
                        copy(
                            streamInfo = this.streamInfo?.copy(
                                mainHostProfileStats = stats?.asProfileStatsUi(),
                            ),
                        )
                    }
                }
        }

    private fun CoroutineScope.observeFollowState(mainHostId: String) =
        launch {
            activeAccountStore.activeUserAccount
                .map { mainHostId in it.following }
                .distinctUntilChanged()
                .collect { isFollowed ->
                    setState {
                        copy(streamInfo = this.streamInfo?.copy(isMainHostFollowedByActiveUser = isFollowed))
                    }
                }
        }

    private fun CoroutineScope.observeMuteState(mainHostId: String) =
        launch {
            mutedItemRepository.observeIsUserMutedByOwnerId(
                pubkey = mainHostId,
                ownerId = activeAccountStore.activeUserId(),
            ).collect {
                setState {
                    copy(
                        streamInfo = this.streamInfo?.copy(isMainHostMutedByActiveUser = it),
                    )
                }
            }
        }

    private fun observeActiveWallet() =
        viewModelScope.launch {
            walletAccountRepository.observeActiveWallet(userId = activeAccountStore.activeUserId())
                .collect { wallet ->
                    setState {
                        copy(
                            zappingState = zappingState.copy(
                                walletConnected = wallet.isConfigured(),
                                walletBalanceInBtc = wallet?.balanceInBtc?.formatAsString(),
                            ),
                        )
                    }
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
                                zapDefault = it.data.appSettings?.zapDefault ?: this.zappingState.zapDefault,
                                zapsConfig = it.data.appSettings?.zapsConfig ?: this.zappingState.zapsConfig,
                            ),
                        )
                    }
                }
        }

    private fun zapStream(zapAction: UiEvent.ZapStream) {
        val naddr = state.value.naddr ?: return
        val streamInfo = state.value.streamInfo ?: return
        val authorProfile = _state.value.streamInfo?.mainHostProfile ?: return

        viewModelScope.launch {
            val postAuthorProfileData = profileRepository.findProfileDataOrNull(profileId = authorProfile.pubkey)
            val lnUrlDecoded = postAuthorProfileData?.lnUrlDecoded
            if (lnUrlDecoded == null) {
                setState { copy(error = UiError.MissingLightningAddress(IllegalStateException("Missing ln url"))) }
                return@launch
            }

            val walletId = walletAccountRepository.getActiveWallet(userId = activeAccountStore.activeUserId())
                ?.walletId ?: return@launch

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
                walletId = walletId,
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
            setState {
                copy(
                    streamInfo = this.streamInfo?.copy(isMainHostFollowedByActiveUser = true),
                    shouldApproveProfileAction = null,
                )
            }
            profileFollowsHandler.follow(
                userId = activeAccountStore.activeUserId(),
                profileId = profileId,
            )
        }

    private fun unfollow(profileId: String) =
        viewModelScope.launch {
            setState {
                copy(
                    streamInfo = this.streamInfo?.copy(isMainHostFollowedByActiveUser = false),
                    shouldApproveProfileAction = null,
                )
            }
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
                    streamInfo = this.streamInfo?.copy(
                        isMainHostFollowedByActiveUser = actions.firstOrNull() is ProfileFollowsHandler.Action.Follow,
                    ),
                )
            }
            profileFollowsHandler.forceUpdateList(actions = actions)
        }

    private fun observeFollowsResults() =
        viewModelScope.launch {
            profileFollowsHandler.observeResults().collect {
                when (it) {
                    is ProfileFollowsHandler.ActionResult.Error -> {
                        setState {
                            copy(
                                streamInfo = this.streamInfo?.copy(
                                    isMainHostFollowedByActiveUser = !this.streamInfo.isMainHostFollowedByActiveUser,
                                ),
                            )
                        }
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
            setState { copy(streamInfo = this.streamInfo?.copy(isMainHostMutedByActiveUser = true)) }
            try {
                mutedItemRepository.muteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    mutedUserId = profileId,
                )
            } catch (error: DomainNostrPublishException) {
                Timber.w(error)
                setState {
                    copy(
                        error = UiError.FailedToMuteUser(error),
                        streamInfo = streamInfo?.copy(isMainHostMutedByActiveUser = false),
                    )
                }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState {
                    copy(
                        error = UiError.MissingRelaysConfiguration(error),
                        streamInfo = streamInfo?.copy(isMainHostMutedByActiveUser = false),
                    )
                }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState {
                    copy(
                        error = UiError.SignatureError(error.asSignatureUiError()),
                        streamInfo = streamInfo?.copy(isMainHostMutedByActiveUser = false),
                    )
                }
            }
        }

    private fun unmute(profileId: String) =
        viewModelScope.launch {
            setState { copy(streamInfo = this.streamInfo?.copy(isMainHostMutedByActiveUser = false)) }
            try {
                mutedItemRepository.unmuteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    unmutedUserId = profileId,
                )
            } catch (error: DomainNostrPublishException) {
                Timber.w(error)
                setState {
                    copy(
                        error = UiError.FailedToUnmuteUser(error),
                        streamInfo = streamInfo?.copy(isMainHostMutedByActiveUser = true),
                    )
                }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState {
                    copy(
                        error = UiError.MissingRelaysConfiguration(error),
                        streamInfo = streamInfo?.copy(isMainHostMutedByActiveUser = true),
                    )
                }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState {
                    copy(
                        error = UiError.SignatureError(error.asSignatureUiError()),
                        streamInfo = streamInfo?.copy(isMainHostMutedByActiveUser = true),
                    )
                }
            }
        }

    private fun reportAbuse(reportType: ReportType) =
        viewModelScope.launch {
            val streamInfo = state.value.streamInfo ?: return@launch
            try {
                profileRepository.reportAbuse(
                    userId = activeAccountStore.activeUserId(),
                    reportType = reportType,
                    profileId = streamInfo.mainHostId,
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

            if (streamInfo == null || activeUserId == null || streamInfo.mainHostId != activeUserId) {
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

                setEffect(SideEffect.StreamDeleted)
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

    private suspend fun ChatMessage.toChatMessageItem(): StreamChatItem.ChatMessageItem {
        val nostrUrisRaw = this.content.parseNostrUris()
        val nostrUrisUi = nostrUrisRaw.mapNotNull { uriString ->
            val position = this.content.indexOf(uriString)
            Nip19TLV.parseUriAsNprofileOrNull(uriString)?.let { nprofile ->
                try {
                    val profileData = profileRepository.findProfileDataOrNull(profileId = nprofile.pubkey)
                    if (profileData?.handle != null) {
                        NoteNostrUriUi(
                            uri = uriString,
                            type = EventUriNostrType.Profile,
                            referencedUser = ReferencedUser(
                                userId = nprofile.pubkey,
                                handle = profileData.handle!!,
                            ),
                            referencedEventAlt = null,
                            referencedNote = null,
                            referencedArticle = null,
                            referencedHighlight = null,
                            referencedZap = null,
                            position = position,
                        )
                    } else {
                        null
                    }
                } catch (error: NetworkException) {
                    Timber.w(error, "Failed to resolve profile for $uriString")
                    null
                }
            }
        }

        return StreamChatItem.ChatMessageItem(
            ChatMessageUi(
                messageId = this.messageId,
                authorProfile = this.author.asProfileDetailsUi(),
                content = this.content,
                timestamp = this.createdAt,
                nostrUris = nostrUrisUi,
            ),
        )
    }
}
