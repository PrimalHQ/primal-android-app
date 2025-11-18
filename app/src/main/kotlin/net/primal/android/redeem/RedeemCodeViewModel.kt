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
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.UserAccount
import net.primal.core.networking.sockets.errors.NostrNoticeException
import net.primal.core.utils.CurrencyConversionUtils.toSats
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.PromoCodeDetails
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.utils.extractNoteId
import net.primal.domain.nostr.utils.extractProfileId
import net.primal.domain.nostr.utils.takeAsNaddrOrNull
import net.primal.domain.parser.WalletTextParser
import net.primal.domain.wallet.Wallet
import timber.log.Timber

@HiltViewModel
class RedeemCodeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val walletAccountRepository: WalletAccountRepository,
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
    private val activeAccountStore: ActiveAccountStore,
    private val walletTextParser: WalletTextParser,
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
            processCode(code = preFilledPromoCode)
        }
        observeEvents()
        observeActiveAccount()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.GetCodeDetails -> processCode(it.code)
                    UiEvent.GoToEnterCodeStage ->
                        setState { copy(stageStack = stageStack.pushStage(RedeemCodeStage.EnterCode)) }

                    is UiEvent.ApplyCode -> applyCode(it.code)
                    UiEvent.DismissError -> setState { copy(error = null) }
                    UiEvent.PreviousStage -> setState { copy(stageStack = stageStack.popStage()) }
                    is UiEvent.QrCodeDetected -> processCode(it.result.value)
                }
            }
        }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                val userState = resolveUserState(it)
                setState { copy(userState = userState) }
            }
        }

    private suspend fun resolveUserState(userAccount: UserAccount): RedeemCodeContract.UserState {
        if (userAccount == UserAccount.EMPTY) return RedeemCodeContract.UserState.NoUser
        val wallet = walletAccountRepository.getActiveWallet(userId = userAccount.pubkey)

        return when (wallet) {
            is Wallet.NWC, is Wallet.Tsunami, null -> RedeemCodeContract.UserState.UserWithoutPrimalWallet
            is Wallet.Primal -> RedeemCodeContract.UserState.UserWithPrimalWallet
        }
    }

    private fun processCode(code: String) =
        viewModelScope.launch {
            setState { copy(loading = true, error = null, showErrorBadge = false) }

            val type = QrCodeDataType.from(code)
            when (type) {
                QrCodeDataType.NPUB, QrCodeDataType.NPUB_URI,
                QrCodeDataType.NPROFILE, QrCodeDataType.NPROFILE_URI,
                ->
                    code.extractProfileId()?.let {
                        setEffect(SideEffect.NostrProfileDetected(profileId = it))
                    }

                QrCodeDataType.NOTE, QrCodeDataType.NOTE_URI,
                QrCodeDataType.NEVENT, QrCodeDataType.NEVENT_URI,
                ->
                    code.extractNoteId()?.let {
                        setEffect(SideEffect.NostrNoteDetected(noteId = it))
                    }

                QrCodeDataType.NADDR, QrCodeDataType.NADDR_URI -> {
                    val naddrObject = code.takeAsNaddrOrNull()
                    if (naddrObject != null) {
                        when (naddrObject.kind) {
                            NostrEventKind.LongFormContent.value -> {
                                setEffect(SideEffect.NostrArticleDetected(code))
                            }
                            NostrEventKind.LiveActivity.value -> {
                                setEffect(SideEffect.NostrLiveStreamDetected(code))
                            }
                        }
                        setState { copy(loading = false) }
                    }
                }

                QrCodeDataType.LNBC, QrCodeDataType.LNURL, QrCodeDataType.LIGHTNING_URI,
                QrCodeDataType.BITCOIN_ADDRESS, QrCodeDataType.BITCOIN_URI,
                -> processAsPayment(code)

                QrCodeDataType.NOSTR_CONNECT -> {
                    setEffect(SideEffect.NostrConnectRequest(url = code))
                    setState { copy(loading = false) }
                }

                QrCodeDataType.PROMO_CODE -> {
                    val promoCode = code.getPromoCodeFromUrl()
                    getCodeDetails(promoCode)
                }

                else -> Unit
            }
        }

    private suspend fun processAsPayment(code: String) {
        walletTextParser.parseAndQueryText(userId = activeAccountStore.activeUserId(), text = code)
            .onSuccess {
                setEffect(SideEffect.DraftTransactionReady(draft = it))
                setState { copy(loading = false) }
            }
            .onFailure {
                Timber.w(it)
                setState { copy(loading = false, error = UiError.GenericError()) }
            }
    }

    private fun applyCode(promoCode: String) =
        viewModelScope.launch {
            setState { copy(loading = true, error = null) }
            try {
                primalWalletAccountRepository.redeemPromoCode(
                    userId = activeAccountStore.activeUserId(),
                    code = promoCode,
                )
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
                val response = primalWalletAccountRepository.getPromoCodeDetails(code = code)

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

    private fun PromoCodeDetails.toBenefitsList() =
        listOfNotNull(
            this.preloadedBtc?.toSats()?.let {
                RedeemCodeContract.PromoCodeBenefit.WalletBalance(sats = it)
            },
        )

    private fun List<RedeemCodeStage>.popStage() =
        if (this.size > 1) {
            this.dropLast(1)
        } else {
            this
        }

    private fun List<RedeemCodeStage>.pushStage(stage: RedeemCodeStage) = this + listOf(stage)
}
