package net.primal.android.redeem

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.redeem.RedeemCodeContract.UiEvent
import net.primal.android.redeem.ui.RedeemCodeSuccessStage
import net.primal.android.redeem.ui.RedeemEnterCodeStage
import net.primal.android.redeem.ui.RedeemScanCodeStage
import net.primal.android.theme.AppTheme

@Composable
fun RedeemCodeScreen(viewModel: RedeemCodeViewModel, callbacks: RedeemCodeContract.ScreenCallbacks) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, callbacks) {
        viewModel.effects.collect {
            when (it) {
                RedeemCodeContract.SideEffect.PromoCodeApplied -> callbacks.onClose()
                is RedeemCodeContract.SideEffect.NostrConnectRequest -> {
                    callbacks.onNostrConnectRequest(it.url)
                }
                is RedeemCodeContract.SideEffect.DraftTransactionReady -> {
                    callbacks.onDraftTransactionReady(it.draft)
                }
                is RedeemCodeContract.SideEffect.NostrNoteDetected -> {
                    callbacks.onNoteScan(it.noteId)
                }
                is RedeemCodeContract.SideEffect.NostrProfileDetected -> {
                    callbacks.onProfileScan(it.profileId)
                }
                is RedeemCodeContract.SideEffect.NostrArticleDetected -> {
                    callbacks.onArticleScan(it.naddr)
                }
                is RedeemCodeContract.SideEffect.NostrLiveStreamDetected -> {
                    callbacks.onLiveStreamScan(it.naddr)
                }
            }
        }
    }

    RedeemCodeScreen(
        state = uiState.value,
        callbacks = callbacks,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemCodeScreen(
    state: RedeemCodeContract.UiState,
    callbacks: RedeemCodeContract.ScreenCallbacks,
    eventPublisher: (RedeemCodeContract.UiEvent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { eventPublisher(UiEvent.DismissError) },
    )
    BackHandler(enabled = state.stageStack.size > 1) {
        eventPublisher(UiEvent.PreviousStage)
    }

    val currentStage = state.getStage()

    PrimalScaffold(
        containerColor = Color.Transparent,
        topBar = {
            RedeemCodeTopAppBar(
                stage = currentStage,
                onClose = {
                    if (state.stageStack.size > 1) {
                        eventPublisher(UiEvent.PreviousStage)
                    } else {
                        callbacks.onClose()
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = currentStage,
            label = "RedeemStage",
        ) { stage ->
            when (stage) {
                RedeemCodeContract.RedeemCodeStage.ScanCode -> {
                    RedeemScanCodeStage(
                        modifier = Modifier.fillMaxSize(),
                        onQrCodeDetected = { eventPublisher(UiEvent.QrCodeDetected(it)) },
                        onEnterCodeClick = { eventPublisher(UiEvent.GoToEnterCodeStage) },
                    )
                }
                RedeemCodeContract.RedeemCodeStage.EnterCode -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppTheme.colorScheme.background),
                    ) {
                        RedeemEnterCodeStage(
                            modifier = Modifier
                                .padding(paddingValues)
                                .padding(horizontal = 24.dp)
                                .padding(bottom = 16.dp),
                            promoCode = state.promoCode,
                            isError = state.showErrorBadge,
                            isLoading = state.loading,
                            onApplyCodeClick = { eventPublisher(UiEvent.GetCodeDetails(it)) },
                        )
                    }
                }
                RedeemCodeContract.RedeemCodeStage.Success -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(id = R.drawable.onboarding_spot2),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                        )
                        state.welcomeMessage?.let {
                            RedeemCodeSuccessStage(
                                modifier = Modifier
                                    .padding(paddingValues)
                                    .padding(top = 32.dp, bottom = 64.dp)
                                    .padding(horizontal = 36.dp),
                                title = it,
                                userState = state.userState,
                                requiresPrimalWallet = state.requiresPrimalWallet,
                                isLoading = state.loading,
                                benefits = state.promoCodeBenefits,
                                onApplyCodeClick = {
                                    state.promoCode?.let { code -> eventPublisher(UiEvent.ApplyCode(code)) }
                                },
                                onOnboardToPrimalClick = {
                                    state.promoCode?.let { code -> callbacks.navigateToOnboarding(code) }
                                },
                                onActivateWalletClick = {
                                    state.promoCode?.let { code -> callbacks.navigateToWalletOnboarding(code) }
                                },
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
    val isEnterCodeStage = stage == RedeemCodeContract.RedeemCodeStage.EnterCode
    CenterAlignedTopAppBar(
        modifier = modifier,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = if (isEnterCodeStage) AppTheme.colorScheme.background else Color.Transparent,
            titleContentColor = if (isEnterCodeStage) AppTheme.colorScheme.onPrimary else Color.White,
            navigationIconContentColor = if (isEnterCodeStage) AppTheme.colorScheme.onPrimary else Color.White,
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
        RedeemCodeContract.RedeemCodeStage.ScanCode -> stringResource(id = R.string.redeem_code_title)
        RedeemCodeContract.RedeemCodeStage.EnterCode -> stringResource(id = R.string.redeem_code_manual_entry_title)
        RedeemCodeContract.RedeemCodeStage.Success -> stringResource(id = R.string.redeem_code_success_title)
    }
