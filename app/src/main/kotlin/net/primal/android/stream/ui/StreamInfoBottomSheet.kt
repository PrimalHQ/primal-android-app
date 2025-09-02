package net.primal.android.stream.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ContextMuteUser
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.utils.shortened
import net.primal.android.profile.details.ui.ProfileActions
import net.primal.android.theme.AppTheme

@Composable
fun StreamInfoBottomSheet(
    modifier: Modifier = Modifier,
    profileDetails: ProfileDetailsUi,
    isProfileMuted: Boolean,
    isProfileFollowed: Boolean,
    followersCount: Int,
    isMuteUserButtonVisible: Boolean,
    activeUserId: String,
    isLive: Boolean,
    showInternetIdentifier: Boolean,
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
    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.background(color = BottomSheetBackgroundPrimaryColor),
        ) {
            HostInfoAndActions(
                modifier = Modifier.padding(horizontal = 16.dp),
                isMuteButtonVisible = isMuteUserButtonVisible,
                onFollow = onFollow,
                onUnfollow = onUnfollow,
                onMute = onMute,
                onUnmute = onUnmute,
                onZap = onZap,
                onEditProfileClick = onEditProfileClick,
                onMessageClick = onMessageClick,
                onDrawerQrCodeClick = onDrawerQrCodeClick,
                onProfileClick = onProfileClick,
                activeUserId = activeUserId,
                isLive = isLive,
                isProfileMuted = isProfileMuted,
                isProfileFollowed = isProfileFollowed,
                followersCount = followersCount,
                profileDetails = profileDetails,
                showInternetIdentifier = showInternetIdentifier,
            )

            PrimalDivider(
                modifier = Modifier.padding(top = 16.dp),
                color = BottomSheetDividerColor,
                thickness = 1.dp,
            )
        }

        bottomContent()
    }
}

@Composable
private fun HostInfoAndActions(
    modifier: Modifier = Modifier,
    isMuteButtonVisible: Boolean,
    isProfileMuted: Boolean,
    isProfileFollowed: Boolean,
    profileDetails: ProfileDetailsUi,
    followersCount: Int,
    activeUserId: String,
    isLive: Boolean,
    showInternetIdentifier: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onMute: () -> Unit,
    onUnmute: () -> Unit,
    onZap: () -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HostProfileSummary(
            profileDetails = profileDetails,
            followersCount = followersCount,
            isLive = isLive,
            showInternetIdentifier = showInternetIdentifier,
            onProfileClick = onProfileClick,
        )
        HostActionRow(
            isMuteButtonVisible = isMuteButtonVisible,
            activeUserId = activeUserId,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
            onMute = onMute,
            onUnmute = onUnmute,
            onZap = onZap,
            onEditProfileClick = onEditProfileClick,
            onMessageClick = onMessageClick,
            onDrawerQrCodeClick = onDrawerQrCodeClick,
            profileId = profileDetails.pubkey,
            isProfileMuted = isProfileMuted,
            isProfileFollowed = isProfileFollowed,
        )
    }
}

@Composable
private fun HostProfileSummary(
    profileDetails: ProfileDetailsUi,
    followersCount: Int,
    isLive: Boolean,
    showInternetIdentifier: Boolean,
    onProfileClick: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        UniversalAvatarThumbnail(
            isLive = isLive,
            avatarCdnImage = profileDetails.avatarCdnImage,
            avatarSize = 46.dp,
            legendaryCustomization = profileDetails.premiumDetails?.legendaryCustomization,
            onClick = { onProfileClick(profileDetails.pubkey) },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                NostrUserText(
                    displayName = profileDetails.userDisplayName,
                    internetIdentifier = profileDetails.internetIdentifier,
                    internetIdentifierBadgeSize = 13.dp,
                    internetIdentifierBadgeAlign = PlaceholderVerticalAlign.Center,
                    legendaryCustomization = profileDetails.premiumDetails?.legendaryCustomization,
                    style = AppTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
                if (showInternetIdentifier) {
                    profileDetails.internetIdentifier?.let {
                        Text(
                            text = it,
                            style = AppTheme.typography.bodyLarge.copy(
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                            ),
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                        )
                    }
                } else {
                    Text(
                        text = stringResource(id = R.string.live_stream_followers_count, followersCount.shortened()),
                        style = AppTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            lineHeight = 16.sp,
                        ),
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    )
                }
            }

            if (showInternetIdentifier) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = followersCount.shortened(),
                        style = AppTheme.typography.bodyLarge.copy(
                            fontSize = 14.sp,
                            lineHeight = 16.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = AppTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = stringResource(id = R.string.profile_followers_stat),
                        style = AppTheme.typography.bodyLarge.copy(
                            fontSize = 14.sp,
                            lineHeight = 14.sp,
                        ),
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    )
                }
            }
        }
    }
}

@Composable
private fun HostActionRow(
    profileId: String,
    isMuteButtonVisible: Boolean,
    isProfileMuted: Boolean,
    isProfileFollowed: Boolean,
    activeUserId: String,
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
            PrimalFilledButton(
                containerColor = ActionButtonHandleColor,
                contentColor = AppTheme.colorScheme.onSurface,
                textStyle = AppTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp),
                onClick = { if (isProfileMuted) onUnmute() else onMute() },
                contentPadding = PaddingValues(16.dp, 0.dp),
                height = 35.dp,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = PrimalIcons.ContextMuteUser,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(top = 3.dp),
                        text = if (isProfileMuted) {
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
            isFollowed = isProfileFollowed,
            isActiveUser = activeUserId == profileId,
            onEditProfileClick = onEditProfileClick,
            onMessageClick = { onMessageClick(profileId) },
            onZapProfileClick = onZap,
            onDrawerQrCodeClick = { onDrawerQrCodeClick(profileId) },
            onFollow = onFollow,
            onUnfollow = onUnfollow,
            containerColor = ActionButtonHandleColor,
        )
    }
}
