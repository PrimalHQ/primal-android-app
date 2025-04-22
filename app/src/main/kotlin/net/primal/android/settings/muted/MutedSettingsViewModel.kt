package net.primal.android.settings.muted

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.asSignatureUiError
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.settings.muted.MutedSettingsContract.UiEvent
import net.primal.android.settings.muted.MutedSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.mutes.MutedItemRepository
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.nostr.publisher.MissingRelaysException
import timber.log.Timber

@HiltViewModel
class MutedSettingsViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val mutedItemRepository: MutedItemRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchLatestMuteList()
        observeMutedUsers()
        observeMutedHashtags()
        observeMutedWords()
        observeEvents()
    }

    private fun observeMutedUsers() =
        viewModelScope.launch {
            mutedItemRepository.observeMutedUsersByOwnerId(ownerId = activeAccountStore.activeUserId())
                .collect {
                    setState { copy(mutedUsers = it.map { it.asProfileDetailsUi() }) }
                }
        }

    private fun observeMutedHashtags() =
        viewModelScope.launch {
            mutedItemRepository.observeMutedHashtagsByOwnerId(ownerId = activeAccountStore.activeUserId())
                .collect {
                    setState { copy(mutedHashtags = it) }
                }
        }

    private fun observeMutedWords() =
        viewModelScope.launch {
            mutedItemRepository.observeMutedWordsByOwnerId(ownerId = activeAccountStore.activeUserId())
                .collect {
                    setState { copy(mutedWords = it) }
                }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.MuteHashtag -> muteHashtag(it.hashtag)
                    is UiEvent.MuteWord -> muteWord(it.word)
                    is UiEvent.UnmuteHashtag -> unmuteHashtag(it.hashtag)
                    is UiEvent.UnmuteUser -> unmuteUser(it)
                    is UiEvent.UnmuteWord -> unmuteWord(it.word)
                    is UiEvent.DismissError -> setState { copy(error = null) }
                    is UiEvent.UpdateNewMutedHashtag -> setState { copy(newMutedHashtag = it.hashtag) }
                    is UiEvent.UpdateNewMutedWord -> setState { copy(newMutedWord = it.word) }
                }
            }
        }

    private fun muteHashtag(hashtag: String) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedItemRepository.muteHashtagAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                        hashtag = hashtag,
                    )
                }
                setState { copy(newMutedHashtag = "") }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = UiError.SignatureError(error.asSignatureUiError())) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToMuteHashtag(error)) }
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = UiError.NetworkError(error)) }
            }
        }

    private fun unmuteHashtag(hashtag: String) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedItemRepository.unmuteHashtagAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                        hashtag = hashtag,
                    )
                }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = UiError.SignatureError(error.asSignatureUiError())) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToUnmuteHashtag(error)) }
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = UiError.NetworkError(error)) }
            }
        }

    private fun muteWord(word: String) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedItemRepository.muteWordAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                        word = word,
                    )
                }
                setState { copy(newMutedWord = "") }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = UiError.SignatureError(error.asSignatureUiError())) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToMuteWord(error)) }
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = UiError.NetworkError(error)) }
            }
        }

    private fun unmuteWord(word: String) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedItemRepository.unmuteWordAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                        word = word,
                    )
                }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = UiError.SignatureError(error.asSignatureUiError())) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToUnmuteWord(error)) }
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = UiError.NetworkError(error)) }
            }
        }

    private fun unmuteUser(event: UiEvent.UnmuteUser) =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedItemRepository.unmuteUserAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                        unmutedUserId = event.pubkey,
                    )
                }
            } catch (error: MissingRelaysException) {
                Timber.w(error)
                setState { copy(error = UiError.MissingRelaysConfiguration(error)) }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(error = UiError.SignatureError(error.asSignatureUiError())) }
            } catch (error: NostrPublishException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToUnmuteUser(error)) }
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = UiError.NetworkError(error)) }
            }
        }

    private fun fetchLatestMuteList() =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    mutedItemRepository.fetchAndPersistMuteList(
                        userId = activeAccountStore.activeUserId(),
                    )
                }
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = UiError.FailedToFetchMuteList(error)) }
            }
        }
}
