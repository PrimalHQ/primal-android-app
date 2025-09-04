package net.primal.android.stream

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import net.primal.android.stream.ui.ActiveBottomSheet
import net.primal.android.stream.ui.ChatMessageUi
import net.primal.android.stream.ui.StreamChatItem
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.accounts.active.ActiveUserAccountState
import net.primal.android.user.handler.ProfileFollowsHandler
import net.primal.android.user.handler.ProfileFollowsHandler.Companion.foldActions
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.zaps.ZapHandler
import net.primal.core.utils.CurrencyConversionUtils.formatAsString
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.events.EventInteractionRepository
import net.primal.domain.events.EventRelayHintsRepository
import net.primal.domain.events.EventRepository
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.ReferencedUser
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.Naddr
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.ReportType
import net.primal.domain.nostr.asATagValue
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.nostr.publisher.NostrPublishException as DomainNostrPublishException
import net.primal.domain.nostr.utils.extractProfileId
import net.primal.domain.nostr.utils.parseNostrUris
import net.primal.domain.nostr.zaps.ZapError
import net.primal.domain.nostr.zaps.ZapResult
import net.primal.domain.nostr.zaps.ZapTarget
import net.primal.domain.profile.ProfileRepository
import net.primal.domain.streams.StreamContentModerationMode
import net.primal.domain.streams.StreamRepository
import net.primal.domain.streams.chat.ChatMessage
import net.primal.domain.streams.chat.LiveStreamChatRepository
import net.primal.domain.utils.isConfigured
import timber.log.Timber

@Suppress("LargeClass")
class LiveStreamViewModel @AssistedInject constructor(
    userMentionHandlerFactory: UserMentionHandler.Factory,
    @Assisted val streamNaddr: Naddr,
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val streamRepository: StreamRepository,
    private val liveStreamChatRepository: LiveStreamChatRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val profileFollowsHandler: ProfileFollowsHandler,
    private val zapHandler: ZapHandler,
    private val walletAccountRepository: WalletAccountRepository,
    private val mutedItemRepository: MutedItemRepository,
    private val eventInteractionRepository: EventInteractionRepository,
    private val eventRepository: EventRepository,
    private val relayHintsRepository: EventRelayHintsRepository,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(naddr: Naddr): LiveStreamViewModel
    }

    private val userMentionHandler = userMentionHandlerFactory.create(
        scope = viewModelScope,
        userId = activeAccountStore.activeUserId(),
    )

    private val _state = MutableStateFlow(
        UiState(
            naddr = streamNaddr,
            activeUserId = activeAccountStore.activeUserId(),
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    private var authorObserversJob: Job? = null
    private var streamSubscriptionJob: Job? = null

    private var zaps: List<StreamChatItem.ZapMessageItem>? = null
    private var chatMessages: List<StreamChatItem.ChatMessageItem>? = null

    init {
        startLiveStreamSubscription()
        observeEvents()
        observeFollowsResults()
        observeUserTaggingState()
        observeStreamInfo()
        observeActiveWallet()
        observeActiveAccount()
        observeChatMessages()
        observeZaps()
        observeFollowState()
        observeMuteState()
    }

    private fun startLiveStreamSubscription() {
        viewModelScope.launch {
            streamSubscriptionJob?.cancel()
            streamSubscriptionJob = streamRepository.startLiveStreamSubscription(
                naddr = streamNaddr,
                userId = activeAccountStore.activeUserId(),
                streamContentModerationMode = _state.value.contentModerationMode,
            )
        }
    }

    private fun changeContentModeration(moderationMode: StreamContentModerationMode) =
        viewModelScope.launch {
            if (moderationMode != _state.value.contentModerationMode) {
                val aTagValue = streamNaddr.asATagValue()

                setState { copy(chatLoading = true, contentModerationMode = moderationMode) }
                liveStreamChatRepository.clearMessages(streamATag = aTagValue)
                eventInteractionRepository.deleteZaps(eventId = aTagValue)
                startLiveStreamSubscription()
            }
        }

    private fun observeUserTaggingState() {
        viewModelScope.launch {
            userMentionHandler.state.collect { taggingState ->
                setState { copy(userTaggingState = taggingState) }
            }
        }
    }

    private fun observeZaps() =
        viewModelScope.launch {
            eventRepository.observeZapsByEventId(eventId = streamNaddr.asATagValue())
                .collect { streamZaps ->
                    setState {
                        copy(
                            zaps = streamZaps.map { it.asEventZapUiModel() }
                                .sortedWith(EventZapUiModel.DefaultComparator),
                        )
                    }
                    zaps = streamZaps.map { StreamChatItem.ZapMessageItem(it.asEventZapUiModel()) }
                    updateChatItems()
                }
        }

    private fun observeChatMessages() =
        viewModelScope.launch {
            liveStreamChatRepository.observeMessages(streamATag = streamNaddr.asATagValue())
                .map { chatList -> chatList.map { it.toChatMessageItem() } }
                .collect {
                    chatMessages = it
                    updateChatItems()
                }
        }

    private fun updateChatItems() {
        val mutedProfiles = state.value.activeUserMutedProfiles

        val filteredZaps = zaps?.filterNot { it.zap.zapperId in mutedProfiles } ?: return
        val filteredChatMessages = chatMessages
            ?.filterNot { it.message.authorProfile.pubkey in mutedProfiles }
            ?: return

        val combinedAndSorted = (filteredZaps + filteredChatMessages).sortedByDescending { it.timestamp }
        setState { copy(chatLoading = streamSubscriptionJob?.isActive != true, chatItems = combinedAndSorted) }

        updateLiveProfilesStatus(combinedAndSorted)
    }

    private fun updateLiveProfilesStatus(chatItems: List<StreamChatItem>) {
        viewModelScope.launch {
            val authorIds = chatItems.map {
                when (it) {
                    is StreamChatItem.ChatMessageItem -> it.message.authorProfile.pubkey
                    is StreamChatItem.ZapMessageItem -> it.zap.zapperId
                }
            }.distinct()

            if (authorIds.isNotEmpty()) {
                val liveProfileIds = streamRepository.findWhoIsLive(mainHostIds = authorIds)
                setState { copy(liveProfiles = liveProfileIds) }
            }
        }
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
                                    isVideoFinished = if (it.isPlaying == true && playerState.isVideoFinished) {
                                        false
                                    } else {
                                        playerState.isVideoFinished
                                    },
                                ),
                            )
                        }
                    }

                    is UiEvent.OnSeekStarted -> setState {
                        copy(playerState = playerState.copy(isSeeking = true, isVideoFinished = false))
                    }

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
                    UiEvent.ToggleMute -> toggleMute()

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

                    is UiEvent.ChangeContentModeration -> changeContentModeration(moderationMode = it.moderationMode)

                    is UiEvent.ReportMessage -> reportMessage(
                        reportType = it.reportType,
                        messageId = it.messageId,
                        authorId = it.authorId,
                    )

                    is UiEvent.ChangeActiveBottomSheet -> {
                        setState { copy(activeBottomSheet = it.sheet) }
                        when (val sheet = it.sheet) {
                            is ActiveBottomSheet.ChatDetails -> {
                                fetchFollowerCount(sheet.message.authorProfile.pubkey)
                            }

                            is ActiveBottomSheet.ZapDetails -> {
                                fetchFollowerCount(sheet.zap.zapperId)
                            }

                            is ActiveBottomSheet.StreamInfo -> {
                                state.value.streamInfo?.mainHostId?.let { hostId ->
                                    fetchFollowerCount(hostId)
                                }
                            }

                            else -> Unit
                        }
                    }

                    UiEvent.OnVideoUnavailable -> {
                        setState { copy(isStreamUnavailable = true) }
                    }

                    UiEvent.OnVideoEnded -> {
                        setState { copy(playerState = playerState.copy(isVideoFinished = true, isPlaying = false)) }
                    }

                    is UiEvent.ChangeStreamMuted -> changeStreamMuted(it.isMuted)
                }
            }
        }

    private fun changeStreamMuted(isMuted: Boolean) =
        viewModelScope.launch {
            val mainHostId = state.value.streamInfo?.mainHostId ?: return@launch

            setState { copy(mainHostStreamsMuted = isMuted) }
            if (isMuted) {
                mutedItemRepository.muteStreamNotifications(
                    ownerId = activeAccountStore.activeUserId(),
                    pubkey = mainHostId,
                )
            } else {
                mutedItemRepository.unmuteStreamNotifications(
                    ownerId = activeAccountStore.activeUserId(),
                    pubkey = mainHostId,
                )
            }
        }

    private fun fetchFollowerCount(profileId: String) {
        if (state.value.profileIdToFollowerCount.containsKey(profileId)) return

        viewModelScope.launch {
            try {
                profileRepository.fetchProfile(profileId = profileId)

                val statsList = profileRepository.findProfileStats(profileIds = listOf(profileId))
                val followerCount = statsList.firstOrNull()?.followers

                if (followerCount != null) {
                    setState {
                        copy(profileIdToFollowerCount = profileIdToFollowerCount + (profileId to followerCount))
                    }
                }
            } catch (error: NetworkException) {
                Timber.w(error)
            }
        }
    }

    private fun toggleMute() =
        viewModelScope.launch {
            val newIsMuted = !state.value.playerState.isMuted
            updateVideoSoundSettings(soundOn = !newIsMuted)

            setState { copy(playerState = playerState.copy(isMuted = newIsMuted)) }
        }

    private fun updateVideoSoundSettings(soundOn: Boolean) =
        viewModelScope.launch {
            userRepository.updateContentDisplaySettings(userId = activeAccountStore.activeUserId()) {
                copy(autoPlayVideoSoundOn = soundOn)
            }
        }

    private fun sendMessage(text: String) {
        val streamInfo = state.value.streamInfo ?: return
        viewModelScope.launch {
            setState { copy(sendingMessage = true) }
            try {
                val content = userMentionHandler.replaceUserMentionsWithUserIds(
                    content = text,
                    users = state.value.taggedUsers,
                )

                liveStreamChatRepository.sendMessage(
                    userId = activeAccountStore.activeUserId(),
                    streamATag = streamInfo.atag,
                    content = content,
                )
                setState { copy(comment = TextFieldValue(), taggedUsers = emptyList()) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToPublishZapEvent(error)) }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = UiError.SignatureError(error.asSignatureUiError())) }
            } finally {
                setState { copy(sendingMessage = false) }
            }
        }
    }

    private fun observeStreamInfo() =
        viewModelScope.launch {
            streamRepository.observeStream(aTag = streamNaddr.asATagValue())
                .filterNotNull()
                .collect { stream ->
                    val isLive = stream.isLive()
                    val streamUrlToPlay = if (isLive) stream.streamingUrl else stream.recordingUrl

                    if (streamUrlToPlay == null) {
                        setState { copy(loading = false, isStreamUnavailable = true) }
                        return@collect
                    }

                    if (authorObserversJob == null || state.value.streamInfo?.mainHostId != stream.mainHostId) {
                        initializeMainHostObservers(mainHostId = stream.mainHostId)
                    }

                    setState {
                        copy(
                            loading = false,
                            isStreamUnavailable = false,
                            playerState = playerState.copy(
                                isLive = isLive,
                                atLiveEdge = isLive,
                                isVideoFinished = false,
                            ),
                            streamInfo = this.streamInfo?.copy(
                                atag = stream.aTag,
                                eventId = stream.eventId,
                                title = stream.title ?: "Live Stream",
                                streamUrl = streamUrlToPlay,
                                viewers = stream.currentParticipants ?: 0,
                                startedAt = stream.startsAt,
                                description = stream.summary,
                                rawNostrEventJson = stream.rawNostrEventJson,
                                mainHostId = stream.mainHostId,
                            ) ?: StreamInfoUi(
                                atag = stream.aTag,
                                eventId = stream.eventId,
                                title = stream.title ?: "Live Stream",
                                streamUrl = streamUrlToPlay,
                                viewers = stream.currentParticipants ?: 0,
                                startedAt = stream.startsAt,
                                description = stream.summary,
                                rawNostrEventJson = stream.rawNostrEventJson,
                                mainHostId = stream.mainHostId,
                            ),
                        )
                    }
                }
        }

    private fun initializeMainHostObservers(mainHostId: String) {
        authorObserversJob?.cancel()
        authorObserversJob = viewModelScope.launch {
            observeAuthorProfile(mainHostId)
            observeAuthorProfileStats(mainHostId)
            observeIsStreamMuted(mainHostId)
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

    private fun CoroutineScope.observeIsStreamMuted(pubkey: String) =
        launch {
            mutedItemRepository.observeIsStreamMutedByOwnerId(
                ownerId = activeAccountStore.activeUserId(),
                pubkey = pubkey,
            ).collect { setState { copy(mainHostStreamsMuted = it) } }
        }

    private fun observeFollowState() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount
                .collect { followers ->
                    setState {
                        copy(
                            activeUserFollowedProfiles = followers.following,
                        )
                    }
                }
        }

    private fun observeMuteState() =
        viewModelScope.launch {
            mutedItemRepository.observeMutedUsersByOwnerId(ownerId = activeAccountStore.activeUserId())
                .collect { muted -> setState { copy(activeUserMutedProfiles = muted.map { it.profileId }.toSet()) } }

            updateChatItems()
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
                    activeUserFollowedProfiles = this.activeUserFollowedProfiles + profileId,
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
                    activeUserFollowedProfiles = this.activeUserFollowedProfiles - profileId,
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
                )
            }
            profileFollowsHandler.forceUpdateList(actions = actions)
        }

    private fun observeFollowsResults() =
        viewModelScope.launch {
            profileFollowsHandler.observeResults().collect { result ->
                when (result) {
                    is ProfileFollowsHandler.ActionResult.Error -> {
                        setState {
                            val following = activeUserFollowedProfiles
                                .foldActions(actions = result.actions.map { it.flip() }.reversed())
                            copy(activeUserFollowedProfiles = following)
                        }
                        when (result.error) {
                            is NetworkException, is NostrPublishException -> {
                                setState { copy(error = UiError.FailedToUpdateFollowList(cause = result.error)) }
                            }

                            is SignatureException -> {
                                setState {
                                    copy(
                                        error = UiError.SignatureError(error = result.error.asSignatureUiError()),
                                    )
                                }
                            }

                            is MissingRelaysException -> {
                                setState { copy(error = UiError.MissingRelaysConfiguration(cause = result.error)) }
                            }

                            is UserRepository.FollowListNotFound -> {
                                setState { copy(shouldApproveProfileAction = FollowsApproval(result.actions)) }
                            }

                            else -> setState { copy(error = UiError.GenericError()) }
                        }
                    }

                    ProfileFollowsHandler.ActionResult.Success -> Unit
                }
            }
        }

    private fun mute(profileId: String) =
        viewModelScope.launch {
            setState {
                copy(activeUserMutedProfiles = this.activeUserMutedProfiles + profileId)
            }

            try {
                mutedItemRepository.muteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    mutedUserId = profileId,
                )
            } catch (error: DomainNostrPublishException) {
                Timber.w(error)
                setState {
                    copy(
                        activeUserMutedProfiles = this.activeUserMutedProfiles - profileId,
                        error = UiError.FailedToMuteUser(error),
                    )
                }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState {
                    copy(
                        activeUserMutedProfiles = this.activeUserMutedProfiles - profileId,
                        error = UiError.MissingRelaysConfiguration(error),
                    )
                }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState {
                    copy(
                        activeUserMutedProfiles = this.activeUserMutedProfiles - profileId,
                        error = UiError.SignatureError(error.asSignatureUiError()),
                    )
                }
            }
        }

    private fun unmute(profileId: String) =
        viewModelScope.launch {
            setState {
                copy(activeUserMutedProfiles = this.activeUserMutedProfiles - profileId)
            }

            try {
                mutedItemRepository.unmuteUserAndPersistMuteList(
                    userId = activeAccountStore.activeUserId(),
                    unmutedUserId = profileId,
                )
            } catch (error: DomainNostrPublishException) {
                Timber.w(error)
                setState {
                    copy(
                        activeUserMutedProfiles = this.activeUserMutedProfiles + profileId,
                        error = UiError.FailedToUnmuteUser(error),
                    )
                }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState {
                    copy(
                        activeUserMutedProfiles = this.activeUserMutedProfiles + profileId,
                        error = UiError.MissingRelaysConfiguration(error),
                    )
                }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState {
                    copy(
                        activeUserMutedProfiles = this.activeUserMutedProfiles + profileId,
                        error = UiError.SignatureError(error.asSignatureUiError()),
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
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = UiError.SignatureError(error.asSignatureUiError())) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
            }
        }

    private fun reportMessage(
        reportType: ReportType,
        messageId: String,
        authorId: String,
    ) = viewModelScope.launch {
        val streamInfo = state.value.streamInfo ?: return@launch
        try {
            profileRepository.reportAbuse(
                userId = activeAccountStore.activeUserId(),
                reportType = reportType,
                profileId = authorId,
                eventId = messageId,
                articleId = streamInfo.atag,
            )
        } catch (error: SignatureException) {
            Timber.w(error)
            setState { copy(error = UiError.SignatureError(error.asSignatureUiError())) }
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
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = UiError.SignatureError(error.asSignatureUiError())) }
            }
        }

    private suspend fun ChatMessage.toChatMessageItem(): StreamChatItem.ChatMessageItem {
        val nostrUrisRaw = this.content.parseNostrUris()
        val nostrUrisUi = nostrUrisRaw.mapNotNull { uriString ->
            val position = this.content.indexOf(uriString)
            val pubkey = uriString.extractProfileId()
            if (pubkey != null) {
                try {
                    val profileData = profileRepository.findProfileDataOrNull(profileId = pubkey)
                    val handle = profileData?.handle
                    if (handle != null) {
                        NoteNostrUriUi(
                            uri = uriString,
                            type = EventUriNostrType.Profile,
                            referencedUser = ReferencedUser(
                                userId = pubkey,
                                handle = handle,
                            ),
                            referencedEventAlt = null,
                            referencedNote = null,
                            referencedArticle = null,
                            referencedHighlight = null,
                            referencedZap = null,
                            referencedStream = null,
                            position = position,
                        )
                    } else {
                        null
                    }
                } catch (error: NetworkException) {
                    Timber.w(error, "Failed to resolve profile for $uriString")
                    null
                }
            } else {
                null
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
