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
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.messages.conversation.MessageConversationListContract.UiEvent
import net.primal.android.messages.conversation.MessageConversationListContract.UiState
import net.primal.android.messages.conversation.model.MessageConversationUi
import net.primal.android.messages.db.MessageConversation
import net.primal.android.messages.domain.ConversationRelation
import net.primal.android.messages.repository.MessageRepository
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.notes.feed.model.asNoteNostrUriUi
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager
import timber.log.Timber

@HiltViewModel
class MessageConversationListViewModel @Inject constructor(
    activeAccountStore: ActiveAccountStore,
    private val subscriptionsManager: SubscriptionsManager,
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
                    UiEvent.RefreshConversations -> fetchConversations()
                }
            }
        }

    private fun subscribeToTotalUnreadCountChanges() =
        viewModelScope.launch {
            subscriptionsManager.badges
                .map { it.unreadMessagesCount }
                .distinctUntilChanged()
                .collect {
                    fetchConversations()
                }
        }

    private fun fetchConversations() =
        viewModelScope.launch {
            setState { copy(loading = true) }
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
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
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
            lastMessageId = this.lastMessage?.messageId,
            lastMessageSnippet = this.lastMessage?.content,
            lastMessageAttachments = this.lastMessageNoteAttachments.map { it.asNoteAttachmentUi() },
            lastMessageNostrUris = this.lastMessageNostrUris.map { it.asNoteNostrUriUi() },
            lastMessageAt = this.lastMessage?.createdAt?.let { Instant.ofEpochSecond(it) },
            isLastMessageFromUser = this.lastMessage?.senderId == activeUserId,
            participantInternetIdentifier = this.participant?.internetIdentifier,
            participantAvatarCdnImage = this.participant?.avatarCdnImage,
            unreadMessagesCount = this.data.unreadMessagesCount,
        )
}
