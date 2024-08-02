package net.primal.android.profile.qr.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.core.compose.MAX_COMPONENT_WIDTH
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.profile.qr.ProfileQrCodeContract
import net.primal.android.profile.qr.ProfileQrCodeViewModel
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.domain.DraftTx

@Composable
fun ProfileQrCodeViewerScreen(
    viewModel: ProfileQrCodeViewModel,
    onClose: () -> Unit,
    onProfileScan: (profileId: String) -> Unit,
    onNoteScan: (noteId: String) -> Unit,
    onDraftTxScan: (draftTx: DraftTx) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, onProfileScan) {
        viewModel.effects.collect {
            when (it) {
                is ProfileQrCodeContract.SideEffect.NostrProfileDetected -> onProfileScan(it.profileId)
                is ProfileQrCodeContract.SideEffect.NostrNoteDetected -> onNoteScan(it.noteId)
                is ProfileQrCodeContract.SideEffect.WalletTxDetected -> onDraftTxScan(it.draftTx)
            }
        }
    }

    ProfileQrCodeViewerScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileQrCodeViewerScreen(
    state: ProfileQrCodeContract.UiState,
    eventPublisher: (ProfileQrCodeContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    var isClosing by remember { mutableStateOf(false) }
    BackHandler {
        isClosing = true
        onClose()
    }
    var qrCodeMode by remember { mutableStateOf(QrCodeMode.Viewer) }
    ColumnWithBackground(backgroundPainter = painterResource(id = R.drawable.profile_qrcode_background)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                PrimalTopAppBar(
                    title = qrCodeMode.toTitle(),
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
                AnimatedContent(
                    targetState = qrCodeMode,
                    label = "QrCodeModeContent",
                ) { mode ->
                    when (mode) {
                        QrCodeMode.Viewer -> {
                            ProfileQrCodeViewer(
                                profileId = state.profileId,
                                profileDetails = state.profileDetails,
                                paddingValues = paddingValues,
                            )
                        }

                        QrCodeMode.Scanner -> {
                            ProfileQrCodeScanner(
                                paddingValues = paddingValues,
                                cameraVisible = !isClosing,
                                onQrCodeDetected = {
                                    eventPublisher(ProfileQrCodeContract.UiEvent.ProcessQrCodeResult(it))
                                },
                                hint = {
                                    ScanningHint(modifier = Modifier.padding(horizontal = 64.dp))
                                },
                            )
                        }
                    }
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    PrimalLoadingButton(
                        modifier = Modifier
                            .widthIn(240.dp, MAX_COMPONENT_WIDTH.dp)
                            .fillMaxWidth()
                            .padding(bottom = 32.dp),
                        text = qrCodeMode.toActionButtonText(),
                        containerColor = profileQrCodeButtonBackgroundColor,
                        disabledContainerColor = profileQrCodeButtonBackgroundColor,
                        contentColor = Color.White,
                        onClick = { qrCodeMode = qrCodeMode.invert() },
                    )
                }
            },
        )
    }
}

@Composable
private fun ScanningHint(modifier: Modifier) {
    Text(
        modifier = modifier,
        text = stringResource(id = R.string.profile_qr_code_scan_qr_code_hint),
        color = Color.White,
        textAlign = TextAlign.Center,
        style = AppTheme.typography.bodyLarge,
    )
}

val profileQrCodeButtonBackgroundColor = Color(0xFF4B002D)

@Composable
private fun QrCodeMode.toTitle(): String {
    return when (this) {
        QrCodeMode.Viewer -> ""
        QrCodeMode.Scanner -> stringResource(id = R.string.profile_qr_code_scan_qr_code)
    }
}

@Composable
private fun QrCodeMode.toActionButtonText(): String {
    return when (this) {
        QrCodeMode.Viewer -> stringResource(id = R.string.profile_qr_code_scan_qr_code)
        QrCodeMode.Scanner -> stringResource(id = R.string.profile_qr_code_view_qr_code)
    }
}

private fun QrCodeMode.invert(): QrCodeMode {
    return when (this) {
        QrCodeMode.Viewer -> QrCodeMode.Scanner
        QrCodeMode.Scanner -> QrCodeMode.Viewer
    }
}

@Preview
@Composable
private fun PreviewProfileQrCodeViewerScreen() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            ProfileQrCodeViewerScreen(
                state = ProfileQrCodeContract.UiState(
                    profileId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
                    profileDetails = ProfileDetailsUi(
                        pubkey = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
                        authorDisplayName = "alex",
                        userDisplayName = "alex",
                        coverCdnImage = null,
                        avatarCdnImage = null,
                        internetIdentifier = "alex@primal.net",
                        lightningAddress = "alex@primal.net",
                        about = "Primal Android",
                        website = "https://appollo41.com",
                    ),
                ),
                eventPublisher = {},
                onClose = {},
            )
        }
    }
}
