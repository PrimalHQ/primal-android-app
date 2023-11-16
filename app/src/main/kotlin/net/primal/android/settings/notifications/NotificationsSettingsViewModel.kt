package net.primal.android.settings.notifications

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
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonPrimitive
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.notifications.domain.NotificationType
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiEvent.NotificationSettingChanged
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiState.ApiError.FetchAppSettingsError
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiState.ApiError.UpdateAppSettingsError
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class NotificationsSettingsViewModel @Inject constructor(
    val settingsRepository: SettingsRepository,
    val activeAccountStore: ActiveAccountStore,
) : ViewModel() {
    private val _state = MutableStateFlow(NotificationsSettingsContract.UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: NotificationsSettingsContract.UiState.() -> NotificationsSettingsContract.UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val events: MutableSharedFlow<NotificationsSettingsContract.UiEvent> = MutableSharedFlow()
    fun setEvent(event: NotificationsSettingsContract.UiEvent) =
        viewModelScope.launch {
            events.emit(event)
        }

    init {
        fetchLatestAppSettings()
        observeEvents()
        observeActiveAccount()
        observeDebouncedNotificationSettingsChanges()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is NotificationSettingChanged -> setState {
                        copy(
                            notificationSwitches = this.notificationSwitches.toMutableList().apply {
                                val existingSetting = first { it.notificationType == event.type }
                                val existingSettingIndex = indexOf(existingSetting)
                                this[existingSettingIndex] = existingSetting.copy(enabled = event.value)
                            },
                        )
                    }

                    NotificationsSettingsContract.UiEvent.DismissErrors -> setState {
                        copy(
                            error = null,
                        )
                    }
                }
            }
        }

    @OptIn(FlowPreview::class)
    private fun observeDebouncedNotificationSettingsChanges() =
        viewModelScope.launch {
            events.filterIsInstance<NotificationSettingChanged>()
                .debounce(1.seconds)
                .collect {
                    updateNotificationsSettings(
                        notifications = state.value.notificationSwitches,
                    )
                }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount
                .mapNotNull { it.appSettings }
                .collect { appSettings ->
                    val notificationSettings = appSettings.notifications.toMap()
                        .mapNotNull {
                            val type = NotificationType.valueOf(id = it.key)
                            val enabled = it.value.jsonPrimitive.booleanOrNull
                            if (type != null && enabled != null) {
                                NotificationSwitchUi(notificationType = type, enabled = enabled)
                            } else {
                                null
                            }
                        }
                        .sortedBy { it.notificationType.type }

                    setState {
                        copy(notificationSwitches = notificationSettings)
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
                setState { copy(error = FetchAppSettingsError(cause = error)) }
            }
        }

    private suspend fun updateNotificationsSettings(notifications: List<NotificationSwitchUi>) =
        viewModelScope.launch {
            val notificationsJsonObject = JsonObject(
                content = notifications.associate {
                    it.notificationType.id to JsonPrimitive(value = it.enabled)
                },
            )

            try {
                settingsRepository.updateAndPersistNotifications(
                    userId = activeAccountStore.activeUserId(),
                    notifications = notificationsJsonObject,
                )
            } catch (error: WssException) {
                setState { copy(error = UpdateAppSettingsError(cause = error)) }
            }
        }
}
