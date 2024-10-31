package net.primal.android.premium.primalName

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.premium.primalName.PremiumPrimalNameContract.UiState
import net.primal.android.premium.primalName.PremiumPrimalNameContract.UiEvent

@HiltViewModel
class PremiumPrimalNameViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }


    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.CheckPrimalName -> checkPrimalName(it.name)
                }
            }
        }

    private fun checkPrimalName(name: String) =
        viewModelScope.launch {
            setState { copy(isNameAvailable = false) }
        }
}
