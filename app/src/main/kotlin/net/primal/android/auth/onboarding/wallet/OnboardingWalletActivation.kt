package net.primal.android.auth.onboarding.wallet

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.auth.compose.OnboardingBottomBar
import net.primal.android.auth.compose.onboardingTextHintTypography
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.wallet.activation.WalletActivationContract
import net.primal.android.wallet.activation.WalletActivationViewModel
import net.primal.android.wallet.activation.domain.WalletActivationStatus
import net.primal.android.wallet.activation.ui.WalletActivationForm
import net.primal.android.wallet.activation.ui.WalletActivationScreenContent
import net.primal.android.wallet.activation.ui.WalletOtpVerification
import net.primal.android.wallet.activation.ui.isOtpCodeValid

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
                    title = stringResource(id = R.string.wallet_activation_title),
                )
            },
            content = { paddingValues ->
                WalletActivationScreenContent(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    pendingDataContent = {
                        WalletActivationForm(
                            modifier = Modifier
                                .padding(paddingValues)
                                .verticalScroll(state = rememberScrollState()),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White,
                                focusedTextColor = Color.Black,
                                focusedBorderColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                unfocusedTextColor = Color.Black,
                                unfocusedBorderColor = Color.White,
                            ),
                            allCountries = state.allCountries,
                            availableStates = state.availableStates,
                            isHeaderIconVisible = !isKeyboardVisible,
                            data = state.data,
                            onDataChange = {
                                eventPublisher(
                                    WalletActivationContract.UiEvent.ActivationDataChanged(it),
                                )
                            },
                        )
                    },
                    pendingOtpValidation = {
                        WalletOtpVerification(
                            modifier = Modifier.padding(paddingValues),
                            error = state.error,
                            code = state.otpCode,
                            email = state.data.email,
                            onCodeChanged = { eventPublisher(WalletActivationContract.UiEvent.OtpCodeChanged(it)) },
                            onCodeConfirmed = { eventPublisher(WalletActivationContract.UiEvent.Activate) },
                        )
                    },
                    success = {
                    },
                )
            },
            bottomBar = {
                WalletOnboardingBottomBar(
                    state = state,
                    isKeyboardVisible = isKeyboardVisible,
                    eventPublisher = eventPublisher,
                    onDoneOrDismiss = onDoneOrDismiss,
                )
            },
        )
    }
}

@Composable
private fun WalletOnboardingBottomBar(
    state: WalletActivationContract.UiState,
    isKeyboardVisible: Boolean,
    eventPublisher: (WalletActivationContract.UiEvent) -> Unit,
    onDoneOrDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = !isKeyboardVisible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = ExitTransition.None,
    ) {
        OnboardingBottomBar(
            buttonText = "Next",
            buttonLoading = state.working,
            buttonEnabled = when (state.status) {
                WalletActivationStatus.PendingData -> state.isDataValid
                WalletActivationStatus.PendingCodeConfirmation -> state.otpCode.isOtpCodeValid()
                WalletActivationStatus.ActivationSuccess -> true
            },
            onButtonClick = {
                when (state.status) {
                    WalletActivationStatus.PendingData -> {
                        eventPublisher(WalletActivationContract.UiEvent.ActivationRequest)
                    }

                    WalletActivationStatus.PendingCodeConfirmation -> {
                        eventPublisher(WalletActivationContract.UiEvent.Activate)
                    }

                    WalletActivationStatus.ActivationSuccess -> TODO()
                }
            },
            footer = {
                TextButton(
                    modifier = Modifier.height(56.dp),
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
