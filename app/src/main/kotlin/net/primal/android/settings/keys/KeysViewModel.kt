package net.primal.android.settings.keys

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.primal.android.settings.keys.KeysContract.UiState
import net.primal.android.user.active.ActiveAccountStore
import net.primal.android.user.credentials.CredentialsStore
import javax.inject.Inject

@HiltViewModel
class KeysViewModel @Inject constructor(
    credentialsStore: CredentialsStore,
    activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val credential = credentialsStore.credentials.value.first()

    private val _state = MutableStateFlow(
        UiState(
            avatarUrl = activeAccountStore.activeUserAccount.value.pictureUrl,
            nsec = credential.nsec,
            npub = credential.npub,
        )
    )
    val state = _state.asStateFlow()

}
