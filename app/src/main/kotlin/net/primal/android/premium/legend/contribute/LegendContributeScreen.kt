package net.primal.android.premium.legend.contribute

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import net.primal.android.R
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.premium.legend.contribute.LegendContributeContract.LegendContributeState
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiEvent
import net.primal.android.premium.legend.contribute.LegendContributeContract.UiState
import net.primal.android.premium.legend.contribute.amount.LegendContributeAmountStage
import net.primal.android.premium.legend.contribute.intro.LegendContributeIntroStage
import net.primal.android.premium.legend.contribute.payment.LegendContributePaymentInstructionsStage
import net.primal.android.premium.ui.PaymentSuccess
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.repository.isValidExchangeRate

@Composable
fun LegendContributeScreen(viewModel: LegendContributeViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> viewModel.setEvent(UiEvent.StartPurchaseMonitor)
            Lifecycle.Event.ON_STOP -> viewModel.setEvent(UiEvent.StopPurchaseMonitor)
            else -> Unit
        }
    }

    LegendContributeScreen(
        state = state.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LegendContributeScreen(
    state: UiState,
    eventPublisher: (UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    LegendContributeBackHandler(
        stage = state.stage,
        eventPublisher = eventPublisher,
        onClose = onClose,
    )

    AnimatedContent(
        modifier = Modifier
            .background(AppTheme.colorScheme.surfaceVariant)
            .fillMaxSize(),
        label = "ContributeLegendStages",
        targetState = state.stage,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
    ) { stage ->
        when (stage) {
            LegendContributeState.Intro -> {
                LegendContributeIntroStage(
                    modifier = Modifier.fillMaxSize(),
                    onClose = onClose,
                    onNext = {
                        eventPublisher(UiEvent.ShowAmountEditor(it))
                        eventPublisher(UiEvent.FetchPaymentInstructions)
                    },
                )
            }
            LegendContributeState.PickAmount -> {
                LegendContributeAmountStage(
                    modifier = Modifier.fillMaxSize(),
                    onNext = { eventPublisher(UiEvent.ShowPaymentInstructions) },
                    onBack = { eventPublisher(UiEvent.GoBackToIntro) },
                    state = state,
                    onAmountClick = {
                        if (state.currentExchangeRate.isValidExchangeRate()) {
                            eventPublisher(UiEvent.ChangeCurrencyMode)
                        }
                    },
                    onAmountChanged = {
                        eventPublisher(UiEvent.AmountChanged(amount = it))
                    },
                )
            }
            LegendContributeState.Payment -> {
                LegendContributePaymentInstructionsStage(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onBack = { eventPublisher(UiEvent.GoBackToPickAmount) },
                    onPaymentInstructionRetry = { eventPublisher(UiEvent.FetchPaymentInstructions) },
                )
            }
            LegendContributeState.Success -> {
                PaymentSuccess(
                    modifier = Modifier.fillMaxSize(),
                    title = stringResource(R.string.legend_contribution_success_stage_title),
                    headlineText = stringResource(R.string.legend_contribution_success_stage_appreciation_title),
                    supportText = stringResource(R.string.legend_contribution_success_stage_appreciation_subtitle),
                    buttonText = stringResource(R.string.legend_contribution_success_stage_appreciation_done_button),
                    onDoneClick = onClose,
                )
            }
        }
    }
}

@Composable
private fun LegendContributeBackHandler(
    stage: LegendContributeState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    BackHandler {
        when (stage) {
            LegendContributeState.Intro -> onClose()
            LegendContributeState.PickAmount -> eventPublisher(UiEvent.GoBackToIntro)
            LegendContributeState.Payment -> eventPublisher(UiEvent.GoBackToPickAmount)
            LegendContributeState.Success -> onClose()
        }
    }
}
