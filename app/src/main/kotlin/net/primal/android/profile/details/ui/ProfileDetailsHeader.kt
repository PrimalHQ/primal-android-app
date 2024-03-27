package net.primal.android.profile.details.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.FeedZaps
import net.primal.android.core.compose.icons.primaliconpack.Key
import net.primal.android.core.compose.icons.primaliconpack.Message
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.ext.openUriSafely
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.profile.details.ProfileDetailsContract
import net.primal.android.profile.domain.ProfileFeedDirective
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
            isFollowed = state.isProfileFollowed,
            isActiveUser = state.isActiveUser,
            onEditProfileClick = onEditProfileClick,
            onMessageClick = onMessageClick,
            onZapProfileClick = onZapProfileClick,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
        )

        NostrUserText(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp),
            displayName = state.profileDetails?.authorDisplayName ?: state.profileId.asEllipsizedNpub(),
            internetIdentifier = state.profileDetails?.internetIdentifier,
            internetIdentifierBadgeSize = 24.dp,
            style = AppTheme.typography.titleLarge,
        )

        if (state.profileDetails?.internetIdentifier?.isNotEmpty() == true) {
            UserInternetIdentifier(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                internetIdentifier = state.profileDetails.internetIdentifier,
            )
        }

        if (state.profileDetails?.about?.isNotEmpty() == true) {
            ProfileAboutSection(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                website = state.profileDetails.website,
                onClick = { localUriHandler.openUriSafely(state.profileDetails.website) },
            )
        }

        ProfileTabs(
            modifier = Modifier.padding(bottom = 8.dp),
            feedDirective = state.profileDirective,
            notesCount = state.profileStats?.notesCount,
            onNotesCountClick = {
                eventPublisher(
                    ProfileDetailsContract.UiEvent.ChangeProfileFeed(
                        profileDirective = ProfileFeedDirective.AuthoredNotes,
                    ),
                )
            },
            repliesCount = state.profileStats?.repliesCount,
            onRepliesCountClick = {
                eventPublisher(
                    ProfileDetailsContract.UiEvent.ChangeProfileFeed(
                        profileDirective = ProfileFeedDirective.AuthoredReplies,
                    ),
                )
            },
            followingCount = state.profileStats?.followingCount,
            onFollowingCountClick = { onFollowsClick(state.profileId, ProfileFollowsType.Following) },
            followersCount = state.profileStats?.followersCount,
            onFollowersCountClick = { onFollowsClick(state.profileId, ProfileFollowsType.Followers) },
        )
    }
}

@Composable
private fun ProfileActions(
    isFollowed: Boolean,
    isActiveUser: Boolean,
    onEditProfileClick: () -> Unit,
    onMessageClick: () -> Unit,
    onZapProfileClick: () -> Unit,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .background(AppTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.End,
    ) {
        ActionButton(
            onClick = onZapProfileClick,
            iconVector = PrimalIcons.FeedZaps,
            contentDescription = stringResource(id = R.string.accessibility_profile_send_zap),
        )

        ActionButton(
            onClick = onMessageClick,
            iconVector = PrimalIcons.Message,
            contentDescription = stringResource(id = R.string.accessibility_profile_messages),
        )

        if (!isActiveUser) {
            when (isFollowed) {
                true -> UnfollowButton(onClick = onUnfollow)
                false -> FollowButton(onClick = onFollow)
            }
        } else {
            Spacer(modifier = Modifier.width(8.dp))
            EditProfileButton(
                onClick = {
                    onEditProfileClick()
                },
            )
        }
    }
}

@Composable
private fun ActionButton(
    iconVector: ImageVector,
    onClick: () -> Unit,
    contentDescription: String? = null,
) {
    IconButton(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(36.dp),
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            contentColor = AppTheme.colorScheme.onSurface,
        ),
    ) {
        Icon(
            modifier = Modifier.padding(all = 2.dp),
            imageVector = iconVector,
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun UnfollowButton(onClick: () -> Unit) {
    ProfileButton(
        text = stringResource(id = R.string.profile_unfollow_button).lowercase(),
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        contentColor = AppTheme.colorScheme.onSurface,
        onClick = onClick,
    )
}

@Composable
private fun EditProfileButton(onClick: () -> Unit) {
    ProfileButton(
        text = stringResource(id = R.string.profile_edit_profile_button).lowercase(),
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        contentColor = AppTheme.colorScheme.onSurface,
        onClick = onClick,
    )
}

@Composable
private fun FollowButton(onClick: () -> Unit) {
    ProfileButton(
        text = stringResource(id = R.string.profile_follow_button).lowercase(),
        containerColor = AppTheme.colorScheme.onSurface,
        contentColor = AppTheme.colorScheme.surface,
        onClick = onClick,
    )
}

@Composable
private fun ProfileButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    PrimalFilledButton(
        modifier = Modifier
            .height(36.dp)
            .wrapContentWidth()
            .defaultMinSize(minWidth = 100.dp),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 0.dp,
        ),
        containerColor = containerColor,
        contentColor = contentColor,
        textStyle = AppTheme.typography.titleMedium.copy(
            lineHeight = 18.sp,
        ),
        onClick = onClick,
    ) {
        Text(text = text)
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
        style = AppTheme.typography.bodySmall,
        color = AppTheme.colorScheme.secondary,
    )
}

@Suppress("UnusedPrivateMember")
@Composable
private fun UserPublicKey(
    modifier: Modifier = Modifier,
    pubkey: String,
    onCopyClick: (String) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconText(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .wrapContentWidth(),
            text = pubkey.asEllipsizedNpub(),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            leadingIconTintColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            leadingIcon = PrimalIcons.Key,
            iconSize = 12.sp,
        )

        Box(
            modifier = Modifier
                .size(20.dp)
                .clickable(
                    onClick = { onCopyClick(pubkey.hexToNpubHrp()) },
                    role = Role.Button,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                imageVector = Icons.Outlined.ContentCopy,
                colorFilter = ColorFilter.tint(color = AppTheme.colorScheme.primary),
                contentDescription = stringResource(id = R.string.accessibility_copy_content),
            )
        }
    }
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
        style = AppTheme.typography.bodySmall,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
    )
}
