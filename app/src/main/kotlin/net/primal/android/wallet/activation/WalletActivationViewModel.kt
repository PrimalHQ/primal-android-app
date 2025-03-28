package net.primal.android.wallet.activation

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.core.coroutines.CoroutineDispatcherProvider
import net.primal.android.networking.relays.errors.NostrPublishException
import net.primal.android.nostr.notary.exceptions.SignException
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.domain.WalletPreference
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.activation.WalletActivationContract.UiEvent
import net.primal.android.wallet.activation.WalletActivationContract.UiState
import net.primal.android.wallet.activation.domain.WalletActivationData
import net.primal.android.wallet.activation.domain.WalletActivationStatus
import net.primal.android.wallet.activation.regions.Country
import net.primal.android.wallet.activation.regions.Region
import net.primal.android.wallet.activation.regions.Regions
import net.primal.android.wallet.activation.regions.State
import net.primal.android.wallet.activation.regions.WalletRegionJson
import net.primal.android.wallet.api.model.GetActivationCodeRequestBody
import net.primal.android.wallet.api.model.WalletActivationDetails
import net.primal.android.wallet.repository.WalletRepository
import net.primal.core.networking.sockets.errors.WssException
import net.primal.core.utils.serialization.CommonJson
import net.primal.domain.nostr.publisher.MissingRelaysException
import timber.log.Timber

@HiltViewModel
class WalletActivationViewModel @Inject constructor(
    private val coroutineDispatcher: CoroutineDispatcherProvider,
    private val activeAccountStore: ActiveAccountStore,
    private val walletRepository: WalletRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(value = UiState())
    val uiState = _uiState.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _uiState.getAndUpdate { it.reducer() }

    private val events = MutableSharedFlow<UiEvent>()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    init {
        subscribeToEvents()
        loadAllCountries()
    }

    private fun subscribeToEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ActivationDataChanged -> {
                        val availableStates = it.data.country.mapToAvailableStates()
                        setState {
                            copy(
                                data = it.data,
                                isDataValid = it.data.isValid(availableStates),
                                availableStates = availableStates,
                                error = null,
                            )
                        }
                    }

                    is UiEvent.OtpCodeChanged -> setState { copy(otpCode = it.code, error = null) }
                    is UiEvent.Activate -> onActivateWallet(code = _uiState.value.otpCode)
                    is UiEvent.ActivationRequest -> onActivationRequest()
                    UiEvent.ClearErrorMessage -> setState { copy(error = null) }
                    UiEvent.RequestBackToDataInput -> setState { copy(status = WalletActivationStatus.PendingData) }
                }
            }
        }

    private fun loadAllCountries() =
        viewModelScope.launch {
            val allCountries = CommonJson.decodeFromString<Regions>(WalletRegionJson).mapToListOfCountries()
            setState { copy(allCountries = allCountries) }
        }

    private fun Regions.mapToListOfCountries(): List<Country> {
        return countries.map { country ->
            val countryName = country[0]
            val countryCode = country[1]
            Country(
                name = countryName,
                code = countryCode,
                states = states.mapNotNull { state ->
                    val stateName = state[0]
                    val stateCode = state[1]
                    if (stateCode.startsWith(countryCode)) {
                        State(name = stateName, code = stateCode)
                    } else {
                        null
                    }
                },
            )
        }
    }

    private fun Region?.mapToAvailableStates(): List<State> {
        return _uiState.value.allCountries.find { it.code == this?.code }?.states ?: emptyList()
    }

    private fun WalletActivationData.isValid(availableStates: List<State>): Boolean {
        return firstName.isNotBlank() && lastName.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
            dateOfBirth != null && country != null && (availableStates.isEmpty() || state != null)
    }

    private fun onActivationRequest() =
        viewModelScope.launch {
            val data = _uiState.value.data
            setState { copy(working = true) }
            try {
                val userId = activeAccountStore.activeUserId()
                checkNotNull(data.dateOfBirth)
                checkNotNull(data.country)
                walletRepository.requestActivationCodeToEmail(
                    userId = userId,
                    body = GetActivationCodeRequestBody(
                        userDetails = WalletActivationDetails(
                            firstName = data.firstName,
                            lastName = data.lastName,
                            email = data.email,
                            dateOfBirth = data.dateOfBirth.formatDateOfBirth(),
                            country = data.country.code,
                            state = data.state?.code ?: "",
                        ),
                    ),
                )
                setState { copy(status = WalletActivationStatus.PendingOtpVerification) }
            } catch (error: SignException) {
                Timber.w(error)
                setState { copy(error = error) }
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = error) }
            } finally {
                setState { copy(working = false) }
            }
        }

    private fun Long.formatDateOfBirth(): String {
        return LocalDate.ofEpochDay(this / Duration.ofDays(1).toMillis())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    private fun onActivateWallet(code: String) =
        viewModelScope.launch {
            setState { copy(working = true) }
            try {
                val userId = activeAccountStore.activeUserId()
                val lightningAddress = withContext(coroutineDispatcher.io()) {
                    val lightningAddress = walletRepository.activateWallet(userId, code)
                    walletRepository.fetchUserWalletInfoAndUpdateUserAccount(userId)
                    lightningAddress
                }

                val activeUser = activeAccountStore.activeUserAccount()
                if (activeUser.nostrWallet == null) {
                    userRepository.updateWalletPreference(userId, WalletPreference.PrimalWallet)
                }
                activeUser.primalWallet?.lightningAddress?.let {
                    try {
                        userRepository.setLightningAddress(userId = userId, lightningAddress = it)
                    } catch (error: SignException) {
                        Timber.w(error)
                    } catch (error: NostrPublishException) {
                        Timber.w(error)
                    } catch (error: WssException) {
                        Timber.w(error)
                    } catch (error: MissingRelaysException) {
                        Timber.w(error)
                    }
                }

                setState {
                    copy(
                        activatedLightningAddress = lightningAddress,
                        status = WalletActivationStatus.ActivationSuccess,
                    )
                }
            } catch (error: WssException) {
                Timber.w(error)
                setState { copy(error = error) }
            } finally {
                setState { copy(working = false) }
            }
        }
}
