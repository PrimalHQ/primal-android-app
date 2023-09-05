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
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.auth.create.CreateContract.UiState
import net.primal.android.auth.create.CreateContract.UiEvent
import net.primal.android.auth.create.CreateContract.SideEffect
import net.primal.android.auth.create.CreateContract.StepState
import net.primal.android.profile.db.ProfileMetadata
import javax.inject.Inject

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
): ViewModel() {
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

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is UiEvent.MetadataCreateEvent -> createMetadata(profileMetadata = it.profileMetadata)
                is UiEvent.FollowEvent -> follow(pubkeys = it.followedPubkeys)
            }
        }
    }

    private fun createMetadata(profileMetadata: ProfileMetadata) = viewModelScope.launch {

    }

    private fun follow(pubkeys: Set<String>) = viewModelScope.launch {

    }
}