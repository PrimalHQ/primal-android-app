package net.primal.android.settings.wallet.nwc.primal.create

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import net.primal.android.core.compose.PrimalScaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
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
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.settings.wallet.nwc.primal.PrimalNwcDefaults
import net.primal.android.settings.wallet.nwc.primal.ui.DailyBudgetBottomSheet
import net.primal.android.settings.wallet.nwc.primal.ui.WalletConnectionEditorHeader
import net.primal.android.settings.wallet.nwc.primal.ui.WalletConnectionFooter
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun CreateNewWalletConnectionScreen(viewModel: CreateNewWalletConnectionViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()
    CreateNewWalletConnectionScreen(
        eventPublisher = { viewModel.setEvent(it) },
        state = state.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateNewWalletConnectionScreen(
    state: CreateNewWalletConnectionContract.UiState,
    eventPublisher: (CreateNewWalletConnectionContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_wallet_new_nwc_connection_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            AnimatedContent(targetState = state.nwcConnectionUri) { secret ->
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
            when (state.nwcConnectionUri) {
                null -> WalletConnectionFooter(
                    loading = state.creatingSecret,
                    enabled = !state.creatingSecret && state.appName.isNotEmpty(),
                    primaryButtonText = stringResource(
                        id = R.string.settings_wallet_new_nwc_connection_create_new_connection_button,
                    ),
                    onPrimaryButtonClick = {
                        eventPublisher(CreateNewWalletConnectionContract.UiEvent.CreateWalletConnection)
                    },
                    secondaryButtonText = stringResource(
                        id = R.string.settings_wallet_new_nwc_connection_cancel_button,
                    ),
                    onSecondaryButtonClick = onClose,
                )

                else -> WalletConnectionFooter(
                    primaryButtonText = stringResource(
                        id = R.string.settings_wallet_new_nwc_connection_copy_nwc_string_button,
                    ),
                    onPrimaryButtonClick = {
                        clipboard.setText(AnnotatedString(text = state.nwcConnectionUri))
                    },
                    secondaryButtonText = stringResource(id = R.string.settings_wallet_new_nwc_connection_done_button),
                    onSecondaryButtonClick = onClose,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletConnectionEditor(
    modifier: Modifier,
    state: CreateNewWalletConnectionContract.UiState,
    eventPublisher: (CreateNewWalletConnectionContract.UiEvent) -> Unit,
) {
    var showDailyBudgetBottomSheet by rememberSaveable { mutableStateOf(false) }

    if (showDailyBudgetBottomSheet) {
        DailyBudgetBottomSheet(
            initialDailyBudget = state.dailyBudget,
            onDismissRequest = { showDailyBudgetBottomSheet = false },
            onBudgetSelected = { dailyBudget ->
                eventPublisher(CreateNewWalletConnectionContract.UiEvent.DailyBudgetChanged(dailyBudget))
            },
            budgetOptions = PrimalNwcDefaults.ALL_BUDGET_OPTIONS,
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WalletConnectionEditorHeader(modifier = Modifier)

        Column {
            Text(
                modifier = Modifier.padding(horizontal = 34.dp),
                text = stringResource(id = R.string.settings_wallet_new_nwc_connection_app_name_input_header),
                color = AppTheme.colorScheme.onPrimary,
                style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 23.sp),
            )
            PrimalOutlinedTextField(
                header = null,
                value = state.appName,
                onValueChange = {
                    eventPublisher(
                        CreateNewWalletConnectionContract.UiEvent.AppNameChanged(
                            it,
                        ),
                    )
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            DailyBudgetPicker(
                dailyBudget = state.dailyBudget,
                onChangeDailyBudgetBottomSheetVisibility = { showDailyBudgetBottomSheet = it },
            )

            Spacer(modifier = Modifier.height(21.dp))

            Text(
                modifier = Modifier.padding(horizontal = 48.dp),
                text = stringResource(id = R.string.settings_wallet_new_nwc_connection_hint),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp, lineHeight = 23.sp),
                textAlign = TextAlign.Center,
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

class CreateNewWalletConnectionUiStateProvider : PreviewParameterProvider<CreateNewWalletConnectionContract.UiState> {
    override val values: Sequence<CreateNewWalletConnectionContract.UiState>
        get() = sequenceOf(
            CreateNewWalletConnectionContract.UiState(),
            CreateNewWalletConnectionContract.UiState(
                nwcConnectionUri = "nostr+walletconnect://1291af9c1125151f7a59636432c6e06a7a2515" +
                    "15b27c0f20f61f3734e52relay=wss%3A%2F%2Fnwc.primal.net%2Fb9PwCaYmNOVBl13" +
                    "&secret=f4d681f07f51783708ef1b331225c5s1js0jns8f10391f2074e8333741m",
            ),
            CreateNewWalletConnectionContract.UiState(
                creatingSecret = true,
            ),
        )
}

@Preview
@Composable
private fun PreviewCreateNewWalletConnectionScreen(
    @PreviewParameter(CreateNewWalletConnectionUiStateProvider::class)
    state: CreateNewWalletConnectionContract.UiState,
) {
    PrimalPreview(
        primalTheme = PrimalTheme.Sunset,
    ) {
        CreateNewWalletConnectionScreen(
            state = state,
            eventPublisher = {},
            onClose = {},
        )
    }
}
