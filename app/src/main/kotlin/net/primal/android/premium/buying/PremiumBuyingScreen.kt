package net.primal.android.premium.buying

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.premium.buying.home.PremiumBuyingHomeStage
import net.primal.android.premium.buying.name.PremiumPrimalNameStage
import net.primal.android.premium.buying.purchase.PremiumPurchaseStage
import net.primal.android.premium.buying.success.PremiumBuyingSuccessStage
import net.primal.android.premium.ui.toHumanReadableString
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumBuyingScreen(
    state: PremiumBuyingContract.UiState,
    onClose: () -> Unit,
    onMoreInfoClick: () -> Unit,
    eventPublisher: (PremiumBuyingContract.UiEvent) -> Unit,
) {
    PremiumBuyingBackHandler(
        stage = state.stage,
        onClose = onClose,
        eventPublisher = eventPublisher,
    )

    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.toHumanReadableString() },
        onErrorDismiss = { eventPublisher(PremiumBuyingContract.UiEvent.DismissError) },
    )

    Box(contentAlignment = Alignment.BottomCenter) {
        AnimatedContent(
            modifier = Modifier
                .background(AppTheme.colorScheme.surfaceVariant)
                .fillMaxSize(),
            label = "BuyingPremiumStages",
            targetState = state.stage,
            transitionSpec = { transitionSpecBetweenStages() },
        ) { stage ->
            when (stage) {
                PremiumBuyingContract.PremiumStage.Home -> {
                    PremiumBuyingHomeStage(
                        loading = state.loading,
                        subscriptions = state.subscriptions,
                        onClose = onClose,
                        onLearnMoreClick = onMoreInfoClick,
                        onFindPrimalName = {
                            eventPublisher(
                                PremiumBuyingContract.UiEvent.MoveToPremiumStage(
                                    PremiumBuyingContract.PremiumStage.FindPrimalName,
                                ),
                            )
                        },
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
                    PremiumPurchaseStage(
                        state = state,
                        eventPublisher = eventPublisher,
                        onBack = {
                            if (state.isExtendingPremium) {
                                onClose()
                            } else {
                                eventPublisher(
                                    PremiumBuyingContract.UiEvent.MoveToPremiumStage(
                                        PremiumBuyingContract.PremiumStage.FindPrimalName,
                                    ),
                                )
                            }
                        },
                        onLearnMoreClick = onMoreInfoClick,
                    )
                }

                PremiumBuyingContract.PremiumStage.Success -> {
                    PremiumBuyingSuccessStage(
                        modifier = Modifier.fillMaxSize(),
                        onDoneClick = onClose,
                    )
                }
            }
        }

        SnackbarHost(
            modifier = Modifier.navigationBarsPadding(),
            hostState = snackbarHostState,
        )
    }
}

@Composable
private fun PremiumBuyingBackHandler(
    stage: PremiumBuyingContract.PremiumStage,
    onClose: () -> Unit,
    eventPublisher: (PremiumBuyingContract.UiEvent) -> Unit,
) {
    BackHandler {
        when (stage) {
            PremiumBuyingContract.PremiumStage.Home,
            PremiumBuyingContract.PremiumStage.Success,
            -> {
                onClose()
            }

            PremiumBuyingContract.PremiumStage.FindPrimalName -> {
                eventPublisher(
                    PremiumBuyingContract.UiEvent.MoveToPremiumStage(
                        PremiumBuyingContract.PremiumStage.Home,
                    ),
                )
            }

            PremiumBuyingContract.PremiumStage.Purchase -> {
                eventPublisher(
                    PremiumBuyingContract.UiEvent.MoveToPremiumStage(
                        PremiumBuyingContract.PremiumStage.FindPrimalName,
                    ),
                )
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
