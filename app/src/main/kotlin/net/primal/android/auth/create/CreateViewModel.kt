package net.primal.android.auth.create

import android.app.Application
import android.net.Uri
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
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
import net.primal.android.auth.create.api.RecommendedFollowsApi
import net.primal.android.core.api.model.UploadImageRequest
import net.primal.android.crypto.CryptoUtils
import net.primal.android.networking.primal.PrimalCacheFilter
import net.primal.android.networking.primal.PrimalApiClient
import net.primal.android.networking.primal.PrimalVerb
import net.primal.android.networking.relays.RelayPool
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.networking.sockets.errors.WssException
import net.primal.android.nostr.model.NostrEventKind
import net.primal.android.nostr.notary.NostrNotary
import net.primal.android.serialization.NostrJson
import net.primal.android.serialization.nostrJsonSerializer
import net.primal.android.settings.repository.SettingsRepository
import net.primal.android.user.accounts.BOOTSTRAP_RELAYS
import net.primal.android.user.domain.toRelay
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class CreateViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository,
    private val nostrNotary: NostrNotary,
    private val relayPool: RelayPool,
    @Named("Upload") private val primalUploadClient: PrimalApiClient,
    private val recommendedFollowsApi: RecommendedFollowsApi,
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
        _event.collect { event ->
            when (event) {
                is UiEvent.GoToProfilePreviewStepEvent -> setState { copy(currentStep = UiState.CreateAccountStep.PROFILE_PREVIEW) }
                is UiEvent.GoToNostrCreatedStepEvent -> {
                    val keypair = CryptoUtils.generateHexEncodedKeypair()
                    setState { copy(keypair = keypair) }
                    createNostrAccount()
                }

                is UiEvent.GoToFollowContactsStepEvent -> {
                    fetchRecommendedFollows()
                    setState { copy(currentStep = UiState.CreateAccountStep.FOLLOW_RECOMMENDED_ACCOUNTS) }
                }

                is UiEvent.GoBack -> goBack()
                is UiEvent.FinishEvent -> finish()
                is UiEvent.AvatarUriChangedEvent -> setState { copy(avatarUri = event.avatarUri) }
                is UiEvent.BannerUriChangedEvent -> setState { copy(bannerUri = event.bannerUri) }
                is UiEvent.NameChangedEvent -> setState { copy(name = event.name) }
                is UiEvent.HandleChangedEvent -> setState { copy(handle = event.handle) }
                is UiEvent.LightningAddressChangedEvent -> setState { copy(lightningAddress = event.lightningAddress) }
                is UiEvent.Nip05IdentifierChangedEvent -> setState { copy(nip05Identifier = event.nip05Identifier) }
                is UiEvent.WebsiteChangedEvent -> setState { copy(website = event.website) }
                is UiEvent.AboutMeChangedEvent -> setState { copy(aboutMe = event.aboutMe) }
                is UiEvent.ToggleFollowEvent -> toggleFollow(event = event)
                is UiEvent.ToggleGroupFollowEvent -> toggleGroupFollow(event = event)
            }
        }
    }

    private suspend fun createNostrAccount() {
        try {
            var avatarUrl: String? = null
            var bannerUrl: String? = null

            setState { copy(loading = true) }

            if (state.value.avatarUri != null) {
                avatarUrl = uploadImage(state.value.avatarUri!!)
            }

            if (state.value.bannerUri != null) {
                bannerUrl = uploadImage(state.value.bannerUri!!)
            }

            val metadata = state.value.toCreateNostrProfileMetadata(
                resolvedAvatarUrl = avatarUrl, resolvedBannerUrl = bannerUrl
            )
            val metadataNostrEvent = nostrNotary.signMetadataNostrEvent(
                pubkey = state.value.keypair!!.pubkey,
                privkey = state.value.keypair!!.privkey,
                metadata = metadata
            )
            val firstContactEvent =
                nostrNotary.signFirstContactNostrEvent(pubkey = state.value.keypair!!.pubkey,
                    privkey = state.value.keypair!!.privkey,
                    relays = BOOTSTRAP_RELAYS.map { it.toRelay() })

            relayPool.publishEvent(metadataNostrEvent)
            relayPool.publishEvent(firstContactEvent)

            setState { copy(currentStep = UiState.CreateAccountStep.ACCOUNT_CREATED) }
        } catch (e: IOException) {
            setState { copy(error = UiState.CreateError.FailedToUploadImage(e)) }
        } catch (e: NostrPublishException) {
            setState { copy(error = UiState.CreateError.FailedToCreateMetadata(e)) }
        } catch (e: WssException) {
            setState { copy(error = UiState.CreateError.FailedToCreateMetadata(e)) }
        } finally {
            setState { copy(loading = false) }
        }
    }

    private suspend fun fetchRecommendedFollows() {
        try {
            setState { copy(loading = true) }
            val response = recommendedFollowsApi.fetch(state.value.name)

            val result = response.suggestions.map { sg ->
                return@map sg.members.map { Pair(sg.group, it) }
            }.flatten().map {
                return@map RecommendedFollow(
                    pubkey = it.second.pubkey,
                    groupName = it.first,
                    content = NostrJson.decodeFromString(response.metadata[it.second.pubkey]!!.content),
                    isCurrentUserFollowing = false
                )
            }

            setState { copy(recommendedFollows = result) }
        } catch (e: IOException) {
            setState { copy(error = UiState.CreateError.FailedToFetchRecommendedFollows(e)) }
        } finally {
            setState { copy(loading = false) }
        }
    }

    private suspend fun finish() {
        try {
            setState { copy(loading = true) }

            followSelectedAccounts()

            val pubkey = authRepository.login(state.value.keypair!!.privkey)
            settingsRepository.fetchAndPersistAppSettings(userId = pubkey)
            setEffect(SideEffect.AccountCreatedAndPersisted(pubkey = pubkey))
        } catch (e: NostrPublishException) {
            setState { copy(error = UiState.CreateError.FailedToFollow(e)) }
        } finally {
            setState { copy(loading = false) }
        }
    }

    private suspend fun followSelectedAccounts() {
        val contactsEvent =
            nostrNotary.signInitialContactsNostrEvent(privkey = state.value.keypair!!.privkey,
                pubkey = state.value.keypair!!.pubkey,
                contacts = state.value.recommendedFollows.filter { it.isCurrentUserFollowing }
                    .map { it.pubkey }.toSet(),
                relays = BOOTSTRAP_RELAYS.map { it.toRelay() })

        relayPool.publishEvent(contactsEvent)
    }

    private fun goBack() {
        var step = state.value.currentStep.step - 1
        if (step <= 1) step = 1
        setState { copy(currentStep = UiState.CreateAccountStep(step)!!) }
    }

    private fun toggleFollow(event: UiEvent.ToggleFollowEvent) {
        val oldFollow =
            state.value.recommendedFollows.first { it.pubkey == event.pubkey && it.groupName == event.groupName }

        val index = state.value.recommendedFollows.indexOf(oldFollow)

        val newFollow =
            oldFollow.copy(isCurrentUserFollowing = !oldFollow.isCurrentUserFollowing)

        val newFollows = state.value.recommendedFollows.toMutableList()
        newFollows[index] = newFollow

        setState { copy(recommendedFollows = newFollows) }
    }

    private fun toggleGroupFollow(event: UiEvent.ToggleGroupFollowEvent) {
        val newFollows = state.value.recommendedFollows.toMutableList()

        val groupFollowState =
            state.value.recommendedFollows.filter { it.groupName == event.groupName }
                .any { !it.isCurrentUserFollowing }

        for (f in newFollows) {
            if (f.groupName == event.groupName) {
                newFollows[newFollows.indexOf(f)] =
                    f.copy(isCurrentUserFollowing = groupFollowState)
            }
        }

        setState { copy(recommendedFollows = newFollows) }
    }

    private suspend fun uploadImage(uri: Uri): String? {
        val base64AvatarImage = readImageAndConvertToBase64(uri)

        val uploadImageNostrEvent = nostrNotary.signImageUploadNostrEvent(
            privkey = state.value.keypair!!.privkey,
            pubkey = state.value.keypair!!.pubkey,
            base64Image = "data:image/svg+xml;base64,$base64AvatarImage" // yuck
        )

        val queryResult = primalUploadClient.query(
            message = PrimalCacheFilter(
                primalVerb = PrimalVerb.UPLOAD,
                optionsJson = nostrJsonSerializer(shouldEncodeDefaults = true).encodeToString(
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