package net.primal.android.scan

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.navigation.scanMode
import net.primal.android.scan.ScanCodeContract.ScanCodeStage
import net.primal.android.scan.ScanCodeContract.SideEffect
import net.primal.android.scan.ScanCodeContract.UiEvent
import net.primal.android.scan.ScanCodeContract.UiState
import net.primal.android.scanner.domain.QrCodeDataType
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.utils.extractNoteId
import net.primal.domain.nostr.utils.extractProfileId
import net.primal.domain.nostr.utils.takeAsNaddrOrNull
import net.primal.domain.parser.WalletTextParser

@HiltViewModel
class ScanCodeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val walletTextParser: WalletTextParser,
) : ViewModel() {

    private val scanMode = savedStateHandle.scanMode

    private val _state = MutableStateFlow(UiState(scanMode = scanMode))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val qrCodeEvents = MutableSharedFlow<String>()

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        observeQrCodeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ProcessCode -> processCode(it.value)
                    UiEvent.GoToManualInput ->
                        setState { copy(stageStack = stageStack.pushStage(ScanCodeStage.ManualInput)) }

                    UiEvent.DismissError -> setState { copy(error = null, showErrorBadge = false) }
                    UiEvent.PreviousStage -> setState { copy(stageStack = stageStack.popStage()) }
                    is UiEvent.QrCodeDetected -> viewModelScope.launch { qrCodeEvents.emit(it.result.value) }
                }
            }
        }

    private fun observeQrCodeEvents() =
        viewModelScope.launch {
            qrCodeEvents.distinctUntilChanged().collect { processCode(it) }
        }

    private fun processCode(code: String) =
        viewModelScope.launch {
            setState { copy(loading = true, error = null, showErrorBadge = false) }
            try {
                when (state.value.scanMode) {
                    ScanCodeContract.ScanMode.RemoteLogin -> processCodeForRemoteLogin(code)
                    ScanCodeContract.ScanMode.Anything -> processCodeForAnything(code)
                }
            } finally {
                setState { copy(loading = false) }
            }
        }

    private fun processCodeForRemoteLogin(code: String) {
        val type = QrCodeDataType.from(code)
        if (type == QrCodeDataType.NOSTR_CONNECT) {
            setEffect(SideEffect.NostrConnectRequest(url = code))
        } else {
            setState {
                copy(showErrorBadge = true, error = UiError.GenericError())
            }
        }
    }

    private suspend fun processCodeForAnything(code: String) {
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

            QrCodeDataType.NADDR, QrCodeDataType.NADDR_URI -> processNaddr(code)

            QrCodeDataType.LNBC, QrCodeDataType.LNURL, QrCodeDataType.LIGHTNING_URI,
            QrCodeDataType.BITCOIN_ADDRESS, QrCodeDataType.BITCOIN_URI,
            -> processAsPayment(code)

            QrCodeDataType.NOSTR_CONNECT -> {
                setEffect(SideEffect.NostrConnectRequest(url = code))
            }

            else -> setState { copy(showErrorBadge = true) }
        }
    }

    private fun processNaddr(code: String) {
        val naddrObject = code.takeAsNaddrOrNull() ?: return
        when (naddrObject.kind) {
            NostrEventKind.LongFormContent.value -> {
                setEffect(SideEffect.NostrArticleDetected(code))
            }

            NostrEventKind.LiveActivity.value -> {
                setEffect(SideEffect.NostrLiveStreamDetected(code))
            }
        }
    }

    private suspend fun processAsPayment(code: String) {
        walletTextParser.parseAndQueryText(userId = activeAccountStore.activeUserId(), text = code)
            .onSuccess {
                setEffect(SideEffect.DraftTransactionReady(draft = it))
            }
            .onFailure {
                Napier.w(throwable = it) { "Failed to process payment code: $code" }
                setState { copy(error = UiError.GenericError()) }
            }
    }

    private fun List<ScanCodeStage>.popStage() =
        if (this.size > 1) {
            this.dropLast(1)
        } else {
            this
        }

    private fun List<ScanCodeStage>.pushStage(stage: ScanCodeStage) = this + listOf(stage)
}
