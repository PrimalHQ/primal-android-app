package net.primal.android.signer.provider.approvals

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.signer.provider.approvals.PermissionRequestsContract.SideEffect
import net.primal.android.signer.provider.approvals.PermissionRequestsContract.UiEvent
import net.primal.android.signer.provider.approvals.PermissionRequestsContract.UiState
import net.primal.android.signer.provider.parser.SignerIntentParser
import net.primal.domain.account.model.SessionEventUserChoice
import net.primal.domain.account.model.UserChoice
import net.primal.domain.account.service.LocalSignerService
import timber.log.Timber

@HiltViewModel
@OptIn(ExperimentalUuidApi::class)
class PermissionRequestsViewModel @Inject constructor(
    private val localSignerService: LocalSignerService,
    private val intentParser: SignerIntentParser,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
        observeSessionEventsPendingUserAction()
    }

    fun onNewIntent(intent: Intent, packageName: String?) =
        viewModelScope.launch {
            intentParser.parse(intent = intent, callingPackage = packageName)
                .onSuccess {
                    localSignerService.processMethod(method = it)
                }
                .onFailure {
                    Timber.tag("LocalSigner").d("Failed to parse intent: ${it.message}")
                }
        }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect {
                when (it) {
                    UiEvent.ApproveSelectedMethods -> approveSelectedMethods()
                }
            }
        }
    }

    private fun observeSessionEventsPendingUserAction() {
        viewModelScope.launch {
            localSignerService.observeSessionEventsPendingUserAction().collect { events ->
                setState { copy(requestQueue = events) }
                Timber.tag("LocalSigner").i(events.toString())
            }
        }
    }

    private fun approveSelectedMethods() =
        viewModelScope.launch {
            val selectedMethods = _state.value.requestQueue
            localSignerService.respondToUserActions(
                eventChoices = selectedMethods.map {
                    SessionEventUserChoice(
                        sessionEventId = it.eventId,
                        userChoice = UserChoice.Allow,
                    )
                },
            )
            setEffect(
                SideEffect.ApprovalSuccess(
                    approvedMethods = localSignerService.getMethodResponses(),
                ),
            )
        }
}
