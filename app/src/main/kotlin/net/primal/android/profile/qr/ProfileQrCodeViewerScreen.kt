package net.primal.android.profile.qr

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.alexzhirkevich.customqrgenerator.QrData
import com.github.alexzhirkevich.customqrgenerator.vector.QrCodeDrawable
import com.github.alexzhirkevich.customqrgenerator.vector.createQrVectorOptions
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorBallShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorColor
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorFrameShape
import com.github.alexzhirkevich.customqrgenerator.vector.style.QrVectorPixelShape
import com.wajahatkarim.flippable.Flippable
import com.wajahatkarim.flippable.FlippableState
import com.wajahatkarim.flippable.rememberFlipController
import net.primal.android.R
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Copy
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.wallet.utils.isLightningAddress

@Composable
fun ProfileQrCodeViewerScreen(viewModel: ProfileQrCodeViewerViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()
    ProfileQrCodeViewerScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileQrCodeViewerScreen(state: ProfileQrCodeViewerContract.UiState, onClose: () -> Unit) {
    ColumnWithBackground(
        backgroundPainter = painterResource(id = R.drawable.profile_qrcode_background),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                PrimalTopAppBar(
                    title = "",
                    navigationIcon = PrimalIcons.ArrowBack,
                    navigationIconTintColor = Color.White,
                    onNavigationIconClick = onClose,
                    showDivider = false,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                )
            },
            content = { paddingValues ->
                ProfileQrCodeViewerContent(
                    state = state,
                    paddingValues = paddingValues,
                )
            },
        )
    }
}

@Composable
private fun ProfileQrCodeViewerContent(state: ProfileQrCodeViewerContract.UiState, paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AvatarThumbnail(
            modifier = Modifier.size(108.dp),
            avatarCdnImage = state.profileDetails?.avatarCdnImage,
            hasBorder = true,
            borderColor = Color.White,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = state.profileDetails?.authorDisplayName ?: state.profileId.asEllipsizedNpub(),
            color = Color.White,
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            ),
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state.profileDetails?.internetIdentifier != null) {
            Text(
                text = state.profileDetails.internetIdentifier,
                color = Color.White,
                style = AppTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                ),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        val pubkey = state.profileId.hexToNpubHrp()
        val lud16 = state.profileDetails?.lightningAddress.orEmpty()

        val flipController = rememberFlipController()
        var qrCodeValueText by remember { mutableStateOf(pubkey) }

        Flippable(
            modifier = Modifier.size(280.dp),
            flipController = flipController,
            flipOnTouch = state.profileDetails?.lightningAddress != null,
            frontSide = {
                QrCodeBox(qrCodeValue = state.profileId.hexToNpubHrp())
            },
            backSide = {
                QrCodeBox(qrCodeValue = state.profileDetails?.lightningAddress.orEmpty())
            },
            onFlippedListener = {
                qrCodeValueText = when (it) {
                    FlippableState.INITIALIZED -> pubkey
                    FlippableState.FRONT -> pubkey
                    FlippableState.BACK -> lud16
                }
            },
        )

        val clipboardManager = LocalClipboardManager.current

        Spacer(modifier = Modifier.height(32.dp))

        CopyText(
            modifier = Modifier.padding(horizontal = 32.dp),
            text = if (qrCodeValueText.isLightningAddress()) {
                qrCodeValueText
            } else {
                qrCodeValueText.ellipsizeMiddle(size = 12)
            },
            onCopyClick = {
                clipboardManager.setText(AnnotatedString(text = qrCodeValueText))
            },
        )
    }
}

@Composable
private fun QrCodeBox(qrCodeValue: String) {
    Box(
        modifier = Modifier.background(Color.White, shape = AppTheme.shapes.extraLarge),
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

@Composable
private fun CopyText(
    modifier: Modifier = Modifier,
    text: String,
    onCopyClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(
                onClick = onCopyClick,
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.wrapContentWidth(),
            text = text,
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
            ),
            color = Color.White,
        )

        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                imageVector = PrimalIcons.Copy,
                colorFilter = ColorFilter.tint(color = Color.White),
                contentDescription = stringResource(id = R.string.accessibility_copy_content),
            )
        }
    }
}

@Preview
@Composable
private fun PreviewProfileQrCodeViewerScreen() {
    PrimalTheme(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunset) {
        Surface {
            ProfileQrCodeViewerScreen(
                state = ProfileQrCodeViewerContract.UiState(
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
                onClose = {},
            )
        }
    }
}
