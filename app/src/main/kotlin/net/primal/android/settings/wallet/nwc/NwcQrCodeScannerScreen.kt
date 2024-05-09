package net.primal.android.settings.wallet.nwc

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.core.compose.MAX_COMPONENT_WIDTH
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.profile.qr.ui.ProfileQrCodeScanner
import net.primal.android.settings.wallet.nwc.NwcQrCodeScannerContract.UiEvent
import net.primal.android.theme.AppTheme

@Composable
fun NwcQrCodeScannerScreen(viewModel: NwcQrCodeScannerViewModel, onClose: () -> Unit) {
    LaunchedEffect(viewModel, onClose) {
        viewModel.effects.collect {
            when (it) {
                NwcQrCodeScannerContract.SideEffect.NwcConnected -> onClose()
            }
        }
    }
    NwcQrCodeScannerScreen(
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NwcQrCodeScannerScreen(onClose: () -> Unit, eventPublisher: (UiEvent) -> Unit) {
    var isClosing by remember { mutableStateOf(false) }
    BackHandler {
        isClosing = true
        onClose()
    }
    ColumnWithBackground(backgroundPainter = painterResource(id = R.drawable.profile_qrcode_background)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                PrimalTopAppBar(
                    title = stringResource(id = R.string.settings_wallet_nwc_qr_code_scan_title),
                    textColor = Color.White,
                    navigationIcon = PrimalIcons.ArrowBack,
                    navigationIconTintColor = Color.White,
                    onNavigationIconClick = {
                        isClosing = true
                        onClose()
                    },
                    showDivider = false,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                )
            },
            content = { paddingValues ->
                ProfileQrCodeScanner(
                    paddingValues = paddingValues,
                    cameraVisible = !isClosing,
                    onQrCodeDetected = { eventPublisher(UiEvent.ProcessQrCodeResult(it)) },
                    hint = {
                        Text(
                            modifier = Modifier.padding(horizontal = 64.dp),
                            text = stringResource(id = R.string.settings_wallet_nwc_qr_code_scan_hint),
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            style = AppTheme.typography.bodyLarge,
                        )
                    },
                )
            },
            bottomBar = {
                val clipboardManager = LocalClipboardManager.current
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    PrimalLoadingButton(
                        modifier = Modifier
                            .widthIn(240.dp, MAX_COMPONENT_WIDTH.dp)
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        text = stringResource(id = R.string.settings_wallet_nwc_qr_code_scan_paste),
                        containerColor = profileQrCodeButtonBackgroundColor,
                        disabledContainerColor = profileQrCodeButtonBackgroundColor,
                        contentColor = Color.White,
                        onClick = {
                            val clipboardText = clipboardManager.getText()?.text.orEmpty().trim()
                            eventPublisher(UiEvent.ProcessText(text = clipboardText))
                        },
                    )
                }
            },
        )
    }
}

val profileQrCodeButtonBackgroundColor = Color(0xFF4B002D)
