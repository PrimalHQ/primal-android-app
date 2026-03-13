package net.primal.android.auth.onboarding.account.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.auth.compose.DefaultOnboardingAvatar
import net.primal.android.auth.onboarding.account.ui.model.FollowPackMember
import net.primal.android.core.compose.PrimalDarkTextColor
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

private val FollowedButtonColor = Color(0xFFE5E5E5)

@Composable
fun FollowPackMemberRow(
    member: FollowPackMember,
    isFollowed: Boolean,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        UniversalAvatarThumbnail(
            avatarSize = 40.dp,
            avatarCdnImage = member.avatarUrl?.let { CdnImage(sourceUrl = it) },
            legendaryCustomization = null,
            defaultAvatar = { DefaultOnboardingAvatar() },
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = member.displayName,
                style = AppTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 14.sp,
                ),
                color = PrimalDarkTextColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (!member.about.isNullOrBlank()) {
                Text(
                    text = member.about,
                    style = AppTheme.typography.labelSmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 12.sp,
                    ),
                    color = PrimalDarkTextColor.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        PrimalFilledButton(
            modifier = Modifier.defaultMinSize(minWidth = 80.dp),
            height = 32.dp,
            containerColor = if (isFollowed) FollowedButtonColor else PrimalDarkTextColor,
            contentColor = if (isFollowed) PrimalDarkTextColor else Color.White,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            textStyle = AppTheme.typography.bodySmall,
            onClick = onFollowClick,
        ) {
            Text(
                text = if (isFollowed) {
                    stringResource(id = R.string.onboarding_follow_packs_following)
                } else {
                    stringResource(id = R.string.onboarding_follow_packs_follow)
                },
            )
        }
    }
}
