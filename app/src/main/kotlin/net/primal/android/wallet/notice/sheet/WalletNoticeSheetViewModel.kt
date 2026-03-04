package net.primal.android.wallet.notice.sheet

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
import net.primal.android.core.di.AppNoticePreferences
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.wallet.notice.sheet.WalletNoticeSheetContract.UiEvent
import net.primal.android.wallet.notice.sheet.WalletNoticeSheetContract.UiState
import net.primal.core.utils.onSuccess
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.PrimalWalletStatus
import net.primal.domain.account.SparkWalletAccountRepository

@HiltViewModel
class WalletNoticeSheetViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
    private val appNoticePreferences: AppNoticePreferences,
) : ViewModel() {

    companion object {
        private val INITIAL_DELAY = 1.seconds
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
        if (currentState.noticeType != null && !currentState.shouldShowNotice) {
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
                if (userId.isEmpty()) return@collect

                setState { copy(noticeType = null, shouldShowNotice = false) }
                showDelayJob?.cancel()

                primalWalletAccountRepository.fetchWalletStatus(userId = userId)
                    .onSuccess { status ->
                        val noticeType = resolveNoticeType(userId = userId, status = status)
                        setState { copy(noticeType = noticeType) }

                        if (noticeType != null) {
                            scheduleNoticeIfEligible()
                        }
                    }
            }
        }

    private suspend fun resolveNoticeType(userId: String, status: PrimalWalletStatus): WalletNoticeType? {
        return when {
            status.hasCustodialWallet && !status.hasMigratedToSparkWallet && !status.primalWalletDeprecated ->
                WalletNoticeType.UpgradeWallet

            status.hasCustodialWallet && !status.hasMigratedToSparkWallet && status.primalWalletDeprecated ->
                if (!appNoticePreferences.isNoticeDismissed(userId, AppNoticePreferences.NOTICE_WALLET_DISCONTINUED)) {
                    WalletNoticeType.WalletDiscontinued
                } else {
                    null
                }

            status.hasMigratedToSparkWallet && !localSparkWalletExists(userId) ->
                if (!appNoticePreferences.isNoticeDismissed(userId, AppNoticePreferences.NOTICE_WALLET_DETECTED)) {
                    WalletNoticeType.WalletDetected
                } else {
                    null
                }

            else -> null
        }
    }

    private suspend fun localSparkWalletExists(userId: String): Boolean {
        return sparkWalletAccountRepository.findPersistedWalletId(userId) != null
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    UiEvent.DismissSheet -> handleDismiss()
                }
            }
        }

    private fun handleDismiss() {
        val currentState = _state.value
        val userId = activeAccountStore.activeUserId.value

        when (currentState.noticeType) {
            WalletNoticeType.UpgradeWallet -> {
                setState { copy(shouldShowNotice = false) }
            }
            WalletNoticeType.WalletDiscontinued -> {
                appNoticePreferences.setNoticeDismissed(userId, AppNoticePreferences.NOTICE_WALLET_DISCONTINUED)
                setState { copy(noticeType = null, shouldShowNotice = false) }
            }
            WalletNoticeType.WalletDetected -> {
                appNoticePreferences.setNoticeDismissed(userId, AppNoticePreferences.NOTICE_WALLET_DETECTED)
                setState { copy(noticeType = null, shouldShowNotice = false) }
            }
            null -> Unit
        }

        showDelayJob?.cancel()
    }
}
