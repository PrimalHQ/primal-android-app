package net.primal.android.stream.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ContextMuteUser
import net.primal.android.profile.details.ui.ProfileActions
import net.primal.android.stream.LiveStreamContract
import net.primal.android.theme.AppTheme

private val MuteButtonHandleColor: Color
    @Composable
    get() = if (LocalPrimalTheme.current.isDarkTheme) {
        Color(0xFF333333)
    } else {
        Color(0xFFD5D5D5)
    }

@Composable
fun StreamInfoBottomSheet(
    modifier: Modifier = Modifier,
    isMuteStreamHostButtonVisible: Boolean,
    activeUserId: String,
    streamInfo: LiveStreamContract.StreamInfoUi,
    isLive: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onMute: () -> Unit,
    onUnmute: () -> Unit,
    onZap: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
    bottomContent: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HostInfoAndActions(
            modifier = Modifier.padding(horizontal = 16.dp),
            isMuteButtonVisible = isMuteStreamHostButtonVisible,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
            onMute = onMute,
            onUnmute = onUnmute,
            onZap = onZap,
            onEditProfileClick = onEditProfileClick,
            onMessageClick = onMessageClick,
            onDrawerQrCodeClick = onDrawerQrCodeClick,
            activeUserId = activeUserId,
            streamInfo = streamInfo,
            isLive = isLive,
        )

        PrimalDivider(modifier = Modifier.padding(top = 16.dp))

        bottomContent()
    }
}

@Composable
private fun HostInfoAndActions(
    modifier: Modifier = Modifier,
    isMuteButtonVisible: Boolean,
    activeUserId: String,
    streamInfo: LiveStreamContract.StreamInfoUi,
    isLive: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onMute: () -> Unit,
    onUnmute: () -> Unit,
    onZap: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HostProfileSummary(
            streamInfo = streamInfo,
            isLive = isLive,
        )
        HostActionRow(
            isMuteButtonVisible = isMuteButtonVisible,
            activeUserId = activeUserId,
            streamInfo = streamInfo,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
            onMute = onMute,
            onUnmute = onUnmute,
            onZap = onZap,
            onEditProfileClick = onEditProfileClick,
            onMessageClick = onMessageClick,
            onDrawerQrCodeClick = onDrawerQrCodeClick,
        )
    }
}

@Composable
private fun HostProfileSummary(streamInfo: LiveStreamContract.StreamInfoUi, isLive: Boolean) {
    val mainHostProfile = streamInfo.mainHostProfile!!
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        UniversalAvatarThumbnail(
            isLive = isLive,
            avatarCdnImage = mainHostProfile.avatarCdnImage,
            avatarSize = 56.dp,
            legendaryCustomization = mainHostProfile.premiumDetails?.legendaryCustomization,
        )
        Column(modifier = Modifier.weight(1f)) {
            NostrUserText(
                modifier = Modifier.padding(top = 4.dp),
                displayName = streamInfo.mainHostProfile.userDisplayName,
                internetIdentifier = streamInfo.mainHostProfile.internetIdentifier,
                internetIdentifierBadgeSize = 20.dp,
                internetIdentifierBadgeAlign = PlaceholderVerticalAlign.Center,
                legendaryCustomization = streamInfo.mainHostProfile.premiumDetails?.legendaryCustomization,
            )
            streamInfo.mainHostProfileStats?.followersCount?.let {
                Text(
                    text = stringResource(id = R.string.live_stream_followers_count, numberFormat.format(it)),
                    style = AppTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                    ),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        }
    }
}

@Composable
private fun HostActionRow(
    isMuteButtonVisible: Boolean,
    activeUserId: String,
    streamInfo: LiveStreamContract.StreamInfoUi,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onMute: () -> Unit,
    onUnmute: () -> Unit,
    onZap: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isMuteButtonVisible) {
            val isMuted = streamInfo.isMainHostMutedByActiveUser
            PrimalFilledButton(
                containerColor = MuteButtonHandleColor,
                contentColor = AppTheme.colorScheme.onSurface,
                textStyle = AppTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp),
                onClick = { if (isMuted) onUnmute() else onMute() },
                contentPadding = PaddingValues(16.dp, 0.dp),
                height = 40.dp,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = PrimalIcons.ContextMuteUser,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = if (isMuted) {
                            stringResource(id = R.string.live_stream_bottom_sheet_unmute_user)
                        } else {
                            stringResource(id = R.string.live_stream_bottom_sheet_mute_user)
                        },
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        ProfileActions(
            modifier = Modifier,
            isFollowed = streamInfo.isMainHostFollowedByActiveUser,
            isActiveUser = activeUserId == streamInfo.mainHostId,
            onEditProfileClick = onEditProfileClick,
            onMessageClick = { onMessageClick(streamInfo.mainHostId) },
            onZapProfileClick = onZap,
            onDrawerQrCodeClick = { onDrawerQrCodeClick(streamInfo.mainHostId) },
            onFollow = onFollow,
            onUnfollow = onUnfollow,
        )
    }
}
