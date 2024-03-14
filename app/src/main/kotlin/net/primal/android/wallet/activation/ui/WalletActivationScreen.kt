package net.primal.android.wallet.activation.ui

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.ApplyEdgeToEdge
import net.primal.android.core.compose.OtpTextField
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.ToSAndPrivacyPolicyText
import net.primal.android.core.compose.applyEdgeToEdge
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.WalletPrimalActivation
import net.primal.android.core.compose.icons.primaliconpack.WalletSuccess
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.wallet.activation.WalletActivationContract
import net.primal.android.wallet.activation.WalletActivationContract.UiEvent
import net.primal.android.wallet.activation.WalletActivationViewModel
import net.primal.android.wallet.activation.domain.WalletActivationData
import net.primal.android.wallet.activation.domain.WalletActivationStatus
import net.primal.android.wallet.activation.regions.Country
import net.primal.android.wallet.activation.regions.State
import net.primal.android.wallet.walletSuccessColor
import net.primal.android.wallet.walletSuccessContentColor
import net.primal.android.wallet.walletSuccessDimColor

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WalletActivationScreen(viewModel: WalletActivationViewModel, onClose: () -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val uiState = viewModel.uiState.collectAsState()

    BackHandler(
        enabled = uiState.value.status == WalletActivationStatus.PendingCodeConfirmation,
    ) {
        viewModel.setEvent(UiEvent.RequestBackToDataInput)
    }

    WalletActivationScreen(
        state = uiState.value,
        eventPublisher = {
            viewModel.setEvent(it)
        },
        onClose = {
            keyboardController?.hide()
            when (uiState.value.status) {
                WalletActivationStatus.PendingCodeConfirmation -> viewModel.setEvent(UiEvent.RequestBackToDataInput)
                else -> onClose()
            }
        },
    )
}

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
private fun WalletActivationScreen(
    state: WalletActivationContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            PrimalTopAppBar(
                title = when (state.status) {
                    WalletActivationStatus.ActivationSuccess -> stringResource(
                        id = R.string.wallet_activation_success_title,
                    )

                    else -> stringResource(id = R.string.wallet_activation_title)
                },
                colors = when (state.status) {
                    WalletActivationStatus.ActivationSuccess -> TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = walletSuccessColor,
                        scrolledContainerColor = walletSuccessColor,
                    )

                    else -> TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = AppTheme.colorScheme.surface,
                        scrolledContainerColor = AppTheme.colorScheme.surface,
                    )
                },
                textColor = when (state.status) {
                    WalletActivationStatus.ActivationSuccess -> walletSuccessContentColor
                    else -> LocalContentColor.current
                },
                showDivider = state.status != WalletActivationStatus.ActivationSuccess,
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                navigationIconTintColor = when (state.status) {
                    WalletActivationStatus.ActivationSuccess -> walletSuccessContentColor
                    else -> LocalContentColor.current
                },
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            WalletActivationScreenContent(
                modifier = Modifier.fillMaxSize(),
                state = state,
                pendingDataContent = {
                    WalletActivationDataInput(
                        modifier = Modifier.padding(paddingValues),
                        allCountries = state.allCountries,
                        availableStates = state.availableStates,
                        data = state.data,
                        isDataValid = state.isDataValid,
                        working = state.working,
                        error = state.error,
                        onErrorDismiss = {
                            eventPublisher(UiEvent.ClearErrorMessage)
                            onClose()
                        },
                        onDataChange = { eventPublisher(UiEvent.ActivationDataChanged(data = it)) },
                        onActivationCodeRequest = { eventPublisher(UiEvent.ActivationRequest) },
                    )
                },
                pendingOtpValidation = {
                    WalletCodeActivationInput(
                        modifier = Modifier.padding(paddingValues),
                        working = state.working,
                        error = state.error,
                        code = state.otpCode,
                        email = state.data.email,
                        onCodeChanged = { eventPublisher(UiEvent.OtpCodeChanged(code = it)) },
                        onCodeConfirmed = { eventPublisher(UiEvent.Activate) },
                    )
                },
                success = {
                    WalletActivationSuccess(
                        modifier = Modifier.fillMaxSize(),
                        lightningAddress = state.activatedLightningAddress.orEmpty(),
                        onDone = onClose,
                    )
                },
            )
        },
    )
}

@ExperimentalComposeUiApi
@Composable
fun WalletActivationScreenContent(
    modifier: Modifier,
    state: WalletActivationContract.UiState,
    pendingDataContent: @Composable () -> Unit,
    pendingOtpValidation: @Composable () -> Unit,
    success: @Composable () -> Unit,
) {
    AnimatedContent(
        modifier = modifier,
        targetState = state.status,
        contentAlignment = Alignment.Center,
        transitionSpec = {
            fadeIn(animationSpec = tween(STEP_ANIMATION_DURATION))
                .togetherWith(fadeOut(animationSpec = tween(STEP_ANIMATION_DURATION)))
        },
        label = "WalletActivationContent",
        contentKey = { it.name },
        content = { status ->
            when (status) {
                WalletActivationStatus.PendingData -> pendingDataContent()
                WalletActivationStatus.PendingCodeConfirmation -> pendingOtpValidation()
                WalletActivationStatus.ActivationSuccess -> success()
            }
        },
    )
}

private const val STEP_ANIMATION_DURATION = 256

@Composable
private fun StepContainerWithActionButton(
    modifier: Modifier = Modifier,
    onActionClick: () -> Unit,
    actionButtonText: String,
    actionButtonEnabled: Boolean,
    actionButtonLoading: Boolean = false,
    actionButtonVisible: Boolean = true,
    tosAndPrivacyPolicyVisible: Boolean = false,
    containerContent: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        containerContent()

        if (actionButtonVisible) {
            PrimalLoadingButton(
                modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                enabled = actionButtonEnabled && !actionButtonLoading,
                loading = actionButtonLoading,
                onClick = onActionClick,
                text = actionButtonText,
            )

            if (tosAndPrivacyPolicyVisible) {
                ToSAndPrivacyPolicyText(
                    modifier = Modifier
                        .widthIn(0.dp, 360.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                        .padding(top = 8.dp),
                    tosPrefix = stringResource(id = R.string.wallet_tos_prefix),
                )
            }
        }
    }
}

@Suppress("MagicNumber")
@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
private fun WalletActivationDataInput(
    modifier: Modifier = Modifier,
    allCountries: List<Country>,
    availableStates: List<State>,
    data: WalletActivationData,
    isDataValid: Boolean,
    working: Boolean,
    error: Throwable?,
    onErrorDismiss: () -> Unit,
    onDataChange: (WalletActivationData) -> Unit,
    onActivationCodeRequest: () -> Unit,
) {
    WalletActivationErrorHandler(
        error = error,
        fallbackMessage = stringResource(id = R.string.app_generic_error),
        onErrorDismiss = onErrorDismiss,
    )

    StepContainerWithActionButton(
        modifier = modifier,
        actionButtonText = stringResource(id = R.string.wallet_activation_next_button),
        actionButtonEnabled = isDataValid,
        actionButtonLoading = working,
        tosAndPrivacyPolicyVisible = true,
        onActionClick = onActivationCodeRequest,
    ) {
        WalletActivationForm(
            modifier = Modifier.fillMaxWidth(),
            allCountries = allCountries,
            availableStates = availableStates,
            data = data,
            onDataChange = onDataChange,
        )
    }
}

@ExperimentalComposeUiApi
@Composable
fun WalletCodeActivationInput(
    modifier: Modifier = Modifier,
    working: Boolean,
    error: Throwable?,
    email: String,
    code: String,
    onCodeChanged: (String) -> Unit,
    onCodeConfirmed: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    StepContainerWithActionButton(
        modifier = modifier,
        actionButtonText = stringResource(id = R.string.wallet_activation_finish_button),
        actionButtonEnabled = code.isOtpCodeValid(),
        actionButtonLoading = working,
        onActionClick = {
            keyboardController?.hide()
            onCodeConfirmed()
        },
    ) {
        WalletOtpVerification(
            modifier = Modifier.fillMaxWidth(),
            email = email,
            code = code,
            onCodeChanged = onCodeChanged,
            onCodeConfirmed = onCodeConfirmed,
            error = error,
        )
    }
}

@Composable
fun WalletOtpVerification(
    modifier: Modifier,
    email: String,
    code: String,
    onCodeChanged: (String) -> Unit,
    onCodeConfirmed: () -> Unit,
    error: Throwable?,
) {
    val isKeyboardVisible by keyboardVisibilityAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(visible = !isKeyboardVisible) {
            Image(
                modifier = Modifier.padding(vertical = 16.dp),
                imageVector = PrimalIcons.WalletPrimalActivation,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = AppTheme.colorScheme.onSurface),
            )
        }

        Text(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .padding(top = 32.dp),
            text = stringResource(id = R.string.wallet_activation_pending_code_subtitle),
            textAlign = TextAlign.Center,
            color = AppTheme.colorScheme.onSurface,
            style = AppTheme.typography.titleLarge,
        )

        val textHint = stringResource(id = R.string.wallet_activation_pending_code_hint, email)
        val textHintAnnotation = buildAnnotatedString {
            append(textHint)
            val startIndex = textHint.indexOf(email)
            if (startIndex >= 0) {
                val endIndex = startIndex + email.length
                addStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold),
                    start = startIndex,
                    end = endIndex,
                )
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .padding(top = 16.dp, bottom = 32.dp),
            text = textHintAnnotation,
            textAlign = TextAlign.Center,
            color = AppTheme.colorScheme.onSurface,
            style = AppTheme.typography.bodyMedium,
        )

        OtpTextField(
            modifier = Modifier.fillMaxWidth(fraction = 0.8f),
            otpText = code,
            onOtpTextChange = {
                if (it.isDigitsOnly()) {
                    onCodeChanged(it.trim())
                }
            },
            onCodeConfirmed = {
                if (code.isOtpCodeValid()) {
                    keyboardController?.hide()
                    onCodeConfirmed()
                }
            },
        )

        WalletErrorText(
            error = error,
            fallbackMessage = stringResource(id = R.string.wallet_activation_error_invalid_code),
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private const val CODE_LENGTH = 6

fun String.isOtpCodeValid() = this.isDigitsOnly() && this.length == CODE_LENGTH

@Composable
private fun WalletActivationSuccess(
    modifier: Modifier = Modifier,
    lightningAddress: String,
    onDone: () -> Unit,
) {
    ApplyEdgeToEdge(isDarkTheme = true)

    val context = LocalContext.current
    val primalTheme = LocalPrimalTheme.current
    fun closingSequence() {
        onDone()
        (context as ComponentActivity).applyEdgeToEdge(isDarkTheme = primalTheme.isDarkTheme)
    }

    BackHandler {
        closingSequence()
    }

    Column(
        modifier = modifier
            .background(color = walletSuccessColor)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .wrapContentHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                modifier = Modifier
                    .size(160.dp)
                    .padding(vertical = 16.dp),
                imageVector = PrimalIcons.WalletSuccess,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = walletSuccessContentColor),
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .padding(top = 32.dp, bottom = 16.dp),
                text = stringResource(id = R.string.wallet_activation_success_description),
                textAlign = TextAlign.Center,
                color = walletSuccessContentColor,
                style = AppTheme.typography.bodyLarge,
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .padding(vertical = 32.dp),
                text = lightningAddress,
                textAlign = TextAlign.Center,
                color = walletSuccessContentColor,
                style = AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }

        PrimalLoadingButton(
            modifier = Modifier
                .width(200.dp)
                .padding(bottom = 32.dp),
            text = stringResource(id = R.string.wallet_activation_done_button),
            containerColor = walletSuccessDimColor,
            onClick = { closingSequence() },
        )
    }
}

@Composable
private fun WalletErrorText(error: Throwable?, fallbackMessage: String) {
    Text(
        modifier = Modifier
            .alpha(if (error != null) 1.0f else 0.0f)
            .fillMaxWidth(fraction = 0.8f)
            .padding(top = 16.dp),
        text = (error?.message ?: "").ifEmpty { fallbackMessage },
        textAlign = TextAlign.Center,
        color = AppTheme.colorScheme.error,
        style = AppTheme.typography.bodyMedium,
    )
}

@Composable
private fun WalletActivationErrorHandler(
    error: Throwable?,
    fallbackMessage: String,
    onErrorDismiss: () -> Unit,
) {
    if (error != null) {
        val text = (error.message ?: "").ifEmpty { fallbackMessage }
        AlertDialog(
            containerColor = AppTheme.colorScheme.surfaceVariant,
            onDismissRequest = onErrorDismiss,
            title = {
                Text(
                    text = stringResource(id = R.string.wallet_activation_error_dialog_title),
                    style = AppTheme.typography.titleLarge,
                )
            },
            text = {
                Text(
                    text = text,
                    style = AppTheme.typography.bodyLarge,
                )
            },
            confirmButton = {
                TextButton(onClick = onErrorDismiss) {
                    Text(
                        text = stringResource(id = android.R.string.ok),
                    )
                }
            },
        )
    }
}

@ExperimentalComposeUiApi
@Preview
@Composable
private fun PreviewWalletActivationDataInput() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            WalletActivationDataInput(
                data = WalletActivationData(
                    firstName = "alex",
                    lastName = "alex",
                    email = "alex@primal.net",
                    country = Country(name = "Serbia", code = "RS", states = emptyList()),
                ),
                allCountries = emptyList(),
                availableStates = emptyList(),
                isDataValid = true,
                working = false,
                error = null,
                onErrorDismiss = { },
                onDataChange = { },
                onActivationCodeRequest = { },
            )
        }
    }
}

@ExperimentalComposeUiApi
@Preview
@Composable
private fun PreviewWalletCodeActivationInput() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            WalletCodeActivationInput(
                working = false,
                error = null,
                email = "alex@primal.net",
                code = "124",
                onCodeChanged = { },
                onCodeConfirmed = { },
            )
        }
    }
}
