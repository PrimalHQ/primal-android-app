package net.primal.android.scan

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import net.primal.android.scan.ScanCodeContract.UiEvent
import net.primal.android.scan.ui.ScanCameraStage
import net.primal.android.scan.ui.ScanManualEntryStage
import net.primal.android.scan.ui.ScanSuccessStage
import net.primal.android.theme.AppTheme

@Composable
fun ScanCodeScreen(viewModel: ScanCodeViewModel, callbacks: ScanCodeContract.ScreenCallbacks) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, callbacks) {
        viewModel.effects.collect {
            when (it) {
                ScanCodeContract.SideEffect.PromoCodeApplied -> callbacks.onClose()
                is ScanCodeContract.SideEffect.NostrConnectRequest -> {
                    callbacks.onNostrConnectRequest(it.url)
                }
                is ScanCodeContract.SideEffect.DraftTransactionReady -> {
                    callbacks.onDraftTransactionReady(it.draft)
                }
                is ScanCodeContract.SideEffect.NostrNoteDetected -> {
                    callbacks.onNoteScan(it.noteId)
                }
                is ScanCodeContract.SideEffect.NostrProfileDetected -> {
                    callbacks.onProfileScan(it.profileId)
                }
                is ScanCodeContract.SideEffect.NostrArticleDetected -> {
                    callbacks.onArticleScan(it.naddr)
                }
                is ScanCodeContract.SideEffect.NostrLiveStreamDetected -> {
                    callbacks.onLiveStreamScan(it.naddr)
                }
            }
        }
    }

    ScanCodeScreen(
        state = uiState.value,
        callbacks = callbacks,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCodeScreen(
    state: ScanCodeContract.UiState,
    callbacks: ScanCodeContract.ScreenCallbacks,
    eventPublisher: (ScanCodeContract.UiEvent) -> Unit,
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
            ScanCodeTopAppBar(
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
        ScanCodeMainContent(
            paddingValues = paddingValues,
            state = state,
            callbacks = callbacks,
            eventPublisher = eventPublisher,
        )
    }
}

@Composable
private fun ScanCodeMainContent(
    paddingValues: PaddingValues,
    state: ScanCodeContract.UiState,
    callbacks: ScanCodeContract.ScreenCallbacks,
    eventPublisher: (ScanCodeContract.UiEvent) -> Unit,
) {
    val currentStage = state.getStage()
    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = currentStage,
        label = "ScanStage",
    ) { stage ->
        when (stage) {
            ScanCodeContract.ScanCodeStage.ScanCamera -> {
                ScanCameraStage(
                    modifier = Modifier.fillMaxSize(),
                    onQrCodeDetected = { eventPublisher(UiEvent.QrCodeDetected(it)) },
                    onEnterCodeClick = { eventPublisher(UiEvent.GoToManualInput) },
                )
            }
            ScanCodeContract.ScanCodeStage.ManualInput -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AppTheme.colorScheme.background),
                ) {
                    ScanManualEntryStage(
                        modifier = Modifier
                            .padding(paddingValues)
                            .padding(horizontal = 24.dp)
                            .padding(bottom = 16.dp),
                        value = state.scannedValue,
                        isError = state.showErrorBadge,
                        isLoading = state.loading,
                        onApplyClick = { eventPublisher(UiEvent.ProcessCode(it)) },
                    )
                }
            }
            ScanCodeContract.ScanCodeStage.Success -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(id = R.drawable.onboarding_spot2),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )
                    state.welcomeMessage?.let {
                        ScanSuccessStage(
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
                                state.scannedValue?.let { code -> eventPublisher(UiEvent.ApplyPromoCode(code)) }
                            },
                            onOnboardToPrimalClick = {
                                state.scannedValue?.let { code -> callbacks.navigateToOnboarding(code) }
                            },
                            onActivateWalletClick = {
                                state.scannedValue?.let { code -> callbacks.navigateToWalletOnboarding(code) }
                            },
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun ScanCodeTopAppBar(
    onClose: () -> Unit,
    stage: ScanCodeContract.ScanCodeStage,
    modifier: Modifier = Modifier,
) {
    val isEnterCodeStage = stage == ScanCodeContract.ScanCodeStage.ManualInput
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
private fun ScanCodeContract.ScanCodeStage.toTitle() =
    when (this) {
        ScanCodeContract.ScanCodeStage.ScanCamera -> stringResource(id = R.string.scan_code_title)
        ScanCodeContract.ScanCodeStage.ManualInput -> stringResource(id = R.string.scan_code_manual_entry_title)
        ScanCodeContract.ScanCodeStage.Success -> stringResource(id = R.string.scan_code_success_title)
    }
