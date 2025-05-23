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
import net.primal.android.core.compose.attachment.model.asEventUriUiModel
import net.primal.android.core.utils.usernameUiFriendly
import net.primal.android.messages.conversation.MessageConversationListContract.UiEvent
import net.primal.android.messages.conversation.MessageConversationListContract.UiState
import net.primal.android.messages.conversation.model.MessageConversationUi
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.notes.feed.model.asNoteNostrUriUi
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.subscriptions.SubscriptionsManager
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.messages.ChatRepository
import net.primal.domain.messages.ConversationRelation
import net.primal.domain.messages.DMConversation
import net.primal.domain.nostr.cryptography.SignResult
import timber.log.Timber

@HiltViewModel
class MessageConversationListViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val subscriptionsManager: SubscriptionsManager,
    private val chatRepository: ChatRepository,
    private val nostrNotary: NostrNotary,
) : ViewModel() {

    private val _state = MutableStateFlow(
        value = UiState(
            activeRelation = ConversationRelation.Follows,
            conversations = chatRepository
                .newestConversations(
                    userId = activeAccountStore.activeUserId(),
                    relation = ConversationRelation.Follows,
                )
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
                val userId = activeAccountStore.activeUserId()
                when (state.value.activeRelation) {
                    ConversationRelation.Follows -> {
                        chatRepository.fetchFollowConversations(userId = userId)
                        chatRepository.fetchNonFollowsConversations(userId = userId)
                    }

                    ConversationRelation.Other -> {
                        chatRepository.fetchNonFollowsConversations(userId = userId)
                        chatRepository.fetchFollowConversations(userId = userId)
                    }
                }
            } catch (error: NetworkException) {
                Timber.w(error)
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun changeRelation(relation: ConversationRelation) {
        setState {
            copy(
                activeRelation = relation,
                conversations = chatRepository
                    .newestConversations(userId = activeAccountStore.activeUserId(), relation = relation)
                    .mapAsPagingDataOfMessageConversationUi(),
            )
        }
    }

    private fun markAllConversationAsRead() =
        viewModelScope.launch {
            try {
                val signResult = nostrNotary.signAuthorizationNostrEvent(
                    userId = activeAccountStore.activeUserId(),
                    description = "Mark all messages as read.",
                )

                when (signResult) {
                    is SignResult.Rejected -> Timber.w(signResult.error)
                    is SignResult.Signed ->
                        chatRepository.markAllMessagesAsRead(authorization = signResult.event)
                }
            } catch (error: NetworkException) {
                Timber.w(error)
            }
        }

    private fun Flow<PagingData<DMConversation>>.mapAsPagingDataOfMessageConversationUi() =
        map { pagingData -> pagingData.map { it.mapAsMessageConversationUi() } }

    private fun DMConversation.mapAsMessageConversationUi() =
        MessageConversationUi(
            participantId = this.participant.profileId,
            participantUsername = this.participant.usernameUiFriendly(),
            lastMessageId = this.lastMessage?.messageId,
            lastMessageSnippet = this.lastMessage?.content,
            lastMessageAttachments = this.lastMessage?.links?.map { it.asEventUriUiModel() } ?: emptyList(),
            lastMessageNostrUris = this.lastMessage?.nostrUris?.map { it.asNoteNostrUriUi() } ?: emptyList(),
            lastMessageAt = this.lastMessage?.createdAt?.let { Instant.ofEpochSecond(it) },
            isLastMessageFromUser = this.lastMessage?.senderId == activeAccountStore.activeUserId(),
            participantInternetIdentifier = this.participant.internetIdentifier,
            participantAvatarCdnImage = this.participant.avatarCdnImage,
            participantLegendaryCustomization = this.participant.primalPremiumInfo
                ?.legendProfile?.asLegendaryCustomization(),
            unreadMessagesCount = this.unreadMessagesCount,
        )
}
