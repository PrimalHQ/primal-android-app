package net.primal.android.wallet.notice.sheet

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.UserAccount
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.notice.sheet.WalletNoticeSheetContract.UiEvent
import net.primal.android.wallet.notice.sheet.WalletNoticeSheetContract.UiState
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.PrimalWalletStatus
import net.primal.domain.account.SparkWalletAccountRepository
import net.primal.domain.usecase.EnsureSparkWalletExistsUseCase

@HiltViewModel
class WalletNoticeSheetViewModel @Inject constructor(
    private val activeAccountStore: ActiveAccountStore,
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
    private val sparkWalletAccountRepository: SparkWalletAccountRepository,
    private val userRepository: UserRepository,
    private val ensureSparkWalletExistsUseCase: EnsureSparkWalletExistsUseCase,
) : ViewModel() {

    companion object {
        private val INITIAL_DELAY = 1.seconds
        private val AWAY_THRESHOLD = 5.minutes
        private val WALLET_CREATION_TIMEOUT = 30.seconds
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
                        refreshNoticeFromServer()
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

    private fun refreshNoticeFromServer() {
        viewModelScope.launch {
            val userId = activeAccountStore.activeUserId()
            if (userId.isEmpty()) return@launch
            fetchAndUpdateNoticeType(userId)
        }
    }

    private suspend fun fetchAndUpdateNoticeType(userId: String) {
        primalWalletAccountRepository.fetchWalletStatus(userId = userId)
            .onSuccess { status ->
                val userAccount = activeAccountStore.activeUserAccount()
                val noticeType = resolveNoticeType(
                    userId = userId,
                    status = status,
                    userAccount = userAccount,
                )
                setState { copy(noticeType = noticeType, shouldShowNotice = false) }
                if (noticeType != null) {
                    scheduleNoticeIfEligible()
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

                fetchAndUpdateNoticeType(userId)
            }
        }

    private suspend fun resolveNoticeType(
        userId: String,
        status: PrimalWalletStatus,
        userAccount: UserAccount,
    ): WalletNoticeType? {
        return when {
            status.hasCustodialWallet && !status.hasMigratedToSparkWallet && !status.primalWalletDeprecated ->
                WalletNoticeType.UpgradeWallet

            status.hasCustodialWallet && !status.hasMigratedToSparkWallet && status.primalWalletDeprecated ->
                if (userAccount.shouldShowWalletDiscontinuedNotice) {
                    WalletNoticeType.WalletDiscontinued
                } else {
                    null
                }

            status.hasMigratedToSparkWallet && !localSparkWalletExists(userId) ->
                if (userAccount.shouldShowWalletDetectedNotice) {
                    WalletNoticeType.WalletDetected
                } else {
                    null
                }

            else -> null
        }
    }

    private suspend fun localSparkWalletExists(userId: String): Boolean {
        return sparkWalletAccountRepository.hasPersistedSparkWallet(userId)
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    UiEvent.DismissSheet -> handleDismiss()
                    UiEvent.CreateWallet -> handleCreateWallet()
                    UiEvent.DismissError -> setState { copy(error = null) }
                }
            }
        }

    private fun handleCreateWallet() {
        setState { copy(creatingWallet = true, error = null) }
        viewModelScope.launch {
            try {
                withTimeout(WALLET_CREATION_TIMEOUT) {
                    ensureSparkWalletExistsUseCase.invoke(userId = activeAccountStore.activeUserId())
                }.onSuccess {
                    handleDismiss()
                }.onFailure { error ->
                    Napier.e(throwable = error) { "Failed to create wallet from notice sheet." }
                    setState { copy(creatingWallet = false, error = UiState.WalletCreationError.Failed(error)) }
                }
            } catch (e: TimeoutCancellationException) {
                Napier.e(throwable = e) { "Wallet creation timed out." }
                setState { copy(creatingWallet = false, error = UiState.WalletCreationError.Failed(e)) }
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
                viewModelScope.launch { userRepository.dismissWalletDiscontinuedNotice(userId) }
                setState { copy(noticeType = null, shouldShowNotice = false, creatingWallet = false) }
            }
            WalletNoticeType.WalletDetected -> {
                viewModelScope.launch { userRepository.dismissWalletDetectedNotice(userId) }
                setState { copy(noticeType = null, shouldShowNotice = false, creatingWallet = false) }
            }
            null -> Unit
        }

        showDelayJob?.cancel()
    }
}
