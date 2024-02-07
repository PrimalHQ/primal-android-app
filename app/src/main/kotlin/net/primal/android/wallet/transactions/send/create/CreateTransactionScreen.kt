package net.primal.android.wallet.transactions.send.create

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.text.NumberFormat
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.ApplySystemBarColors
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.applySystemColors
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.WalletBitcoinPayment
import net.primal.android.core.compose.icons.primaliconpack.WalletError
import net.primal.android.core.compose.icons.primaliconpack.WalletSuccess
import net.primal.android.core.compose.numericpad.PrimalNumericPad
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.dashboard.ui.BtcAmountText
import net.primal.android.wallet.domain.DraftTxStatus
import net.primal.android.wallet.numericPadContentTransformAnimation
import net.primal.android.wallet.transactions.send.create.CreateTransactionContract.UiEvent.SendTransaction
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats
import net.primal.android.wallet.walletSuccessColor
import net.primal.android.wallet.walletSuccessContentColor
import net.primal.android.wallet.walletSuccessDimColor

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CreateTransactionScreen(viewModel: CreateTransactionViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()

    CreateTransactionScreen(
        state = state.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
    )
}

@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun CreateTransactionScreen(
    state: CreateTransactionContract.UiState,
    eventPublisher: (CreateTransactionContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            if (state.transaction.status != DraftTxStatus.Sent) {
                PrimalTopAppBar(
                    title = when (state.transaction.status) {
                        DraftTxStatus.Draft -> stringResource(id = R.string.wallet_create_transaction_draft_title)
                        DraftTxStatus.Sending -> stringResource(
                            id = R.string.wallet_create_transaction_sending_title,
                        )

                        DraftTxStatus.Sent -> stringResource(id = R.string.wallet_create_transaction_success_title)
                        DraftTxStatus.Failed -> stringResource(id = R.string.wallet_create_transaction_failed_title)
                    },
                    navigationIcon = PrimalIcons.ArrowBack,
                    showDivider = false,
                    onNavigationIconClick = onClose,
                )
            }
        },
        content = { paddingValues ->
            when (state.transaction.status) {
                DraftTxStatus.Draft -> {
                    TransactionEditor(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        state = state,
                        eventPublisher = eventPublisher,
                        onCancelClick = onClose,
                    )
                }

                DraftTxStatus.Sending -> {
                    TransactionSending(
                        amountInSats = state.transaction.amountSats.toLong(),
                        receiver = state.profileLightningAddress
                            ?: state.transaction.targetLud16
                            ?: state.transaction.targetLnUrl?.ellipsizeLnUrl()
                            ?: state.transaction.lnInvoiceData?.description,
                    )
                }

                DraftTxStatus.Sent -> {
                    TransactionSuccess(
                        amountInSats = state.transaction.amountSats.toLong(),
                        receiver = state.profileLightningAddress
                            ?: state.transaction.targetLud16
                            ?: state.transaction.targetLnUrl?.ellipsizeLnUrl()
                            ?: state.transaction.lnInvoiceData?.description,
                        onDoneClick = onClose,
                    )
                }

                DraftTxStatus.Failed -> {
                    TransactionFailed(
                        errorMessage = state.error?.message ?: stringResource(id = R.string.app_generic_error),
                        onCloseClick = onClose,
                    )
                }
            }
        },
    )
}

private fun String.ellipsizeLnUrl() = this.ellipsizeMiddle(size = 8).lowercase()

@ExperimentalComposeUiApi
@Composable
private fun TransactionEditor(
    modifier: Modifier,
    state: CreateTransactionContract.UiState,
    eventPublisher: (CreateTransactionContract.UiEvent) -> Unit,
    onCancelClick: () -> Unit,
) {
    val keyboardVisible by keyboardVisibilityAsState()

    var noteRecipientText by remember { mutableStateOf(state.transaction.noteRecipient ?: "") }
    var noteSelfText by remember { mutableStateOf(state.transaction.noteSelf ?: "") }
    var isNumericPadOn by remember { mutableStateOf(state.isNotInvoice()) }

    val sendPayment = {
        eventPublisher(
            SendTransaction(
                noteRecipient = noteRecipientText.ifEmpty { null },
                noteSelf = noteSelfText.ifEmpty { null },
                miningFeeTierId = state.resolveSelectedMiningFee()?.id,
            ),
        )
    }

    val headerSpacing = animateDpAsState(targetValue = if (keyboardVisible) 0.dp else 32.dp, label = "headerSpacing")
    val amountSpacing = animateDpAsState(targetValue = if (keyboardVisible) 16.dp else 48.dp, label = "amountSpacing")

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(headerSpacing.value / 2))

            TransactionHeader(state = state, keyboardVisible = keyboardVisible)

            Spacer(modifier = Modifier.height(headerSpacing.value))

            BtcAmountText(
                modifier = Modifier
                    .padding(start = 32.dp)
                    .height(72.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (state.isNotInvoice()) {
                                isNumericPadOn = true
                            }
                        },
                    ),
                amountInBtc = state.transaction.amountSats.toLong().toBtc().toBigDecimal(),
                textSize = 48.sp,
            )

            Spacer(modifier = Modifier.height(amountSpacing.value))

            AnimatedContent(
                targetState = isNumericPadOn,
                transitionSpec = { numericPadContentTransformAnimation },
                label = "NumericPadAndNote",
            ) {
                when (it) {
                    true -> Column(
                        modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        PrimalNumericPad(
                            modifier = Modifier.fillMaxWidth(),
                            amountInSats = state.transaction.amountSats,
                            onAmountInSatsChanged = { newAmount ->
                                eventPublisher(
                                    CreateTransactionContract.UiEvent.AmountChanged(amountInSats = newAmount),
                                )
                            },
                        )
                    }

                    false -> {
                        when {
                            state.transaction.isLightningTx() -> {
                                LightningTxNotes(
                                    state = state,
                                    noteRecipientText = noteRecipientText,
                                    onNoteRecipientTextChanged = { text -> noteRecipientText = text },
                                    noteSelfText = noteSelfText,
                                    onNoteSelfTextChanged = { text -> noteSelfText = text },
                                    keyboardVisible = keyboardVisible,
                                    sendPayment = sendPayment,
                                )
                            }

                            state.transaction.isBtcTx() -> {
                                BitcoinTxSelfNoteAndMiningFee(
                                    state = state,
                                    noteSelfText = noteSelfText,
                                    onNoteSelfTextChanged = { text -> noteSelfText = text },
                                    keyboardVisible = keyboardVisible,
                                    sendPayment = sendPayment,
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .padding(bottom = if (keyboardVisible) 0.dp else 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            PrimalLoadingButton(
                modifier = if (isNumericPadOn) Modifier.weight(1f) else Modifier.width(0.dp),
                text = stringResource(id = R.string.wallet_create_transaction_cancel_numeric_pad_button),
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                contentColor = AppTheme.colorScheme.onSurface,
                onClick = onCancelClick,
            )

            Spacer(
                modifier = Modifier
                    .animateContentSize()
                    .width(if (isNumericPadOn) 16.dp else 0.dp),
            )

            PrimalLoadingButton(
                modifier = Modifier.weight(1f),
                enabled = if (isNumericPadOn) !state.isAmountZero() else state.isTxReady(),
                text = if (isNumericPadOn) {
                    stringResource(id = R.string.wallet_create_transaction_next_numeric_pad_button)
                } else {
                    stringResource(id = R.string.wallet_create_transaction_send_button)
                },
                onClick = {
                    if (isNumericPadOn) {
                        isNumericPadOn = false
                    } else {
                        sendPayment()
                    }
                },
            )
        }
    }
}

private fun CreateTransactionContract.UiState.isAmountZero(): Boolean {
    return transaction.amountSats.toBigDecimal() == BigDecimal.ZERO
}

private fun CreateTransactionContract.UiState.isTxReady(): Boolean {
    val txAmount = transaction.amountSats.toBigDecimal()
    val minTxAmount = if (transaction.isBtcTx()) {
        minBtcTxAmountInSats?.toBigDecimal() ?: BigDecimal.ONE
    } else {
        BigDecimal.ONE
    }
    return txAmount >= minTxAmount
}

@Composable
private fun TransactionHeader(
    state: CreateTransactionContract.UiState,
    keyboardVisible: Boolean,
    modifier: Modifier = Modifier,
) {
    val verticalPadding = animateDpAsState(targetValue = if (keyboardVisible) 4.dp else 16.dp, label = "verticalMargin")
    val avatarSize = animateDpAsState(targetValue = if (keyboardVisible) 56.dp else 88.dp, label = "avatarSize")
    val iconSize = animateDpAsState(targetValue = if (keyboardVisible) 36.dp else 56.dp, label = "iconSize")

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.transaction.targetOnChainAddress != null) {
            Box(
                modifier = Modifier
                    .padding(vertical = verticalPadding.value)
                    .size(avatarSize.value)
                    .clip(CircleShape)
                    .background(color = AppTheme.colorScheme.onPrimary),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    modifier = Modifier.size(iconSize.value),
                    imageVector = PrimalIcons.WalletBitcoinPayment,
                    contentDescription = null,
                )
            }
        } else {
            AvatarThumbnail(
                modifier = Modifier.padding(vertical = verticalPadding.value),
                avatarCdnImage = state.profileAvatarCdnImage,
                avatarSize = avatarSize.value,
            )
        }

        val title = state.profileDisplayName
            ?: state.transaction.targetLud16
            ?: state.transaction.targetLnUrl?.ellipsizeLnUrl()
            ?: state.transaction.targetOnChainAddress?.let {
                stringResource(id = R.string.wallet_create_transaction_bitcoin_address)
            }
            ?: ""

        if (title.isNotEmpty()) {
            Text(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                text = title,
                color = AppTheme.colorScheme.onSurface,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            )
        }

        val subtitle = state.profileLightningAddress
            ?: state.transaction.targetOnChainAddress?.ellipsizeMiddle(size = 16)

        if (!subtitle.isNullOrEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 32.dp),
                text = subtitle,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                style = AppTheme.typography.bodyMedium,
            )
        }
    }
}

@ExperimentalComposeUiApi
@Composable
private fun LightningTxNotes(
    state: CreateTransactionContract.UiState,
    noteRecipientText: String,
    onNoteRecipientTextChanged: (String) -> Unit,
    noteSelfText: String,
    onNoteSelfTextChanged: (String) -> Unit,
    keyboardVisible: Boolean,
    sendPayment: () -> Unit,
) {
    Column {
        LightningNoteToRecipientTextField(
            state = state,
            noteRecipientText = noteRecipientText,
            onNoteRecipientTextChanged = onNoteRecipientTextChanged,
            keyboardVisible = keyboardVisible,
        )

        Spacer(
            modifier = Modifier
                .height(16.dp),
        )

        NoteToSelfTextField(
            noteSelfText = noteSelfText,
            onNoteSelfTextChanged = onNoteSelfTextChanged,
            keyboardVisible = keyboardVisible,
            onGoAction = sendPayment,
        )
    }
}

@Composable
private fun LightningNoteToRecipientTextField(
    state: CreateTransactionContract.UiState,
    noteRecipientText: String,
    onNoteRecipientTextChanged: (String) -> Unit,
    keyboardVisible: Boolean,
) {
    var isFocused by remember { mutableStateOf(false) }

    if (state.isNotInvoice()) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .onFocusChanged {
                    isFocused = it.isFocused
                },
            value = noteRecipientText,
            onValueChange = onNoteRecipientTextChanged,
            colors = PrimalDefaults.outlinedTextFieldColors(),
            shape = AppTheme.shapes.extraLarge,
            maxLines = if (keyboardVisible) 2 else 3,
            placeholder = {
                if (!keyboardVisible || !isFocused) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = if (state.profileDisplayName != null) {
                            stringResource(
                                id = R.string.wallet_create_transaction_note_hint_with_recipient,
                                state.profileDisplayName,
                            )
                        } else {
                            stringResource(id = R.string.wallet_create_transaction_note_hint)
                        },
                        textAlign = TextAlign.Center,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        style = AppTheme.typography.bodyMedium,
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next,
            ),
        )
    } else {
        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = noteRecipientText.ifEmpty { state.transaction.lnInvoiceData?.description ?: "" },
            color = AppTheme.colorScheme.onSurface,
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@ExperimentalComposeUiApi
@Composable
private fun NoteToSelfTextField(
    noteSelfText: String,
    onNoteSelfTextChanged: (String) -> Unit,
    keyboardVisible: Boolean,
    onGoAction: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }

    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .onFocusChanged {
                isFocused = it.isFocused
            },
        value = noteSelfText,
        onValueChange = onNoteSelfTextChanged,
        colors = PrimalDefaults.outlinedTextFieldColors(),
        shape = AppTheme.shapes.extraLarge,
        maxLines = if (keyboardVisible) 2 else 3,
        placeholder = {
            if (!keyboardVisible || !isFocused) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.wallet_create_transaction_note_to_self),
                    textAlign = TextAlign.Center,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    style = AppTheme.typography.bodyMedium,
                )
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Go,
        ),
        keyboardActions = KeyboardActions(
            onGo = {
                keyboardController?.hide()
                onGoAction()
            },
        ),
    )
}

@ExperimentalComposeUiApi
@Composable
private fun BitcoinTxSelfNoteAndMiningFee(
    state: CreateTransactionContract.UiState,
    noteSelfText: String,
    onNoteSelfTextChanged: (String) -> Unit,
    keyboardVisible: Boolean,
    sendPayment: () -> Unit,
) {
    Column {
        NoteToSelfTextField(
            noteSelfText = noteSelfText,
            onNoteSelfTextChanged = onNoteSelfTextChanged,
            keyboardVisible = keyboardVisible,
            onGoAction = sendPayment,
        )

        Spacer(modifier = Modifier.height(16.dp))

        MiningFeeRow(
            miningFee = state.resolveSelectedMiningFee(),
            fetching = state.fetchingMiningFees,
            minBtcTxInSats = state.minBtcTxAmountInSats,
            onClick = {
            },
        )
    }
}

private fun CreateTransactionContract.UiState.resolveSelectedMiningFee(): MiningFeeUi? {
    return selectedFeeTierIndex?.let { index -> miningFeeTiers.getOrNull(index) }
}

@Composable
private fun MiningFeeRow(
    miningFee: MiningFeeUi?,
    fetching: Boolean,
    minBtcTxInSats: String?,
    onClick: () -> Unit,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    val maxHeight = OutlinedTextFieldDefaults.MinHeight
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight)
                .padding(horizontal = 32.dp)
                .background(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    shape = AppTheme.shapes.extraLarge,
                )
                .clickable(enabled = true, onClick = onClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.padding(start = 24.dp),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.onPrimary,
                text = stringResource(id = R.string.wallet_create_transaction_mining_fee),
            )

            if (miningFee != null) {
                val formattedAmountInSats = numberFormat.format(miningFee.feeInBtc.toSats().toLong())
                Text(
                    modifier = Modifier.padding(end = 24.dp),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.onPrimary,
                    text = "${miningFee.label}: $formattedAmountInSats sats",
                )
            } else if (fetching) {
                Box(
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .padding(vertical = 8.dp)
                        .width(maxHeight),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    PrimalLoadingSpinner(size = maxHeight)
                }
            } else {
                Text(
                    modifier = Modifier.padding(end = 24.dp),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.onPrimary,
                    text = stringResource(
                        id = R.string.wallet_create_transaction_mining_fee_free_label,
                        "0 sats",
                    ),
                )
            }
        }

        if (miningFee == null && !fetching) {
            val minSatsForBtcTxFormatted = minBtcTxInSats?.let { numberFormat.format(it.toLong()) }
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .padding(top = 16.dp),
                style = AppTheme.typography.bodySmall,
                text = stringResource(
                    id = R.string.wallet_create_transaction_min_btc_tx_amount,
                    "$minSatsForBtcTxFormatted sats",
                ),
                color = AppTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
fun TransactionSending(amountInSats: Long, receiver: String?) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TransactionStatusColumn(
            icon = null,
            headlineText = stringResource(id = R.string.wallet_create_transaction_sending_headline),
            supportText = if (receiver != null) {
                stringResource(
                    id = R.string.wallet_create_transaction_transaction_description,
                    numberFormat.format(amountInSats),
                    receiver,
                )
            } else {
                stringResource(
                    id = R.string.wallet_create_transaction_transaction_description_lite,
                    numberFormat.format(amountInSats),
                )
            },
        )

        PrimalLoadingButton(
            modifier = Modifier
                .width(200.dp)
                .alpha(0f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionSuccess(
    amountInSats: Long,
    receiver: String?,
    onDoneClick: () -> Unit,
) {
    var isClosing by remember { mutableStateOf(false) }
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    val primalTheme = LocalPrimalTheme.current
    val localView = LocalView.current

    val closingSequence = {
        applySystemColors(primalTheme = primalTheme, localView = localView)
        isClosing = true
        onDoneClick()
    }

    val backgroundColor = if (!isClosing) walletSuccessColor else AppTheme.colorScheme.surface

    ApplySystemBarColors(statusBarColor = walletSuccessColor, navigationBarColor = walletSuccessColor)

    BackHandler {
        closingSequence()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = backgroundColor),
    ) {
        PrimalTopAppBar(
            modifier = if (isClosing) Modifier.alpha(0.0f) else Modifier,
            title = stringResource(id = R.string.wallet_create_transaction_success_title),
            textColor = walletSuccessContentColor,
            navigationIcon = PrimalIcons.ArrowBack,
            showDivider = false,
            onNavigationIconClick = { closingSequence() },
            navigationIconTintColor = if (!isClosing) walletSuccessContentColor else AppTheme.colorScheme.onSurface,
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = backgroundColor,
                scrolledContainerColor = backgroundColor,
            ),
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TransactionStatusColumn(
                icon = PrimalIcons.WalletSuccess,
                iconTint = walletSuccessContentColor,
                headlineText = stringResource(id = R.string.wallet_create_transaction_success_headline),
                supportText = if (receiver != null) {
                    stringResource(
                        id = R.string.wallet_create_transaction_transaction_description,
                        numberFormat.format(amountInSats),
                        receiver,
                    )
                } else {
                    stringResource(
                        id = R.string.wallet_create_transaction_transaction_description_lite,
                        numberFormat.format(amountInSats),
                    )
                },
                textColor = walletSuccessContentColor,
            )

            PrimalLoadingButton(
                modifier = Modifier
                    .width(200.dp)
                    .then(if (isClosing) Modifier.alpha(0.0f) else Modifier),
                text = stringResource(id = R.string.wallet_create_transaction_done_button),
                containerColor = walletSuccessDimColor,
                onClick = { closingSequence() },
            )
        }
    }
}

@Composable
fun TransactionFailed(errorMessage: String, onCloseClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TransactionStatusColumn(
            icon = PrimalIcons.WalletError,
            iconTint = AppTheme.colorScheme.error,
            headlineText = stringResource(id = R.string.wallet_create_transaction_failed_headline),
            supportText = errorMessage,
        )

        PrimalLoadingButton(
            modifier = Modifier.width(200.dp),
            text = stringResource(id = R.string.wallet_create_transaction_close_button),
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            onClick = onCloseClick,
        )
    }
}

@Composable
private fun TransactionStatusColumn(
    icon: ImageVector?,
    iconTint: Color = LocalContentColor.current,
    headlineText: String,
    supportText: String,
    textColor: Color = LocalContentColor.current,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        if (icon != null) {
            Image(
                modifier = Modifier
                    .size(160.dp)
                    .padding(vertical = 16.dp),
                imageVector = icon,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = iconTint),
            )
        } else {
            Box(modifier = Modifier.size(160.dp)) {
                PrimalLoadingSpinner(size = 160.dp)
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .padding(top = 32.dp, bottom = 8.dp),
            text = headlineText,
            textAlign = TextAlign.Center,
            color = textColor,
            style = AppTheme.typography.headlineSmall,
        )

        Text(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .padding(vertical = 32.dp),
            text = supportText,
            textAlign = TextAlign.Center,
            color = textColor,
            style = AppTheme.typography.bodyLarge.copy(
                lineHeight = 28.sp,
            ),
        )
    }
}
