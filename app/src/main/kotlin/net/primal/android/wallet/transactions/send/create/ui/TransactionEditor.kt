package net.primal.android.wallet.transactions.send.create.ui

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.UiDensityMode
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.detectUiDensityModeFromMaxHeight
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletBitcoinPayment
import net.primal.android.core.compose.isCompactOrLower
import net.primal.android.core.compose.numericpad.PrimalNumericPad
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.dashboard.ui.BtcAmountText
import net.primal.android.wallet.numericPadContentTransformAnimation
import net.primal.android.wallet.transactions.send.create.CreateTransactionContract
import net.primal.android.wallet.transactions.send.create.ellipsizeLnUrl
import net.primal.android.wallet.transactions.send.create.ellipsizeOnChainAddress
import net.primal.android.wallet.transactions.send.create.ui.model.MiningFeeUi
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
fun TransactionEditor(
    modifier: Modifier,
    state: CreateTransactionContract.UiState,
    paddingValues: PaddingValues,
    eventPublisher: (CreateTransactionContract.UiEvent) -> Unit,
    onCancelClick: () -> Unit,
) {
    val keyboardVisible by keyboardVisibilityAsState()

    var noteRecipientText by remember { mutableStateOf(state.transaction.noteRecipient ?: "") }
    var noteSelfText by remember { mutableStateOf(state.transaction.noteSelf ?: "") }
    var isNumericPadOn by remember { mutableStateOf(state.isNotInvoice()) }

    val sendPayment = {
        eventPublisher(
            CreateTransactionContract.UiEvent.SendTransaction(
                noteRecipient = noteRecipientText.ifEmpty { null },
                noteSelf = noteSelfText.ifEmpty { null },
                miningFeeTierId = state.resolveSelectedMiningFee()?.id,
            ),
        )
    }

    val density = LocalDensity.current
    var uiMode by remember { mutableStateOf<UiDensityMode?>(null) }

    val layoutDirection = LocalLayoutDirection.current
    Column(
        modifier = modifier
            .padding(
                top = paddingValues.calculateTopPadding(),
                bottom = if (keyboardVisible) 8.dp else paddingValues.calculateBottomPadding(),
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection),
            )
            .onSizeChanged {
                with(density) {
                    if (uiMode == null) {
                        uiMode = it.height
                            .toDp()
                            .detectUiDensityModeFromMaxHeight()
                    }
                }
            },
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TransactionHeaderColumn(
                modifier = Modifier.fillMaxWidth(),
                uiMode = uiMode ?: UiDensityMode.Normal,
                state = state,
                keyboardVisible = keyboardVisible,
                onAmountClick = {
                    if (state.isNotInvoice()) {
                        isNumericPadOn = true
                    }
                },
            )

            TransactionMainContent(
                state = state,
                isNumericPadOn = isNumericPadOn,
                keyboardVisible = keyboardVisible,
                noteRecipientText = noteRecipientText,
                onNoteRecipientTextChanged = { text -> noteRecipientText = text },
                noteSelfText = noteSelfText,
                onNoteSelfTextChanged = { text -> noteSelfText = text },
                onAmountChanged = {
                    eventPublisher(
                        CreateTransactionContract.UiEvent.AmountChanged(amountInSats = it),
                    )
                },
                onMiningFeeChanged = {
                    eventPublisher(
                        CreateTransactionContract.UiEvent.MiningFeeChanged(tierId = it.id),
                    )
                },
                onReloadMiningFees = {
                    eventPublisher(CreateTransactionContract.UiEvent.ReloadMiningFees)
                },
            )
        }

        var minAmountAlertVisible by remember { mutableStateOf(false) }
        val actionsRowHidden = uiMode.isCompactOrLower() && keyboardVisible
        if (!actionsRowHidden) {
            TransactionActionsRow(
                state = state,
                uiMode = uiMode ?: UiDensityMode.Normal,
                keyboardVisible = keyboardVisible,
                isNumericPadOn = isNumericPadOn,
                onCancelClick = onCancelClick,
                onActionClick = {
                    if (isNumericPadOn) {
                        if (state.isTxSatisfiesMinAmount()) {
                            isNumericPadOn = false
                            eventPublisher(CreateTransactionContract.UiEvent.AmountApplied)
                        } else {
                            minAmountAlertVisible = true
                        }
                    } else {
                        sendPayment()
                    }
                },
            )
        }

        if (minAmountAlertVisible) {
            MinTxAmountAlertDialog(onDismissRequest = { minAmountAlertVisible = false })
        }
    }
}

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
private fun TransactionMainContent(
    state: CreateTransactionContract.UiState,
    isNumericPadOn: Boolean,
    keyboardVisible: Boolean,
    noteRecipientText: String,
    onNoteRecipientTextChanged: (String) -> Unit,
    noteSelfText: String,
    onNoteSelfTextChanged: (String) -> Unit,
    onAmountChanged: (String) -> Unit,
    onMiningFeeChanged: (MiningFeeUi) -> Unit,
    onReloadMiningFees: () -> Unit,
) {
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
                    onAmountInSatsChanged = { newAmount -> onAmountChanged(newAmount) },
                )
            }

            false -> {
                when {
                    state.transaction.isLightningTx() -> {
                        LightningTxNotes(
                            state = state,
                            noteRecipientText = noteRecipientText,
                            onNoteRecipientTextChanged = onNoteRecipientTextChanged,
                            noteSelfText = noteSelfText,
                            onNoteSelfTextChanged = onNoteSelfTextChanged,
                            keyboardVisible = keyboardVisible,
                        )
                    }

                    state.transaction.isBtcTx() -> {
                        BitcoinTxSelfNoteAndMiningFee(
                            state = state,
                            keyboardVisible = keyboardVisible,
                            noteSelfText = noteSelfText,
                            onNoteSelfTextChanged = onNoteSelfTextChanged,
                            onMiningFeeChanged = onMiningFeeChanged,
                            onReloadMiningFees = onReloadMiningFees,
                        )
                    }
                }
            }
        }
    }
}

@Suppress("MagicNumber")
@Composable
private fun TransactionActionsRow(
    state: CreateTransactionContract.UiState,
    uiMode: UiDensityMode,
    keyboardVisible: Boolean,
    isNumericPadOn: Boolean,
    onCancelClick: () -> Unit,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .padding(
                bottom = if (keyboardVisible) {
                    0.dp
                } else {
                    when (uiMode) {
                        UiDensityMode.Normal -> 32.dp
                        UiDensityMode.Comfortable -> 8.dp
                        else -> 4.dp
                    }
                },
            ),
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
            enabled = if (isNumericPadOn) {
                !state.isAmountZero()
            } else {
                state.isTxSatisfiesMinAmount() && state.isSelectedMiningFeeValidForBtcTx()
            },
            text = if (isNumericPadOn) {
                stringResource(id = R.string.wallet_create_transaction_next_numeric_pad_button)
            } else {
                stringResource(id = R.string.wallet_create_transaction_send_button)
            },
            onClick = onActionClick,
        )
    }
}

@Composable
private fun TransactionHeaderColumn(
    modifier: Modifier,
    uiMode: UiDensityMode,
    state: CreateTransactionContract.UiState,
    keyboardVisible: Boolean,
    onAmountClick: () -> Unit,
) {
    val verticalPadding = animateDpAsState(
        targetValue = if (keyboardVisible) {
            4.dp
        } else {
            when (uiMode) {
                UiDensityMode.Normal, UiDensityMode.Comfortable -> 16.dp
                else -> 4.dp
            }
        },
        label = "verticalMargin",
    )
    val avatarSize = animateDpAsState(
        targetValue = if (keyboardVisible) {
            when (uiMode) {
                UiDensityMode.Normal -> 64.dp
                UiDensityMode.Comfortable -> 48.dp
                UiDensityMode.Compact -> 40.dp
                else -> 0.dp
            }
        } else {
            88.dp
        },
        label = "avatarSize",
    )
    val iconSize = animateDpAsState(targetValue = if (keyboardVisible) 36.dp else 56.dp, label = "iconSize")
    val headerSpacing = animateDpAsState(
        targetValue = if (keyboardVisible) {
            0.dp
        } else {
            when (uiMode) {
                UiDensityMode.Normal -> 32.dp
                UiDensityMode.Comfortable -> 24.dp
                else -> 8.dp
            }
        },
        label = "headerSpacing",
    )
    val amountSpacing = animateDpAsState(
        targetValue = if (keyboardVisible) {
            16.dp
        } else {
            when (uiMode) {
                UiDensityMode.Normal -> 48.dp
                UiDensityMode.Comfortable -> 32.dp
                else -> 24.dp
            }
        },
        label = "amountSpacing",
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(headerSpacing.value / 2))

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
            UniversalAvatarThumbnail(
                modifier = Modifier.padding(vertical = verticalPadding.value),
                avatarCdnImage = state.profileAvatarCdnImage,
                avatarSize = avatarSize.value,
                legendaryCustomization = state.profileLegendaryCustomization,
            )
        }

        val title = state.resolveTransactionTitle()
        if (!title.isNullOrEmpty()) {
            Text(
                modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                text = title,
                textAlign = TextAlign.Center,
                color = AppTheme.colorScheme.onSurface,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            )
        }

        val subtitle = state.resolveTransactionSubtitle()
        if (!subtitle.isNullOrEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 32.dp),
                text = subtitle,
                textAlign = TextAlign.Center,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                style = AppTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(headerSpacing.value))

        BtcAmountText(
            modifier = Modifier
                .padding(
                    start = when (uiMode) {
                        UiDensityMode.Normal -> 32.dp
                        UiDensityMode.Comfortable -> 26.dp
                        else -> 18.dp
                    },
                )
                .height(
                    when (uiMode) {
                        UiDensityMode.Normal -> 72.dp
                        UiDensityMode.Comfortable -> 64.dp
                        else -> 56.dp
                    },
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onAmountClick,
                ),
            amountInBtc = state.transaction.amountSats.toLong().toBtc().toBigDecimal(),
            textSize = 48.sp,
        )

        Spacer(modifier = Modifier.height(amountSpacing.value))
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            text = noteRecipientText.ifEmpty { state.transaction.lnInvoiceData?.description ?: "" },
            textAlign = TextAlign.Center,
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
    onGoAction: (() -> Unit)? = null,
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
                onGoAction?.invoke()
            },
        ),
    )
}

@ExperimentalMaterial3Api
@ExperimentalComposeUiApi
@Composable
private fun BitcoinTxSelfNoteAndMiningFee(
    state: CreateTransactionContract.UiState,
    keyboardVisible: Boolean,
    noteSelfText: String,
    onNoteSelfTextChanged: (String) -> Unit,
    onMiningFeeChanged: (MiningFeeUi) -> Unit,
    onReloadMiningFees: () -> Unit,
) {
    val selectedFeeTierIndex = state.selectedFeeTierIndex
    var isMiningFeeSelectionVisible by remember { mutableStateOf(false) }

    Column {
        NoteToSelfTextField(
            noteSelfText = noteSelfText,
            onNoteSelfTextChanged = onNoteSelfTextChanged,
            keyboardVisible = keyboardVisible,
        )

        Spacer(modifier = Modifier.height(16.dp))

        MiningFeeRow(
            miningFee = state.resolveSelectedMiningFee(),
            fetching = state.fetchingMiningFees,
            clickable = selectedFeeTierIndex != null,
            isTxAmountEligibleForSelectedTier = state.isSelectedMiningFeeValidForBtcTx(),
            keyboardVisible = keyboardVisible,
            onClick = { isMiningFeeSelectionVisible = true },
            onRetry = onReloadMiningFees,
        )
    }

    if (isMiningFeeSelectionVisible && selectedFeeTierIndex != null) {
        MiningFeeBottomSheet(
            fees = state.miningFeeTiers,
            selectedFeeIndex = selectedFeeTierIndex,
            onMiningFeeChanged = onMiningFeeChanged,
            onDismissRequest = {
                isMiningFeeSelectionVisible = false
            },
        )
    }
}

@Composable
private fun MiningFeeRow(
    miningFee: MiningFeeUi?,
    fetching: Boolean,
    clickable: Boolean,
    isTxAmountEligibleForSelectedTier: Boolean,
    keyboardVisible: Boolean,
    onClick: () -> Unit,
    onRetry: () -> Unit,
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
                .clip(AppTheme.shapes.extraLarge)
                .clickable(enabled = clickable, onClick = onClick),
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
                    style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp),
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
                    modifier = Modifier
                        .padding(end = 24.dp)
                        .clickable { onRetry() },
                    style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                    color = AppTheme.colorScheme.error,
                    text = stringResource(id = R.string.wallet_create_transaction_mining_fees_not_fetched),
                )
            }
        }

        if (!isTxAmountEligibleForSelectedTier && miningFee?.minAmountInBtc != null && !keyboardVisible) {
            val minAmountInSatsFormatted = numberFormat.format(miningFee.minAmountInBtc.toSats().toLong())
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                style = AppTheme.typography.bodySmall,
                text = stringResource(
                    id = R.string.wallet_create_transaction_min_tx_amount_for_selected_fee_tier_error,
                    "$minAmountInSatsFormatted sats",
                    miningFee.label,
                ),
                color = AppTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun MinTxAmountAlertDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(id = R.string.wallet_create_transaction_min_tx_amount_title),
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.wallet_create_transaction_min_tx_amount_text),
            )
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = stringResource(id = android.R.string.ok),
                )
            }
        },
    )
}

private fun CreateTransactionContract.UiState.isAmountZero(): Boolean {
    return transaction.amountSats.toBigDecimal() == BigDecimal.ZERO
}

private val MIN_BTC_TX_AMOUNT = BigDecimal.valueOf(21_000)

private fun CreateTransactionContract.UiState.isTxSatisfiesMinAmount(): Boolean {
    val txAmount = transaction.amountSats.toBigDecimal()
    val minTxAmount = if (transaction.isBtcTx()) MIN_BTC_TX_AMOUNT else BigDecimal.ONE
    return txAmount >= minTxAmount
}

private fun CreateTransactionContract.UiState.isSelectedMiningFeeValidForBtcTx(): Boolean {
    if (!transaction.isBtcTx()) return true

    val miningFee = selectedFeeTierIndex?.let { miningFeeTiers.getOrNull(it) }
    val txAmountInSats = transaction.amountSats.toBigDecimal()
    val miningFeeMinTxAmountInSats = miningFee?.minAmountInBtc?.toSats()?.toLong()?.toBigDecimal() ?: BigDecimal.ZERO

    return miningFee != null && txAmountInSats >= miningFeeMinTxAmountInSats
}

private fun CreateTransactionContract.UiState.resolveSelectedMiningFee(): MiningFeeUi? {
    return selectedFeeTierIndex?.let { index -> miningFeeTiers.getOrNull(index) }
}

@Composable
private fun CreateTransactionContract.UiState.resolveTransactionTitle(): String? {
    return profileDisplayName
        ?: transaction.targetLud16
        ?: transaction.targetLnUrl?.ellipsizeLnUrl()
        ?: transaction.targetOnChainAddress?.let {
            stringResource(id = R.string.wallet_create_transaction_bitcoin_address)
        }
}

@Composable
private fun CreateTransactionContract.UiState.resolveTransactionSubtitle(): String? {
    return profileLightningAddress ?: transaction.targetOnChainAddress?.ellipsizeOnChainAddress()
}
