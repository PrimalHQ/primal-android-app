package net.primal.android.profile.qr

import android.graphics.drawable.Drawable
import androidx.compose.animation.core.animateIntAsState
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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
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
import net.primal.android.core.compose.DefaultAvatarThumbnailPlaceholderListItemImage
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Copy
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.nostr.ext.isNPub
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

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
            defaultAvatar = {
                DefaultAvatarThumbnailPlaceholderListItemImage(
                    backgroundColor = Color.White,
                )
            },
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
                text = state.profileDetails.internetIdentifier.formatNip05Identifier(),
                color = Color.White,
                style = AppTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                ),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        var qrCodeValueText by remember { mutableStateOf(state.profileId.hexToNpubHrp()) }
        QrCodeViewer(
            profileId = state.profileId,
            lightningAddress = state.profileDetails?.lightningAddress,
            onQrCodeValueChanged = { qrCodeValueText = it },
        )

        Spacer(modifier = Modifier.height(32.dp))

        CopyText(
            modifier = Modifier.padding(horizontal = 32.dp),
            copyText = qrCodeValueText,
            visibleText = if (qrCodeValueText.isNPub()) {
                qrCodeValueText.ellipsizeMiddle(size = 12)
            } else {
                qrCodeValueText
            },
        )
    }
}

@Composable
private fun QrCodeViewer(
    profileId: String,
    lightningAddress: String?,
    onQrCodeValueChanged: (String) -> Unit,
) {
    val pubkey = profileId.hexToNpubHrp()
    val lud16 = lightningAddress.orEmpty()
    var isProfileTabSelected by remember { mutableStateOf(true) }
    val flipController = rememberFlipController()

    if (lud16.isNotEmpty()) {
        QrCodeTabs(
            modifier = Modifier.wrapContentWidth(),
            isProfileTabSelected = isProfileTabSelected,
            onProfileTabClick = {
                isProfileTabSelected = true
                onQrCodeValueChanged(pubkey)
                flipController.flipToFront()
            },
            onLightningTabClick = {
                isProfileTabSelected = false
                onQrCodeValueChanged(lud16)
                flipController.flipToBack()
            },
            indicatorColor = Color.White,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }

    Flippable(
        modifier = Modifier.size(280.dp),
        flipController = flipController,
        flipOnTouch = false,
        frontSide = {
            QrCodeBox(qrCodeValue = "nostr:${profileId.hexToNpubHrp()}")
        },
        backSide = {
            QrCodeBox(qrCodeValue = "lightning:$lud16")
        },
        onFlippedListener = {
            onQrCodeValueChanged(
                when (it) {
                    FlippableState.INITIALIZED -> pubkey
                    FlippableState.FRONT -> pubkey
                    FlippableState.BACK -> lud16
                },
            )
        },
    )
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
    copyText: String,
    visibleText: String,
) {
    val clipboardManager = LocalClipboardManager.current
    Row(
        modifier = modifier
            .clickable(
                onClick = {
                    clipboardManager.setText(AnnotatedString(text = copyText))
                },
                role = Role.Button,
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(),
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.wrapContentWidth(),
            text = visibleText,
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

@Composable
private fun QrCodeTabs(
    modifier: Modifier = Modifier,
    isProfileTabSelected: Boolean,
    onProfileTabClick: () -> Unit,
    onLightningTabClick: () -> Unit,
    indicatorColor: Color = AppTheme.colorScheme.primary,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var profileTabWidth by remember { mutableIntStateOf(0) }
        var lightningAddressTabWidth by remember { mutableIntStateOf(0) }
        val tabsSpaceWidth = 16.dp

        Column(
            modifier = Modifier.wrapContentWidth(),
        ) {
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QrCodeTab(
                    text = stringResource(id = R.string.profile_qr_code_profile_tab).uppercase(),
                    onSizeChanged = { size -> profileTabWidth = size.width },
                    onClick = onProfileTabClick,
                )

                Spacer(modifier = Modifier.width(tabsSpaceWidth))

                QrCodeTab(
                    text = stringResource(id = R.string.profile_qr_code_lightning_tab).uppercase(),
                    onSizeChanged = { size -> lightningAddressTabWidth = size.width },
                    onClick = onLightningTabClick,
                )
            }

            with(LocalDensity.current) {
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(
                            animateIntAsState(
                                targetValue = if (isProfileTabSelected) {
                                    profileTabWidth
                                } else {
                                    lightningAddressTabWidth
                                },
                                label = "indicatorWidth",
                            ).value.toDp(),
                        )
                        .offset(
                            y = (-4).dp,
                            x = animateIntAsState(
                                targetValue = if (isProfileTabSelected) {
                                    0
                                } else {
                                    profileTabWidth + tabsSpaceWidth
                                        .toPx()
                                        .toInt()
                                },
                                label = "indicatorOffsetX",
                            ).value.toDp(),
                        )
                        .background(
                            color = indicatorColor,
                            shape = AppTheme.shapes.small,
                        ),
                )
            }
        }
    }
}

@Composable
private fun QrCodeTab(
    text: String,
    onSizeChanged: (IntSize) -> Unit,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .wrapContentWidth()
            .onSizeChanged { onSizeChanged(it) }
            .defaultMinSize(minHeight = 32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                role = Role.Button,
            ),
        text = text,
        style = AppTheme.typography.bodyMedium,
        fontWeight = FontWeight.SemiBold,
        color = AppTheme.colorScheme.onSurface,
    )
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
