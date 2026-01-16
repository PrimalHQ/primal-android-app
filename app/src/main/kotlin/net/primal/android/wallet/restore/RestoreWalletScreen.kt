package net.primal.android.wallet.restore

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.wallet.restore.RestoreWalletContract.RestoreStage
import net.primal.android.wallet.restore.RestoreWalletContract.UiEvent
import net.primal.android.wallet.restore.RestoreWalletContract.UiState
import net.primal.android.wallet.restore.RestoreWalletContract.UiState.MnemonicValidation

@Composable
fun RestoreWalletScreen(viewModel: RestoreWalletViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, onClose) {
        viewModel.effect.collect {
            when (it) {
                RestoreWalletContract.SideEffect.RestoreSuccess -> onClose()
            }
        }
    }

    RestoreWalletScreen(
        state = uiState.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RestoreWalletScreen(
    state: UiState,
    eventPublisher: (UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.wallet_restore_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            AnimatedContent(
                targetState = state.currentStage,
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "RestoreWalletContent",
            ) { stage ->
                when (stage) {
                    RestoreStage.MnemonicInput -> {
                        MnemonicInputStage(
                            state = state,
                            eventPublisher = eventPublisher,
                            contentPadding = paddingValues,
                        )
                    }
                    RestoreStage.Restoring -> {
                        RestoringStage()
                    }
                }
            }
        },
    )
}

@Composable
private fun MnemonicInputStage(
    state: UiState,
    eventPublisher: (UiEvent) -> Unit,
    contentPadding: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp)
            .navigationBarsPadding()
            .imePadding(),
    ) {
        Column(
            modifier = Modifier.weight(1.0f),
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            val outlineColor = when (state.mnemonicValidation) {
                is MnemonicValidation.Invalid -> AppTheme.colorScheme.error
                is MnemonicValidation.Valid -> AppTheme.extraColorScheme.successBright
                else -> AppTheme.colorScheme.outline
            }

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                value = state.mnemonic,
                onValueChange = {
                    eventPublisher(UiEvent.MnemonicChange(mnemonic = it))
                },
                textStyle = AppTheme.typography.bodyLarge,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.wallet_restore_enter_recovery_phrase_placeholder),
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                        style = AppTheme.typography.bodyLarge,
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                shape = AppTheme.shapes.large,
                colors = PrimalDefaults.outlinedTextFieldColors(
                    focusedBorderColor = outlineColor,
                    unfocusedBorderColor = outlineColor,
                ),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                ValidationMessage(state = state)
            }

            Text(
                modifier = Modifier.padding(top = 32.dp),
                text = stringResource(id = R.string.wallet_restore_helper_text),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                textAlign = TextAlign.Center,
            )
        }

        PrimalLoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            text = stringResource(id = R.string.wallet_restore_button),
            enabled = state.mnemonicValidation is MnemonicValidation.Valid,
            onClick = {
                eventPublisher(UiEvent.RestoreWalletClick)
            },
        )
    }
}

@Composable
private fun ValidationMessage(state: UiState) {
    val text: String?
    val color: Color

    when (val validation = state.mnemonicValidation) {
        is MnemonicValidation.Invalid -> {
            text = validation.message
            color = AppTheme.colorScheme.error
        }
        is MnemonicValidation.Valid -> {
            text = stringResource(id = R.string.wallet_restore_valid_recovery_phrase)
            color = AppTheme.extraColorScheme.successBright
        }
        else -> {
            text = null
            color = Color.Unspecified
        }
    }

    if (text != null) {
        Text(
            text = text,
            style = AppTheme.typography.bodySmall,
            color = color,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun RestoringStage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.height(124.dp)) {
                PrimalLoadingSpinner(size = 124.dp)
            }
            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = stringResource(id = R.string.wallet_restore_restoring_your_wallet),
                style = AppTheme.typography.titleLarge,
            )
        }
    }
}

@Preview
@Composable
fun PreviewRestoreWalletScreen() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        RestoreWalletScreen(
            state = UiState(mnemonicValidation = MnemonicValidation.Valid),
            eventPublisher = {},
            onClose = {},
        )
    }
}
