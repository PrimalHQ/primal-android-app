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
import kotlinx.coroutines.withContext
import net.primal.android.core.errors.asSignatureUiError
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiEvent.DismissErrors
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiEvent.NotificationSettingsChanged
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiState
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiState.ApiError.FetchAppSettingsError
import net.primal.android.settings.notifications.ui.NotificationSwitchUi
import net.primal.android.settings.notifications.ui.mapAsNotificationsPreferences
import net.primal.android.settings.notifications.ui.mapAsPushNotificationSwitchUi
import net.primal.android.settings.notifications.ui.mapAsTabNotificationSwitchUi
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.domain.nostr.cryptography.SignatureException
import net.primal.domain.notifications.NotificationSettingsType
import timber.log.Timber

@HiltViewModel
class NotificationsSettingsViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val settingsRepository: SettingsRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val nostrNotary: NostrNotary,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) {
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
                    DismissErrors -> setState { copy(error = null) }
                    is NotificationSettingsChanged -> {
                        when (event.type) {
                            is NotificationSettingsType.Preferences -> setState {
                                copy(preferencesSettings = this.preferencesSettings.update(event))
                            }

                            is NotificationSettingsType.PushNotifications -> setState {
                                copy(pushNotificationsSettings = this.pushNotificationsSettings.update(event))
                            }

                            is NotificationSettingsType.TabNotifications -> setState {
                                copy(tabNotificationsSettings = this.tabNotificationsSettings.update(event))
                            }
                        }
                    }
                }
            }
        }

    private fun <T : NotificationSettingsType> List<NotificationSwitchUi<T>>.update(
        event: NotificationSettingsChanged,
    ): List<NotificationSwitchUi<T>> {
        return this.toMutableList().apply {
            val existingSetting = first { it.settingsType == event.type }
            val existingSettingIndex = indexOf(existingSetting)
            this[existingSettingIndex] = existingSetting.copy(enabled = event.value)
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeDebouncedNotificationSettingsChanges() =
        viewModelScope.launch {
            events.filterIsInstance<NotificationSettingsChanged>()
                .debounce(1.seconds)
                .collect {
                    // TODO Reimplement updating settings remotely
                    // Trigger updates
//                    updateNotificationsSettings(
//                        notifications = state.value.notificationSwitches,
//                    )
                }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount
                .mapNotNull { it.appSettings }
                .collect { appSettings ->
                    setState {
                        copy(
                            pushNotificationsSettings = appSettings.mapAsPushNotificationSwitchUi(),
                            tabNotificationsSettings = appSettings.mapAsTabNotificationSwitchUi(),
                            preferencesSettings = appSettings.mapAsNotificationsPreferences(),
                        )
                    }
                }
        }

    private fun fetchLatestAppSettings() =
        viewModelScope.launch {
            try {
                val userId = activeAccountStore.activeUserId()
                val authorizationEvent = nostrNotary.signAuthorizationNostrEvent(
                    userId = userId,
                    description = "Sync app settings",
                )

                withContext(dispatcherProvider.io()) {
                    settingsRepository.fetchAndPersistAppSettings(authorizationEvent)
                }
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = FetchAppSettingsError(cause = error)) }
            } catch (error: SignatureException) {
                Timber.w(error)
                setState { copy(signatureError = error.asSignatureUiError()) }
            }
        }

//    private fun updateNotificationsSettings(
//        notifications: List<NotificationSwitchUi>,
//    ) =
//        viewModelScope.launch {
//            val notificationsJsonObject = JsonObject(
//                content = notifications.associate {
//                    it.notificationType.id to JsonPrimitive(value = it.enabled)
//                },
//            )
//
//            try {
//                val userId = activeAccountStore.activeUserId()
//                val authorizationEvent = nostrNotary.signAuthorizationNostrEvent(
//                    userId = userId,
//                    description = "Sync app settings",
//                )
//                settingsRepository.fetchAndUpdateAndPublishAppSettings(authorizationEvent) {
//                    val newAppSettings = this.copy(notifications = notificationsJsonObject)
//                    nostrNotary.signAppSettingsNostrEvent(
//                        userId = userId,
//                        appSettings = newAppSettings,
//                    )
//                }
//            } catch (error: WssException) {
//                setState { copy(error = UpdateAppSettingsError(cause = error)) }
//            }
//        }
}
