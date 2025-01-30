package net.primal.android.settings.wallet.connection

import android.graphics.drawable.Drawable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
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
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import net.primal.android.R
import net.primal.android.core.compose.DailyBudgetPicker
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalOutlinedTextField
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.NwcExternalAppConnection
import net.primal.android.core.compose.icons.primaliconpack.NwcExternalAppForeground
import net.primal.android.theme.AppTheme

@Composable
fun NwcNewWalletConnectionScreen(viewModel: NwcNewWalletConnectionViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()
    NwcNewWalletConnectionScreen(
        eventPublisher = { viewModel.setEvent(it) },
        state = state.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NwcNewWalletConnectionScreen(
    eventPublisher: (NwcNewWalletConnectionContract.UiEvent) -> Unit,
    state: NwcNewWalletConnectionContract.UiState,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_wallet_new_nwc_connection_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            AnimatedContent(targetState = state.secret) { secret ->
                when (secret) {
                    null -> {
                        WalletConnectionEditor(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = AppTheme.colorScheme.surfaceVariant)
                                .verticalScroll(rememberScrollState())
                                .padding(paddingValues),
                            state = state,
                            eventPublisher = eventPublisher,
                        )
                    }

                    else -> {
                        WalletConnectionPreview(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = AppTheme.colorScheme.surfaceVariant)
                                .verticalScroll(rememberScrollState())
                                .padding(paddingValues),
                            secret = secret,
                        )
                    }
                }
            }
        },
        bottomBar = {
            val clipboard = LocalClipboardManager.current
            when (state.secret) {
                null -> NewWalletConnectionFooter(
                    loading = state.creatingSecret,
                    enabled = !state.creatingSecret && state.appName.isNotEmpty(),
                    primaryButtonText = stringResource(
                        id = R.string.settings_wallet_new_nwc_connection_create_new_connection_button,
                    ),
                    onPrimaryButtonClick = {
                        eventPublisher(NwcNewWalletConnectionContract.UiEvent.CreateWalletConnection)
                    },
                    secondaryButtonText = stringResource(
                        id = R.string.settings_wallet_new_nwc_connection_cancel_button,
                    ),
                    onSecondaryButtonClick = onClose,
                )

                else -> NewWalletConnectionFooter(
                    primaryButtonText = stringResource(
                        id = R.string.settings_wallet_new_nwc_connection_copy_nwc_string_button,
                    ),
                    onPrimaryButtonClick = {
                        clipboard.setText(AnnotatedString(text = state.secret))
                    },
                    secondaryButtonText = stringResource(id = R.string.settings_wallet_new_nwc_connection_done_button),
                    onSecondaryButtonClick = onClose,
                )
            }
        },
    )
}

@Composable
fun NewWalletConnectionFooter(
    primaryButtonText: String,
    onPrimaryButtonClick: () -> Unit,
    secondaryButtonText: String,
    onSecondaryButtonClick: () -> Unit,
    loading: Boolean = false,
    enabled: Boolean = true,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier.navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PrimalLoadingButton(
            text = primaryButtonText,
            enabled = enabled,
            loading = loading,
            onClick = {
                keyboardController?.hide()
                onPrimaryButtonClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 32.dp)
                .height(56.dp),
        )

        TextButton(
            onClick = onSecondaryButtonClick,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 48.dp),
                text = secondaryButtonText,
                fontWeight = FontWeight.SemiBold,
                color = AppTheme.colorScheme.onPrimary,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletConnectionEditor(
    modifier: Modifier,
    state: NwcNewWalletConnectionContract.UiState,
    eventPublisher: (NwcNewWalletConnectionContract.UiEvent) -> Unit,
) {
    var showDailyBudgetBottomSheet by rememberSaveable { mutableStateOf(false) }

    if (showDailyBudgetBottomSheet) {
        DailyBudgetBottomSheet(
            initialDailyBudget = state.dailyBudget,
            onDismissRequest = { showDailyBudgetBottomSheet = false },
            onBudgetSelected = { dailyBudget ->
                eventPublisher(NwcNewWalletConnectionContract.UiEvent.DailyBudgetChanged(dailyBudget))
            },
            budgetOptions = NwcNewWalletConnectionContract.budgetOptions,
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WalletConnectionEditorHeader(
            modifier = Modifier,
        )

        Column {
            Text(
                modifier = Modifier.padding(horizontal = 32.dp),
                text = stringResource(id = R.string.settings_wallet_new_nwc_connection_app_name_input_header),
                color = AppTheme.colorScheme.onPrimary,
                style = AppTheme.typography.bodyLarge,
            )
            PrimalOutlinedTextField(
                header = null,
                value = state.appName,
                onValueChange = { eventPublisher(NwcNewWalletConnectionContract.UiEvent.AppNameChanged(it)) },
            )

            Spacer(modifier = Modifier.height(16.dp))

            DailyBudgetPicker(
                dailyBudget = state.dailyBudget,
                onChangeDailyBudgetBottomSheetVisibility = { showDailyBudgetBottomSheet = it },
            )

            Spacer(modifier = Modifier.height(21.dp))

            Text(
                modifier = Modifier.padding(horizontal = 21.dp),
                text = stringResource(id = R.string.settings_wallet_new_nwc_connection_hint),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                style = AppTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun WalletConnectionEditorHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(19.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(id = R.drawable.primal_nwc_logo),
                contentDescription = stringResource(id = R.string.settings_wallet_nwc_primal_wallet),
                modifier = Modifier
                    .clip(AppTheme.shapes.small)
                    .size(99.dp),
                tint = Color.Unspecified,
            )

            Text(
                modifier = Modifier.padding(top = 13.dp),
                text = stringResource(id = R.string.settings_wallet_nwc_primal_wallet),
            )
        }

        Icon(
            modifier = Modifier.offset(y = (-13).dp),
            imageVector = PrimalIcons.NwcExternalAppConnection,
            contentDescription = "Connection",
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                modifier = Modifier
                    .clip(AppTheme.shapes.small)
                    .background(color = Color(color = 0xFFE5E5E5))
                    .padding(21.dp)
                    .size(54.dp),
                imageVector = PrimalIcons.NwcExternalAppForeground,
                contentDescription = stringResource(id = R.string.settings_wallet_nwc_external_app),
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            )

            Text(
                modifier = Modifier.padding(top = 13.dp),
                text = stringResource(id = R.string.settings_wallet_nwc_external_app),
            )
        }
    }
}

@Composable
private fun WalletConnectionPreview(modifier: Modifier, secret: String) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WalletConnectionPreviewHeader(
            modifier = Modifier.fillMaxWidth(),
            secret = secret,
        )

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = stringResource(id = R.string.settings_wallet_new_nwc_connection_secret_hint),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun WalletConnectionPreviewHeader(modifier: Modifier, secret: String) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        QrCodeBox(
            modifier = Modifier.size(256.dp),
            qrCodeValue = secret,
        )

        Row(
            modifier = Modifier.height(48.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Spacer(
                    modifier = Modifier
                        .size(18.dp)
                        .background(color = Color.White, shape = CircleShape),
                )

                Icon(
                    modifier = Modifier
                        .size(22.dp),
                    imageVector = Icons.Filled.CheckCircle,
                    tint = Color(color = 0xFF2FD058),
                    contentDescription = null,
                )
            }

            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .padding(top = 2.dp),
                text = stringResource(R.string.settings_wallet_new_nwc_connection_connection_created_text),
                color = Color(color = 0xFF2FD058),
                style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.SemiBold,
            )
        }

        Text(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = secret,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            style = AppTheme.typography.bodySmall.copy(fontSize = 13.sp),
            softWrap = true,
            maxLines = 6,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun QrCodeBox(modifier: Modifier, qrCodeValue: String) {
    Box(
        modifier = modifier.background(Color.White, shape = AppTheme.shapes.large),
        contentAlignment = Alignment.Center,
    ) {
        if (qrCodeValue.isNotEmpty()) {
            val drawable = rememberQrCodeDrawable(text = qrCodeValue)
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
private fun rememberQrCodeDrawable(text: String): Drawable {
    return remember(text) {
        val data = QrData.Text(text)
        val options = createQrVectorOptions {
            padding = .125f
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
