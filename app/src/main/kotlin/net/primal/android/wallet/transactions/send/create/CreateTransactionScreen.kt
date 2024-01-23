package net.primal.android.wallet.transactions.send.create

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.OutlinedTextField
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
import net.primal.android.core.compose.icons.primaliconpack.WalletError
import net.primal.android.core.compose.icons.primaliconpack.WalletSuccess
import net.primal.android.core.compose.numericpad.PrimalNumericPad
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.dashboard.ui.BtcAmountText
import net.primal.android.wallet.numericPadContentTransformAnimation
import net.primal.android.wallet.transactions.send.create.CreateTransactionContract.UiEvent.SendTransaction
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
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
            if (state.transaction.status != TransactionStatus.Sent) {
                PrimalTopAppBar(
                    title = when (state.transaction.status) {
                        TransactionStatus.Draft -> stringResource(id = R.string.wallet_create_transaction_draft_title)
                        TransactionStatus.Sending -> stringResource(
                            id = R.string.wallet_create_transaction_sending_title,
                        )

                        TransactionStatus.Sent -> stringResource(id = R.string.wallet_create_transaction_success_title)
                        TransactionStatus.Failed -> stringResource(id = R.string.wallet_create_transaction_failed_title)
                    },
                    navigationIcon = PrimalIcons.ArrowBack,
                    showDivider = false,
                    onNavigationIconClick = onClose,
                )
            }
        },
        content = { paddingValues ->
            when (state.transaction.status) {
                TransactionStatus.Draft -> {
                    TransactionEditor(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        state = state,
                        eventPublisher = eventPublisher,
                        onCancelClick = onClose,
                    )
                }

                TransactionStatus.Sending -> {
                    TransactionSending(
                        amountInSats = state.transaction.amountSats.toLong(),
                        receiver = state.profileLightningAddress
                            ?: state.transaction.targetLud16
                            ?: state.transaction.targetLnUrl?.ellipsizeLnurl()
                            ?: state.transaction.lnInvoiceData?.description,
                    )
                }

                TransactionStatus.Sent -> {
                    TransactionSuccess(
                        amountInSats = state.transaction.amountSats.toLong(),
                        receiver = state.profileLightningAddress
                            ?: state.transaction.targetLud16
                            ?: state.transaction.targetLnUrl?.ellipsizeLnurl()
                            ?: state.transaction.lnInvoiceData?.description,
                        onDoneClick = onClose,
                    )
                }

                TransactionStatus.Failed -> {
                    TransactionFailed(
                        errorMessage = state.error?.message ?: stringResource(id = R.string.app_generic_error),
                        onCloseClick = onClose,
                    )
                }
            }
        },
    )
}

private fun String.ellipsizeLnurl() = this.ellipsizeMiddle(size = 8).lowercase()

@ExperimentalComposeUiApi
@Composable
private fun TransactionEditor(
    modifier: Modifier,
    state: CreateTransactionContract.UiState,
    eventPublisher: (CreateTransactionContract.UiEvent) -> Unit,
    onCancelClick: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardVisible by keyboardVisibilityAsState()

    var noteText by remember { mutableStateOf(state.transaction.note ?: "") }
    var isNumericPadOn by remember {
        mutableStateOf(state.transaction.lnInvoiceData == null)
    }

    val sendPayment = {
        eventPublisher(SendTransaction(note = noteText.ifEmpty { null }))
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            AvatarThumbnail(
                modifier = Modifier.padding(vertical = 16.dp),
                avatarCdnImage = state.profileAvatarCdnImage,
                avatarSize = 88.dp,
            )

            val receiver = state.profileDisplayName
                ?: state.transaction.targetLud16
                ?: state.transaction.targetLnUrl?.ellipsizeLnurl()
                ?: ""
            if (receiver.isNotEmpty()) {
                Text(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                    text = receiver,
                    color = AppTheme.colorScheme.onSurface,
                    style = AppTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                )
            }

            if (state.profileLightningAddress != null) {
                Text(
                    text = state.profileLightningAddress,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            BtcAmountText(
                modifier = Modifier
                    .padding(start = 32.dp)
                    .height(72.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (state.transaction.lnInvoiceData == null) {
                                isNumericPadOn = true
                            }
                        },
                    ),
                amountInBtc = state.transaction.amountSats.toLong().toBtc().toBigDecimal(),
                textSize = 48.sp,
            )

            Spacer(modifier = Modifier.height(48.dp))

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
                            onAmountInSatsChanged = {
                                eventPublisher(
                                    CreateTransactionContract.UiEvent.AmountChanged(
                                        amountInSats = it,
                                    ),
                                )
                            },
                        )
                    }

                    false -> {
                        if (state.transaction.lnInvoiceData == null) {
                            OutlinedTextField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 32.dp),
                                value = noteText,
                                onValueChange = { input -> noteText = input },
                                colors = PrimalDefaults.outlinedTextFieldColors(),
                                shape = AppTheme.shapes.extraLarge,
                                maxLines = 3,
                                placeholder = {
                                    if (!keyboardVisible) {
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
                                    imeAction = ImeAction.Go,
                                ),
                                keyboardActions = KeyboardActions(
                                    onGo = {
                                        keyboardController?.hide()
                                        sendPayment()
                                    },
                                ),
                            )
                        } else {
                            Text(
                                modifier = Modifier.padding(horizontal = 32.dp),
                                text = noteText.ifEmpty { state.transaction.lnInvoiceData.description ?: "" },
                                color = AppTheme.colorScheme.onSurface,
                                style = AppTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }

        if (!keyboardVisible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(bottom = 32.dp),
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
                    enabled = state.transaction.amountSats.toBigDecimal() > BigDecimal.ZERO,
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
