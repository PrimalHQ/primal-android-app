package net.primal.android.auth.login

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.auth.compose.DefaultOnboardingAvatar
import net.primal.android.auth.compose.OnboardingButton
import net.primal.android.auth.compose.defaultOnboardingAvatarBackground
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.UiDensityMode
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.detectUiDensityModeFromMaxHeight
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.isCompactOrLower
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.utils.isExternalSignerInstalled
import net.primal.android.core.utils.isValidNostrPrivateKey
import net.primal.android.core.utils.isValidNostrPublicKey
import net.primal.android.signer.event.buildAppSpecificDataEvent
import net.primal.android.signer.launchGetPublicKey
import net.primal.android.signer.launchSignEvent
import net.primal.android.signer.rememberAmberPubkeyLauncher
import net.primal.android.signer.rememberAmberSignerLauncher
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.LoginType

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onClose: () -> Unit,
    onLoginSuccess: () -> Unit,
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(viewModel, onLoginSuccess) {
        viewModel.effect.collect {
            when (it) {
                is LoginContract.SideEffect.LoginSuccess -> onLoginSuccess()
            }
        }
    }

    LaunchedErrorHandler(viewModel = viewModel)

    val uiState = viewModel.state.collectAsState()
    LoginScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = {
            keyboardController?.hide()
            onClose()
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: LoginContract.UiState,
    eventPublisher: (LoginContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val signLauncher = rememberAmberSignerLauncher { nostrEvent ->
        eventPublisher(LoginContract.UiEvent.LoginRequestEvent(nostrEvent = nostrEvent))
    }

    val pubkeyLauncher = rememberAmberPubkeyLauncher { pubkey ->
        eventPublisher(LoginContract.UiEvent.UpdateLoginInput(newInput = pubkey, loginType = LoginType.ExternalSigner))
        signLauncher.launchSignEvent(event = buildAppSpecificDataEvent(pubkey = pubkey))
    }

    BackHandler(enabled = state.loading) { }
    ColumnWithBackground(
        backgroundPainter = painterResource(id = R.drawable.onboarding_spot2),
    ) { size ->
        val uiMode = size.height.detectUiDensityModeFromMaxHeight()

        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
            ),
            title = {
                Text(text = stringResource(id = R.string.login_title))
            },
            navigationIcon = {
                if (!state.loading) {
                    AppBarIcon(
                        icon = PrimalIcons.ArrowBack,
                        onClick = onClose,
                    )
                }
            },
        )

        LoginContent(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = 32.dp),
            state = state,
            uiMode = uiMode,
            onLoginInputChanged = { eventPublisher(LoginContract.UiEvent.UpdateLoginInput(newInput = it)) },
            onLoginClick = { eventPublisher(LoginContract.UiEvent.LoginRequestEvent()) },
            onLoginWithAmberClick = { pubkeyLauncher.launchGetPublicKey() },
        )
    }
}

@Composable
fun LoginContent(
    modifier: Modifier = Modifier,
    state: LoginContract.UiState,
    uiMode: UiDensityMode,
    onLoginInputChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
    onLoginWithAmberClick: () -> Unit,
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    val keyboardVisible by keyboardVisibilityAsState()

    fun pasteFromClipboard() {
        val clipboardText = clipboardManager.getText()?.text.orEmpty().trim()
        if (clipboardText.isValidNostrPrivateKey() || clipboardText.isValidNostrPublicKey()) {
            onLoginInputChanged(clipboardText)
            keyboardController?.hide()
        }
    }

    LaunchedEffect(Unit) { pasteFromClipboard() }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            LoginInputFieldContent(
                state = state,
                uiMode = uiMode,
                keyboardVisible = keyboardVisible,
                onLoginInputChanged = onLoginInputChanged,
                onLoginClick = onLoginClick,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .wrapContentHeight(align = Alignment.Bottom),
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.Bottom),
        ) {
            OnboardingButton(
                text = when {
                    state.isValidKey -> stringResource(id = R.string.login_button_sign_in)
                    state.loginInput.isEmpty() -> stringResource(id = R.string.login_button_paste_your_key)
                    else -> stringResource(id = R.string.login_button_paste_new_key)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .align(alignment = Alignment.CenterHorizontally),
                loading = state.loading,
                enabled = !state.loading,
                onClick = {
                    keyboardController?.hide()
                    if (state.isValidKey) {
                        onLoginClick()
                    } else {
                        pasteFromClipboard()
                    }
                },
            )

            if (isExternalSignerInstalled(context = context)) {
                PrimalLoadingButton(
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth(),
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    onClick = onLoginWithAmberClick,
                    text = "Login with Amber",
                )
            }
        }
    }
}

@Composable
private fun LoginInputFieldContent(
    state: LoginContract.UiState,
    uiMode: UiDensityMode,
    keyboardVisible: Boolean,
    onLoginInputChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        AnimatedContent(
            modifier = Modifier.weight(weight = 0.5f),
            targetState = state.profileDetails,
            label = "LoginHeader",
        ) { profileDetails ->
            when {
                profileDetails != null && !state.fetchingProfileDetails -> {
                    ProfileDetailsColumn(
                        modifier = Modifier.fillMaxWidth(),
                        uiMode = uiMode,
                        keyboardVisible = keyboardVisible,
                        profileDetails = profileDetails,
                    )
                }

                else -> {
                    EnterYourKeyNotice(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        loginInput = state.loginInput,
                    )
                }
            }
        }

        Box(
            modifier = Modifier.weight(weight = 0.5f),
            contentAlignment = Alignment.TopCenter,
        ) {
            LoginInputField(
                loginInput = state.loginInput,
                isValidKey = state.isValidKey,
                keyboardVisible = keyboardVisible,
                onLoginInputChanged = onLoginInputChanged,
                onLoginClick = onLoginClick,
            )
        }
    }
}

@Composable
private fun LoginInputField(
    modifier: Modifier = Modifier,
    isValidKey: Boolean,
    loginInput: String,
    keyboardVisible: Boolean,
    onLoginInputChanged: (String) -> Unit,
    onLoginClick: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val shape = if (keyboardVisible) AppTheme.shapes.medium else AppTheme.shapes.extraLarge
    Row(
        modifier = modifier
            .background(color = Color.White, shape = shape)
            .padding(all = 2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (!keyboardVisible) Modifier.height(56.dp) else Modifier),
            value = if (keyboardVisible) loginInput else "",
            onValueChange = { input -> onLoginInputChanged(input.trim()) },
            placeholder = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = when {
                        loginInput.isEmpty() -> stringResource(id = R.string.nsec_or_npub)
                        else -> "••••••••••••••••••••••••••••••••••••••"
                    },
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.bodyLarge.copy(
                        fontSize = if (loginInput.isEmpty()) 16.sp else 28.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                    color = if (loginInput.isEmpty()) {
                        AppTheme.extraColorScheme.onSurfaceVariantAlt4
                    } else {
                        Color.Black
                    },
                )
            },
            isError = loginInput.isNotEmpty() && !isValidKey,
            keyboardOptions = KeyboardOptions(
                imeAction = if (isValidKey) ImeAction.Go else ImeAction.Default,
                keyboardType = KeyboardType.Password,
            ),
            keyboardActions = KeyboardActions(
                onGo = {
                    if (isValidKey) {
                        keyboardController?.hide()
                        onLoginClick()
                    }
                },
            ),
            visualTransformation = if (keyboardVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            textStyle = AppTheme.typography.titleLarge.copy(
                fontSize = if (keyboardVisible) 16.sp else 28.sp,
                lineHeight = if (keyboardVisible) 16.sp else 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
            ),
            colors = loginTextFieldColors(keyboardVisible, loginInput),
            shape = shape,
        )
    }
}

@Composable
private fun loginTextFieldColors(keyboardVisible: Boolean, loginInput: String) =
    PrimalDefaults.outlinedTextFieldColors(
        cursorColor = if (keyboardVisible) AppTheme.colorScheme.primary else Color.White,
        focusedContainerColor = Color.White,
        focusedBorderColor = when {
            loginInput.isEmpty() -> Color.White
            else -> AppTheme.extraColorScheme.successBright
        },
        unfocusedContainerColor = Color.White,
        unfocusedBorderColor = when {
            loginInput.isEmpty() -> Color.White
            else -> AppTheme.extraColorScheme.successBright
        },
        disabledContainerColor = Color.White,
        disabledBorderColor = when {
            loginInput.isEmpty() -> Color.White
            else -> AppTheme.extraColorScheme.successBright
        },
        errorContainerColor = Color.White,
        errorBorderColor = AppTheme.colorScheme.error,
    )

@Composable
private fun EnterYourKeyNotice(modifier: Modifier = Modifier, loginInput: String) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter,
    ) {
        Text(
            modifier = Modifier
                .padding(vertical = 16.dp, horizontal = 32.dp)
                .fillMaxWidth(),
            text = when {
                loginInput.isEmpty() -> stringResource(id = R.string.login_enter_nsec_key)
                else -> stringResource(id = R.string.login_invalid_nsec_key)
            },
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ProfileDetailsColumn(
    modifier: Modifier = Modifier,
    uiMode: UiDensityMode,
    keyboardVisible: Boolean,
    profileDetails: ProfileDetailsUi,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
    ) {
        if (!(uiMode.isCompactOrLower() && keyboardVisible)) {
            UniversalAvatarThumbnail(
                avatarCdnImage = profileDetails.avatarCdnImage,
                avatarSize = 100.dp,
                hasBorder = profileDetails.avatarCdnImage != null,
                fallbackBorderColor = Color.White,
                legendaryCustomization = profileDetails.premiumDetails?.legendaryCustomization,
                backgroundColor = defaultOnboardingAvatarBackground,
                defaultAvatar = { DefaultOnboardingAvatar() },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            modifier = Modifier.padding(bottom = 4.dp),
            text = profileDetails.userDisplayName,
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = Color.White,
        )

        Text(
            text = profileDetails.internetIdentifier ?: "",
            style = AppTheme.typography.bodyLarge,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
@Deprecated("Replace with SnackbarErrorHandler")
fun LaunchedErrorHandler(viewModel: LoginViewModel) {
    val genericMessage = stringResource(id = R.string.app_generic_error)
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.state
            .filter { it.error != null }
            .map { it.error }
            .filterNotNull()
            .collect {
                uiScope.launch {
                    Toast.makeText(
                        context,
                        genericMessage,
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
    }
}

@Preview
@Composable
fun PreviewLoginScreen() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        LoginScreen(
            state = LoginContract.UiState(loading = false),
            eventPublisher = {},
            onClose = {},
        )
    }
}
