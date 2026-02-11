package net.primal.android.wallet.upgrade.sheet

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.upgrade.sheet.UpgradeWalletSheetContract.UiEvent
import net.primal.android.wallet.upgrade.sheet.UpgradeWalletSheetContract.UiState
import net.primal.core.utils.onSuccess
import net.primal.domain.account.PrimalWalletAccountRepository

@HiltViewModel
class UpgradeWalletSheetViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
) : ViewModel() {

    companion object {
        private val INITIAL_DELAY = 10.seconds
        private val AWAY_THRESHOLD = 5.minutes
    }

    private var backgroundedAtMillis: Long? = null
    private var showDelayJob: Job? = null

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val lifecycleObserver = LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                backgroundedAtMillis = System.currentTimeMillis()
                showDelayJob?.cancel()
            }

            Lifecycle.Event.ON_START -> {
                val backgroundedAt = backgroundedAtMillis
                backgroundedAtMillis = null
                if (backgroundedAt != null) {
                    val elapsedMillis = System.currentTimeMillis() - backgroundedAt
                    if (elapsedMillis >= AWAY_THRESHOLD.inWholeMilliseconds) {
                        scheduleNoticeIfEligible()
                    }
                }
            }

            else -> Unit
        }
    }

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleObserver)
        observeActiveAccount()
        observeActiveAccountId()
        observeEvents()
    }

    override fun onCleared() {
        super.onCleared()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(lifecycleObserver)
        showDelayJob?.cancel()
    }

    private fun scheduleNoticeIfEligible() {
        val currentState = _state.value
        if (currentState.shouldUserUpgrade && !currentState.shouldShowNotice) {
            showDelayJob?.cancel()
            showDelayJob = viewModelScope.launch {
                delay(INITIAL_DELAY)
                setState { copy(shouldShowNotice = true) }
            }
        }
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect { userAccount ->
                setState {
                    copy(
                        activeUserCdnImage = userAccount.avatarCdnImage,
                        activeUserLegendaryCustomization = userAccount.primalLegendProfile?.asLegendaryCustomization(),
                    )
                }
            }
        }

    private fun observeActiveAccountId() =
        viewModelScope.launch {
            activeAccountStore.activeUserId.collect { userId ->
                primalWalletAccountRepository.fetchWalletStatus(userId = userId)
                    .onSuccess {
                        val shouldUpgrade =
                            userId.isNotEmpty() &&
                                it.hasCustodialWallet &&
                                !it.hasMigratedToSparkWallet &&
                                !it.primalWalletDeprecated

                        setState { copy(shouldUserUpgrade = shouldUpgrade) }

                        if (shouldUpgrade) {
                            scheduleNoticeIfEligible()
                        }
                    }
            }
        }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    UiEvent.DismissSheet -> {
                        setState { copy(shouldShowNotice = false) }
                        showDelayJob?.cancel()
                    }
                }
            }
        }
}
