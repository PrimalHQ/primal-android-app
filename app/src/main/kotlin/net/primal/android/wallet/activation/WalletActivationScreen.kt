package net.primal.android.wallet.activation

import android.util.Patterns
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import java.io.IOException
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import net.primal.android.R
import net.primal.android.core.compose.AdjustTemporarilySystemBarColors
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.WalletPrimalActivation
import net.primal.android.core.compose.icons.primaliconpack.WalletSuccess
import net.primal.android.core.serialization.json.NostrJson
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.wallet.activation.WalletActivationContract.UiEvent
import net.primal.android.wallet.activation.regions.Country
import net.primal.android.wallet.activation.regions.RegionSelectionBottomSheet
import net.primal.android.wallet.activation.regions.Regions
import net.primal.android.wallet.activation.regions.State
import net.primal.android.wallet.activation.regions.toListOfCountries
import net.primal.android.wallet.walletSuccessColor
import net.primal.android.wallet.walletSuccessContentColor
import net.primal.android.wallet.walletSuccessDimColor
import timber.log.Timber

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
        uiState = uiState.value,
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
fun WalletActivationScreen(
    uiState: WalletActivationContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val isKeyboardVisible by keyboardVisibilityAsState()

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            PrimalTopAppBar(
                title = when (uiState.status) {
                    WalletActivationStatus.ActivationSuccess -> stringResource(
                        id = R.string.wallet_activation_success_title,
                    )

                    else -> stringResource(id = R.string.wallet_activation_title)
                },
                colors = when (uiState.status) {
                    WalletActivationStatus.ActivationSuccess -> TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = walletSuccessColor,
                        scrolledContainerColor = walletSuccessColor,
                        titleContentColor = walletSuccessContentColor,
                    )

                    else -> TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = AppTheme.colorScheme.surface,
                        scrolledContainerColor = AppTheme.colorScheme.surface,
                    )
                },
                showDivider = uiState.status != WalletActivationStatus.ActivationSuccess,
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconTintColor = when (uiState.status) {
                    WalletActivationStatus.ActivationSuccess -> walletSuccessContentColor
                    else -> LocalContentColor.current
                },
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            AnimatedContent(
                targetState = uiState.status,
                modifier = Modifier.padding(paddingValues),
                contentAlignment = Alignment.Center,
                transitionSpec = {
                    fadeIn(animationSpec = tween(STEP_ANIMATION_DURATION))
                        .togetherWith(fadeOut(animationSpec = tween(STEP_ANIMATION_DURATION)))
                },
                label = "WalletActivationContent",
                contentKey = { it.name },
                content = { status ->
                    when (status) {
                        WalletActivationStatus.PendingData -> WalletActivationDataInput(
                            data = uiState.data,
                            working = uiState.working,
                            error = uiState.error,
                            isKeyboardVisible = isKeyboardVisible,
                            onErrorDismiss = {
                                eventPublisher(UiEvent.ClearErrorMessage)
                                onClose()
                            },
                            onDataChanged = { eventPublisher(UiEvent.ActivationDataChanged(data = it)) },
                            onActivationCodeRequest = { eventPublisher(UiEvent.ActivationRequest(data = it)) },
                        )

                        WalletActivationStatus.PendingCodeConfirmation -> WalletCodeActivationInput(
                            working = uiState.working,
                            error = uiState.error,
                            email = uiState.data.email,
                            isKeyboardVisible = isKeyboardVisible,
                            onCodeChanged = { eventPublisher(UiEvent.ClearErrorMessage) },
                            onCodeConfirmation = { code -> eventPublisher(UiEvent.Activate(code = code)) },
                        )

                        WalletActivationStatus.ActivationSuccess -> WalletActivationSuccess(
                            lightningAddress = uiState.activatedLightningAddress.orEmpty(),
                            onDone = onClose,
                        )
                    }
                },
            )
        },
    )
}

private const val STEP_ANIMATION_DURATION = 256

@Composable
private fun StepContainerWithActionButton(
    onActionClick: () -> Unit,
    actionButtonText: String,
    actionButtonEnabled: Boolean,
    actionButtonLoading: Boolean = false,
    containerContent: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        containerContent()

        PrimalLoadingButton(
            modifier = Modifier.fillMaxWidth(fraction = 0.8f),
            enabled = actionButtonEnabled && !actionButtonLoading,
            loading = actionButtonLoading,
            onClick = onActionClick,
            text = actionButtonText,
        )
    }
}

@ExperimentalComposeUiApi
@Composable
private fun WalletActivationDataInput(
    data: WalletActivationData,
    working: Boolean,
    error: Throwable?,
    onErrorDismiss: () -> Unit,
    onDataChanged: (WalletActivationData) -> Unit,
    onActivationCodeRequest: (WalletActivationData) -> Unit,
    isKeyboardVisible: Boolean,
) {
    var name by rememberSaveable { mutableStateOf(data.name) }
    var email by rememberSaveable { mutableStateOf(data.email) }
    var country by remember { mutableStateOf(data.country) }
    var state by remember { mutableStateOf(data.state) }
    val activationDataSnapshot = {
        WalletActivationData(name = name, email = email, country = country, state = state)
    }

    val countries = rememberListOfCountries()
    val availableStates by remember {
        derivedStateOf {
            countries.find { it.code == country?.code }?.states
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current
    var countrySelectionVisible by remember { mutableStateOf(false) }
    var stateSelectionVisible by remember { mutableStateOf(false) }

    if (countrySelectionVisible) {
        RegionSelectionBottomSheet(
            regions = countries,
            title = stringResource(id = R.string.wallet_activation_country_picker_title),
            onRegionClick = {
                country = it
                onDataChanged(activationDataSnapshot())
            },
            onDismissRequest = { countrySelectionVisible = false },
        )
    } else if (stateSelectionVisible) {
        RegionSelectionBottomSheet(
            regions = countries.find { it.code == country?.code }?.states ?: emptyList(),
            title = stringResource(id = R.string.wallet_activation_state_picker_title),
            onRegionClick = {
                state = it
                onDataChanged(activationDataSnapshot())
            },
            onDismissRequest = { stateSelectionVisible = false },
        )
    }

    WalletActivationErrorHandler(
        error = error,
        fallbackMessage = stringResource(id = R.string.app_generic_error),
        onErrorDismiss = onErrorDismiss,
    )

    StepContainerWithActionButton(
        actionButtonText = stringResource(id = R.string.wallet_activation_next_button),
        actionButtonEnabled = activationDataSnapshot().isValid(availableStates),
        actionButtonLoading = working,
        onActionClick = { onActivationCodeRequest(activationDataSnapshot()) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
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
                    .padding(vertical = 32.dp),
                text = stringResource(id = R.string.wallet_activation_pending_data_hint),
                textAlign = TextAlign.Center,
                color = AppTheme.colorScheme.onSurface,
                style = AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )

            WalletOutlinedTextField(
                modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                value = name,
                onValueChange = {
                    name = it
                    onDataChanged(activationDataSnapshot())
                },
                placeholderText = stringResource(id = R.string.wallet_activation_your_name),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            WalletOutlinedTextField(
                modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                value = email,
                onValueChange = {
                    email = it.trim()
                    onDataChanged(activationDataSnapshot())
                },
                placeholderText = stringResource(id = R.string.wallet_activation_your_email_address),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = if (activationDataSnapshot().isValid(availableStates)) ImeAction.Go else ImeAction.None,
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (activationDataSnapshot().isValid(availableStates)) {
                            keyboardController?.hide()
                            onActivationCodeRequest(activationDataSnapshot())
                        }
                    },
                ),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .animateContentSize(),
            ) {
                WalletOutlinedTextField(
                    modifier = Modifier.weight(0.75f),
                    onClick = { countrySelectionVisible = true },
                    value = country?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholderText = stringResource(id = R.string.wallet_activation_your_country_of_residence),
                )

                if (!availableStates.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.width(16.dp))

                    WalletOutlinedTextField(
                        modifier = Modifier.weight(0.25f),
                        onClick = { stateSelectionVisible = true },
                        value = state?.code?.split("-")?.last() ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholderText = stringResource(id = R.string.wallet_activation_state),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalSerializationApi::class)
@Composable
private fun rememberListOfCountries(): List<Country> {
    val assets = LocalContext.current.assets
    val regionsInputStream = try {
        assets.open("regions.json")
    } catch (error: IOException) {
        Timber.w(error)
        return emptyList()
    }
    return remember { NostrJson.decodeFromStream<Regions>(regionsInputStream).toListOfCountries() }
}

private fun WalletActivationData.isValid(availableStates: List<State>?): Boolean {
    return name.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches() &&
        country != null && (availableStates.isNullOrEmpty() || state != null)
}

@ExperimentalComposeUiApi
@Composable
private fun WalletCodeActivationInput(
    working: Boolean,
    error: Throwable?,
    email: String,
    onCodeChanged: () -> Unit,
    onCodeConfirmation: (String) -> Unit,
    isKeyboardVisible: Boolean,
) {
    var code by rememberSaveable { mutableStateOf("") }

    val keyboardController = LocalSoftwareKeyboardController.current

    StepContainerWithActionButton(
        actionButtonText = stringResource(id = R.string.wallet_activation_finish_button),
        actionButtonEnabled = code.isCodeValid(),
        actionButtonLoading = working,
        onActionClick = {
            keyboardController?.hide()
            onCodeConfirmation(code)
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
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

            WalletOutlinedTextField(
                modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                value = code,
                onValueChange = {
                    if (it.isDigitsOnly()) {
                        code = it.trim()
                        onCodeChanged()
                    }
                },
                placeholderText = stringResource(id = R.string.wallet_activation_your_code),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = if (code.isCodeValid()) ImeAction.Go else ImeAction.Done,
                    keyboardType = KeyboardType.Number,
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (code.isCodeValid()) {
                            keyboardController?.hide()
                            onCodeConfirmation(code)
                        }
                    },
                ),
            )

            WalletErrorText(
                error = error,
                fallbackMessage = stringResource(id = R.string.wallet_activation_error_invalid_code),
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private const val CODE_LENGTH = 6

private fun String.isCodeValid() = this.isDigitsOnly() && this.length == CODE_LENGTH

@Composable
private fun WalletActivationSuccess(lightningAddress: String, onDone: () -> Unit) {
    AdjustTemporarilySystemBarColors(statusBarColor = walletSuccessColor, navigationBarColor = walletSuccessColor)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = walletSuccessColor),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
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
            modifier = Modifier.width(200.dp),
            text = stringResource(id = R.string.wallet_activation_done_button),
            containerColor = walletSuccessDimColor,
            onClick = onDone,
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

@Composable
private fun WalletOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember {
        if (onClick != null) {
            object : MutableInteractionSource {
                override val interactions = MutableSharedFlow<Interaction>(
                    extraBufferCapacity = 16,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST,
                )

                override suspend fun emit(interaction: Interaction) {
                    if (interaction is PressInteraction.Release) {
                        onClick()
                    }
                    interactions.emit(interaction)
                }

                override fun tryEmit(interaction: Interaction): Boolean {
                    return interactions.tryEmit(interaction)
                }
            }
        } else {
            MutableInteractionSource()
        }
    }

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        colors = PrimalDefaults.outlinedTextFieldColors(),
        shape = AppTheme.shapes.large,
        singleLine = true,
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        placeholder = {
            Text(
                text = placeholderText.lowercase(),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodyLarge,
            )
        },
        interactionSource = interactionSource,
    )
}

@ExperimentalComposeUiApi
@Preview
@Composable
private fun PreviewWalletActivationDataInput() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            WalletActivationDataInput(
                data = WalletActivationData(name = "alex", email = "alex@primal.net"),
                working = false,
                error = null,
                isKeyboardVisible = false,
                onErrorDismiss = { },
                onDataChanged = { },
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
                isKeyboardVisible = false,
                onCodeChanged = { },
                onCodeConfirmation = { },
            )
        }
    }
}
