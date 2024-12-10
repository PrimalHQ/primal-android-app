package net.primal.android.profile.details.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.compose.AvatarOverlap
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.premium.legend.LegendaryStyle
import net.primal.android.premium.utils.isPremiumFreeTier
import net.primal.android.premium.utils.isPremiumTier
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.profile.details.ProfileDetailsContract
import net.primal.android.profile.details.ui.model.PremiumProfileDataUi
import net.primal.android.profile.details.ui.model.shouldShowPremiumBadge
import net.primal.android.profile.domain.ProfileFollowsType
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.domain.DraftTx
import net.primal.android.wallet.utils.isLightningAddress

@Composable
fun ProfileDetailsHeader(
    state: ProfileDetailsContract.UiState,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onZapProfileClick: (DraftTx) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    onUnableToZapProfile: () -> Unit,
    onFollowsClick: (String, ProfileFollowsType) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onPremiumBadgeClick: (tier: String) -> Unit,
) {
    ProfileHeaderDetails(
        state = state,
        onEditProfileClick = onEditProfileClick,
        onMessageClick = { onMessageClick(state.profileId) },
        onZapProfileClick = {
            val profileLud16 = state.profileDetails?.lightningAddress
            if (profileLud16?.isLightningAddress() == true) {
                onZapProfileClick(
                    DraftTx(targetUserId = state.profileId, targetLud16 = profileLud16),
                )
            } else {
                onUnableToZapProfile()
            }
        },
        onFollow = {
            eventPublisher(
                ProfileDetailsContract.UiEvent.FollowAction(
                    profileId = state.profileId,
                    forceUpdate = false,
                ),
            )
        },
        onUnfollow = {
            eventPublisher(
                ProfileDetailsContract.UiEvent.UnfollowAction(
                    profileId = state.profileId,
                    forceUpdate = false,
                ),
            )
        },
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        onFollowsClick = onFollowsClick,
        onProfileClick = onProfileClick,
        onHashtagClick = onHashtagClick,
        onPremiumBadgeClick = onPremiumBadgeClick,
    )
}

@Composable
private fun ProfileHeaderDetails(
    state: ProfileDetailsContract.UiState,
    onEditProfileClick: () -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    onZapProfileClick: () -> Unit,
    onMessageClick: () -> Unit,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onFollowsClick: (String, ProfileFollowsType) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onPremiumBadgeClick: (tier: String) -> Unit,
) {
    val localUriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.colorScheme.surfaceVariant),
    ) {
        ProfileActions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
                .height(56.dp)
                .padding(horizontal = 14.dp)
                .padding(top = 14.dp)
                .background(AppTheme.colorScheme.surfaceVariant),
            isFollowed = state.isProfileFollowed,
            isActiveUser = state.isActiveUser,
            onEditProfileClick = onEditProfileClick,
            onMessageClick = onMessageClick,
            onZapProfileClick = onZapProfileClick,
            onDrawerQrCodeClick = onDrawerQrCodeClick,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
        )

        UserDisplayName(
            displayName = state.profileDetails?.authorDisplayName ?: state.profileId.asEllipsizedNpub(),
            internetIdentifier = state.profileDetails?.internetIdentifier,
            profilePremiumDetails = state.profileDetails?.premiumDetails,
            activeUserPremiumTier = state.activeUserPremiumTier,
            onPremiumBadgeClick = onPremiumBadgeClick,
        )

        if (state.profileDetails?.internetIdentifier?.isNotEmpty() == true) {
            UserInternetIdentifier(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                internetIdentifier = state.profileDetails.internetIdentifier,
            )
        }

        ProfileFollowIndicators(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            followingCount = state.profileStats?.followingCount,
            followersCount = state.profileStats?.followersCount,
            isProfileFollowingMe = state.isProfileFollowingMe,
            onFollowingClick = { onFollowsClick(state.profileId, ProfileFollowsType.Following) },
            onFollowersClick = { onFollowsClick(state.profileId, ProfileFollowsType.Followers) },
        )

        if (state.profileDetails?.about?.isNotEmpty() == true) {
            ProfileAboutSection(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                about = state.profileDetails.about,
                aboutUris = state.profileDetails.aboutUris,
                aboutHashtags = state.profileDetails.aboutHashtags,
                referencedUsers = state.referencedProfilesData,
                onProfileClick = onProfileClick,
                onHashtagClick = onHashtagClick,
                onUrlClick = { localUriHandler.openUriSafely(it) },
            )
        }

        if (state.profileDetails?.website?.isNotEmpty() == true) {
            UserWebsiteText(
                modifier = Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .padding(bottom = 2.dp),
                website = state.profileDetails.website,
                onClick = { localUriHandler.openUriSafely(state.profileDetails.website) },
            )
        }

        if (state.userFollowedByProfiles.isNotEmpty()) {
            UserFollowedByIndicator(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                profiles = state.userFollowedByProfiles.filterNot { it == state.profileDetails },
                onProfileClick = onProfileClick,
            )
        }
    }
}

@Composable
private fun UserFollowedByIndicator(
    modifier: Modifier,
    profiles: List<ProfileDetailsUi>,
    onProfileClick: (String) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        AvatarThumbnailsRow(
            avatarBorderColor = AppTheme.colorScheme.background,
            avatarCdnImages = profiles.map { it.avatarCdnImage },
            avatarLegendaryCustomizations = profiles.map { it.premiumDetails?.legendaryCustomization },
            onClick = {
                onProfileClick(profiles[it].pubkey)
            },
            avatarOverlap = AvatarOverlap.Start,
            avatarBorderSize = 1.dp,
            avatarSize = 36.dp,
            maxAvatarsToShow = 5,
            displayAvatarOverflowIndicator = false,
        )
        val text =
            stringResource(id = R.string.profile_followed_by) + " " + profiles.joinToString { it.userDisplayName }
        Text(
            text = text,
            maxLines = 2,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            overflow = TextOverflow.Ellipsis,
            style = AppTheme.typography.bodyMedium.copy(lineHeight = 16.sp),
        )
    }
}

@Composable
private fun ProfileFollowIndicators(
    modifier: Modifier = Modifier,
    followingCount: Int?,
    followersCount: Int?,
    isProfileFollowingMe: Boolean,
    onFollowingClick: () -> Unit,
    onFollowersClick: () -> Unit,
) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    val followingAnnotatedString = buildAnnotatedString {
        append(
            AnnotatedString(
                text = followingCount?.let { numberFormat.format(it) } ?: "-",
                spanStyle = SpanStyle(
                    color = AppTheme.colorScheme.onSurfaceVariant,
                    fontStyle = AppTheme.typography.labelLarge.fontStyle,
                ),
            ),
        )
        append(
            AnnotatedString(
                text = " " + stringResource(id = R.string.drawer_following_suffix).lowercase(),
                spanStyle = SpanStyle(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    fontStyle = AppTheme.typography.labelLarge.fontStyle,
                ),
            ),
        )
    }
    val followersAnnotatedString = buildAnnotatedString {
        append(
            AnnotatedString(
                text = followersCount?.let { numberFormat.format(it) } ?: "-",
                spanStyle = SpanStyle(
                    color = AppTheme.colorScheme.onSurfaceVariant,
                    fontStyle = AppTheme.typography.labelLarge.fontStyle,
                ),
            ),
        )
        append(
            AnnotatedString(
                text = " " + stringResource(id = R.string.drawer_followers_suffix).lowercase(),
                spanStyle = SpanStyle(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    fontStyle = AppTheme.typography.labelLarge.fontStyle,
                ),
            ),
        )
    }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            modifier = Modifier.clickable { onFollowingClick() },
            text = followingAnnotatedString,
            style = AppTheme.typography.labelLarge,
        )
        Text(
            modifier = Modifier.clickable { onFollowersClick() },
            text = followersAnnotatedString,
            style = AppTheme.typography.labelLarge,
        )

        if (isProfileFollowingMe) {
            Box(
                modifier = Modifier
                    .height(20.dp)
                    .clip(AppTheme.shapes.extraLarge)
                    .background(
                        color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                        shape = AppTheme.shapes.extraSmall,
                    )
                    .padding(horizontal = 12.dp)
                    .padding(top = 0.5.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(id = R.string.profile_follows_you).lowercase(),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
private fun UserDisplayName(
    modifier: Modifier = Modifier,
    displayName: String,
    internetIdentifier: String?,
    profilePremiumDetails: PremiumProfileDataUi?,
    activeUserPremiumTier: String?,
    onPremiumBadgeClick: (tier: String) -> Unit,
) {
    val hasCustomBadge = profilePremiumDetails?.legendaryCustomization?.customBadge == true

    Row(
        modifier = modifier.padding(top = 12.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NostrUserText(
            modifier = Modifier.padding(start = 14.dp, end = 6.dp),
            displayName = displayName,
            internetIdentifier = internetIdentifier,
            internetIdentifierBadgeSize = 21.dp,
            internetIdentifierBadgeAlign = PlaceholderVerticalAlign.Center,
            style = AppTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
            customBadgeStyle = if (hasCustomBadge) {
                profilePremiumDetails?.legendaryCustomization?.legendaryStyle
            } else {
                null
            },
        )

        val isPremiumBadgeClickable = activeUserPremiumTier == null ||
            activeUserPremiumTier.isPremiumFreeTier() ||
            (activeUserPremiumTier.isPremiumTier() && profilePremiumDetails?.tier.isPrimalLegendTier())

        if (profilePremiumDetails?.shouldShowPremiumBadge() == true) {
            ProfilePremiumBadge(
                modifier = if (isPremiumBadgeClickable && profilePremiumDetails.tier != null) {
                    Modifier.clickable { onPremiumBadgeClick(profilePremiumDetails.tier) }
                } else {
                    Modifier
                },
                firstCohort = profilePremiumDetails.cohort1 ?: "",
                secondCohort = profilePremiumDetails.cohort2 ?: "",
                legendaryStyle = profilePremiumDetails.legendaryCustomization?.legendaryStyle,
            )
        }
    }
}

@Composable
private fun ProfilePremiumBadge(
    modifier: Modifier = Modifier,
    firstCohort: String,
    secondCohort: String,
    legendaryStyle: LegendaryStyle?,
) {
    val density = LocalDensity.current
    val shadowOffset = with(density) { 0.5.dp.toPx() }
    Row(
        modifier = modifier
            .padding(bottom = 1.dp)
            .shadow(elevation = 1.dp, shape = AppTheme.shapes.extraLarge)
            .height(20.dp)
            .clip(AppTheme.shapes.extraLarge)
            .background(
                brush = if (legendaryStyle != null && legendaryStyle != LegendaryStyle.NO_CUSTOMIZATION) {
                    legendaryStyle.brush
                } else {
                    Brush.linearGradient(listOf(AppTheme.colorScheme.tertiary, AppTheme.colorScheme.tertiary))
                },
            )
            .padding(start = 10.dp, end = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.padding(top = 1.5.dp),
            text = firstCohort,
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    offset = Offset(x = shadowOffset, y = shadowOffset),
                ),
            ),
            fontSize = 12.sp,
            color = Color.White,
        )
        Box(
            modifier = Modifier
                .height(16.dp)
                .clip(AppTheme.shapes.extraLarge)
                .background(Color.Black.copy(alpha = 0.4f)),
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .padding(top = 0.75.dp),
                text = secondCohort,
                style = AppTheme.typography.bodySmall,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun UserWebsiteText(
    modifier: Modifier = Modifier,
    website: String,
    onClick: () -> Unit,
) {
    IconText(
        modifier = modifier.clickable { onClick() },
        text = website,
        style = AppTheme.typography.bodyMedium,
        color = AppTheme.colorScheme.secondary,
    )
}

@Composable
private fun UserInternetIdentifier(modifier: Modifier = Modifier, internetIdentifier: String) {
    Text(
        modifier = modifier,
        text = internetIdentifier.formatNip05Identifier(),
        style = AppTheme.typography.bodyMedium.copy(
            lineHeight = 16.sp,
        ),
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
    )
}

@Preview
@Composable
private fun PreviewProfileHeaderDetails() {
    PrimalPreview(primalTheme = net.primal.android.theme.domain.PrimalTheme.Sunrise) {
        Surface {
            ProfileHeaderDetails(
                state = ProfileDetailsContract.UiState(
                    profileId = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
                    isActiveUser = false,
                    isProfileFollowingMe = true,
                    profileDetails = ProfileDetailsUi(
                        pubkey = "88cc134b1a65f54ef48acc1df3665063d3ea45f04eab8af4646e561c5ae99079",
                        userDisplayName = "qauser",
                        authorDisplayName = "qauser",
                        internetIdentifier = "qa@primal.net",
                        about = "qauser",
                        premiumDetails = PremiumProfileDataUi(
                            primalName = "qa",
                            cohort1 = "Legend",
                            cohort2 = "2024",
                            tier = "primal-legend",
                            legendaryCustomization = LegendaryCustomization(
                                avatarGlow = true,
                                legendaryStyle = LegendaryStyle.BLUE,
                                customBadge = true,
                            ),
                        ),
                    ),
                    profileStats = ProfileStatsUi(
                        followingCount = 11,
                        followersCount = 12,
                        notesCount = 13,
                        repliesCount = 14,
                    ),
                ),
                onEditProfileClick = {},
                onZapProfileClick = {},
                onMessageClick = {},
                onDrawerQrCodeClick = {},
                onFollow = {},
                onUnfollow = {},
                onFollowsClick = { _, _ -> },
                onProfileClick = {},
                onHashtagClick = {},
                onPremiumBadgeClick = {},
            )
        }
    }
}
