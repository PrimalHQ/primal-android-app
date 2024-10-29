package net.primal.android.premium

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import net.primal.android.premium.ui.PremiumHomeStage
import net.primal.android.theme.AppTheme

@Composable
fun PremiumHomeScreen(viewModel: PremiumHomeViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    PremiumHomeScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
    )
}

@Composable
private fun PremiumHomeScreen(
    state: PremiumHomeContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (PremiumHomeContract.UiEvent) -> Unit,
) {
    AnimatedContent(
        modifier = Modifier
            .background(AppTheme.colorScheme.background)
            .fillMaxSize(),
        label = "PremiumStages",
        targetState = state.stage,
        transitionSpec = { transitionSpecBetweenStages() },
    ) { stage ->
        when (stage) {
            PremiumHomeContract.PremiumStage.Home -> {
                PremiumHomeStage(
                    onClose = onClose,
                    onFindPrimalName = {
                        eventPublisher(
                            PremiumHomeContract.UiEvent.MoveToPremiumStage(
                                PremiumHomeContract.PremiumStage.FindPrimalName,
                            ),
                        )
                    },
                )
            }

            PremiumHomeContract.PremiumStage.FindPrimalName -> {
                Text(text = "find primal name", color = AppTheme.colorScheme.onBackground)
            }

            PremiumHomeContract.PremiumStage.Purchase -> {
                Text(text = "purchase", color = AppTheme.colorScheme.onBackground)
            }
        }
    }
}

private fun AnimatedContentTransitionScope<PremiumHomeContract.PremiumStage>.transitionSpecBetweenStages() =
    when (initialState) {
        PremiumHomeContract.PremiumStage.Home -> {
            slideInHorizontally(initialOffsetX = { it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
        }

        PremiumHomeContract.PremiumStage.FindPrimalName -> {
            when (targetState) {
                PremiumHomeContract.PremiumStage.Home -> {
                    slideInHorizontally(initialOffsetX = { -it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
                }

                PremiumHomeContract.PremiumStage.FindPrimalName, PremiumHomeContract.PremiumStage.Purchase -> {
                    slideInHorizontally(initialOffsetX = { it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
                }
            }
        }

        PremiumHomeContract.PremiumStage.Purchase -> {
            slideInHorizontally(initialOffsetX = { -it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
        }
    }
