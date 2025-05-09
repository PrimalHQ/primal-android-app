package net.primal.android.redeem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.navigation.promoCode
import net.primal.android.redeem.RedeemCodeContract.RedeemCodeStage
import net.primal.android.redeem.RedeemCodeContract.SideEffect
import net.primal.android.redeem.RedeemCodeContract.UiEvent
import net.primal.android.redeem.RedeemCodeContract.UiState
import net.primal.android.redeem.utils.getPromoCodeFromUrl
import net.primal.android.scanner.domain.QrCodeDataType
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.UserAccount
import net.primal.android.wallet.api.model.PromoCodeDetailsResponse
import net.primal.android.wallet.repository.WalletRepository
import net.primal.android.wallet.zaps.hasPrimalWallet
import net.primal.core.networking.sockets.errors.NostrNoticeException
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.domain.common.exception.NetworkException
import timber.log.Timber

@HiltViewModel
class RedeemCodeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val walletRepository: WalletRepository,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val preFilledPromoCode = savedStateHandle.promoCode

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        if (preFilledPromoCode != null) {
            setState {
                copy(
                    promoCode = preFilledPromoCode,
                    stageStack = listOf(RedeemCodeStage.EnterCode),
                )
            }

            getCodeDetails(code = preFilledPromoCode)
        }
        observeEvents()
        observeActiveAccount()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.GetCodeDetails -> getCodeDetails(it.code)

                    UiEvent.GoToEnterCodeStage ->
                        setState { copy(stageStack = stageStack.pushStage(RedeemCodeStage.EnterCode)) }

                    is UiEvent.ApplyCode -> applyCode(it.code)
                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.PreviousStage -> setState { copy(stageStack = stageStack.popStage()) }

                    is UiEvent.QrCodeDetected -> qrCodeDetected(it.result)
                }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                val userState = when {
                    it == UserAccount.EMPTY -> RedeemCodeContract.UserState.NoUser

                    it.hasPrimalWallet() -> RedeemCodeContract.UserState.UserWithPrimalWallet

                    else -> RedeemCodeContract.UserState.UserWithoutPrimalWallet
                }

                setState { copy(userState = userState) }
            }
        }

    private fun qrCodeDetected(result: QrCodeResult) =
        viewModelScope.launch {
            when (result.type) {
                QrCodeDataType.PROMO_CODE -> {
                    val promoCode = result.value.getPromoCodeFromUrl()

                    getCodeDetails(
                        code = promoCode,
                        onFailure = {
                            setState {
                                copy(
                                    promoCode = promoCode,
                                    stageStack = stageStack.pushStage(RedeemCodeStage.EnterCode),
                                )
                            }
                        },
                    )
                }

                else -> Unit
            }
        }

    private fun applyCode(promoCode: String) =
        viewModelScope.launch {
            setState { copy(loading = true, error = null) }
            try {
                walletRepository.redeemPromoCode(userId = activeAccountStore.activeUserId(), code = promoCode)
                setEffect(SideEffect.PromoCodeApplied)
            } catch (error: NetworkException) {
                Timber.w(error)

                val uiError = if (error.cause is NostrNoticeException) {
                    UiError.InvalidPromoCode(error)
                } else {
                    UiError.NetworkError(error)
                }

                setState { copy(error = uiError) }
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun getCodeDetails(code: String, onFailure: (() -> Unit)? = null) =
        viewModelScope.launch {
            setState { copy(loading = true, error = null, showErrorBadge = false) }
            try {
                val response = walletRepository.getPromoCodeDetails(code = code)

                setState {
                    copy(
                        promoCode = code,
                        welcomeMessage = response.welcomeMessage,
                        promoCodeBenefits = response.toBenefitsList(),
                        requiresPrimalWallet = response.preloadedBtc != null,
                        stageStack = listOf(RedeemCodeStage.Success),
                    )
                }
            } catch (error: NetworkException) {
                onFailure?.invoke()
                Timber.w(error)
                if (error.cause is NostrNoticeException) {
                    setState { copy(showErrorBadge = true) }
                } else {
                    setState { copy(error = UiError.NetworkError(error)) }
                }
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun PromoCodeDetailsResponse.toBenefitsList() =
        listOfNotNull(
            this.preloadedBtc?.toSats()?.let { RedeemCodeContract.PromoCodeBenefit.WalletBalance(sats = it) },
        )

    private fun List<RedeemCodeStage>.popStage() =
        if (this.size > 1) {
            this.dropLast(1)
        } else {
            this
        }

    private fun List<RedeemCodeStage>.pushStage(stage: RedeemCodeStage) = this + listOf(stage)
}
