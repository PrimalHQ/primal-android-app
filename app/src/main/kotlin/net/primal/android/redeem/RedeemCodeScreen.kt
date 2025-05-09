package net.primal.android.redeem

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.redeem.ui.RedeemCodeSuccessStage
import net.primal.android.redeem.ui.RedeemEnterCodeStage

@Composable
fun RedeemCodeScreen(
    onClose: () -> Unit,
    navigateToOnboarding: (String) -> Unit,
    navigateToWalletOnboarding: (String) -> Unit,
    viewModel: RedeemCodeViewModel,
) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect {
            when (it) {
                RedeemCodeContract.SideEffect.PromoCodeApplied -> onClose()
            }
        }
    }

    RedeemCodeScreen(
        onClose = onClose,
        state = uiState.value,
        navigateToOnboarding = navigateToOnboarding,
        navigateToWalletOnboarding = navigateToWalletOnboarding,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemCodeScreen(
    onClose: () -> Unit,
    state: RedeemCodeContract.UiState,
    navigateToOnboarding: (String) -> Unit,
    navigateToWalletOnboarding: (String) -> Unit,
    eventPublisher: (RedeemCodeContract.UiEvent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { eventPublisher(RedeemCodeContract.UiEvent.DismissError) },
    )
    BackHandler(enabled = state.stage == RedeemCodeContract.RedeemCodeStage.Success) {
        eventPublisher(RedeemCodeContract.UiEvent.GoToEnterCodeStage)
    }

    ColumnWithBackground(
        backgroundPainter = painterResource(id = R.drawable.onboarding_spot2),
    ) { size ->
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                RedeemCodeTopAppBar(
                    stage = state.stage,
                    onClose = {
                        if (state.stage == RedeemCodeContract.RedeemCodeStage.Success) {
                            eventPublisher(RedeemCodeContract.UiEvent.GoToEnterCodeStage)
                        } else {
                            onClose()
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { paddingValues ->
            AnimatedContent(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(top = 32.dp)
                    .padding(horizontal = 24.dp),
                targetState = state.stage,
            ) { stage ->
                when (stage) {
                    RedeemCodeContract.RedeemCodeStage.EnterCode -> {
                        RedeemEnterCodeStage(
                            modifier = Modifier.padding(top = 96.dp, bottom = 64.dp),
                            isError = state.showErrorBadge,
                            isLoading = state.loading,
                            onApplyCodeClick = { eventPublisher(RedeemCodeContract.UiEvent.GetCodeDetails(it)) },
                        )
                    }

                    RedeemCodeContract.RedeemCodeStage.Success -> {
                        state.welcomeMessage?.let {
                            RedeemCodeSuccessStage(
                                modifier = Modifier
                                    .padding(bottom = 64.dp)
                                    .padding(horizontal = 12.dp),
                                title = state.welcomeMessage,
                                userState = state.userState,
                                requiresPrimalWallet = state.requiresPrimalWallet,
                                isLoading = state.loading,
                                benefits = state.promoCodeBenefits,
                                onApplyCodeClick = {
                                    state.promoCode?.let { eventPublisher(RedeemCodeContract.UiEvent.ApplyCode(it)) }
                                },
                                onOnboardToPrimalClick = { state.promoCode?.let { navigateToOnboarding(it) } },
                                onActivateWalletClick = { state.promoCode?.let { navigateToWalletOnboarding(it) } },
                            )
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun RedeemCodeTopAppBar(
    onClose: () -> Unit,
    stage: RedeemCodeContract.RedeemCodeStage,
    modifier: Modifier = Modifier,
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
        ),
        title = {
            Text(text = stage.toTitle())
        },
        navigationIcon = {
            AppBarIcon(
                icon = PrimalIcons.ArrowBack,
                onClick = onClose,
            )
        },
    )
}

@Composable
private fun RedeemCodeContract.RedeemCodeStage.toTitle() =
    when (this) {
        RedeemCodeContract.RedeemCodeStage.EnterCode -> stringResource(id = R.string.redeem_code_title)
        RedeemCodeContract.RedeemCodeStage.Success -> stringResource(id = R.string.redeem_code_success_title)
    }
