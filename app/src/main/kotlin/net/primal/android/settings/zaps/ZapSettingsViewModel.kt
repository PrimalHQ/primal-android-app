package net.primal.android.settings.zaps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.settings.zaps.ZapSettingsContract.UiEvent
import net.primal.android.settings.zaps.ZapSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class ZapSettingsViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        fetchLatestAppSettings()
        observeEvents()
        observeActiveAccount()
        observeDebouncedZapOptionChanges()
        observeDebouncedZapDefaultAmountChanges()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ZapDefaultAmountChanged -> setState {
                        copy(
                            defaultZapAmount = it.newAmount,
                        )
                    }
                    is UiEvent.ZapOptionsChanged -> setState { copy(zapOptions = it.newOptions) }
                }
            }
        }

    @OptIn(FlowPreview::class)
    private fun observeDebouncedZapOptionChanges() =
        viewModelScope.launch {
            events.filterIsInstance<UiEvent.ZapOptionsChanged>()
                .debounce(1.seconds)
                .mapNotNull { it.newOptions.toListOfULongsOrNull() }
                .collect {
                    updateZapOptions(newZapOptions = it)
                }
        }

    private fun List<ULong?>.toListOfULongsOrNull(): List<ULong>? {
        return if (this.contains(null)) null else mapNotNull { it }
    }

    @OptIn(FlowPreview::class)
    private fun observeDebouncedZapDefaultAmountChanges() =
        viewModelScope.launch {
            events.filterIsInstance<UiEvent.ZapDefaultAmountChanged>()
                .debounce(1.seconds)
                .mapNotNull { it.newAmount }
                .collect {
                    updateDefaultZapAmount(newDefaultAmount = it)
                }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount
                .mapNotNull { it.appSettings }
                .collect {
                    setState {
                        copy(
                            defaultZapAmount = it.defaultZapAmount ?: 42.toULong(),
                            zapOptions = if (it.zapOptions.size == PRESETS_COUNT) {
                                it.zapOptions
                            } else {
                                List(PRESETS_COUNT) { null }
                            },
                        )
                    }
                }
        }

    private fun fetchLatestAppSettings() =
        viewModelScope.launch {
            try {
                settingsRepository.fetchAndPersistAppSettings(
                    userId = activeAccountStore.activeUserId(),
                )
            } catch (error: WssException) {
                // Ignore
            }
        }

    private suspend fun updateDefaultZapAmount(newDefaultAmount: ULong) {
        try {
            val userAccount = activeAccountStore.activeUserAccount()
            settingsRepository.updateAndPersistDefaultZapAmount(
                userId = userAccount.pubkey,
                defaultAmount = newDefaultAmount,
            )
        } catch (error: WssException) {
            // Something went wrong
        }
    }

    private suspend fun updateZapOptions(newZapOptions: List<ULong>) {
        try {
            val userAccount = activeAccountStore.activeUserAccount()
            settingsRepository.updateAndPersistZapOptions(
                userId = userAccount.pubkey,
                zapOptions = newZapOptions,
            )
        } catch (error: WssException) {
            // Something went wrong
        }
    }
}
