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
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.premium.primalName.PremiumPrimalNameContract.UiEvent
import net.primal.android.premium.primalName.PremiumPrimalNameContract.UiState
import net.primal.android.premium.repository.PremiumRepository
import timber.log.Timber

@HiltViewModel
class PremiumPrimalNameViewModel @Inject constructor(
    private val premiumRepository: PremiumRepository,
) : ViewModel() {
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
                    UiEvent.ResetNameAvailable -> setState { copy(isNameAvailable = null) }
                }
            }
        }

    private fun checkPrimalName(name: String) =
        viewModelScope.launch {
            try {
                val isNameAvailable = premiumRepository.isPrimalNameAvailable(name = name)
                setState { copy(isNameAvailable = isNameAvailable) }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }
}
