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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AvatarOverlap
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.profile.model.ProfileStatsUi
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.LegendaryStyle
import net.primal.android.profile.details.ProfileDetailsContract
import net.primal.android.profile.details.ui.model.PremiumProfileDataUi
import net.primal.android.profile.details.ui.model.shouldShowPremiumBadge
import net.primal.android.profile.domain.ProfileFollowsType
import net.primal.android.theme.AppTheme
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.domain.DraftTx
import net.primal.android.wallet.utils.isLightningAddress
import net.primal.domain.nostr.utils.asEllipsizedNpub

@Composable
fun ProfileHeaderDetails(
    state: ProfileDetailsContract.UiState,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    callbacks: ProfileDetailsContract.ScreenCallbacks,
    noteCallbacks: NoteCallbacks,
    showZapOptions: () -> Unit,
    showCantZapWarning: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    val uiScope = rememberCoroutineScope()
    val context = LocalContext.current

    fun onZapProfile(draftTx: DraftTx) {
        if (state.zappingState.walletConnected) {
            if (state.zappingState.walletPreference == WalletPreference.NostrWalletConnect) {
                showZapOptions()
            } else {
                callbacks.onSendWalletTx(draftTx)
            }
        } else {
            showCantZapWarning()
        }
    }

    fun onUnableToZapProfile() {
        uiScope.launch {
            snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.wallet_send_payment_error_nostr_user_without_lightning_address,
                    state.profileDetails?.authorDisplayName
                        ?: context.getString(R.string.wallet_send_payment_this_user_chunk),
                ),
                duration = SnackbarDuration.Short,
            )
        }
    }

    ProfileHeaderDetails(
        state = state,
        onEditProfileClick = callbacks.onEditProfileClick,
        onMessageClick = { state.profileId?.let { callbacks.onMessageClick(state.profileId) } },
        onZapProfileClick = {
            val profileLud16 = state.profileDetails?.lightningAddress
            if (profileLud16?.isLightningAddress() == true) {
                onZapProfile(DraftTx(targetUserId = state.profileId, targetLud16 = profileLud16))
            } else {
                onUnableToZapProfile()
            }
        },
        onFollow = {
            state.profileId?.let {
                eventPublisher(ProfileDetailsContract.UiEvent.FollowAction(profileId = it))
            }
        },
        onUnfollow = {
            state.profileId?.let {
                eventPublisher(ProfileDetailsContract.UiEvent.UnfollowAction(profileId = it))
            }
        },
        onDrawerQrCodeClick = { state.profileId?.let { callbacks.onDrawerQrCodeClick(it) } },
        onFollowsClick = callbacks.onFollowsClick,
        onProfileClick = { noteCallbacks.onProfileClick?.invoke(it) },
        onHashtagClick = { noteCallbacks.onHashtagClick?.invoke(it) },
        onPremiumBadgeClick = callbacks.onPremiumBadgeClick,
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
    onPremiumBadgeClick: (tier: String, profileId: String) -> Unit,
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
            isActiveUser = state.isActiveUser == true,
            onEditProfileClick = onEditProfileClick,
            onMessageClick = onMessageClick,
            onZapProfileClick = onZapProfileClick,
            onDrawerQrCodeClick = onDrawerQrCodeClick,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
        )

        state.profileId?.let { profileId ->
            UserDisplayName(
                profileId = profileId,
                displayName = state.profileDetails?.authorDisplayName ?: profileId.asEllipsizedNpub(),
                internetIdentifier = state.profileDetails?.internetIdentifier,
                profilePremiumDetails = state.profileDetails?.premiumDetails,
                onPremiumBadgeClick = onPremiumBadgeClick,
            )
        }

        if (state.profileDetails?.internetIdentifier?.isNotEmpty() == true) {
            UserInternetIdentifier(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                internetIdentifier = state.profileDetails.internetIdentifier,
            )
        }

        state.profileId?.let { profileId ->
            ProfileFollowIndicators(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                followingCount = state.profileStats?.followingCount,
                followersCount = state.profileStats?.followersCount,
                isProfileFollowingMe = state.isProfileFollowingMe,
                onFollowingClick = { onFollowsClick(profileId, ProfileFollowsType.Following) },
                onFollowersClick = { onFollowsClick(profileId, ProfileFollowsType.Followers) },
            )
        }

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
                profiles = state.userFollowedByProfiles,
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
    profileId: String,
    displayName: String,
    internetIdentifier: String?,
    profilePremiumDetails: PremiumProfileDataUi?,
    onPremiumBadgeClick: (tier: String, profileId: String) -> Unit,
) {
    Row(
        modifier = modifier.padding(top = 12.dp, bottom = 3.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NostrUserText(
            modifier = Modifier
                .weight(1f, fill = false)
                .padding(start = 14.dp, end = 6.dp),
            displayName = displayName,
            internetIdentifier = internetIdentifier,
            internetIdentifierBadgeSize = 21.dp,
            autoResizeToFit = true,
            internetIdentifierBadgeAlign = PlaceholderVerticalAlign.Center,
            style = AppTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
            legendaryCustomization = profilePremiumDetails?.legendaryCustomization,
        )

        if (profilePremiumDetails?.shouldShowPremiumBadge() == true) {
            ProfilePremiumBadge(
                modifier = Modifier.clickable(enabled = profilePremiumDetails.tier != null) {
                    profilePremiumDetails.tier?.let { onPremiumBadgeClick(profilePremiumDetails.tier, profileId) }
                },
                firstCohort = profilePremiumDetails.cohort1 ?: "",
                secondCohort = profilePremiumDetails.cohort2 ?: "",
                legendaryStyle = profilePremiumDetails.legendaryCustomization?.legendaryStyle,
            )
        }
    }
}

@Composable
fun ProfilePremiumBadge(
    modifier: Modifier = Modifier,
    firstCohort: String,
    secondCohort: String,
    legendaryStyle: LegendaryStyle?,
    firstCohortFontSize: TextUnit = 12.sp,
    secondCohortFontSize: TextUnit = 12.sp,
) {
    val density = LocalDensity.current
    val shadowOffset = with(density) { 0.5.dp.toPx() }
    Row(
        modifier = modifier
            .padding(bottom = 1.dp)
            .shadow(elevation = 1.dp, shape = AppTheme.shapes.extraLarge)
            .clip(AppTheme.shapes.extraLarge)
            .background(
                brush = if (legendaryStyle != null && legendaryStyle != LegendaryStyle.NO_CUSTOMIZATION) {
                    legendaryStyle.primaryBrush
                } else {
                    Brush.linearGradient(listOf(AppTheme.colorScheme.tertiary, AppTheme.colorScheme.tertiary))
                },
            )
            .padding(start = 10.dp, end = 2.dp)
            .padding(vertical = 2.dp),
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
            fontSize = firstCohortFontSize,
            color = Color.White,
        )
        Box(
            modifier = Modifier
                .clip(AppTheme.shapes.extraLarge)
                .background(Color.Black.copy(alpha = 0.4f)),
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .padding(top = 0.75.dp),
                text = secondCohort,
                style = AppTheme.typography.bodySmall,
                fontSize = secondCohortFontSize,
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
                onPremiumBadgeClick = { _, _ -> },
            )
        }
    }
}
