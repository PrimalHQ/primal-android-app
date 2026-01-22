package net.primal.android.wallet.activation

import android.util.Patterns
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.errors.UiError
import net.primal.android.navigation.promoCode
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.repository.UserRepository
import net.primal.android.wallet.activation.WalletActivationContract.UiEvent
import net.primal.android.wallet.activation.WalletActivationContract.UiState
import net.primal.core.utils.alsoCatching
import net.primal.core.utils.onFailure
import net.primal.core.utils.onSuccess
import net.primal.core.utils.runCatching
import net.primal.core.utils.serialization.decodeFromJsonStringOrNull
import net.primal.domain.account.Country
import net.primal.domain.account.PrimalWalletAccountRepository
import net.primal.domain.account.Region
import net.primal.domain.account.Regions
import net.primal.domain.account.State
import net.primal.domain.account.WalletAccountRepository
import net.primal.domain.account.WalletActivationData
import net.primal.domain.account.WalletActivationParams
import net.primal.domain.account.WalletActivationStatus
import net.primal.domain.account.WalletRegionJson
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.nostr.cryptography.SignatureException

@HiltViewModel
class WalletActivationViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val activeAccountStore: ActiveAccountStore,
    private val walletAccountRepository: WalletAccountRepository,
    private val primalWalletAccountRepository: PrimalWalletAccountRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val promoCode = savedStateHandle.promoCode

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
                    UiEvent.DismissSnackbarError -> setState { copy(uiError = null) }
                }
            }
        }

    private fun loadAllCountries() =
        viewModelScope.launch {
            val allCountries = WalletRegionJson
                .decodeFromJsonStringOrNull<Regions>()
                ?.mapToListOfCountries()

            setState { copy(allCountries = allCountries ?: emptyList()) }
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
                val dateOfBirth = data.dateOfBirth
                val country = data.country
                checkNotNull(dateOfBirth)
                checkNotNull(country)
                primalWalletAccountRepository.requestActivationCodeToEmail(
                    params = WalletActivationParams(
                        userId = userId,
                        firstName = data.firstName,
                        lastName = data.lastName,
                        email = data.email,
                        dateOfBirth = dateOfBirth.formatDateOfBirth(),
                        country = country.code,
                        state = data.state?.code ?: "",
                    ),
                )
                setState { copy(status = WalletActivationStatus.PendingOtpVerification) }
            } catch (error: SignatureException) {
                Napier.w(throwable = error) { "Activation request failed due to signature error." }
                setState { copy(error = error) }
            } catch (error: NetworkException) {
                Napier.w(throwable = error) { "Activation request failed due to network error." }
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
            val userId = activeAccountStore.activeUserId()

            primalWalletAccountRepository.activateWallet(userId, code)
                .alsoCatching { primalWalletAccountRepository.fetchWalletAccountInfo(userId) }
                .onSuccess { response ->
                    walletAccountRepository.setActiveWallet(userId = userId, walletId = userId)
                    setLightningAddress(userId, response.lightningAddress)
                    promoCode?.let { redeemPromoCode(it) }

                    setState {
                        copy(
                            activatedLightningAddress = response.lightningAddress,
                            status = WalletActivationStatus.ActivationSuccess,
                        )
                    }
                }.onFailure { error ->
                    Napier.w(throwable = error) { "Wallet activation failed." }
                    setState { copy(error = error) }
                }
            setState { copy(working = false) }
        }

    private suspend fun redeemPromoCode(promoCode: String) =
        runCatching {
            primalWalletAccountRepository.redeemPromoCode(
                userId = activeAccountStore.activeUserId(),
                code = promoCode,
            )
        }.onFailure { error ->
            Napier.w(throwable = error) { "Failed to redeem promo code." }
            setState { copy(uiError = UiError.InvalidPromoCode(error)) }
        }

    private suspend fun setLightningAddress(userId: String, lightningAddress: String) =
        runCatching {
            userRepository.setLightningAddress(
                userId = userId,
                lightningAddress = lightningAddress,
            )
        }.onFailure { Napier.w(throwable = it) { "Failed to set lightning address." } }
}
