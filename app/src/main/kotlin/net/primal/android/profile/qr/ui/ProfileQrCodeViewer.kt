package net.primal.android.profile.qr.ui

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
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
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
import com.wajahatkarim.flippable.FlippableState
import com.wajahatkarim.flippable.rememberFlipController
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.DefaultAvatarThumbnailPlaceholderListItemImage
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Copy
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.ellipsizeMiddle
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.nostr.ext.isNPub
import net.primal.android.theme.AppTheme

@Composable
fun ProfileQrCodeViewer(
    profileId: String,
    profileDetails: ProfileDetailsUi?,
    paddingValues: PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AvatarThumbnail(
            modifier = Modifier.size(108.dp),
            avatarCdnImage = profileDetails?.avatarCdnImage,
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
            text = profileDetails?.authorDisplayName ?: profileId.asEllipsizedNpub(),
            color = Color.White,
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
            ),
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (profileDetails?.internetIdentifier != null) {
            Text(
                text = profileDetails.internetIdentifier.formatNip05Identifier(),
                color = Color.White,
                style = AppTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                ),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        var qrCodeValueText by remember { mutableStateOf(profileId.hexToNpubHrp()) }
        QrCodeViewer(
            profileId = profileId,
            lightningAddress = profileDetails?.lightningAddress,
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
            QrCodeBox(
                qrCodeValue = "nostr:${profileId.hexToNpubHrp()}",
                type = QrCodeType.Nostr,
            )
        },
        backSide = {
            QrCodeBox(
                qrCodeValue = "lightning:$lud16",
                type = QrCodeType.Lightning,
            )
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
private fun QrCodeBox(qrCodeValue: String, type: QrCodeType) {
    Box(
        modifier = Modifier.background(Color.White, shape = AppTheme.shapes.extraLarge),
        contentAlignment = Alignment.Center,
    ) {
        if (qrCodeValue.isNotEmpty()) {
            val drawable = rememberQrCodeDrawable(text = qrCodeValue, type = type)
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

private enum class QrCodeType {
    Nostr,
    Lightning,
}

@Composable
@Suppress("MagicNumber")
private fun rememberQrCodeDrawable(text: String, type: QrCodeType): Drawable {
    val context = LocalContext.current
    return remember(text) {
        val data = QrData.Text(text)
        val options = createQrVectorOptions {
            padding = .125f
            logo {
                drawable = context.getDrawable(
                    when (type) {
                        QrCodeType.Nostr -> R.drawable.qr_center_nostr
                        QrCodeType.Lightning -> R.drawable.qr_center_lightning
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
                indication = ripple(),
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
