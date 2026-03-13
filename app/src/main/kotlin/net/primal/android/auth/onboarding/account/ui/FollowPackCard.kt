package net.primal.android.auth.onboarding.account.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale
import net.primal.android.R
import net.primal.android.auth.onboarding.account.ui.model.OnboardingFollowPack
import net.primal.android.core.compose.AvatarOverlap
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.PrimalAsyncImage
import net.primal.android.core.compose.PrimalDarkTextColor
import net.primal.android.core.compose.PrimalSecondaryTextColor
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

private val BannerPlaceholderColor = Color(0xFFCCCCCC)
private val SubtleBorderColor = Color(0xFFE5E5E5)
private val BannerHeight = 120.dp
private const val MaxHighlightedAvatars = 5
private const val AvatarOverlapPercentage = 0.25f

@Composable
fun FollowPackCard(
    pack: OnboardingFollowPack,
    isExpanded: Boolean,
    followedUserIds: Set<String>,
    onToggleExpanded: () -> Unit,
    onFollowUser: (String) -> Unit,
    onFollowAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val followedCountInPack = pack.members.count { it.userId in followedUserIds }
    val allFollowed = followedCountInPack == pack.members.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White),
    ) {
        FollowPackBanner(coverUrl = pack.coverUrl)

        FollowPackHeader(
            pack = pack,
            isExpanded = isExpanded,
            allFollowed = allFollowed,
            onToggleExpanded = onToggleExpanded,
            onFollowAll = onFollowAll,
        )

        ExpandedMembersList(
            isExpanded = isExpanded,
            pack = pack,
            followedUserIds = followedUserIds,
            onFollowUser = onFollowUser,
        )
    }
}

@Composable
private fun FollowPackHeader(
    pack: OnboardingFollowPack,
    isExpanded: Boolean,
    allFollowed: Boolean,
    onToggleExpanded: () -> Unit,
    onFollowAll: () -> Unit,
) {
    val avatarCdnImages = pack.members
        .take(MaxHighlightedAvatars)
        .map { it.avatarUrl?.let { url -> CdnImage(sourceUrl = url) } }
    val avatarSize = 28.dp
    val avatarsShown = avatarCdnImages.size
    val avatarRowWidth = avatarSize * (1f - AvatarOverlapPercentage) * (avatarsShown - 1) + avatarSize

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = pack.name.toTitleCase(),
            style = AppTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = PrimalDarkTextColor,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AvatarThumbnailsRow(
                    modifier = Modifier.size(width = avatarRowWidth, height = avatarSize),
                    avatarCdnImages = avatarCdnImages,
                    avatarSize = avatarSize,
                    avatarOverlap = AvatarOverlap.Start,
                    avatarOverlapPercentage = AvatarOverlapPercentage,
                    avatarBorderColor = Color.White,
                    avatarBorderSize = 1.dp,
                    maxAvatarsToShow = MaxHighlightedAvatars,
                    displayAvatarOverflowIndicator = false,
                )

                UserCountChevron(
                    count = pack.members.size,
                    isExpanded = isExpanded,
                    onClick = onToggleExpanded,
                )
            }

            FollowAllButton(
                allFollowed = allFollowed,
                onClick = onFollowAll,
            )
        }
    }
}

@Composable
private fun ExpandedMembersList(
    isExpanded: Boolean,
    pack: OnboardingFollowPack,
    followedUserIds: Set<String>,
    onFollowUser: (String) -> Unit,
) {
    AnimatedVisibility(
        visible = isExpanded,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Column {
            HorizontalDivider(color = SubtleBorderColor)
            pack.members.forEach { member ->
                FollowPackMemberRow(
                    member = member,
                    isFollowed = member.userId in followedUserIds,
                    onFollowClick = { onFollowUser(member.userId) },
                )
            }
        }
    }
}

@Composable
private fun FollowAllButton(allFollowed: Boolean, onClick: () -> Unit) {
    PrimalFilledButton(
        height = 32.dp,
        containerColor = if (allFollowed) SubtleBorderColor else PrimalDarkTextColor,
        contentColor = if (allFollowed) PrimalDarkTextColor else Color.White,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
        textStyle = AppTheme.typography.bodySmall,
        onClick = onClick,
    ) {
        Text(
            text = if (allFollowed) {
                stringResource(id = R.string.onboarding_follow_packs_following_all)
            } else {
                stringResource(id = R.string.onboarding_follow_packs_follow_all)
            },
        )
    }
}

@Composable
private fun FollowPackBanner(coverUrl: String?) {
    if (coverUrl != null) {
        PrimalAsyncImage(
            model = coverUrl,
            modifier = Modifier
                .fillMaxWidth()
                .height(BannerHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
            contentScale = ContentScale.Crop,
            placeholderColor = BannerPlaceholderColor,
            errorColor = BannerPlaceholderColor,
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BannerHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(BannerPlaceholderColor),
        )
    }
}

@Composable
private fun UserCountChevron(
    count: Int,
    isExpanded: Boolean,
    onClick: () -> Unit,
) {
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "ChevronRotation",
    )

    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = pluralStringResource(
                id = R.plurals.onboarding_follow_packs_user_count,
                count = count,
                count,
            ),
            style = AppTheme.typography.bodySmall,
            color = PrimalSecondaryTextColor,
        )
        Icon(
            modifier = Modifier
                .size(16.dp)
                .rotate(rotation),
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = null,
            tint = PrimalSecondaryTextColor,
        )
    }
}

private fun String.toTitleCase(): String {
    return lowercase(Locale.getDefault())
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
}
