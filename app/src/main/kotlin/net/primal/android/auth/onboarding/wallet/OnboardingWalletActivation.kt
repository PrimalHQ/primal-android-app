package net.primal.android.auth.onboarding.wallet

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import java.io.IOException
import net.primal.android.R
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.auth.compose.OnboardingBottomBar
import net.primal.android.auth.compose.onboardingTextHintTypography
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletSuccess
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.wallet.activation.WalletActivationContract
import net.primal.android.wallet.activation.WalletActivationViewModel
import net.primal.android.wallet.activation.domain.WalletActivationStatus
import net.primal.android.wallet.activation.ui.WalletActivationErrorHandler
import net.primal.android.wallet.activation.ui.WalletActivationForm
import net.primal.android.wallet.activation.ui.WalletActivationScreenContent
import net.primal.android.wallet.activation.ui.WalletOtpVerification
import net.primal.android.wallet.activation.ui.isOtpCodeValid
import net.primal.android.wallet.walletSuccessContentColor

@Composable
fun OnboardingWalletActivation(viewModel: WalletActivationViewModel, onDoneOrDismiss: () -> Unit) {
    val uiState = viewModel.uiState.collectAsState()
    OnboardingWalletActivation(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onDoneOrDismiss = onDoneOrDismiss,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun OnboardingWalletActivation(
    state: WalletActivationContract.UiState,
    eventPublisher: (WalletActivationContract.UiEvent) -> Unit,
    onDoneOrDismiss: () -> Unit,
) {
    val isKeyboardVisible by keyboardVisibilityAsState()
    BackHandler {}
    ColumnWithBackground(
        backgroundPainter = painterResource(id = R.drawable.onboarding_spot5),
    ) {
        Scaffold(
            modifier = Modifier.imePadding(),
            containerColor = Color.Transparent,
            topBar = {
                PrimalTopAppBar(
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                    showDivider = false,
                    textColor = Color.White,
                    title = state.status.asTitle(),
                )
            },
            content = { paddingValues ->
                WalletActivationScreenContent(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    pendingDataContent = {
                        WalletActivationFormContent(
                            modifier = Modifier
                                .padding(paddingValues)
                                .verticalScroll(state = rememberScrollState()),
                            state = state,
                            eventPublisher = eventPublisher,
                            isKeyboardVisible = isKeyboardVisible,
                        )
                    },
                    pendingOtpValidation = {
                        WalletOtpVerification(
                            modifier = Modifier.padding(paddingValues),
                            code = state.otpCode,
                            email = state.data.email,
                            onCodeChanged = { eventPublisher(WalletActivationContract.UiEvent.OtpCodeChanged(it)) },
                            onCodeConfirmed = { eventPublisher(WalletActivationContract.UiEvent.Activate) },
                            otpTheme = PrimalTheme.Sunrise,
                            errorContent = { OtpErrorText(error = state.error) },
                        )
                    },
                    success = {
                        WalletSuccessContent(modifier = Modifier.padding(paddingValues))
                    },
                )
            },
            bottomBar = {
                WalletOnboardingBottomBar(
                    state = state,
                    buttonText = state.status.asActionButtonText(),
                    visible = when (state.status) {
                        WalletActivationStatus.PendingData -> !isKeyboardVisible
                        else -> true
                    },
                    footerVisible = state.status != WalletActivationStatus.ActivationSuccess,
                    eventPublisher = eventPublisher,
                    onDoneOrDismiss = onDoneOrDismiss,
                )
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun WalletActivationFormContent(
    modifier: Modifier,
    state: WalletActivationContract.UiState,
    eventPublisher: (WalletActivationContract.UiEvent) -> Unit,
    isKeyboardVisible: Boolean,
) {
    WalletActivationErrorHandler(
        error = state.error,
        fallbackMessage = stringResource(id = R.string.app_generic_error),
        onErrorDismiss = {
            eventPublisher(WalletActivationContract.UiEvent.ClearErrorMessage)
        },
    )

    WalletActivationForm(
        modifier = modifier,
        colors = walletOnboardingOutlinedTextColor(),
        allCountries = state.allCountries,
        availableStates = state.availableStates,
        isHeaderIconVisible = !isKeyboardVisible,
        data = state.data,
        onDataChange = {
            eventPublisher(
                WalletActivationContract.UiEvent.ActivationDataChanged(it),
            )
        },
        bottomSheetTheme = PrimalTheme.Sunrise,
        bottomSheetScrimColor = Color.Transparent,
    )
}

@Composable
private fun OtpErrorText(error: Throwable?) {
    val fallbackMessage = stringResource(id = R.string.wallet_activation_error_invalid_code)
    Text(
        modifier = Modifier
            .alpha(if (error != null) 1.0f else 0.0f)
            .padding(top = 16.dp)
            .padding(horizontal = 64.dp)
            .wrapContentWidth()
            .background(color = errorBackgroundColor, shape = AppTheme.shapes.large)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        text = (error?.message ?: "").ifEmpty { fallbackMessage },
        textAlign = TextAlign.Center,
        color = Color.White,
        style = AppTheme.typography.bodyMedium,
    )
}

private val errorBackgroundColor = Color(0xFFFE3D2F)

@Composable
private fun WalletActivationStatus.asTitle(): String {
    return when (this) {
        WalletActivationStatus.ActivationSuccess -> stringResource(
            id = R.string.wallet_activation_success_title,
        )

        else -> stringResource(id = R.string.wallet_activation_title)
    }
}

@Composable
private fun WalletActivationStatus.asActionButtonText(): String {
    return when (this) {
        WalletActivationStatus.PendingData -> stringResource(
            id = R.string.wallet_activation_next_button,
        )

        WalletActivationStatus.PendingOtpVerification -> stringResource(
            id = R.string.wallet_activation_finish_button,
        )

        WalletActivationStatus.ActivationSuccess -> stringResource(
            id = R.string.wallet_activation_done_button,
        )
    }
}

@Composable
private fun walletOnboardingOutlinedTextColor() =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        focusedTextColor = Color.Black,
        focusedBorderColor = Color.White,
        unfocusedContainerColor = Color.White,
        unfocusedTextColor = Color.Black,
        unfocusedBorderColor = Color.White,
    )

@Composable
private fun WalletOnboardingBottomBar(
    state: WalletActivationContract.UiState,
    buttonText: String,
    visible: Boolean,
    footerVisible: Boolean,
    eventPublisher: (WalletActivationContract.UiEvent) -> Unit,
    onDoneOrDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = spring(stiffness = Spring.StiffnessHigh)),
        exit = ExitTransition.None,
    ) {
        OnboardingBottomBar(
            buttonText = buttonText,
            buttonLoading = state.working,
            buttonEnabled = when (state.status) {
                WalletActivationStatus.PendingData -> state.isDataValid
                WalletActivationStatus.PendingOtpVerification -> state.otpCode.isOtpCodeValid()
                WalletActivationStatus.ActivationSuccess -> true
            },
            onButtonClick = {
                when (state.status) {
                    WalletActivationStatus.PendingData -> {
                        eventPublisher(WalletActivationContract.UiEvent.ActivationRequest)
                    }

                    WalletActivationStatus.PendingOtpVerification -> {
                        eventPublisher(WalletActivationContract.UiEvent.Activate)
                    }

                    WalletActivationStatus.ActivationSuccess -> {
                        onDoneOrDismiss()
                    }
                }
            },
            footer = {
                TextButton(
                    modifier = Modifier
                        .alpha(if (footerVisible) 1.0f else 0.0f)
                        .height(56.dp),
                    onClick = onDoneOrDismiss,
                ) {
                    Text(
                        text = stringResource(id = R.string.onboarding_button_label_i_will_do_this_later),
                        style = onboardingTextHintTypography(),
                    )
                }
            },
        )
    }
}

@Composable
private fun WalletSuccessContent(modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            modifier = Modifier
                .size(200.dp)
                .padding(vertical = 16.dp),
            imageVector = PrimalIcons.WalletSuccess,
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = walletSuccessContentColor),
        )

        Text(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .padding(top = 32.dp, bottom = 16.dp),
            text = stringResource(id = R.string.wallet_activation_success_description_short),
            textAlign = TextAlign.Center,
            color = walletSuccessContentColor,
            style = AppTheme.typography.bodyLarge,
        )
    }
}

private class UiStateProvider(
    override val values: Sequence<WalletActivationContract.UiState> = sequenceOf(
        WalletActivationContract.UiState(
            status = WalletActivationStatus.PendingData,
        ),
        WalletActivationContract.UiState(
            status = WalletActivationStatus.PendingOtpVerification,
            otpCode = "123",
            error = IOException(),
        ),
        WalletActivationContract.UiState(
            status = WalletActivationStatus.ActivationSuccess,
        ),
    ),
) : PreviewParameterProvider<WalletActivationContract.UiState>

@Preview
@Composable
private fun PreviewPendingDataScreen(
    @PreviewParameter(provider = UiStateProvider::class) uiState: WalletActivationContract.UiState,
) {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        OnboardingWalletActivation(
            state = uiState,
            eventPublisher = {},
            onDoneOrDismiss = {},
        )
    }
}
