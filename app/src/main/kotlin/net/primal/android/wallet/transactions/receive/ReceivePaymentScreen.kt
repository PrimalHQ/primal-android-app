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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoPadding
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorLogoShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import java.math.BigDecimal
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.numericpad.PrimalNumericPad
import net.primal.android.crypto.urlToLnUrlHrp
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.api.parseAsLNUrlOrNull
import net.primal.android.wallet.dashboard.ui.BtcAmountText
import net.primal.android.wallet.transactions.receive.ReceivePaymentContract.UiState
import net.primal.android.wallet.utils.CurrencyConversionUtils.formatAsString
import net.primal.android.wallet.utils.CurrencyConversionUtils.toBtc
import net.primal.android.wallet.utils.CurrencyConversionUtils.toSats

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ReceivePaymentScreen(viewModel: ReceivePaymentViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    ReceivePaymentScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@ExperimentalComposeUiApi
@ExperimentalMaterial3Api
@Composable
fun ReceivePaymentScreen(
    state: UiState,
    onClose: () -> Unit,
    eventPublisher: (ReceivePaymentContract.UiEvent) -> Unit,
) {
    val clipboardManager = LocalClipboardManager.current

    val onCancel = { eventPublisher(ReceivePaymentContract.UiEvent.CancelInvoiceCreation) }
    BackHandler(enabled = state.editMode) { onCancel() }

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is UiState.ReceivePaymentError.FailedToCreateInvoice ->
                    context.getString(R.string.wallet_receive_transaction_invoice_creation_error)
            }
        },
        onErrorDismiss = { eventPublisher(ReceivePaymentContract.UiEvent.DismissError) },
    )

    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.wallet_receive_transaction_title),
                navigationIcon = PrimalIcons.ArrowBack,
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
            AnimatedContent(targetState = state.editMode, label = "ReceivePaymentContent") {
                when (it) {
                    true -> ReceivePaymentEditor(
                        paddingValues = paddingValues,
                        paymentDetails = state.paymentDetails,
                        applying = state.creating,
                        onCancel = onCancel,
                        onApplyChanges = { amountInBtc, comment ->
                            eventPublisher(
                                ReceivePaymentContract.UiEvent.CreateInvoice(
                                    amountInBtc = amountInBtc,
                                    comment = comment,
                                ),
                            )
                        },
                    )

                    false -> ReceivePaymentViewer(
                        paddingValues = paddingValues,
                        paymentDetails = state.paymentDetails,
                        onCopyClick = {
                            val copyInput = state.paymentDetails.invoice ?: state.paymentDetails.lightningAddress
                            if (copyInput != null) {
                                clipboardManager.setText(AnnotatedString(text = copyInput))
                            }
                        },
                        onEditClick = {
                            eventPublisher(ReceivePaymentContract.UiEvent.OpenInvoiceCreation)
                        },
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun ReceivePaymentViewer(
    paddingValues: PaddingValues,
    paymentDetails: PaymentDetails,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .size(280.dp)
                .background(Color.White, shape = AppTheme.shapes.extraLarge),
            contentAlignment = Alignment.Center,
        ) {
            val text = paymentDetails.invoice ?: paymentDetails.lightningAddress?.parseAsLNUrlOrNull()?.urlToLnUrlHrp()
            if (!text.isNullOrEmpty()) {
                val drawable = rememberQrCodeDrawable(text)
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

        if (paymentDetails.amountInBtc != null) {
            Spacer(modifier = Modifier.height(16.dp))
            BtcAmountText(
                modifier = Modifier
                    .padding(start = 32.dp)
                    .height(72.dp),
                amountInBtc = paymentDetails.amountInBtc.toBigDecimal(),
                textSize = when (paymentDetails.amountInBtc.toSats().toString().length) {
                    in (0..8) -> 48.sp
                    else -> 40.sp
                },
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (paymentDetails.lightningAddress != null) {
            TwoLineText(
                title = stringResource(id = R.string.wallet_receive_transaction_receiving_to),
                content = paymentDetails.lightningAddress,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (paymentDetails.comment != null) {
            TwoLineText(
                title = stringResource(id = R.string.wallet_receive_transaction_comment),
                content = paymentDetails.comment,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(fraction = 0.85f),
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
                text = if (paymentDetails.invoice == null) {
                    stringResource(id = R.string.wallet_receive_transaction_add_details_button)
                } else {
                    stringResource(id = R.string.wallet_receive_transaction_edit_details_button)
                }.uppercase(),
                onClick = onEditClick,
            )
        }
    }
}

@Composable
@Suppress("MagicNumber")
private fun rememberQrCodeDrawable(text: String): Drawable {
    val warningColor = Color(0xFF480101).toArgb()
    val context = LocalContext.current
    return remember(text) {
        val data = QrData.Text(text)
        val options = createQrVectorOptions {
            padding = .125f

            logo {
                drawable = context.getDrawable(R.drawable.primal_wave_logo_summer)
                size = .25f
                padding = QrVectorLogoPadding.Natural(.1f)
                shape = QrVectorLogoShape.Circle
            }
            colors {
                ball = QrVectorColor.Solid(warningColor)
                frame = QrVectorColor.LinearGradient(
                    colors = listOf(
                        0f to android.graphics.Color.RED,
                        1f to android.graphics.Color.BLUE,
                    ),
                    orientation = QrVectorColor.LinearGradient
                        .Orientation.LeftDiagonal,
                )
            }
            shapes {
                darkPixel = QrVectorPixelShape.RoundCorners(.5f)
                ball = QrVectorBallShape.RoundCorners(.25f)
                frame = QrVectorFrameShape.RoundCorners(.25f)
            }
        }

        QrCodeDrawable(data, options)
    }
}

@Composable
private fun TwoLineText(title: String, content: String) {
    Text(
        modifier = Modifier.padding(horizontal = 32.dp),
        text = title,
        style = AppTheme.typography.bodyLarge,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
    )

    Text(
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 32.dp),
        text = content,
        style = AppTheme.typography.bodyLarge,
        fontWeight = FontWeight.Bold,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
    )
}

@ExperimentalComposeUiApi
@Composable
private fun ReceivePaymentEditor(
    paddingValues: PaddingValues,
    paymentDetails: PaymentDetails,
    applying: Boolean,
    onApplyChanges: (String, String?) -> Unit,
    onCancel: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val keyboardVisible by keyboardVisibilityAsState()

    var amountInBtc by remember(paymentDetails) { mutableStateOf(paymentDetails.amountInBtc ?: "0") }
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

            BtcAmountText(
                modifier = Modifier
                    .padding(start = 32.dp)
                    .height(72.dp),
                amountInBtc = amountInBtc.toBigDecimal(),
                textSize = 48.sp,
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
            enter = fadeIn(animationSpec = tween(220)) + scaleIn(),
            exit = fadeOut(animationSpec = tween(90)) + scaleOut(),
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
            ) {
                PrimalNumericPad(
                    modifier = Modifier.fillMaxWidth(),
                    amountInSats = amountInBtc.toSats().toString(),
                    onAmountInSatsChanged = { amountInBtc = it.toULong().toBtc().formatAsString() },
                )

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
                            onApplyChanges(amountInBtc, comment.ifEmpty { null })
                        },
                    )
                }
            }
        }
    }
}
