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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.flow.flowOf
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.profile.details.ProfileDetailsContract
import net.primal.android.profile.domain.ProfileFeedSpec
import net.primal.android.profile.domain.ProfileFollowsType
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.domain.DraftTx
import net.primal.android.wallet.utils.isLightningAddress

@Composable
fun ProfileDetailsHeader(
    state: ProfileDetailsContract.UiState,
    pagingItems: LazyPagingItems<FeedPostUi>,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onZapProfileClick: (DraftTx) -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    onUnableToZapProfile: () -> Unit,
    onFollowsClick: (String, ProfileFollowsType) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
) {
    Column {
        ProfileHeaderDetails(
            state = state,
            eventPublisher = eventPublisher,
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
            onFollow = { eventPublisher(ProfileDetailsContract.UiEvent.FollowAction(state.profileId)) },
            onUnfollow = { eventPublisher(ProfileDetailsContract.UiEvent.UnfollowAction(state.profileId)) },
            onDrawerQrCodeClick = onDrawerQrCodeClick,
            onFollowsClick = onFollowsClick,
            onProfileClick = onProfileClick,
            onHashtagClick = onHashtagClick,
        )

        if (state.isProfileMuted) {
            ProfileMutedNotice(
                profileName = state.profileDetails?.authorDisplayName ?: state.profileId.asEllipsizedNpub(),
                onUnmuteClick = {
                    eventPublisher(ProfileDetailsContract.UiEvent.UnmuteAction(state.profileId))
                },
            )
        } else {
            if (pagingItems.isEmpty()) {
                when (pagingItems.loadState.refresh) {
                    LoadState.Loading -> ListLoading(
                        modifier = Modifier
                            .padding(vertical = 64.dp)
                            .fillMaxWidth(),
                    )

                    is LoadState.NotLoading -> ListNoContent(
                        modifier = Modifier
                            .padding(vertical = 64.dp)
                            .fillMaxWidth(),
                        noContentText = stringResource(id = R.string.feed_no_content),
                        onRefresh = { pagingItems.refresh() },
                    )

                    is LoadState.Error -> Unit
                }
            }
        }
    }
}

@Composable
private fun ProfileHeaderDetails(
    state: ProfileDetailsContract.UiState,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    onEditProfileClick: () -> Unit,
    onDrawerQrCodeClick: () -> Unit,
    onZapProfileClick: () -> Unit,
    onMessageClick: () -> Unit,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onFollowsClick: (String, ProfileFollowsType) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
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
            isProfileFollowingMe = state.isProfileFollowingMe,
        )

        if (state.profileDetails?.internetIdentifier?.isNotEmpty() == true) {
            UserInternetIdentifier(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                internetIdentifier = state.profileDetails.internetIdentifier,
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

        ProfileTabs(
            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp),
            feedFeedSpec = state.profileFeedSpec,
            notesCount = state.profileStats?.notesCount,
            onNotesCountClick = {
                eventPublisher(ProfileDetailsContract.UiEvent.ChangeProfileFeed(ProfileFeedSpec.AuthoredNotes))
            },
            repliesCount = state.profileStats?.repliesCount,
            onRepliesCountClick = {
                eventPublisher(ProfileDetailsContract.UiEvent.ChangeProfileFeed(ProfileFeedSpec.AuthoredReplies))
            },
            followingCount = state.profileStats?.followingCount,
            onFollowingCountClick = { onFollowsClick(state.profileId, ProfileFollowsType.Following) },
            followersCount = state.profileStats?.followersCount,
            onFollowersCountClick = { onFollowsClick(state.profileId, ProfileFollowsType.Followers) },
        )
    }
}

@Composable
private fun UserDisplayName(
    modifier: Modifier = Modifier,
    displayName: String,
    internetIdentifier: String?,
    isProfileFollowingMe: Boolean,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NostrUserText(
            modifier = Modifier
                .padding(start = 14.dp, end = 6.dp)
                .padding(top = 12.dp, bottom = 3.dp),
            displayName = displayName,
            internetIdentifier = internetIdentifier,
            internetIdentifierBadgeSize = 20.dp,
            internetIdentifierBadgeAlign = PlaceholderVerticalAlign.Center,
            style = AppTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
        )

        if (isProfileFollowingMe) {
            Box(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .height(24.dp)
                    .background(
                        color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                        shape = AppTheme.shapes.extraSmall,
                    )
                    .padding(horizontal = 6.dp),
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
private fun ProfileMutedNotice(profileName: String, onUnmuteClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(top = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.profile_user_is_muted, profileName),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )
        TextButton(onClick = onUnmuteClick) {
            Text(
                text = stringResource(id = R.string.context_menu_unmute_user).uppercase(),
            )
        }
    }
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
                    ),
                    notes = flowOf(),
                ),
                eventPublisher = {},
                onEditProfileClick = {},
                onZapProfileClick = {},
                onMessageClick = {},
                onDrawerQrCodeClick = {},
                onFollow = {},
                onUnfollow = {},
                onFollowsClick = { _, _ -> },
                onProfileClick = {},
                onHashtagClick = {},
            )
        }
    }
}
