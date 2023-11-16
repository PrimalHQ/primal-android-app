package net.primal.android.auth.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.isValidNostrPrivateKey
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onClose: () -> Unit,
    onLoginSuccess: (String) -> Unit,
) {
    LaunchedEffect(viewModel, onLoginSuccess) {
        viewModel.effect.collect {
            when (it) {
                is LoginContract.SideEffect.LoginSuccess -> onLoginSuccess(it.pubkey)
            }
        }
    }

    LaunchedErrorHandler(viewModel = viewModel)

    val uiState = viewModel.state.collectAsState()
    LoginScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    state: LoginContract.UiState,
    eventPublisher: (LoginContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.login_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            LoginContent(
                state = state,
                paddingValues = paddingValues,
                onLogin = {
                    eventPublisher(LoginContract.UiEvent.LoginEvent(nostrKey = it))
                },
            )
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun LoginContent(
    state: LoginContract.UiState,
    paddingValues: PaddingValues,
    onLogin: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current

    var nsecValue by remember { mutableStateOf("") }
    val isValidNsec by remember { derivedStateOf { nsecValue.isValidNostrPrivateKey() } }

    val pasteKey = {
        val clipboardText = clipboardManager.getText()?.text.orEmpty().trim()
        if (clipboardText.isValidNostrPrivateKey()) {
            nsecValue = clipboardText
        }
    }

    LaunchedEffect(Unit) { pasteKey() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(paddingValues = paddingValues)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 64.dp)
                .weight(1f)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier
                    .padding(all = 16.dp)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.login_description),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodyLarge,
                fontSize = 20.sp,
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = nsecValue,
                onValueChange = { input -> nsecValue = input.trim() },
                placeholder = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = R.string.nsec),
                        textAlign = TextAlign.Center,
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    )
                },
                supportingText = {
                    if (nsecValue.isNotEmpty()) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = if (isValidNsec) {
                                stringResource(id = R.string.login_valid_nsec_key)
                            } else {
                                stringResource(id = R.string.login_invalid_nsec_key)
                            },
                            textAlign = TextAlign.Center,
                            color = if (isValidNsec) {
                                AppTheme.extraColorScheme.successBright
                            } else {
                                AppTheme.colorScheme.error
                            },
                        )
                    }
                },
                isError = nsecValue.isNotEmpty() && !isValidNsec,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = if (isValidNsec) ImeAction.Go else ImeAction.Default,
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        if (isValidNsec) {
                            keyboardController?.hide()
                            onLogin(nsecValue)
                        }
                    },
                ),
                visualTransformation = PasswordVisualTransformation(),
                textStyle = AppTheme.typography.titleLarge.copy(
                    fontSize = 28.sp,
                ),
                colors = PrimalDefaults.outlinedTextFieldColors(
                    focusedBorderColor = if (nsecValue.isEmpty()) {
                        AppTheme.extraColorScheme.surfaceVariantAlt1
                    } else {
                        AppTheme.extraColorScheme.successBright.copy(alpha = 0.5f)
                    },
                    unfocusedBorderColor = if (nsecValue.isEmpty()) {
                        AppTheme.extraColorScheme.surfaceVariantAlt1
                    } else {
                        AppTheme.extraColorScheme.successBright.copy(alpha = 0.5f)
                    },
                ),
                shape = AppTheme.shapes.extraLarge,
            )
        }

        PrimalLoadingButton(
            text = if (isValidNsec) {
                stringResource(id = R.string.login_button_continue)
            } else {
                stringResource(id = R.string.login_button_paste_key)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp)
                .align(alignment = Alignment.CenterHorizontally),
            loading = state.loading,
            enabled = !state.loading,
            onClick = {
                if (isValidNsec) {
                    keyboardController?.hide()
                    onLogin(nsecValue)
                } else {
                    pasteKey()
                }
            },
        )
    }
}

@Composable
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
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        LoginScreen(
            state = LoginContract.UiState(loading = false),
            eventPublisher = {},
            onClose = {},
        )
    }
}
