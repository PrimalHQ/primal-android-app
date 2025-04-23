package net.primal.android.messages.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.attachment.model.asEventUriUiModel
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.messages.chat.ChatContract.UiEvent
import net.primal.android.messages.chat.ChatContract.UiState
import net.primal.android.messages.chat.model.ChatMessageUi
import net.primal.android.navigation.profileIdOrThrow
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.notes.feed.model.asNoteNostrUriUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.messages.ChatRepository
import net.primal.domain.messages.DirectMessage
import net.primal.domain.nostr.cryptography.MessageEncryptException
import net.primal.domain.nostr.cryptography.SignResult
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.MissingRelaysException
import net.primal.domain.profile.ProfileRepository
import timber.log.Timber

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    activeAccountStore: ActiveAccountStore,
    private val subscriptionsManager: SubscriptionsManager,
    private val chatRepository: ChatRepository,
    private val profileRepository: ProfileRepository,
    private val nostrNotary: NostrNotary,
) : ViewModel() {

    private val participantId = savedStateHandle.profileIdOrThrow
    private val userId = activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(
        UiState(
            participantId = participantId,
            messages = chatRepository
                .newestMessages(userId = userId, participantId = participantId)
                .mapAsPagingDataOfChatMessageUi(),
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        observeMessagesSeenEvents()
        observeParticipant()
        subscribeToTotalUnreadCountChanges()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.MessagesSeen -> {}
                    UiEvent.SendMessage -> sendMessage()
                    is UiEvent.UpdateNewMessage -> {
                        setState { copy(newMessageText = it.text) }
                    }
                }
            }
        }

    @OptIn(FlowPreview::class)
    private fun observeMessagesSeenEvents() =
        viewModelScope.launch {
            events
                .filterIsInstance(UiEvent.MessagesSeen::class)
                .debounce(1.seconds)
                .collect {
                    markConversationAsRead()
                }
        }

    private fun observeParticipant() =
        viewModelScope.launch {
            profileRepository.observeProfileData(profileId = participantId).collect {
                setState {
                    copy(participantProfile = it.asProfileDetailsUi())
                }
            }
        }

    private fun subscribeToTotalUnreadCountChanges() =
        viewModelScope.launch {
            subscriptionsManager.badges
                .map { it.unreadMessagesCount }
                .distinctUntilChanged()
                .collect {
                    try {
                        chatRepository.fetchNewConversationMessages(userId, participantId)
                    } catch (error: NetworkException) {
                        Timber.w(error)
                    }
                }
        }

    private fun markConversationAsRead() =
        viewModelScope.launch {
            try {
                val signResult = nostrNotary.signAuthorizationNostrEvent(
                    userId = userId,
                    description = "Mark conversation with $participantId as read.",
                )

                when (signResult) {
                    is SignResult.Rejected -> {
                        Timber.w(signResult.error)
                        setErrorState(error = UiState.ChatError.PublishError(signResult.error))
                    }

                    is SignResult.Signed -> {
                        chatRepository.markConversationAsRead(
                            authorization = signResult.event,
                            conversationUserId = participantId,
                        )
                    }
                }
            } catch (error: NetworkException) {
                Timber.w(error)
            }
        }

    private fun sendMessage() =
        viewModelScope.launch {
            setState { copy(sending = true) }
            try {
                chatRepository.sendMessage(
                    userId = userId,
                    receiverId = participantId,
                    text = state.value.newMessageText,
                )
                setState { copy(newMessageText = "") }
            } catch (error: SignatureException) {
                Timber.w(error)
                setErrorState(error = UiState.ChatError.PublishError(error))
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setErrorState(error = UiState.ChatError.PublishError(error))
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setErrorState(error = UiState.ChatError.MissingRelaysConfiguration(error))
            } catch (error: MessageEncryptException) {
                Timber.w(error)
                setErrorState(error = UiState.ChatError.PublishError(error))
            } finally {
                setState { copy(sending = false) }
            }
        }

    private fun Flow<PagingData<DirectMessage>>.mapAsPagingDataOfChatMessageUi() =
        map { pagingData -> pagingData.map { it.mapAsChatMessageUi() } }

    private fun DirectMessage.mapAsChatMessageUi() =
        ChatMessageUi(
            messageId = this.messageId,
            isUserMessage = userId == this.senderId,
            senderId = this.senderId,
            timestamp = Instant.ofEpochSecond(this.createdAt),
            content = this.content,
            hashtags = this.hashtags,
            uris = this.links.map { it.asEventUriUiModel() },
            nostrUris = this.nostrUris.map { it.asNoteNostrUriUi() },
        )

    private fun setErrorState(error: UiState.ChatError) {
        setState { copy(error = error) }
        viewModelScope.launch {
            delay(2.seconds)
            if (state.value.error == error) {
                setState { copy(error = null) }
            }
        }
    }
}
