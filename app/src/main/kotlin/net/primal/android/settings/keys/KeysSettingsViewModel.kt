package net.primal.android.settings.keys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.premium.legend.domain.asLegendaryCustomization
import net.primal.android.settings.keys.KeysSettingsContract.UiState
import net.primal.android.user.accounts.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp

@HiltViewModel
class KeysSettingsViewModel @Inject constructor(
    private val credentialsStore: CredentialsStore,
    private val activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    init {
        observeActiveAccount()
    }

    private fun observeActiveAccount() =
        viewModelScope.launch {
            activeAccountStore.activeUserAccount.collect {
                setState {
                    val credential = credentialsStore.findOrThrow(it.pubkey.hexToNpubHrp())
                    copy(
                        avatarCdnImage = it.avatarCdnImage,
                        nsec = credential.nsec,
                        npub = credential.npub,
                        legendaryCustomization = it.primalLegendProfile?.asLegendaryCustomization(),
                    )
                }
            }
        }
}
