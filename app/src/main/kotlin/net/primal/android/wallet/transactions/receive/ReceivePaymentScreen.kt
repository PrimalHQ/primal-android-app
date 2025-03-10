package net.primal.android.wallet.transactions.receive

import android.graphics.drawable.Drawable
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.style.BitmapScale
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.wajahatkarim.flippable.Flippable
import com.wajahatkarim.flippable.rememberFlipController
import java.math.BigDecimal
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.numericpad.PrimalNumericPad
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.dashboard.ui.BtcAmountText
import net.primal.android.wallet.domain.CurrencyMode
import net.primal.android.wallet.domain.Network
import net.primal.android.wallet.domain.not
import net.primal.android.wallet.repository.isValidExchangeRate
import net.primal.android.wallet.transactions.receive.ReceivePaymentContract.UiState
import net.primal.android.wallet.transactions.receive.model.NetworkDetails
import net.primal.android.wallet.transactions.receive.model.PaymentDetails
import net.primal.android.wallet.transactions.receive.tabs.ReceivePaymentTab
import net.primal.android.wallet.ui.TransactionAmountText
import net.primal.android.wallet.ui.WalletTabsBar
import net.primal.android.wallet.ui.WalletTabsHeight
import net.primal.android.wallet.utils.CurrencyConversionUtils.formatAsString
import net.primal.android.wallet.utils.CurrencyConversionUtils.fromSatsToUsd
import net.primal.android.wallet.utils.CurrencyConversionUtils.fromUsdToSats
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ReceivePaymentScreen(
    viewModel: ReceivePaymentViewModel,
    onBuyPremium: () -> Unit,
    onClose: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ReceivePaymentScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onBuyPremium = onBuyPremium,
        onClose = onClose,
    )
}

@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun ReceivePaymentScreen(
    state: UiState,
    eventPublisher: (ReceivePaymentContract.UiEvent) -> Unit,
    onBuyPremium: () -> Unit,
    onClose: () -> Unit,
) {
    val onCancel = { eventPublisher(ReceivePaymentContract.UiEvent.CancelInvoiceCreation) }
    BackHandler(enabled = state.editMode) { onCancel() }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is UiState.ReceivePaymentError.FailedToCreateLightningInvoice ->
                    context.getString(R.string.wallet_receive_transaction_invoice_creation_error)
            }
        },
        onErrorDismiss = { eventPublisher(ReceivePaymentContract.UiEvent.DismissError) },
    )

    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = when (state.currentTab) {
                    ReceivePaymentTab.Lightning -> stringResource(
                        id = R.string.wallet_receive_lightning_transaction_title,
                    )

                    ReceivePaymentTab.Bitcoin -> stringResource(id = R.string.wallet_receive_btc_transaction_title)
                },
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                showDivider = false,
                onNavigationIconClick = {
                    if (state.editMode) {
                        onCancel()
                    } else {
                        onClose()
                    }
                },
            )
        },
        content = { paddingValues ->
            ReceiveContent(
                state = state,
                paddingValues = paddingValues,
                onCancel = onCancel,
                onBuyPremium = onBuyPremium,
                eventPublisher = eventPublisher,
            )
        },
        bottomBar = {
            Column {
                PrimalDivider()
                WalletTabsBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .height(WalletTabsHeight)
                        .background(color = AppTheme.colorScheme.surface),
                    tabs = ReceivePaymentTab.entries.map { it.data },
                    activeTab = state.currentTab.data,
                    onTabClick = {
                        if (state.currentTab.data != it) {
                            val network = when (ReceivePaymentTab.valueOfOrThrow(it)) {
                                ReceivePaymentTab.Lightning -> Network.Lightning
                                ReceivePaymentTab.Bitcoin -> Network.Bitcoin
                            }
                            eventPublisher(ReceivePaymentContract.UiEvent.ChangeNetwork(network))
                        }
                    },
                    tabIconSize = 32.dp,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@ExperimentalComposeUiApi
@Composable
private fun ReceiveContent(
    state: UiState,
    paddingValues: PaddingValues,
    eventPublisher: (ReceivePaymentContract.UiEvent) -> Unit,
    onBuyPremium: () -> Unit,
    onCancel: () -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current

    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = state.editMode,
        label = "ReceivePaymentContent",
        contentAlignment = Alignment.Center,
    ) { inEditMode ->
        when (inEditMode) {
            true -> {
                ReceivePaymentEditor(
                    paddingValues = paddingValues,
                    paymentDetails = state.paymentDetails,
                    applying = state.creatingInvoice,
                    onCancel = onCancel,
                    currencyMode = state.currencyMode,
                    currentExchangeRate = state.currentExchangeRate,
                    maximumUsdAmount = state.maximumUsdAmount,
                    onApplyChanges = { amountInBtc, amountInUsd, comment ->
                        eventPublisher(
                            ReceivePaymentContract.UiEvent.CreateInvoice(
                                amountInBtc = amountInBtc,
                                amountInUsd = amountInUsd,
                                comment = comment,
                            ),
                        )
                    },
                )
            }

            false -> {
                ReceivePaymentViewer(
                    paddingValues = paddingValues,
                    state = state,
                    onBuyPremium = onBuyPremium,
                    onCopyClick = {
                        when (state.currentTab) {
                            ReceivePaymentTab.Lightning -> state.lightningNetworkDetails.copyValue
                            ReceivePaymentTab.Bitcoin -> state.bitcoinNetworkDetails.copyValue
                        }?.let { text ->
                            clipboardManager.setText(AnnotatedString(text = text))
                        }
                    },
                    onEditClick = {
                        eventPublisher(ReceivePaymentContract.UiEvent.OpenInvoiceCreation)
                    },
                )
            }
        }
    }
}

@Composable
private fun ReceivePaymentViewer(
    paddingValues: PaddingValues,
    state: UiState,
    onBuyPremium: () -> Unit,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    val flipController = rememberFlipController()

    val networkDetails = when (state.currentTab) {
        ReceivePaymentTab.Lightning -> state.lightningNetworkDetails
        ReceivePaymentTab.Bitcoin -> state.bitcoinNetworkDetails
    }

    when (state.currentTab) {
        ReceivePaymentTab.Lightning -> flipController.flipToFront()
        ReceivePaymentTab.Bitcoin -> flipController.flipToBack()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Flippable(
            modifier = Modifier.size(280.dp),
            flipController = flipController,
            flipOnTouch = false,
            frontSide = {
                QrCodeBox(
                    qrCodeValue = state.lightningNetworkDetails.qrCodeValue,
                    network = Network.Lightning,
                )
            },
            backSide = {
                QrCodeBox(
                    qrCodeValue = state.bitcoinNetworkDetails.qrCodeValue,
                    network = Network.Bitcoin,
                )
            },
        )

        if (state.paymentDetails.amountInBtc != null) {
            Spacer(modifier = Modifier.height(16.dp))
            BtcAmountText(
                modifier = Modifier
                    .padding(start = 32.dp)
                    .height(72.dp),
                amountInBtc = state.paymentDetails.amountInBtc.toBigDecimal(),
                textSize = when (state.paymentDetails.amountInBtc.toSats().toString().length) {
                    in (0..8) -> 48.sp
                    else -> 40.sp
                },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (networkDetails.address != null) {
            TwoLineText(
                title = when (state.currentTab) {
                    ReceivePaymentTab.Lightning -> stringResource(
                        id = R.string.wallet_receive_transaction_your_lightning_address,
                    )

                    ReceivePaymentTab.Bitcoin -> stringResource(
                        id = R.string.wallet_receive_transaction_your_bitcoin_address,
                    )
                },
                content = when (state.currentTab) {
                    ReceivePaymentTab.Lightning -> networkDetails.address
                    ReceivePaymentTab.Bitcoin -> networkDetails.address
                },
                contentFontSize = 20.sp,
                maxLines = when (state.currentTab) {
                    ReceivePaymentTab.Lightning -> 3
                    ReceivePaymentTab.Bitcoin -> 1
                },
            )
            if (state.paymentDetails.comment == null && !state.hasPremium) {
                TextButton(onClick = onBuyPremium) {
                    Text(
                        text = "get a custom lightning address",
                        color = AppTheme.colorScheme.secondary,
                        style = AppTheme.typography.bodyMedium,
                        fontSize = 16.sp,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (state.paymentDetails.comment != null) {
            TwoLineText(
                title = stringResource(id = R.string.wallet_receive_transaction_comment),
                content = state.paymentDetails.comment,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        ViewerActionsRow(
            modifier = Modifier.fillMaxWidth(fraction = 0.85f),
            networkDetails = networkDetails,
            onCopyClick = onCopyClick,
            onEditClick = onEditClick,
        )

        if (state.paymentDetails.comment == null) {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ViewerActionsRow(
    modifier: Modifier,
    networkDetails: NetworkDetails,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        PrimalLoadingButton(
            modifier = Modifier.weight(1f),
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            text = stringResource(id = R.string.wallet_receive_transaction_copy_button).uppercase(),
            onClick = onCopyClick,
        )

        Spacer(modifier = Modifier.width(16.dp))

        PrimalLoadingButton(
            modifier = Modifier.weight(1f),
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            text = if (networkDetails.invoice == null) {
                stringResource(id = R.string.wallet_receive_transaction_add_details_button)
            } else {
                stringResource(id = R.string.wallet_receive_transaction_edit_details_button)
            }.uppercase(),
            onClick = onEditClick,
        )
    }
}

@Composable
fun QrCodeBox(qrCodeValue: String?, network: Network) {
    Box(
        modifier = Modifier.background(Color.White, shape = AppTheme.shapes.extraLarge),
        contentAlignment = Alignment.Center,
    ) {
        if (!qrCodeValue.isNullOrEmpty()) {
            val drawable = rememberQrCodeDrawable(text = qrCodeValue, network = network)
            Spacer(
                modifier = Modifier
                    .drawWithContent {
                        drawIntoCanvas { canvas ->
                            drawable.setBounds(0, 0, size.width.toInt(), size.height.toInt())
                            drawable.draw(canvas.nativeCanvas)
                        }
                    }
                    .fillMaxSize(),
            )
        } else {
            PrimalLoadingSpinner()
        }
    }
}

@Composable
@Suppress("MagicNumber")
fun rememberQrCodeDrawable(text: String, network: Network): Drawable {
    val context = LocalContext.current
    return remember(text, network) {
        val data = QrData.Text(text)
        val options = createQrVectorOptions {
            padding = .125f

            logo {
                drawable = context.getDrawable(
                    when (network) {
                        Network.Lightning -> R.drawable.qr_center_lightning
                        Network.Bitcoin -> R.drawable.qr_center_bitcoin
                    },
                )
                size = .12f
                scale = BitmapScale.CenterCrop
                padding = QrVectorLogoPadding.Natural(.72f)
                shape = QrVectorLogoShape.Circle
                backgroundColor = QrVectorColor.Solid(android.graphics.Color.BLACK)
            }
            colors {
                ball = QrVectorColor.Solid(android.graphics.Color.BLACK)
                frame = QrVectorColor.Solid(android.graphics.Color.BLACK)
            }
            shapes {
                darkPixel = QrVectorPixelShape.RoundCorners(.5f)
                ball = QrVectorBallShape.RoundCorners(.21f)
                frame = QrVectorFrameShape.RoundCorners(.21f)
            }
        }

        QrCodeDrawable(data, options)
    }
}

@Composable
private fun TwoLineText(
    title: String,
    content: String,
    maxLines: Int = 3,
    contentFontSize: TextUnit = 18.sp,
) {
    val context = LocalContext.current

    Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = title,
        style = AppTheme.typography.bodyLarge,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
    )

    Text(
        modifier = Modifier
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clickable {
                copyText(context = context, text = content)
            },
        text = content.ellipsizeMiddle(size = 12),
        style = AppTheme.typography.bodyLarge.copy(fontSize = contentFontSize),
        fontWeight = FontWeight.Bold,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
    )
}

@ExperimentalComposeUiApi
@Composable
private fun ReceivePaymentEditor(
    paddingValues: PaddingValues,
    paymentDetails: PaymentDetails,
    currencyMode: CurrencyMode,
    currentExchangeRate: Double?,
    maximumUsdAmount: BigDecimal?,
    applying: Boolean,
    onApplyChanges: (String, String, String?) -> Unit,
    onCancel: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardVisible by keyboardVisibilityAsState()

    var amountInBtc by remember(paymentDetails) { mutableStateOf(paymentDetails.amountInBtc ?: "0") }
    var amountInUsd by remember(paymentDetails) { mutableStateOf(paymentDetails.amountInUsd ?: "0") }
    var currentCurrencyMode by remember { mutableStateOf(currencyMode) }
    var comment by remember(paymentDetails) { mutableStateOf(paymentDetails.comment ?: "") }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            TransactionAmountText(
                amountInBtc = amountInBtc,
                amountInUsd = amountInUsd,
                currentExchangeRate = currentExchangeRate,
                currentCurrencyMode = currentCurrencyMode,
                onAmountClick = {
                    if (currentExchangeRate.isValidExchangeRate()) {
                        currentCurrencyMode = !currentCurrencyMode
                    }
                },
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                value = comment,
                onValueChange = { input -> comment = input },
                colors = PrimalDefaults.outlinedTextFieldColors(),
                shape = AppTheme.shapes.extraLarge,
                maxLines = 3,
                placeholder = {
                    if (!keyboardVisible) {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(id = R.string.wallet_receive_transaction_comment_hint),
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
                    },
                ),
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        AnimatedVisibility(
            modifier = Modifier.fillMaxWidth(fraction = 0.8f),
            visible = !keyboardVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 220)) + scaleIn(),
            exit = fadeOut(animationSpec = tween(durationMillis = 90)) + scaleOut(),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                PrimalNumericPad(
                    modifier = Modifier.fillMaxWidth(),
                    amountInSats = if (currentCurrencyMode == CurrencyMode.SATS) {
                        amountInBtc.toSats().toString()
                    } else {
                        amountInUsd
                    },
                    onAmountInSatsChanged = {
                        when (currentCurrencyMode) {
                            CurrencyMode.SATS -> {
                                amountInBtc = it.toULong().toBtc().formatAsString()
                                amountInUsd = calculateUsdFromBtc(amountInBtc, currentExchangeRate)
                            }
                            CurrencyMode.FIAT -> {
                                amountInUsd = it
                                amountInBtc = calculateBtcFromUsd(amountInUsd, currentExchangeRate)
                            }
                        }
                    },
                    currencyMode = currentCurrencyMode,
                    maximumUsdAmount = maximumUsdAmount,
                )

                TransactionActionRow(
                    applying = applying,
                    amountInBtc = amountInBtc,
                    amountInUsd = amountInUsd,
                    comment = comment,
                    onApplyChanges = onApplyChanges,
                    onCancel = onCancel,
                )
            }
        }
    }
}

@Composable
private fun TransactionActionRow(
    onCancel: () -> Unit,
    applying: Boolean,
    amountInBtc: String,
    amountInUsd: String,
    onApplyChanges: (String, String, String?) -> Unit,
    comment: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp, top = 48.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        PrimalLoadingButton(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.wallet_receive_transaction_cancel_numeric_pad_button),
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            contentColor = AppTheme.colorScheme.onSurface,
            onClick = onCancel,
        )

        Spacer(
            modifier = Modifier.width(16.dp),
        )

        PrimalLoadingButton(
            modifier = Modifier.weight(1f),
            loading = applying,
            enabled = !applying && amountInBtc.toBigDecimal() > BigDecimal.ZERO,
            text = stringResource(id = R.string.wallet_receive_transaction_apply_numeric_pad_button),
            onClick = {
                onApplyChanges(amountInBtc, amountInUsd, comment.ifEmpty { null })
            },
        )
    }
}

private fun calculateUsdFromBtc(btc: String, currentExchangeRate: Double?): String {
    return BigDecimal(btc.toSats().toDouble())
        .fromSatsToUsd(currentExchangeRate)
        .stripTrailingZeros()
        .let { value -> if (value.compareTo(BigDecimal.ZERO) == 0) BigDecimal.ZERO else value }
        .toPlainString()
}

private fun calculateBtcFromUsd(usd: String, currentExchangeRate: Double?): String {
    return BigDecimal(usd)
        .fromUsdToSats(currentExchangeRate)
        .toBtc()
        .formatAsString()
}
