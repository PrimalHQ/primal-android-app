package net.primal.android.auth.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.auth.AuthRepository
import net.primal.android.auth.create.CreateContract.SideEffect
import net.primal.android.auth.create.CreateContract.UiEvent
import net.primal.android.auth.create.CreateContract.UiState
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.nostr.notary.NostrUnsignedEvent
import net.primal.android.settings.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val nostrNotary: NostrNotary
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch {
        _effect.send(effect)
    }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { _event.emit(event) }
    }

    init {
        observeEvents()
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is UiEvent.GoToProfilePreviewStepEvent -> setState { copy(currentStep = 2) }
                is UiEvent.GoToNostrCreatedStepEvent -> nostrCreated()
                is UiEvent.GoBack -> goBack()
                is UiEvent.FinishEvent -> finish()
                is UiEvent.AvatarUriChangedEvent -> setState { copy(avatarUri = it.avatarUri) }
                is UiEvent.BannerUriChangedEvent -> setState { copy(bannerUri = it.bannerUri) }
                is UiEvent.NameChangedEvent -> setState { copy(name = it.name) }
                is UiEvent.HandleChangedEvent -> setState { copy(handle = it.handle) }
                is UiEvent.WebsiteChangedEvent -> setState { copy(website = it.website) }
                is UiEvent.AboutMeChangedEvent -> setState { copy(aboutMe = it.aboutMe) }
            }
        }
    }

    private fun nostrCreated() = viewModelScope.launch {
        setState { copy(loading = true) }
        try {

        } finally {
            setState { copy(loading = false) }
        }

        setState { copy(currentStep = 3) }
    }

    private fun finish() = viewModelScope.launch {

    }

    private fun goBack() = viewModelScope.launch {
        var step = state.value.currentStep - 1
        if (step <= 1) step = 1
        setState { copy(currentStep = step) }
    }
}