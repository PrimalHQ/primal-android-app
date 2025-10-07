package net.primal.android.stream.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.profile.details.ui.model.PremiumProfileDataUi
import net.primal.android.stream.LiveStreamContract
import net.primal.domain.nostr.ReportType
import net.primal.domain.streams.StreamContentModerationMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamModalBottomSheetHost(
    activeSheet: ActiveBottomSheet,
    streamInfo: LiveStreamContract.StreamInfoUi?,
    zaps: List<EventZapUiModel>,
    isStreamLive: Boolean,
    activeUserId: String?,
    mainHostStreamsMuted: Boolean,
    contentModeration: StreamContentModerationMode,
    followerCountMap: Map<String, Int>,
    liveProfiles: Set<String>,
    mutedProfiles: Set<String>,
    followedProfiles: Set<String>,
    bottomSheetHeight: Dp?,
    onDismiss: () -> Unit,
    onFollow: (String) -> Unit,
    onUnfollow: (String) -> Unit,
    onMute: (String) -> Unit,
    onUnmute: (String) -> Unit,
    onZapClick: (ProfileDetailsUi) -> Unit,
    onReport: (ReportType, String, String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMessageClick: (String) -> Unit,
    onContentModerationChanged: (StreamContentModerationMode) -> Unit,
    onStreamNotificationsChanged: (Boolean) -> Unit,
    onEditProfileClick: () -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
    onZapMessageClick: (EventZapUiModel) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    when (activeSheet) {
        is ActiveBottomSheet.ChatDetails,
        ActiveBottomSheet.StreamInfo,
        is ActiveBottomSheet.ZapDetails,
        -> {
            val profileDetails = activeSheet.getProfileDetails(streamInfo)

            if (profileDetails != null && activeUserId != null && streamInfo != null) {
                ProfileDetailsBottomSheet(
                    sheetState = sheetState,
                    onDismiss = onDismiss,
                    bottomSheetHeight = bottomSheetHeight,
                    profileDetails = profileDetails,
                    isMuteUserButtonVisible = activeSheet.isMuteButtonVisible(),
                    activeUserId = activeUserId,
                    isLive = liveProfiles.contains(profileDetails.pubkey),
                    isProfileMuted = mutedProfiles.contains(profileDetails.pubkey),
                    isProfileFollowed = followedProfiles.contains(profileDetails.pubkey),
                    followersCount = followerCountMap[profileDetails.pubkey] ?: 0,
                    onFollow = { onFollow(profileDetails.pubkey) },
                    onUnfollow = { onUnfollow(profileDetails.pubkey) },
                    onMute = { onMute(profileDetails.pubkey) },
                    onUnmute = { onUnmute(profileDetails.pubkey) },
                    onZap = { onZapClick(profileDetails) },
                    onEditProfileClick = onEditProfileClick,
                    onMessageClick = onMessageClick,
                    onDrawerQrCodeClick = onDrawerQrCodeClick,
                    onProfileClick = onProfileClick,
                    bottomContent = {
                        ProfileDetailsBottomSheetContent(
                            activeSheet = activeSheet,
                            streamInfo = streamInfo,
                            isStreamLive = isStreamLive,
                            onHashtagClick = onHashtagClick,
                            onReport = onReport,
                            onDismiss = onDismiss,
                        )
                    },
                    showInternetIdentifier = activeSheet != ActiveBottomSheet.StreamInfo,
                )
            }
        }

        ActiveBottomSheet.StreamSettings -> {
            StreamSettingsBottomSheet(
                sheetState = sheetState,
                onDismiss = onDismiss,
                bottomSheetHeight = bottomSheetHeight,
                contentModerationMode = contentModeration,
                mainHostStreamsMuted = mainHostStreamsMuted,
                onStreamNotificationsChanged = onStreamNotificationsChanged,
                onContentModerationChanged = onContentModerationChanged,
            )
        }

        ActiveBottomSheet.StreamZapLeaderboard -> {
            StreamZapLeaderboardBottomSheet(
                sheetState = sheetState,
                onDismiss = onDismiss,
                bottomSheetHeight = bottomSheetHeight,
                zaps = zaps,
                onZapMessageClick = onZapMessageClick,
            )
        }

        ActiveBottomSheet.None -> Unit
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileDetailsBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    bottomSheetHeight: Dp?,
    profileDetails: ProfileDetailsUi,
    isMuteUserButtonVisible: Boolean,
    activeUserId: String,
    isLive: Boolean,
    isProfileMuted: Boolean,
    isProfileFollowed: Boolean,
    showInternetIdentifier: Boolean,
    followersCount: Int,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onMute: () -> Unit,
    onUnmute: () -> Unit,
    onZap: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    bottomContent: @Composable () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BottomSheetBackgroundSecondaryColor,
        tonalElevation = 0.dp,
        dragHandle = {
            LiveStreamBottomSheetDragHandle()
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (bottomSheetHeight != null) Modifier.height(bottomSheetHeight) else Modifier),
        ) {
            StreamInfoBottomSheet(
                modifier = Modifier.padding(bottom = 16.dp),
                isMuteUserButtonVisible = isMuteUserButtonVisible,
                activeUserId = activeUserId,
                isLive = isLive,
                onFollow = onFollow,
                onUnfollow = onUnfollow,
                onMute = onMute,
                onUnmute = onUnmute,
                onZap = onZap,
                onEditProfileClick = onEditProfileClick,
                onMessageClick = onMessageClick,
                onDrawerQrCodeClick = onDrawerQrCodeClick,
                onProfileClick = onProfileClick,
                bottomContent = bottomContent,
                profileDetails = profileDetails,
                isProfileMuted = isProfileMuted,
                isProfileFollowed = isProfileFollowed,
                followersCount = followersCount,
                showInternetIdentifier = showInternetIdentifier,
            )
        }
    }
}

@Composable
private fun ProfileDetailsBottomSheetContent(
    activeSheet: ActiveBottomSheet,
    streamInfo: LiveStreamContract.StreamInfoUi,
    isStreamLive: Boolean,
    onHashtagClick: (String) -> Unit,
    onReport: (ReportType, String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    when (activeSheet) {
        is ActiveBottomSheet.StreamInfo -> {
            StreamDescriptionSection(
                streamInfo = streamInfo,
                isLive = isStreamLive,
                onHashtagClick = onHashtagClick,
            )
        }

        is ActiveBottomSheet.ChatDetails -> {
            ChatMessageDetailsSection(
                isScrollable = true,
                onReport = { reportType ->
                    onReport(
                        reportType,
                        activeSheet.message.messageId,
                        activeSheet.message.authorProfile.pubkey,
                    )
                    onDismiss()
                },
            ) {
                ChatMessageListItem(
                    message = activeSheet.message,
                    onProfileClick = {},
                    onNostrUriClick = {},
                )
            }
        }

        is ActiveBottomSheet.ZapDetails -> {
            ChatMessageDetailsSection(
                onReport = { reportType ->
                    activeSheet.zap.zapperId.let { zapperId ->
                        onReport(reportType, activeSheet.zap.id, zapperId)
                    }
                    onDismiss()
                },
            ) {
                ZapMessageListItem(
                    modifier = Modifier.weight(1f, false),
                    zap = activeSheet.zap,
                    isScrollable = true,
                )
            }
        }

        else -> Unit
    }
}

private fun ActiveBottomSheet.getProfileDetails(streamInfo: LiveStreamContract.StreamInfoUi?): ProfileDetailsUi? =
    when (this) {
        is ActiveBottomSheet.ChatDetails -> this.message.authorProfile
        is ActiveBottomSheet.ZapDetails -> this.zap.toProfileDetailsUi()
        is ActiveBottomSheet.StreamInfo -> streamInfo?.mainHostProfile
        ActiveBottomSheet.StreamSettings, ActiveBottomSheet.None, ActiveBottomSheet.StreamZapLeaderboard -> null
    }

private fun ActiveBottomSheet.isMuteButtonVisible(): Boolean =
    this is ActiveBottomSheet.ChatDetails || this is ActiveBottomSheet.ZapDetails

private fun EventZapUiModel.toProfileDetailsUi(): ProfileDetailsUi {
    return ProfileDetailsUi(
        pubkey = this.zapperId,
        authorDisplayName = this.zapperName,
        userDisplayName = this.zapperHandle,
        avatarCdnImage = this.zapperAvatarCdnImage,
        internetIdentifier = this.zapperInternetIdentifier,
        premiumDetails = PremiumProfileDataUi(
            legendaryCustomization = this.zapperLegendaryCustomization,
        ),
    )
}
