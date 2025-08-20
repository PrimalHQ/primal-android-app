package net.primal.android.stream.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.profile.details.ui.ProfileActions
import net.primal.android.stream.LiveStreamContract
import net.primal.android.theme.AppTheme

@Composable
fun StreamInfoBottomSheet(
    modifier: Modifier = Modifier,
    activeUserId: String,
    streamInfo: LiveStreamContract.StreamInfoUi,
    isLive: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onZap: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
    bottomContent: @Composable () -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        HostInfoAndActions(
            modifier = Modifier.padding(horizontal = 16.dp),
            onFollow = onFollow,
            onUnfollow = onUnfollow,
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
    activeUserId: String,
    streamInfo: LiveStreamContract.StreamInfoUi,
    isLive: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onZap: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
) {
    val mainHostProfile = streamInfo.mainHostProfile!!
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

        ProfileActions(
            modifier = Modifier.fillMaxWidth(),
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
