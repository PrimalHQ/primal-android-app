package net.primal.android.stream

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.time.Instant
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.button.FollowUnfollowButton
import net.primal.android.core.compose.icons.primaliconpack.Follow
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
import net.primal.android.theme.AppTheme

private val LiveIndicatorColor = Color(0xFFEE0000)
private val NotLiveIndicatorColor = Color(0xFFAAAAAA)

@Composable
fun StreamInfoSection(
    title: String,
    authorProfile: ProfileDetailsUi,
    viewers: Int,
    startedAt: Long?,
    profileStats: ProfileStatsUi?,
    isFollowed: Boolean,
    isLive: Boolean = true,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            style = AppTheme.typography.titleLarge.copy(
                fontSize = 16.sp,
                lineHeight = 24.sp,
            ),
            fontWeight = FontWeight.Bold,
        )

        Row(
            modifier = Modifier.padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LiveIndicator(isLive = isLive)

            if (startedAt != null) {
                Text(
                    text = stringResource(
                        id = R.string.live_stream_started_at,
                        Instant.ofEpochSecond(startedAt).asBeforeNowFormat(),
                    ),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.bodyMedium,
                )
            }
            IconText(
                text = numberFormat.format(viewers),
                leadingIcon = Follow,
                iconSize = 16.sp,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                style = AppTheme.typography.bodyMedium,
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                UniversalAvatarThumbnail(avatarCdnImage = authorProfile.avatarCdnImage, avatarSize = 48.dp)

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    NostrUserText(
                        displayName = authorProfile.authorDisplayName,
                        internetIdentifier = authorProfile.internetIdentifier,
                        legendaryCustomization = authorProfile.premiumDetails?.legendaryCustomization,
                    )
                    if (profileStats != null) {
                        Text(
                            text = stringResource(
                                id = R.string.live_stream_followers_count,
                                profileStats.followersCount ?: 0,
                            ),
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                            style = AppTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            FollowUnfollowButton(
                modifier = Modifier
                    .height(35.dp)
                    .wrapContentWidth()
                    .defaultMinSize(minWidth = 75.dp),
                isFollowed = isFollowed,
                onClick = { if (isFollowed) onUnfollow() else onFollow() },
                textStyle = AppTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                paddingValues = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            )
        }
    }
}

@Composable
private fun LiveIndicator(modifier: Modifier = Modifier, isLive: Boolean) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(
                    color = if (isLive) LiveIndicatorColor else NotLiveIndicatorColor,
                    shape = CircleShape,
                ),
        )
        Text(
            text = stringResource(id = R.string.live_stream_chip_title),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            style = AppTheme.typography.bodyMedium,
        )
    }
}
