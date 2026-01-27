package net.primal.android.settings.wallet.nwc

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalClipboardManager
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
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

private const val WALLET_ID_PREVIEW_LENGTH = 16

@Composable
fun NwcWalletServiceScreen(viewModel: NwcWalletServiceViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()
    NwcWalletServiceScreen(
        state = state.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NwcWalletServiceScreen(
    state: NwcWalletServiceContract.UiState,
    eventPublisher: (NwcWalletServiceContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current

    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = "NWC Wallet Service Test",
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = "Back",
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            AnimatedContent(
                targetState = state.nwcConnectionUri,
                label = "NwcContent",
            ) { nwcUri ->
                when (nwcUri) {
                    null -> {
                        CreateConnectionContent(
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
                        ConnectionCreatedContent(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = AppTheme.colorScheme.surfaceVariant)
                                .verticalScroll(rememberScrollState())
                                .padding(paddingValues),
                            nwcUri = nwcUri,
                            onCopy = { clipboard.setText(AnnotatedString(nwcUri)) },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun CreateConnectionContent(
    modifier: Modifier,
    state: NwcWalletServiceContract.UiState,
    eventPublisher: (NwcWalletServiceContract.UiEvent) -> Unit,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "NWC Wallet Service",
            style = AppTheme.typography.headlineMedium,
            color = AppTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create a connection to allow external apps to connect to this wallet via NWC protocol.",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.walletId != null) {
            Text(
                text = "Wallet ID: ${state.walletId.take(WALLET_ID_PREVIEW_LENGTH)}...",
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (state.error != null) {
            Text(
                text = state.error,
                style = AppTheme.typography.bodyMedium,
                color = Color.Red,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        PrimalFilledButton(
            onClick = { eventPublisher(NwcWalletServiceContract.UiEvent.CreateConnection) },
            enabled = !state.isCreating && state.walletId != null,
        ) {
            if (state.isCreating) {
                PrimalLoadingSpinner(size = 24.dp)
            } else {
                Text("Create NWC Connection")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "After creating, scan the QR code with Alby, Amethyst, or another NWC-compatible app to test.",
            style = AppTheme.typography.bodySmall,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ConnectionCreatedContent(
    modifier: Modifier,
    nwcUri: String,
    onCopy: () -> Unit,
) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        QrCodeBox(
            modifier = Modifier.size(256.dp),
            qrCodeValue = nwcUri,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Spacer(
                    modifier = Modifier
                        .size(18.dp)
                        .background(color = Color.White, shape = CircleShape),
                )
                Icon(
                    modifier = Modifier.size(22.dp),
                    imageVector = Icons.Filled.CheckCircle,
                    tint = Color(color = 0xFF2FD058),
                    contentDescription = null,
                )
            }
            Text(
                modifier = Modifier.padding(start = 8.dp),
                text = "Connection Created",
                color = Color(color = 0xFF2FD058),
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = nwcUri,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            style = AppTheme.typography.bodySmall.copy(fontSize = 11.sp),
            softWrap = true,
            maxLines = 6,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(modifier = Modifier.height(24.dp))

        PrimalFilledButton(onClick = onCopy) {
            Text("Copy NWC String")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Scan this QR code with Alby, Amethyst, or another NWC client to send get_balance request.",
            style = AppTheme.typography.bodySmall,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            textAlign = TextAlign.Center,
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
