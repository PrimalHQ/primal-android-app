package net.primal.android.wallet.backup

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.BiometricPrompt
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

private const val BACKUP_PATTERN_ROWS = 4
private const val BACKUP_PATTERN_COLS = 3
private const val SEED_PHRASE_COLUMNS = 3

@Composable
fun WalletBackupScreen(viewModel: WalletBackupViewModel, callbacks: WalletBackupContract.ScreenCallbacks) {
    val state = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, callbacks) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is WalletBackupContract.SideEffect.BackupCompleted -> callbacks.onBackupComplete()
            }
        }
    }

    WalletBackupScreen(
        state = state.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = callbacks.onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletBackupScreen(
    state: WalletBackupContract.UiState,
    eventPublisher: (WalletBackupContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val handleBackEvent = {
        when (state.currentStep) {
            WalletBackupContract.BackupStep.Welcome -> onClose()
            WalletBackupContract.BackupStep.Confirm -> onClose()
            else -> eventPublisher(WalletBackupContract.UiEvent.RequestPreviousStep)
        }
    }

    BackHandler {
        handleBackEvent()
    }

    var showBiometricPrompt by rememberSaveable { mutableStateOf(false) }

    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = when (state.currentStep) {
                    WalletBackupContract.BackupStep.Welcome -> stringResource(R.string.wallet_backup_title)
                    WalletBackupContract.BackupStep.SeedPhrase -> stringResource(R.string.wallet_backup_title)
                    WalletBackupContract.BackupStep.Verify -> stringResource(R.string.wallet_backup_verify_title)
                    WalletBackupContract.BackupStep.Confirm -> stringResource(R.string.wallet_backup_success_title)
                },
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = handleBackEvent,
            )
        },
        content = { paddingValues ->
            if (showBiometricPrompt) {
                BiometricPrompt(
                    onAuthSuccess = {
                        showBiometricPrompt = false
                        eventPublisher(WalletBackupContract.UiEvent.ProceedToPhraseDisplay)
                    },
                    onAuthDismiss = {
                        showBiometricPrompt = false
                    },
                )
            }

            AnimatedContent(
                targetState = state.currentStep,
                modifier = Modifier
                    .padding(paddingValues)
                    .imePadding(),
                label = "BackupSteps",
            ) { step ->
                when (step) {
                    WalletBackupContract.BackupStep.Welcome -> WelcomeStep(
                        onContinue = {
                            showBiometricPrompt = true
                        },
                        onCancel = onClose,
                    )
                    WalletBackupContract.BackupStep.SeedPhrase -> SeedPhraseStep(
                        words = state.seedPhrase,
                        onContinue = { eventPublisher(WalletBackupContract.UiEvent.ProceedToVerification) },
                        onCancel = onClose,
                    )
                    WalletBackupContract.BackupStep.Verify -> VerifyStep(
                        words = state.seedPhrase,
                        indicesToVerify = state.verificationIndices,
                        onVerify = { eventPublisher(WalletBackupContract.UiEvent.ProceedToConfirmation) },
                    )
                    WalletBackupContract.BackupStep.Confirm -> ConfirmStep(
                        onFinish = { eventPublisher(WalletBackupContract.UiEvent.CompleteBackup) },
                    )
                }
            }
        },
    )
}

@Composable
private fun WelcomeStep(onContinue: () -> Unit, onCancel: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, fill = false),
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            Text(
                text = stringResource(R.string.wallet_backup_welcome_description),
                style = AppTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = AppTheme.colorScheme.onSurface,
            )

            Spacer(modifier = Modifier.height(60.dp))

            RecoveryGraphic(modifier = Modifier.width(260.dp))

            Spacer(modifier = Modifier.height(80.dp))

            val importantText = stringResource(R.string.wallet_backup_important)
            val disclaimerPart1 = stringResource(R.string.wallet_backup_disclaimer_part_one)
            val disclaimerPart2 = stringResource(R.string.wallet_backup_disclaimer_part_two)

            val disclaimerText = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, textDecoration = TextDecoration.Underline)) {
                    append(importantText.uppercase())
                }
                append(" ")
                append(disclaimerPart1)
                append(" ")
                withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline)) {
                    append(disclaimerPart2)
                }
            }

            Text(
                text = disclaimerText,
                style = AppTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = AppTheme.colorScheme.onSurface,
            )
        }

        Column(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PrimalLoadingButton(
                text = stringResource(R.string.wallet_backup_button_i_understand),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(R.string.wallet_backup_button_cancel),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
private fun RecoveryGraphic(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(30.dp),
        modifier = modifier,
    ) {
        repeat(BACKUP_PATTERN_ROWS) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                repeat(BACKUP_PATTERN_COLS) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(10.dp)
                            .background(
                                color = AppTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                shape = CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun SeedPhraseStep(
    words: List<String>,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(110.dp))

        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            words.chunked(SEED_PHRASE_COLUMNS).forEachIndexed { rowIndex, rowWords ->
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    rowWords.forEachIndexed { colIndex, word ->
                        val wordIndex = rowIndex * SEED_PHRASE_COLUMNS + colIndex + 1
                        SeedWordItem(
                            modifier = Modifier.weight(1f),
                            index = wordIndex,
                            word = word,
                        )
                    }
                    if (rowWords.size < SEED_PHRASE_COLUMNS) {
                        repeat(SEED_PHRASE_COLUMNS - rowWords.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = stringResource(R.string.wallet_backup_seed_description_one),
            style = AppTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = AppTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(26.dp))
        Text(
            text = stringResource(R.string.wallet_backup_seed_description_two),
            style = AppTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = AppTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(
            modifier = Modifier.padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PrimalLoadingButton(
                text = stringResource(R.string.wallet_backup_button_written_down),
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(R.string.wallet_backup_button_cancel),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                )
            }
        }
    }
}

@Composable
private fun SeedWordItem(
    modifier: Modifier = Modifier,
    index: Int,
    word: String,
) {
    Row(
        modifier = modifier
            .border(
                width = 1.dp,
                color = AppTheme.colorScheme.onPrimary.copy(alpha = 0.50f),
                shape = RoundedCornerShape(8.dp),
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .padding(start = 12.dp),
            text = word,
            style = AppTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = AppTheme.colorScheme.onSurface,
        )
        Text(
            modifier = Modifier.padding(bottom = 10.dp, end = 5.dp),
            text = index.toString(),
            style = AppTheme.typography.labelSmall,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
    }
}

@Composable
private fun VerifyStep(
    words: List<String>,
    indicesToVerify: List<Int>,
    onVerify: () -> Unit,
) {
    val inputs = remember { mutableStateMapOf<Int, String>() }

    val isVerifyEnabled by remember(inputs.toMap(), indicesToVerify, words) {
        derivedStateOf {
            if (indicesToVerify.isEmpty() || words.isEmpty()) return@derivedStateOf false
            indicesToVerify.all { index ->
                val input = inputs[index]?.trim()
                val actual = words.getOrNull(index)
                input.equals(actual, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        indicesToVerify.forEach { index ->
            val correctWord = words.getOrNull(index) ?: ""
            val input = inputs[index] ?: ""
            val isValid = input.equals(correctWord, ignoreCase = true)
            val isError = input.isNotEmpty() && !isValid

            Column(modifier = Modifier.padding(bottom = 30.dp)) {
                Text(
                    text = stringResource(R.string.wallet_backup_verify_word_label, index + 1),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = input,
                    onValueChange = { inputs[index] = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    isError = isError,
                    singleLine = true,
                    shape = AppTheme.shapes.extraLarge,
                    colors = verifyTextFieldColors(isValid = isValid),
                    trailingIcon = {
                        VerifyTrailingIcon(isValid = isValid, text = input)
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    textStyle = AppTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimalLoadingButton(
            text = stringResource(R.string.wallet_backup_button_verify),
            onClick = onVerify,
            enabled = isVerifyEnabled,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ConfirmStep(onFinish: () -> Unit) {
    val checks = remember { mutableStateListOf(false, false, false) }

    val checkLabels = listOf(
        stringResource(R.string.wallet_backup_confirm_check_1),
        stringResource(R.string.wallet_backup_confirm_check_2),
        stringResource(R.string.wallet_backup_confirm_check_3),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        Text(
            text = stringResource(R.string.wallet_backup_confirm_description),
            style = AppTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = AppTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(70.dp))

        Text(
            text = stringResource(R.string.wallet_backup_confirm_subtitle),
            style = AppTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center,
            color = AppTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(33.dp))

        checkLabels.forEachIndexed { index, label ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if (index < checks.size) {
                            checks[index] = !checks[index]
                        }
                    }
                    .padding(vertical = 12.dp),
            ) {
                RadioButton(
                    modifier = Modifier.size(24.dp),
                    selected = checks.getOrElse(index) { false },
                    onClick = {
                        if (index < checks.size) {
                            checks[index] = !checks[index]
                        }
                    },
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    modifier = Modifier.padding(top = 5.dp),
                    text = label,
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colorScheme.onSurface,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        PrimalLoadingButton(
            text = stringResource(R.string.wallet_backup_button_finish),
            onClick = onFinish,
            enabled = checks.all { it },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

private val VerifySuccessColor = Color(0xFF2FD058)
private val VerifyErrorColor = Color(0xFFFE3D2F)
private val VerifyDefaultBorderColor = Color(0xFF333333)

@Composable
private fun verifyTextFieldColors(isValid: Boolean) =
    OutlinedTextFieldDefaults.colors(
        focusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        unfocusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        errorContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        cursorColor = AppTheme.colorScheme.onSurface,
        focusedBorderColor = if (isValid) VerifySuccessColor.copy(alpha = 0.5f) else VerifyDefaultBorderColor,
        unfocusedBorderColor = if (isValid) VerifySuccessColor.copy(alpha = 0.5f) else VerifyDefaultBorderColor,
        errorBorderColor = VerifyErrorColor.copy(alpha = 0.5f),
    )

@Composable
private fun VerifyTrailingIcon(isValid: Boolean, text: String) {
    if (text.isNotEmpty()) {
        Text(
            text = if (isValid) {
                stringResource(R.string.wallet_backup_verify_correct)
            } else {
                stringResource(R.string.wallet_backup_verify_incorrect)
            },
            style = AppTheme.typography.bodyMedium,
            color = if (isValid) VerifySuccessColor else VerifyErrorColor,
            modifier = Modifier.padding(end = 12.dp),
        )
    }
}
