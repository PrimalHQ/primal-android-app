package net.primal.android.premium.legend.become

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.premium.legend.become.PremiumBecomeLegendContract.BecomeLegendStage
import net.primal.android.premium.legend.become.amount.BecomeLegendAmountStage
import net.primal.android.premium.legend.become.intro.BecomeLegendIntroStage
import net.primal.android.premium.legend.become.payment.BecomeLegendPaymentStage
import net.primal.android.premium.legend.become.success.BecomeLegendSuccessStage
import net.primal.android.theme.AppTheme

@Composable
fun PremiumBecomeLegendScreen(
    viewModel: PremiumBecomeLegendViewModel,
    onClose: () -> Unit,
    onLegendPurchased: () -> Unit,
) {
    val state = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> viewModel.setEvent(PremiumBecomeLegendContract.UiEvent.StartPurchaseMonitor)
            Lifecycle.Event.ON_STOP -> viewModel.setEvent(PremiumBecomeLegendContract.UiEvent.StopPurchaseMonitor)
            else -> Unit
        }
    }

    PremiumBecomeLegendScreen(
        state = state.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose,
        onLegendPurchased = onLegendPurchased,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumBecomeLegendScreen(
    state: PremiumBecomeLegendContract.UiState,
    eventPublisher: (PremiumBecomeLegendContract.UiEvent) -> Unit,
    onClose: () -> Unit,
    onLegendPurchased: () -> Unit,
) {
    BecomeLegendBackHandler(
        stage = state.stage,
        eventPublisher = eventPublisher,
        onClose = onClose,
        onLegendPurchased = onLegendPurchased,
    )

    AnimatedContent(
        modifier = Modifier
            .background(AppTheme.colorScheme.surfaceVariant)
            .fillMaxSize(),
        label = "BecomeLegendStages",
        targetState = state.stage,
        transitionSpec = { transitionSpecBetweenStages() },
    ) { stage ->
        when (stage) {
            BecomeLegendStage.Intro -> {
                BecomeLegendIntroStage(
                    modifier = Modifier.fillMaxSize(),
                    onClose = onClose,
                    onNext = { eventPublisher(PremiumBecomeLegendContract.UiEvent.ShowAmountEditor) },
                )
            }

            BecomeLegendStage.PickAmount -> {
                BecomeLegendAmountStage(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    eventPublisher = eventPublisher,
                    onClose = { eventPublisher(PremiumBecomeLegendContract.UiEvent.GoBackToIntro) },
                    onNext = { eventPublisher(PremiumBecomeLegendContract.UiEvent.ShowPaymentInstructions) },
                )
            }

            BecomeLegendStage.Payment -> {
                BecomeLegendPaymentStage(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    onClose = { eventPublisher(PremiumBecomeLegendContract.UiEvent.ShowAmountEditor) },
                )
            }

            BecomeLegendStage.Success -> {
                BecomeLegendSuccessStage(
                    modifier = Modifier.fillMaxSize(),
                    onDoneClick = onLegendPurchased,
                )
            }
        }
    }
}

@Composable
private fun BecomeLegendBackHandler(
    stage: BecomeLegendStage,
    onClose: () -> Unit,
    onLegendPurchased: () -> Unit,
    eventPublisher: (PremiumBecomeLegendContract.UiEvent) -> Unit,
) {
    BackHandler {
        when (stage) {
            BecomeLegendStage.Intro -> onClose()

            BecomeLegendStage.PickAmount -> eventPublisher(PremiumBecomeLegendContract.UiEvent.GoBackToIntro)

            BecomeLegendStage.Payment -> eventPublisher(PremiumBecomeLegendContract.UiEvent.ShowAmountEditor)

            BecomeLegendStage.Success -> onLegendPurchased()
        }
    }
}

private fun AnimatedContentTransitionScope<BecomeLegendStage>.transitionSpecBetweenStages() =
    when (initialState) {
        BecomeLegendStage.Intro -> {
            slideInHorizontally(initialOffsetX = { it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
        }

        BecomeLegendStage.PickAmount -> {
            when (targetState) {
                BecomeLegendStage.Intro -> {
                    slideInHorizontally(initialOffsetX = { -it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
                }

                else -> {
                    slideInHorizontally(initialOffsetX = { it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
                }
            }
        }

        BecomeLegendStage.Payment -> {
            when (targetState) {
                BecomeLegendStage.Success -> {
                    slideInHorizontally(initialOffsetX = { it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
                }

                else -> {
                    slideInHorizontally(initialOffsetX = { -it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
                }
            }
        }

        BecomeLegendStage.Success -> {
            slideInHorizontally(initialOffsetX = { -it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
        }
    }
