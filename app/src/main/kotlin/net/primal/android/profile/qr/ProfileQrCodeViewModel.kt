package net.primal.android.profile.qr

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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
import net.primal.android.crypto.bech32ToHexOrThrow
import net.primal.android.navigation.profileId
import net.primal.android.nostr.ext.extractNoteId
import net.primal.android.nostr.ext.extractProfileId
import net.primal.android.nostr.notary.MissingPrivateKeyException
import net.primal.android.profile.qr.ProfileQrCodeContract.SideEffect
import net.primal.android.profile.qr.ProfileQrCodeContract.UiEvent
import net.primal.android.profile.qr.ProfileQrCodeContract.UiState
import net.primal.android.profile.repository.ProfileRepository
import net.primal.android.scanner.analysis.WalletTextParser
import net.primal.android.scanner.domain.QrCodeDataType
import net.primal.android.scanner.domain.QrCodeResult
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.core.networking.sockets.errors.WssException
import timber.log.Timber

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

    private fun processQrCodeResult(result: QrCodeResult) =
        viewModelScope.launch {
            when (result.type) {
                QrCodeDataType.NPUB -> processProfileId(profileId = result.value.bech32ToHexOrThrow())

                QrCodeDataType.NPUB_URI,
                QrCodeDataType.NPROFILE_URI,
                QrCodeDataType.NPROFILE,
                -> result.value.extractProfileId()?.let { processProfileId(profileId = it) }

                QrCodeDataType.NOTE -> processNoteId(noteId = result.value.bech32ToHexOrThrow())
                QrCodeDataType.NOTE_URI -> result.value.extractNoteId()?.let { processNoteId(noteId = it) }

                QrCodeDataType.LNBC,
                QrCodeDataType.LNURL,
                QrCodeDataType.LIGHTNING_URI,
                QrCodeDataType.BITCOIN_URI,
                QrCodeDataType.BITCOIN_ADDRESS,
                -> processWalletText(text = result.value)

                else -> Unit
            }
        }

    private suspend fun processWalletText(text: String) {
        try {
            val draftTx = walletTextParser.parseAndQueryText(
                userId = activeAccountStore.activeUserId(),
                text = text,
            )
            if (draftTx != null) {
                setEffect(SideEffect.WalletTxDetected(draftTx = draftTx))
            } else {
                Timber.w("Unable to parse text. [text = $text]")
            }
        } catch (error: MissingPrivateKeyException) {
            Timber.w(error)
        } catch (error: WssException) {
            Timber.w(error)
        }
    }

    private fun processProfileId(profileId: String) {
        setEffect(SideEffect.NostrProfileDetected(profileId = profileId))
    }

    private fun processNoteId(noteId: String) {
        setEffect(SideEffect.NostrNoteDetected(noteId = noteId))
    }
}
