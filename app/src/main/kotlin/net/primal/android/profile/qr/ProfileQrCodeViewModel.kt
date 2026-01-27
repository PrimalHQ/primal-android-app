package net.primal.android.profile.qr

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
import net.primal.android.core.compose.profile.model.asProfileDetailsUi
import net.primal.android.navigation.profileId
import net.primal.android.profile.qr.ProfileQrCodeContract.SideEffect
import net.primal.android.profile.qr.ProfileQrCodeContract.UiEvent
import net.primal.android.profile.qr.ProfileQrCodeContract.UiState
import net.primal.android.scan.utils.getPromoCodeFromUrl
import net.primal.android.scanner.domain.QrCodeDataType
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.domain.nostr.NostrEventKind
import net.primal.domain.nostr.cryptography.utils.bech32ToHexOrNull
import net.primal.domain.nostr.utils.extractNoteId
import net.primal.domain.nostr.utils.extractProfileId
import net.primal.domain.nostr.utils.takeAsNaddrOrNull
import net.primal.domain.nostr.utils.takeAsNaddrStringOrNull
import net.primal.domain.parser.WalletTextParser
import net.primal.domain.profile.ProfileRepository

@HiltViewModel
class ProfileQrCodeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val profileRepository: ProfileRepository,
    private val walletTextParser: WalletTextParser,
) : ViewModel() {

    private val profileId: String = savedStateHandle.profileId ?: activeAccountStore.activeUserId()

    private val _state = MutableStateFlow(UiState(profileId = profileId))
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate(reducer)

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        observeProfileData()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ProcessQrCodeResult -> processQrCodeResult(result = it.result)
                }
            }
        }

    private fun observeProfileData() =
        viewModelScope.launch {
            profileRepository.observeProfileData(profileId = profileId)
                .distinctUntilChanged()
                .collect { profileData ->
                    setState { copy(profileDetails = profileData.asProfileDetailsUi()) }
                }
        }

    @Suppress("CyclomaticComplexMethod")
    private fun processQrCodeResult(result: QrCodeResult) =
        viewModelScope.launch {
            when (result.type) {
                QrCodeDataType.NPUB -> result.value.bech32ToHexOrNull()?.let { processProfileId(profileId = it) }

                QrCodeDataType.NPUB_URI,
                QrCodeDataType.NPROFILE_URI,
                QrCodeDataType.NPROFILE,
                -> result.value.extractProfileId()?.let { processProfileId(profileId = it) }

                QrCodeDataType.NADDR -> processNaddr(naddr = result.value)
                QrCodeDataType.NADDR_URI -> result.value.takeAsNaddrStringOrNull()?.let { processNaddr(naddr = it) }

                QrCodeDataType.NEVENT -> result.value.bech32ToHexOrNull()?.let { processNoteId(noteId = it) }
                QrCodeDataType.NEVENT_URI -> result.value.extractNoteId()?.let { processNoteId(noteId = it) }
                QrCodeDataType.NOTE -> result.value.bech32ToHexOrNull()?.let { processNoteId(noteId = it) }
                QrCodeDataType.NOTE_URI -> result.value.extractNoteId()?.let { processNoteId(noteId = it) }

                QrCodeDataType.LNBC,
                QrCodeDataType.LNURL,
                QrCodeDataType.LIGHTNING_URI,
                QrCodeDataType.BITCOIN_URI,
                QrCodeDataType.BITCOIN_ADDRESS,
                -> processWalletText(text = result.value)

                QrCodeDataType.PROMO_CODE ->
                    setEffect(SideEffect.PromoCodeDetected(result.value.getPromoCodeFromUrl()))

                else -> Unit
            }
        }

    private suspend fun processWalletText(text: String) {
        walletTextParser.parseAndQueryText(
            userId = activeAccountStore.activeUserId(),
            text = text,
        ).onFailure { error ->
            Napier.w(throwable = error) { "Unable to parse text. [text = $text]" }
        }.onSuccess { draftTx ->
            setEffect(SideEffect.WalletTxDetected(draftTx = draftTx))
        }
    }

    private fun processProfileId(profileId: String) {
        setEffect(SideEffect.NostrProfileDetected(profileId = profileId))
    }

    private fun processNoteId(noteId: String) {
        setEffect(SideEffect.NostrNoteDetected(noteId = noteId))
    }

    private fun processNaddr(naddr: String) {
        val naddrObject = naddr.takeAsNaddrOrNull()
        when (naddrObject?.kind) {
            NostrEventKind.LiveActivity.value -> {
                setEffect(SideEffect.NostrLiveStreamDetected(naddr = naddr))
            }
            NostrEventKind.LongFormContent.value -> {
                setEffect(SideEffect.NostrArticleDetected(naddr = naddr))
            }
            else -> {
                // Ignore unsupported naddr kinds
            }
        }
    }
}
