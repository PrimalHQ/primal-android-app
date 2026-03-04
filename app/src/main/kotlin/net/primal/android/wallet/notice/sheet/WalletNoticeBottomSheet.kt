package net.primal.android.wallet.notice.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalBottomSheetDragHandle
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.theme.AppTheme

private const val FAQ_ANNOTATION_TAG = "FaqAnnotationTag"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletNoticeBottomSheet(
    viewModel: WalletNoticeSheetViewModel,
    onUpgradeClick: () -> Unit,
    onFaqClick: () -> Unit,
    onRestoreWalletClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    if (state.noticeType != null && state.shouldShowNotice) {
        val onDismiss: () -> Unit = { viewModel.setEvent(WalletNoticeSheetContract.UiEvent.DismissSheet) }

        when (state.noticeType) {
            WalletNoticeType.UpgradeWallet -> UpgradeWalletSheetContent(
                state = state,
                sheetState = sheetState,
                onDismissRequest = onDismiss,
                onUpgradeClick = {
                    onDismiss()
                    onUpgradeClick()
                },
                onFaqClick = {
                    onDismiss()
                    onFaqClick()
                },
            )

            WalletNoticeType.WalletDiscontinued -> WalletDiscontinuedSheetContent(
                state = state,
                sheetState = sheetState,
                onDismissRequest = onDismiss,
            )

            WalletNoticeType.WalletDetected -> WalletDetectedSheetContent(
                state = state,
                sheetState = sheetState,
                onDismissRequest = onDismiss,
                onRestoreWalletClick = onRestoreWalletClick,
            )

            null -> Unit
        }
    }

    content()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UpgradeWalletSheetContent(
    state: WalletNoticeSheetContract.UiState,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onUpgradeClick: () -> Unit,
    onFaqClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { PrimalBottomSheetDragHandle() },
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                CompositionLocalProvider(LocalContentColor provides AppTheme.colorScheme.onPrimary) {
                    UniversalAvatarThumbnail(
                        avatarCdnImage = state.activeUserCdnImage,
                        legendaryCustomization = state.activeUserLegendaryCustomization,
                    )
                }

                TitleColumn()
            }

            BodyText(onFaqClick = onFaqClick)
            UpgradeWalletNowButton(onClick = onUpgradeClick)
        }
    }
}

@Composable
private fun TitleColumn(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(id = R.string.wallet_notice_upgrade_title),
            color = AppTheme.colorScheme.onPrimary,
            style = AppTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun BodyText(onFaqClick: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.wallet_notice_upgrade_description_first),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            style = AppTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
            textAlign = TextAlign.Center,
        )

        val descriptionText = stringResource(id = R.string.wallet_notice_upgrade_description_second)
        val faqText = stringResource(id = R.string.wallet_notice_upgrade_faqs)

        val annotatedString = buildAnnotatedString {
            append(descriptionText)
            append(" ")
            pushStringAnnotation(FAQ_ANNOTATION_TAG, "faq")
            withStyle(SpanStyle(color = AppTheme.colorScheme.secondary)) {
                append(faqText)
            }
            pop()
            append(".")
        }

        PrimalClickableText(
            text = annotatedString,
            style = AppTheme.typography.bodyMedium.copy(
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
            ),
            onClick = { position, _ ->
                annotatedString.getStringAnnotations(
                    tag = FAQ_ANNOTATION_TAG,
                    start = position,
                    end = position,
                ).firstOrNull()?.let {
                    onFaqClick()
                }
            },
        )
    }
}

@Composable
private fun UpgradeWalletNowButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    PrimalFilledButton(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        onClick = onClick,
    ) {
        Text(
            text = stringResource(id = R.string.wallet_notice_upgrade_button),
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodyLarge,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletDiscontinuedSheetContent(
    state: WalletNoticeSheetContract.UiState,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { PrimalBottomSheetDragHandle() },
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                CompositionLocalProvider(LocalContentColor provides AppTheme.colorScheme.onPrimary) {
                    UniversalAvatarThumbnail(
                        avatarCdnImage = state.activeUserCdnImage,
                        legendaryCustomization = state.activeUserLegendaryCustomization,
                    )
                }

                Text(
                    text = stringResource(id = R.string.wallet_notice_discontinued_title),
                    color = AppTheme.colorScheme.onPrimary,
                    style = AppTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = stringResource(id = R.string.wallet_notice_discontinued_description),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                style = AppTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                textAlign = TextAlign.Center,
            )

            PrimalFilledButton(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                onClick = onDismissRequest,
            ) {
                Text(
                    text = stringResource(id = R.string.wallet_notice_discontinued_close_button),
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WalletDetectedSheetContent(
    state: WalletNoticeSheetContract.UiState,
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onRestoreWalletClick: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        dragHandle = { PrimalBottomSheetDragHandle() },
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    ) {
        Column(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Column(
                modifier = Modifier.padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                CompositionLocalProvider(LocalContentColor provides AppTheme.colorScheme.onPrimary) {
                    UniversalAvatarThumbnail(
                        avatarCdnImage = state.activeUserCdnImage,
                        legendaryCustomization = state.activeUserLegendaryCustomization,
                    )
                }

                Text(
                    text = stringResource(id = R.string.wallet_notice_detected_title),
                    color = AppTheme.colorScheme.onPrimary,
                    style = AppTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            Text(
                text = stringResource(id = R.string.wallet_notice_detected_description),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                style = AppTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                textAlign = TextAlign.Center,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                PrimalFilledButton(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    onClick = {
                        onDismissRequest()
                        onRestoreWalletClick()
                    },
                ) {
                    Text(
                        text = stringResource(id = R.string.wallet_notice_detected_restore_button),
                        fontWeight = FontWeight.Bold,
                        style = AppTheme.typography.bodyLarge,
                    )
                }

                TextButton(
                    onClick = onDismissRequest,
                ) {
                    Text(
                        text = stringResource(id = R.string.wallet_notice_detected_create_button),
                        color = AppTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold,
                        style = AppTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}
