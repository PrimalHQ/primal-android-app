package net.primal.android.discuss.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.discuss.post.NewPostContract.SideEffect
import net.primal.android.discuss.post.NewPostContract.UiEvent
import net.primal.android.discuss.post.NewPostContract.UiState
import net.primal.android.feed.repository.PostRepository
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.user.active.ActiveAccountStore
import net.primal.android.user.active.ActiveUserAccountState
import javax.inject.Inject

@HiltViewModel
class NewPostViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val postRepository: PostRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { _event.emit(event) }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun sendEffect(effect: SideEffect) = viewModelScope.launch { _effect.send(effect) }

    init {
        subscribeToEvents()
        subscribeToActiveAccount()
    }

    private fun subscribeToEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is UiEvent.PublishPost -> publishPost(it)
            }
        }
    }

    private fun subscribeToActiveAccount() = viewModelScope.launch {
        activeAccountStore.activeAccountState
            .filterIsInstance<ActiveUserAccountState.ActiveUserAccount>()
            .collect {
                setState {
                    copy(activeAccountAvatarUrl = it.data.pictureUrl)
                }
            }
    }

    private fun publishPost(event: UiEvent.PublishPost) = viewModelScope.launch {
        setState { copy(publishing = true) }
        try {
            postRepository.publishShortTextNote(content = event.content)
            sendEffect(SideEffect.PostPublished)
        } catch (error: NostrPublishException) {
            setState { copy(error = UiState.PublishError(cause = error.cause)) }
        } finally {
            setState { copy(publishing = false) }
        }
    }

}
