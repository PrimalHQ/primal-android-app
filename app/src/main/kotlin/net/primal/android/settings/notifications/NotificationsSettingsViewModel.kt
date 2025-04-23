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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import net.primal.android.core.errors.asSignatureUiError
import net.primal.android.core.push.PushNotificationsTokenUpdater
import net.primal.android.networking.UserAgentProvider
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiEvent.DismissErrors
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiEvent.NotificationSettingsChanged
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiState
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiState.ApiError.FetchAppSettingsError
import net.primal.android.settings.notifications.NotificationsSettingsContract.UiState.ApiError.UpdateAppSettingsError
import net.primal.android.settings.notifications.ui.NotificationSwitchUi
import net.primal.android.settings.notifications.ui.mapAsNotificationsPreferences
import net.primal.android.settings.notifications.ui.mapAsPushNotificationSwitchUi
import net.primal.android.settings.notifications.ui.mapAsTabNotificationSwitchUi
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.UserAccountsStore
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.coroutines.DispatcherProvider
import net.primal.core.utils.serialization.encodeToJsonString
import net.primal.data.remote.api.settings.model.AppSettingsDescription
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.NostrUnsignedEvent
import net.primal.domain.nostr.asIdentifierTag
import net.primal.domain.nostr.cryptography.SignResult
import net.primal.domain.nostr.cryptography.utils.unwrapOrThrow
import net.primal.domain.notifications.NotificationSettingsType
import net.primal.domain.notifications.NotificationSettingsType.Preferences
import net.primal.domain.notifications.NotificationSettingsType.PushNotifications
import net.primal.domain.notifications.NotificationSettingsType.TabNotifications
import timber.log.Timber

@HiltViewModel
class NotificationsSettingsViewModel @Inject constructor(
    private val dispatcherProvider: DispatcherProvider,
    private val settingsRepository: SettingsRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val pushNotificationsTokenUpdater: PushNotificationsTokenUpdater,
    private val accountsStore: UserAccountsStore,
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
                            is Preferences -> setState {
                                copy(preferencesSettings = this.preferencesSettings.update(event))
                            }

                            is PushNotifications -> setState {
                                copy(pushNotificationsSettings = this.pushNotificationsSettings.update(event))
                            }

                            is TabNotifications -> setState {
                                copy(tabNotificationsSettings = this.tabNotificationsSettings.update(event))
                            }
                        }
                    }

                    is NotificationsSettingsContract.UiEvent.PushNotificationsToggled ->
                        updatePushNotificationsEnabled(event.value)
                }
            }
        }

    private fun updatePushNotificationsEnabled(value: Boolean) =
        viewModelScope.launch {
            withContext(dispatcherProvider.io()) {
                accountsStore.getAndUpdateAccount(activeAccountStore.activeUserId()) {
                    copy(pushNotificationsEnabled = value)
                }

                pushNotificationsTokenUpdater.updateTokenForAllUsers()
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
                    updateNotificationsSettings(
                        tabNotificationsSettings = state.value.tabNotificationsSettings,
                        pushNotificationsSettings = state.value.pushNotificationsSettings,
                        preferencesSettings = state.value.preferencesSettings,
                    )
                }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount
                .collect { activeAccount ->
                    setState {
                        copy(
                            pushNotificationsEnabled = activeAccount.pushNotificationsEnabled,
                            pushNotificationsSettings = activeAccount.appSettings?.mapAsPushNotificationSwitchUi()
                                ?: emptyList(),
                            tabNotificationsSettings = activeAccount.appSettings?.mapAsTabNotificationSwitchUi()
                                ?: emptyList(),
                            preferencesSettings = activeAccount.appSettings?.mapAsNotificationsPreferences()
                                ?: emptyList(),
                        )
                    }
                }
        }

    private fun fetchLatestAppSettings() =
        viewModelScope.launch {
            try {
                val userId = activeAccountStore.activeUserId()
                val signResult = nostrNotary.signNostrEvent(
                    unsignedNostrEvent = NostrUnsignedEvent(
                        pubKey = userId,
                        kind = NostrEventKind.ApplicationSpecificData.value,
                        tags = listOf("${UserAgentProvider.APP_NAME} App".asIdentifierTag()),
                        content = AppSettingsDescription(description = "Sync app settings").encodeToJsonString(),
                    ),
                )

                when (signResult) {
                    is SignResult.Rejected -> {
                        Timber.w(signResult.error)
                        setState { copy(signatureError = signResult.error.asSignatureUiError()) }
                    }

                    is SignResult.Signed -> {
                        withContext(dispatcherProvider.io()) {
                            settingsRepository.fetchAndPersistAppSettings(signResult.event)
                        }
                    }
                }
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(error = FetchAppSettingsError(cause = error)) }
            }
        }

    private fun updateNotificationsSettings(
        tabNotificationsSettings: List<NotificationSwitchUi<TabNotifications>>,
        pushNotificationsSettings: List<NotificationSwitchUi<PushNotifications>>,
        preferencesSettings: List<NotificationSwitchUi<Preferences>>,
    ) = viewModelScope.launch {
        val tabNotificationsJsonObject = tabNotificationsSettings.mapToRemoteTabNotificationsJsonObject()
        val pushNotificationsJsonObject = pushNotificationsSettings.mapToRemoteJsonObject()
        val preferencesJsonObject = preferencesSettings.mapToRemoteJsonObject()

        try {
            val userId = activeAccountStore.activeUserId()
            val signResult = nostrNotary.signAuthorizationNostrEvent(
                userId = userId,
                description = "Sync app settings",
            )

            when (signResult) {
                is SignResult.Rejected -> {
                    setState { copy(error = UpdateAppSettingsError(cause = signResult.error)) }
                }

                is SignResult.Signed -> {
                    settingsRepository.fetchAndUpdateAndPublishAppSettings(signResult.event) {
                        val newAppSettings = this.copy(
                            notifications = tabNotificationsJsonObject,
                            pushNotifications = pushNotificationsJsonObject,
                            notificationsAdditional = preferencesJsonObject,
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
        } catch (error: NetworkException) {
            setState { copy(error = UpdateAppSettingsError(cause = error)) }
        }
    }

    private fun List<NotificationSwitchUi<TabNotifications>>.mapToRemoteTabNotificationsJsonObject(): JsonObject {
        return JsonObject(
            content = this.flatMap { switchUi ->
                switchUi.settingsType.types.map { it.id to JsonPrimitive(switchUi.enabled) }
            }.toMap(),
        )
    }

    private fun <T : NotificationSettingsType> List<NotificationSwitchUi<T>>.mapToRemoteJsonObject(): JsonObject {
        return JsonObject(
            content = this.associate {
                it.settingsType.id to JsonPrimitive(value = it.enabled)
            },
        )
    }
}
