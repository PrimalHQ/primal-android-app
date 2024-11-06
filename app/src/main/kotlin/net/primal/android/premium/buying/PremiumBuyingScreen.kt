package net.primal.android.premium.buying

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.premium.buying.name.PremiumPrimalNameStage
import net.primal.android.premium.buying.purchase.PremiumPurchaseStage
import net.primal.android.premium.ui.PremiumHomeStage
import net.primal.android.theme.AppTheme

@Composable
fun PremiumBuyingScreen(
    viewModel: PremiumBuyingViewModel,
    onClose: () -> Unit,
    onMoreInfoClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    PremiumBuyingScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
        onMoreInfoClick = onMoreInfoClick,
    )
}

@Composable
private fun PremiumBuyingScreen(
    state: PremiumBuyingContract.UiState,
    onClose: () -> Unit,
    onMoreInfoClick: () -> Unit,
    eventPublisher: (PremiumBuyingContract.UiEvent) -> Unit,
) {
    BackHandler {
        when (state.stage) {
            PremiumBuyingContract.PremiumStage.Home,
            PremiumBuyingContract.PremiumStage.Success,
            -> onClose()

            PremiumBuyingContract.PremiumStage.FindPrimalName -> eventPublisher(
                PremiumBuyingContract.UiEvent.MoveToPremiumStage(
                    PremiumBuyingContract.PremiumStage.Home,
                ),
            )

            PremiumBuyingContract.PremiumStage.Purchase -> eventPublisher(
                PremiumBuyingContract.UiEvent.MoveToPremiumStage(
                    PremiumBuyingContract.PremiumStage.FindPrimalName,
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
            PremiumBuyingContract.PremiumStage.Home -> {
                PremiumHomeStage(
                    subscriptions = state.subscriptions,
                    onClose = onClose,
                    onFindPrimalName = {
                        eventPublisher(
                            PremiumBuyingContract.UiEvent.MoveToPremiumStage(
                                PremiumBuyingContract.PremiumStage.FindPrimalName,
                            ),
                        )
                    },
                    onLearnMoreClick = onMoreInfoClick,
                )
            }

            PremiumBuyingContract.PremiumStage.FindPrimalName -> {
                PremiumPrimalNameStage(
                    titleText = stringResource(id = R.string.premium_primal_name_title),
                    initialName = state.primalName,
                    onBack = {
                        eventPublisher(
                            PremiumBuyingContract.UiEvent.MoveToPremiumStage(
                                PremiumBuyingContract.PremiumStage.Home,
                            ),
                        )
                    },
                    onPrimalNameAvailable = {
                        eventPublisher(
                            PremiumBuyingContract.UiEvent.SetPrimalName(primalName = it),
                        )
                        eventPublisher(
                            PremiumBuyingContract.UiEvent.MoveToPremiumStage(
                                PremiumBuyingContract.PremiumStage.Purchase,
                            ),
                        )
                    },
                )
            }

            PremiumBuyingContract.PremiumStage.Purchase -> {
                state.primalName?.let {
                    PremiumPurchaseStage(
                        primalName = it,
                        subscriptions = state.subscriptions,
                        onBack = {
                            eventPublisher(
                                PremiumBuyingContract.UiEvent.MoveToPremiumStage(
                                    PremiumBuyingContract.PremiumStage.FindPrimalName,
                                ),
                            )
                        },
                        onLearnMoreClick = onMoreInfoClick,
                    )
                }
            }

            PremiumBuyingContract.PremiumStage.Success -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Success! LFG!!!",
                        style = AppTheme.typography.bodyLarge,
                        fontSize = 28.sp,
                    )
                }
            }
        }
    }
}

private fun AnimatedContentTransitionScope<PremiumBuyingContract.PremiumStage>.transitionSpecBetweenStages() =
    when (initialState) {
        PremiumBuyingContract.PremiumStage.Home -> {
            slideInHorizontally(initialOffsetX = { it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
        }

        PremiumBuyingContract.PremiumStage.FindPrimalName -> {
            when (targetState) {
                PremiumBuyingContract.PremiumStage.Home -> {
                    slideInHorizontally(initialOffsetX = { -it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
                }

                else -> {
                    slideInHorizontally(initialOffsetX = { it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
                }
            }
        }

        PremiumBuyingContract.PremiumStage.Purchase -> {
            when (targetState) {
                PremiumBuyingContract.PremiumStage.Success -> {
                    slideInHorizontally(initialOffsetX = { it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
                }
                else -> {
                    slideInHorizontally(initialOffsetX = { -it })
                        .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
                }
            }
        }

        PremiumBuyingContract.PremiumStage.Success -> {
            slideInHorizontally(initialOffsetX = { -it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
        }
    }
