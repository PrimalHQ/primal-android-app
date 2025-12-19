package net.primal.android.signer.provider

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.signer.provider.SignerContract.SideEffect
import net.primal.android.signer.provider.parser.SignerIntentParser
import net.primal.android.user.credentials.CredentialsStore
import net.primal.core.utils.onSuccess
import net.primal.domain.account.model.LocalSignerMethod
import net.primal.domain.account.service.LocalSignerService
import timber.log.Timber

@HiltViewModel
@OptIn(ExperimentalUuidApi::class)
class SignerViewModel @Inject constructor(
    private val localSignerService: LocalSignerService,
    private val intentParser: SignerIntentParser,
    private val credentialsStore: CredentialsStore,
) : ViewModel() {
    private val methods: MutableSharedFlow<LocalSignerMethod> = MutableSharedFlow()
    private fun setMethod(method: LocalSignerMethod) = viewModelScope.launch { methods.emit(method) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeMethods()
    }

    fun processIntent(intent: Intent, packageName: String?) =
        viewModelScope.launch {
            Timber.tag("LocalSigner").d("Processing intent in ViewModel.")
            intentParser.parse(intent = intent, callingPackage = packageName)
                .onSuccess {
                    Timber.tag("LocalSigner").d("Successful, we got $it.")
                    setMethod(method = it)
                }
                .onFailure { Timber.tag("LocalSigner").d("Failed to parse intent: ${it.message}") }
        }

    private fun observeMethods() =
        viewModelScope.launch {
            methods.collect { method ->
                respondToMethod(method = method)
            }
        }

    private fun respondToMethod(method: LocalSignerMethod) =
        viewModelScope.launch {
            localSignerService.processMethod(method = method)
                .onSuccess { setEffect(SideEffect.RespondToIntent(it)) }
        }
}
