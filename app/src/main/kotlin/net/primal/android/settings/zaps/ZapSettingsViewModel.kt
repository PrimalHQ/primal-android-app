package net.primal.android.settings.zaps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.settings.zaps.ZapSettingsContract.UiEvent
import net.primal.android.settings.zaps.ZapSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.networking.sockets.errors.WssException
import net.primal.domain.ContentZapConfigItem
import net.primal.domain.ContentZapDefault
import timber.log.Timber

@HiltViewModel
class ZapSettingsViewModel @Inject constructor(
    private val dispatcherProvider: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val settingsRepository: SettingsRepository,
    private val nostrNotary: NostrNotary,
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
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.EditZapDefault -> {
                        setState { copy(editPresetIndex = -1) }
                    }

                    is UiEvent.EditZapPreset -> {
                        val index = _state.value.zapConfig.indexOf(it.preset)
                        setState { copy(editPresetIndex = index) }
                    }

                    UiEvent.CloseEditor -> {
                        setState { copy(editPresetIndex = null) }
                    }

                    is UiEvent.UpdateZapPreset -> {
                        updateZapPreset(presetIndex = it.index, zapPreset = it.zapPreset)
                    }

                    is UiEvent.UpdateZapDefault -> {
                        updateDefaultZapAmount(newZapDefault = it.newZapDefault)
                    }
                }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount
                .mapNotNull { it.appSettings }
                .collect {
                    setState {
                        copy(zapDefault = it.zapDefault, zapConfig = it.zapsConfig)
                    }
                }
        }

    private fun fetchLatestAppSettings() =
        viewModelScope.launch {
            try {
                withContext(dispatcherProvider.io()) {
                    val userId = activeAccountStore.activeUserId()
                    val authorizationEvent = nostrNotary.signAuthorizationNostrEvent(
                        userId = userId,
                        description = "Sync app settings",
                    )
                    settingsRepository.fetchAndPersistAppSettings(authorizationEvent)
                }
            } catch (error: WssException) {
                Timber.w(error)
            }
        }

    private fun updateDefaultZapAmount(newZapDefault: ContentZapDefault) =
        viewModelScope.launch {
            setState { copy(saving = true) }
            try {
                val userId = activeAccountStore.activeUserId()
                val authorizationEvent = nostrNotary.signAuthorizationNostrEvent(
                    userId = userId,
                    description = "Sync app settings",
                )
                settingsRepository.fetchAndUpdateAndPublishAppSettings(authorizationEvent) {
                    val newAppSettings = copy(zapDefault = newZapDefault)
                    nostrNotary.signAppSettingsNostrEvent(
                        userId = userId,
                        appSettings = newAppSettings,
                    )
                }
                setState { copy(editPresetIndex = null) }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(saving = false) }
            }
        }

    private fun updateZapPreset(presetIndex: Int, zapPreset: ContentZapConfigItem) =
        viewModelScope.launch {
            setState { copy(saving = true) }
            try {
                val userId = activeAccountStore.activeUserId()
                val authorizationEvent = nostrNotary.signAuthorizationNostrEvent(
                    userId = userId,
                    description = "Sync app settings",
                )

                settingsRepository.fetchAndUpdateAndPublishAppSettings(authorizationEvent) {
                    val newAppSettings = this.copy(
                        zapsConfig = this.zapsConfig.toMutableList().apply {
                            this[presetIndex] = zapPreset
                        },
                    )
                    nostrNotary.signAppSettingsNostrEvent(
                        userId = userId,
                        appSettings = newAppSettings,
                    )
                }
                setState { copy(editPresetIndex = null) }
            } catch (error: WssException) {
                Timber.w(error)
            } finally {
                setState { copy(saving = false) }
            }
        }
}
