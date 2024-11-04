package net.primal.android.premium.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.premium.primalName.PremiumPrimalNameStage
import net.primal.android.premium.purchase.PremiumPurchaseStage
import net.primal.android.premium.ui.PremiumHomeStage
import net.primal.android.theme.AppTheme

@Composable
fun PremiumHomeScreen(
    viewModel: PremiumHomeViewModel,
    onClose: () -> Unit,
    onMoreInfoClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    PremiumHomeScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
        onMoreInfoClick = onMoreInfoClick,
    )
}

@Composable
private fun PremiumHomeScreen(
    state: PremiumHomeContract.UiState,
    onClose: () -> Unit,
    onMoreInfoClick: () -> Unit,
    eventPublisher: (PremiumHomeContract.UiEvent) -> Unit,
) {
    BackHandler {
        when (state.stage) {
            PremiumHomeContract.PremiumStage.Home -> onClose()
            PremiumHomeContract.PremiumStage.FindPrimalName -> eventPublisher(
                PremiumHomeContract.UiEvent.MoveToPremiumStage(
                    PremiumHomeContract.PremiumStage.Home,
                ),
            )

            PremiumHomeContract.PremiumStage.Purchase -> eventPublisher(
                PremiumHomeContract.UiEvent.MoveToPremiumStage(
                    PremiumHomeContract.PremiumStage.FindPrimalName,
                ),
            )
        }
    }
    AnimatedContent(
        modifier = Modifier
            .background(AppTheme.colorScheme.surfaceVariant)
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
                    onLearnMoreClick = onMoreInfoClick,
                )
            }

            PremiumHomeContract.PremiumStage.FindPrimalName -> {
                PremiumPrimalNameStage(
                    titleText = stringResource(id = R.string.premium_primal_name_title),
                    initialName = state.primalName,
                    onBack = {
                        eventPublisher(
                            PremiumHomeContract.UiEvent.MoveToPremiumStage(
                                PremiumHomeContract.PremiumStage.Home,
                            ),
                        )
                    },
                    onPrimalNameAvailable = {
                        eventPublisher(
                            PremiumHomeContract.UiEvent.SetPrimalName(primalName = it)
                        )
                        eventPublisher(
                            PremiumHomeContract.UiEvent.MoveToPremiumStage(
                                PremiumHomeContract.PremiumStage.Purchase,
                            ),
                        )
                    },
                )
            }

            PremiumHomeContract.PremiumStage.Purchase -> {
                state.primalName?.let {
                    PremiumPurchaseStage(
                        primalName = it,
                        onBack = {
                            eventPublisher(
                                PremiumHomeContract.UiEvent.MoveToPremiumStage(
                                    PremiumHomeContract.PremiumStage.FindPrimalName,
                                ),
                            )
                        },
                        onLearnMoreClick = onMoreInfoClick,
                    )
                }
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
