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
import net.primal.android.core.errors.asSignatureUiError
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.settings.zaps.ZapSettingsContract.UiEvent
import net.primal.android.settings.zaps.ZapSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignResult
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.domain.notifications.ContentZapConfigItem
import net.primal.domain.notifications.ContentZapDefault
import timber.log.Timber

@HiltViewModel
class ZapSettingsViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
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
                    nostrNotary.signAuthorizationNostrEvent(
                        userId = userId,
                        description = "Sync app settings",
                    ).let { signResult ->
                        when (signResult) {
                            is SignResult.Rejected -> {
                                Timber.w(signResult.error)
                                setState { copy(signatureError = signResult.error.asSignatureUiError()) }
                            }

                            is SignResult.Signed -> settingsRepository.fetchAndPersistAppSettings(signResult.event)
                        }
                    }
                }
            } catch (error: NetworkException) {
                Timber.w(error)
            }
        }

    private fun updateDefaultZapAmount(newZapDefault: ContentZapDefault) =
        viewModelScope.launch {
            setState { copy(saving = true) }
            try {
                val userId = activeAccountStore.activeUserId()
                val signResult = nostrNotary.signAuthorizationNostrEvent(
                    userId = userId,
                    description = "Sync app settings",
                )

                when (signResult) {
                    is SignResult.Rejected -> {
                        setState { copy(signatureError = signResult.error.asSignatureUiError()) }
                    }

                    is SignResult.Signed -> {
                        settingsRepository.fetchAndUpdateAndPublishAppSettings(signResult.event) {
                            val newAppSettings = copy(zapDefault = newZapDefault)

                            nostrNotary.signAppSettingsNostrEvent(
                                userId = userId,
                                appSettings = newAppSettings,
                            ).unwrapOrThrow { error ->
                                setState { copy(signatureError = error.asSignatureUiError()) }
                            }
                        }
                    }
                }

                setState { copy(editPresetIndex = null) }
            } catch (error: NetworkException) {
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
                val signResult = nostrNotary.signAuthorizationNostrEvent(
                    userId = userId,
                    description = "Sync app settings",
                )

                when (signResult) {
                    is SignResult.Rejected -> {
                        setState { copy(signatureError = signResult.error.asSignatureUiError()) }
                    }

                    is SignResult.Signed -> {
                        settingsRepository.fetchAndUpdateAndPublishAppSettings(signResult.event) {
                            val newAppSettings = this.copy(
                                zapsConfig = this.zapsConfig.toMutableList().apply {
                                    this[presetIndex] = zapPreset
                                },
                            )
                            nostrNotary.signAppSettingsNostrEvent(
                                userId = userId,
                                appSettings = newAppSettings,
                            ).unwrapOrThrow { error ->
                                setState { copy(signatureError = error.asSignatureUiError()) }
                            }
                        }
                    }
                }

                setState { copy(editPresetIndex = null) }
            } catch (error: NetworkException) {
                Timber.w(error)
            } finally {
                setState { copy(saving = false) }
            }
        }
}
