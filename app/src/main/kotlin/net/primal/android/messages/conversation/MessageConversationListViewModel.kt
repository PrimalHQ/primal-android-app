package net.primal.android.messages.conversation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.core.compose.attachment.model.asNoteAttachmentUi
import net.primal.android.core.compose.feed.model.asNoteNostrUriUi
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.messages.conversation.MessageConversationListContract.UiEvent
import net.primal.android.messages.conversation.MessageConversationListContract.UiState
import net.primal.android.messages.conversation.model.MessageConversationUi
import net.primal.android.messages.db.MessageConversation
import net.primal.android.messages.domain.ConversationRelation
import net.primal.android.messages.repository.MessageRepository
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.badges.BadgesManager
import timber.log.Timber

@HiltViewModel
class MessageConversationListViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val badgesManager: BadgesManager,
    private val messageRepository: MessageRepository,
) : ViewModel() {

    private val activeUserId = activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(
        value = UiState(
            activeRelation = ConversationRelation.Follows,
            conversations = messageRepository
                .newestConversations(ConversationRelation.Follows)
                .mapAsPagingDataOfMessageConversationUi(),
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
        subscribeToActiveAccount()
        subscribeToBadgesUpdates()
        subscribeToTotalUnreadCountChanges()
        fetchConversations()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ChangeRelation -> changeRelation(relation = it.relation)
                    UiEvent.MarkAllConversationsAsRead -> markAllConversationAsRead()
                    UiEvent.ConversationsSeen -> fetchConversations()
                }
            }
        }

    private fun subscribeToActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    copy(activeAccountAvatarCdnImage = it.avatarCdnImage)
                }
            }
        }

    private fun subscribeToBadgesUpdates() =
        viewModelScope.launch {
            badgesManager.badges.collect {
                setState {
                    copy(badges = it)
                }
            }
        }

    private fun subscribeToTotalUnreadCountChanges() =
        viewModelScope.launch {
            badgesManager.badges
                .map { it.messages }
                .distinctUntilChanged()
                .collect {
                    fetchConversations()
                }
        }

    private fun fetchConversations() =
        viewModelScope.launch {
            try {
                when (state.value.activeRelation) {
                    ConversationRelation.Follows -> {
                        messageRepository.fetchFollowConversations()
                        messageRepository.fetchNonFollowsConversations()
                    }

                    ConversationRelation.Other -> {
                        messageRepository.fetchNonFollowsConversations()
                        messageRepository.fetchFollowConversations()
                    }
                }
            } catch (error: WssException) {
                Timber.e(error)
            }
        }

    private fun changeRelation(relation: ConversationRelation) {
        setState {
            copy(
                activeRelation = relation,
                conversations = messageRepository
                    .newestConversations(relation = relation)
                    .mapAsPagingDataOfMessageConversationUi(),
            )
        }
    }

    private fun markAllConversationAsRead() =
        viewModelScope.launch {
            try {
                messageRepository.markAllMessagesAsRead(userId = activeUserId)
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun Flow<PagingData<MessageConversation>>.mapAsPagingDataOfMessageConversationUi() =
        map { pagingData -> pagingData.map { it.mapAsMessageConversationUi() } }

    private fun MessageConversation.mapAsMessageConversationUi() =
        MessageConversationUi(
            participantId = this.data.participantId,
            participantUsername = this.participant?.usernameUiFriendly()
                ?: this.data.participantId.asEllipsizedNpub(),
            lastMessageId = this.lastMessage.messageId,
            lastMessageSnippet = this.lastMessage.content,
            lastMessageAttachments = this.lastMessageNoteAttachments.map { it.asNoteAttachmentUi() },
            lastMessageNostrUris = this.lastMessageNostrUris.map { it.asNoteNostrUriUi() },
            lastMessageAt = Instant.ofEpochSecond(this.lastMessage.createdAt),
            isLastMessageFromUser = this.lastMessage.senderId == activeUserId,
            participantInternetIdentifier = this.participant?.internetIdentifier,
            participantAvatarCdnImage = this.participant?.avatarCdnImage,
            unreadMessagesCount = this.data.unreadMessagesCount,
        )
}
