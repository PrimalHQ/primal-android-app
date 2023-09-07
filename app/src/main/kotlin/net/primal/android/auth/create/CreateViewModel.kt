package net.primal.android.auth.create

import android.app.Application
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import net.primal.android.auth.AuthRepository
import net.primal.android.auth.create.CreateContract.SideEffect
import net.primal.android.auth.create.CreateContract.UiEvent
import net.primal.android.auth.create.CreateContract.UiState
import net.primal.android.core.api.model.UploadImageRequest
import net.primal.android.crypto.CryptoUtils
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.relays.RelayPool
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.serialization.NostrJson
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject


@HiltViewModel
class CreateViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val nostrNotary: NostrNotary,
    private val relayPool: RelayPool,
    private val primalApiClient: PrimalApiClient,
    private val application: Application
) : AndroidViewModel(application) {
    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private fun setState(reducer: UiState.() -> UiState) {
        _state.getAndUpdate { it.reducer() }
    }

    private val _effect: Channel<SideEffect> = Channel()
    val effect = _effect.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch {
        _effect.send(effect)
    }

    private val _event: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) {
        viewModelScope.launch { _event.emit(event) }
    }

    init {
        observeEvents()
    }

    private fun observeEvents() = viewModelScope.launch {
        _event.collect {
            when (it) {
                is UiEvent.GoToProfilePreviewStepEvent -> setState { copy(currentStep = 2) }
                is UiEvent.GoToNostrCreatedStepEvent -> {
                    val keypair = CryptoUtils.generateHexEncodedKeypair()
                    setState { copy(keypair = keypair) }
                    nostrCreated()
                }

                is UiEvent.GoBack -> goBack()
                is UiEvent.FinishEvent -> finish()
                is UiEvent.AvatarUriChangedEvent -> setState { copy(avatarUri = it.avatarUri) }
                is UiEvent.BannerUriChangedEvent -> setState { copy(bannerUri = it.bannerUri) }
                is UiEvent.NameChangedEvent -> setState { copy(name = it.name) }
                is UiEvent.HandleChangedEvent -> setState { copy(handle = it.handle) }
                is UiEvent.LightningAddressChangedEvent -> setState { copy(lightningAddress = it.lightningAddress) }
                is UiEvent.Nip05IdentifierChangedEvent -> setState { copy(nip05Identifier = it.nip05Identifier) }
                is UiEvent.WebsiteChangedEvent -> setState { copy(website = it.website) }
                is UiEvent.AboutMeChangedEvent -> setState { copy(aboutMe = it.aboutMe) }
            }
        }
    }

    private fun nostrCreated() = viewModelScope.launch {
        setState { copy(loading = true) }
        try {
            val avatarUrl = uploadImage(state.value.avatarUri!!)
            Timber.d(avatarUrl)
            val bannerUrl = uploadImage(state.value.bannerUri!!)
            Timber.d(bannerUrl)
            setState { copy(currentStep = 3) }
        } catch (e: IOException) {

        } finally {
            setState { copy(loading = false) }
        }
    }

    private fun finish() = viewModelScope.launch {

    }

    private fun goBack() = viewModelScope.launch {
        var step = state.value.currentStep - 1
        if (step <= 1) step = 1
        setState { copy(currentStep = step) }
    }

    private suspend fun uploadImage(uri: Uri): String? {
        val base64AvatarImage = readImageAndConvertToBase64(uri)

        val uploadImageNostrEvent = nostrNotary.signImageUploadNostrEvent(
            privkey = state.value.keypair!!.privkey,
            pubkey = state.value.keypair!!.pubkey,
            base64Image = "data:image/svg+xml;base64,$base64AvatarImage"
        )

        val queryResult = primalApiClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.UPLOAD,
                optionsJson = NostrJson.encodeToString(
                    UploadImageRequest(uploadImageEvent = uploadImageNostrEvent)
                )
            )
        )

        val event = queryResult.findPrimalEvent(NostrEventKind.PrimalImageUploadResponse)

        return event?.content
    }

    private suspend fun readImageAndConvertToBase64(path: Uri): String =
        withContext(Dispatchers.IO) {
            val inputStream = application.contentResolver.openInputStream(path)!!

            inputStream.use { stream ->
                val bytes: ByteArray
                val buffer = ByteArray(8192)
                var bytesRead: Int
                val output = ByteArrayOutputStream()

                while (stream.read(buffer).also { bytesRead = it } != -1) {
                    output.write(buffer, 0, bytesRead)
                }

                bytes = output.toByteArray()

                return@withContext Base64.encodeToString(bytes, Base64.DEFAULT)
            }
        }
}