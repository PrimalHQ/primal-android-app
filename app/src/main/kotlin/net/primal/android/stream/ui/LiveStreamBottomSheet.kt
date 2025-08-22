package net.primal.android.stream.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ContextReportContent
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.profile.report.ReportUserDialog
import net.primal.android.stream.LiveStreamContract
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.ReportType

private val ReportButtonHandleColor: Color
    @Composable
    get() = if (LocalPrimalTheme.current.isDarkTheme) {
        Color(0xFF333333)
    } else {
        Color(0xFFD5D5D5)
    }

data class BottomSheetProfile(
    val details: ProfileDetailsUi,
    val stats: ProfileStatsUi?,
    val isFollowed: Boolean,
    val isMuted: Boolean,
    val isActiveUser: Boolean,
)

@Composable
private fun rememberBottomSheetProfile(
    activeSheet: ActiveBottomSheet,
    state: LiveStreamContract.UiState,
): BottomSheetProfile? {
    val bottomSheetProfile by remember(activeSheet, state) {
        derivedStateOf {
            val streamInfo = state.streamInfo
            val activeUserId = state.activeUserId
            if (streamInfo == null || activeUserId == null) {
                return@derivedStateOf null
            }

            when (activeSheet) {
                ActiveBottomSheet.StreamInfo -> BottomSheetProfile(
                    details = streamInfo.mainHostProfile!!,
                    stats = streamInfo.mainHostProfileStats,
                    isFollowed = streamInfo.isMainHostFollowedByActiveUser,
                    isMuted = streamInfo.isMainHostMutedByActiveUser,
                    isActiveUser = activeUserId == streamInfo.mainHostId,
                )

                is ActiveBottomSheet.ChatDetails -> {
                    val messageItem = state.chatItems
                        .filterIsInstance<StreamChatItem.ChatMessageItem>()
                        .find { it.uniqueId == activeSheet.message.uniqueId }
                        ?: activeSheet.message
                    val authorProfile = messageItem.message.authorProfile
                    BottomSheetProfile(
                        details = authorProfile,
                        stats = messageItem.authorProfileStats,
                        isFollowed = messageItem.isAuthorFollowed,
                        isMuted = messageItem.isAuthorMuted,
                        isActiveUser = activeUserId == authorProfile.pubkey,
                    )
                }

                is ActiveBottomSheet.ZapDetails -> {
                    val zapItem = state.chatItems
                        .filterIsInstance<StreamChatItem.ZapMessageItem>()
                        .find { it.uniqueId == activeSheet.zap.uniqueId }
                        ?: activeSheet.zap

                    val zapperProfileDetails = zapItem.zapperProfile ?: ProfileDetailsUi(
                        pubkey = zapItem.zap.zapperId,
                        authorDisplayName = zapItem.zap.zapperHandle,
                        userDisplayName = zapItem.zap.zapperHandle,
                        avatarCdnImage = zapItem.zap.zapperAvatarCdnImage,
                        internetIdentifier = zapItem.zap.zapperInternetIdentifier,
                    )

                    BottomSheetProfile(
                        details = zapperProfileDetails,
                        stats = zapItem.zapperProfileStats,
                        isFollowed = zapItem.isZapperFollowed,
                        isMuted = zapItem.isZapperMuted,
                        isActiveUser = activeUserId == zapperProfileDetails.pubkey,
                    )
                }

                ActiveBottomSheet.None -> null
            }
        }
    }

    return bottomSheetProfile
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LiveStreamModalBottomSheets(
    activeSheet: ActiveBottomSheet,
    onDismiss: () -> Unit,
    state: LiveStreamContract.UiState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    callbacks: LiveStreamContract.ScreenCallbacks,
    onZapClick: (String, String?) -> Unit,
    bottomSheetHeight: Dp?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val bottomSheetProfile = rememberBottomSheetProfile(activeSheet = activeSheet, state = state)

    if (activeSheet != ActiveBottomSheet.None && bottomSheetProfile != null) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (bottomSheetHeight != null) Modifier.height(bottomSheetHeight) else Modifier)
                    .padding(top = 8.dp),
            ) {
                StreamInfoBottomSheet(
                    modifier = Modifier.padding(bottom = 16.dp),
                    profile = bottomSheetProfile,
                    onFollow = {
                        eventPublisher(
                            LiveStreamContract.UiEvent.FollowAction(bottomSheetProfile.details.pubkey),
                        )
                    },
                    onUnfollow = {
                        eventPublisher(
                            LiveStreamContract.UiEvent.UnfollowAction(bottomSheetProfile.details.pubkey),
                        )
                    },
                    onMute = {
                        eventPublisher(
                            LiveStreamContract.UiEvent.MuteAction(profileId = bottomSheetProfile.details.pubkey),
                        )
                    },
                    onUnmute = {
                        eventPublisher(
                            LiveStreamContract.UiEvent.UnmuteAction(profileId = bottomSheetProfile.details.pubkey),
                        )
                    },
                    onZap = {
                        onZapClick(
                            bottomSheetProfile.details.pubkey,
                            bottomSheetProfile.details.lightningAddress,
                        )
                    },
                    onEditProfileClick = callbacks.onEditProfileClick,
                    onMessageClick = callbacks.onMessageClick,
                    onDrawerQrCodeClick = callbacks.onDrawerQrCodeClick,
                    bottomContent = {
                        BottomSheetContent(
                            activeSheet = activeSheet,
                            state = state,
                            eventPublisher = eventPublisher,
                            callbacks = callbacks,
                            onDismiss = onDismiss,
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun BottomSheetContent(
    activeSheet: ActiveBottomSheet,
    state: LiveStreamContract.UiState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    callbacks: LiveStreamContract.ScreenCallbacks,
    onDismiss: () -> Unit,
) {
    when (activeSheet) {
        is ActiveBottomSheet.StreamInfo -> {
            StreamDescriptionSection(
                streamInfo = state.streamInfo!!,
                isLive = state.playerState.isLive,
                onHashtagClick = callbacks.onHashtagClick,
            )
        }

        is ActiveBottomSheet.ChatDetails -> {
            ChatDetailsSection(
                message = activeSheet.message.message,
                onReport = { reportType ->
                    eventPublisher(
                        LiveStreamContract.UiEvent.ReportMessage(
                            reportType = reportType,
                            messageId = activeSheet.message.message.messageId,
                            authorId = activeSheet.message.message.authorProfile.pubkey,
                        ),
                    )
                    onDismiss()
                },
            )
        }

        is ActiveBottomSheet.ZapDetails -> {
            ZapDetailsSection(
                zap = activeSheet.zap.zap,
                onReport = { reportType ->
                    eventPublisher(
                        LiveStreamContract.UiEvent.ReportMessage(
                            reportType = reportType,
                            messageId = activeSheet.zap.zap.id,
                            authorId = activeSheet.zap.zap.zapperId,
                        ),
                    )
                    onDismiss()
                },
            )
        }
        is ActiveBottomSheet.None -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailsSection(message: ChatMessageUi, onReport: (ReportType) -> Unit) {
    var reportDialogVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                AppTheme.extraColorScheme.surfaceVariantAlt1,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.live_stream_chat_message),
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 16.sp,
            ),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        ChatMessageListItem(message = message, onProfileClick = {}, onClick = {})

        PrimalFilledButton(
            modifier = Modifier.padding(start = 40.dp),
            containerColor = ReportButtonHandleColor,
            contentColor = AppTheme.colorScheme.onSurface,
            textStyle = AppTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp),
            onClick = { reportDialogVisible = true },
            contentPadding = PaddingValues(18.dp, vertical = 1.dp),
            height = 41.dp,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = PrimalIcons.ContextReportContent,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    modifier = Modifier.padding(3.dp),
                    text = stringResource(id = R.string.live_stream_report_message_button),
                )
            }
        }
    }

    if (reportDialogVisible) {
        ReportUserDialog(
            onDismissRequest = { reportDialogVisible = false },
            onReportClick = { type ->
                reportDialogVisible = false
                onReport(type)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZapDetailsSection(zap: EventZapUiModel, onReport: (ReportType) -> Unit) {
    var reportDialogVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                AppTheme.extraColorScheme.surfaceVariantAlt1,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.live_stream_chat_message),
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 16.sp,
            ),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        ZapMessageListItem(zap = zap, onClick = {})

        PrimalFilledButton(
            containerColor = ReportButtonHandleColor,
            contentColor = AppTheme.colorScheme.onSurface,
            textStyle = AppTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp),
            onClick = { reportDialogVisible = true },
            contentPadding = PaddingValues(18.dp, vertical = 0.dp),
            height = 41.dp,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = PrimalIcons.ContextReportContent,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    modifier = Modifier.padding(top = 3.dp),
                    text = stringResource(id = R.string.live_stream_report_message_button),
                )
            }
        }
    }

    if (reportDialogVisible) {
        ReportUserDialog(
            onDismissRequest = { reportDialogVisible = false },
            onReportClick = { type ->
                reportDialogVisible = false
                onReport(type)
            },
        )
    }
}
