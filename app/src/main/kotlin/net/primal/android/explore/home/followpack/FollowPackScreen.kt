package net.primal.android.explore.home.followpack

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.approvals.ApproveFollowUnfollowProfileAlertDialog
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.core.compose.rememberIsItemVisible
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.core.utils.ifNotNull
import net.primal.android.explore.home.followpack.FollowPackContract.UiEvent
import net.primal.android.explore.home.people.model.FollowPackUi
import net.primal.android.explore.home.people.ui.ProfileFollowUnfollowListItem
import net.primal.android.explore.home.ui.FollowPackCoverImage
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.LegendaryStyle
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.links.CdnImage

@Composable
fun FollowPackScreen(
    onClose: () -> Unit,
    onShowFeedClick: (feed: String, title: String, description: String) -> Unit,
    onProfileClick: (String) -> Unit,
    viewModel: FollowPackViewModel,
) {
    val uiState = viewModel.state.collectAsState()

    FollowPackScreen(
        onClose = onClose,
        state = uiState.value,
        onShowFeedClick = onShowFeedClick,
        onProfileClick = onProfileClick,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FollowPackScreen(
    onClose: () -> Unit,
    onShowFeedClick: (feed: String, title: String, description: String) -> Unit,
    onProfileClick: (String) -> Unit,
    state: FollowPackContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val isTitleVisible = lazyListState.rememberIsItemVisible("title", true)

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarErrorHandler(
        error = state.uiError,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context = context) },
        onErrorDismiss = { eventPublisher(UiEvent.DismissError) },
    )

    if (state.shouldApproveProfileAction != null) {
        ApproveFollowUnfollowProfileAlertDialog(
            profileApproval = state.shouldApproveProfileAction,
            onFollowApproved = { eventPublisher(UiEvent.FollowUser(userId = it.profileId, forceUpdate = true)) },
            onUnfollowApproved = { eventPublisher(UiEvent.UnfollowUser(userId = it.profileId, forceUpdate = true)) },
            onFollowAllApproved = { eventPublisher(UiEvent.FollowAll(userIds = it.profileIds, forceUpdate = true)) },
            onClose = { eventPublisher(UiEvent.DismissConfirmFollowUnfollowAlertDialog) },
        )
    }

    Scaffold(
        topBar = {
            FollowPackTopAppBar(
                isTitleVisible = !isTitleVisible.value,
                title = state.followPack?.title ?: "",
                onClose = onClose,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { paddingValues ->
        if (state.followPack != null) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.padding(paddingValues),
            ) {
                followPackInfo(followPack = state.followPack, onProfileClick = onProfileClick)
                actionButtonsRow(
                    onShowFeedClick = {
                        ifNotNull(state.feedSpec, state.feedDescription) { feedSpec, description ->
                            onShowFeedClick(feedSpec, state.followPack.title, description)
                        }
                    },
                    isFollowAllEnabled = state.followPack.profiles.any { !state.following.contains(it.profileId) },
                    onFollowAllClick = {
                        eventPublisher(
                            UiEvent.FollowAll(
                                userIds = state.followPack.profiles.map { it.profileId },
                                forceUpdate = false,
                            ),
                        )
                    },
                )
                followPackProfiles(
                    onProfileClick = onProfileClick,
                    profiles = state.followPack.profiles,
                    onFollowUnfollowClick = { profileId, following ->
                        if (following) {
                            eventPublisher(UiEvent.UnfollowUser(userId = profileId, forceUpdate = false))
                        } else {
                            eventPublisher(UiEvent.FollowUser(userId = profileId, forceUpdate = false))
                        }
                    },
                )
            }
        } else if (state.loading) {
            PrimalLoadingSpinner()
        } else {
            ListNoContent(
                modifier = Modifier.fillMaxSize(),
                noContentText = stringResource(id = R.string.follow_pack_unable_to_load),
                onRefresh = { eventPublisher(UiEvent.RefreshFollowPack) },
            )
        }
    }
}

private fun LazyListScope.followPackInfo(followPack: FollowPackUi, onProfileClick: (String) -> Unit) {
    item(key = "cover") {
        FollowPackCoverImage(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
            height = 160.dp,
            coverImage = followPack.coverCdnImage,
            clipShape = AppTheme.shapes.medium,
        )
    }
    followPackTitleDescription(
        modifier = Modifier.padding(horizontal = 16.dp),
        title = followPack.title,
        description = followPack.description,
    )
    followPack.authorProfileData?.let {
        followPackAuthor(
            data = followPack.authorProfileData,
            onProfileClick = onProfileClick,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FollowPackTopAppBar(
    isTitleVisible: Boolean,
    title: String,
    onClose: () -> Unit,
) {
    CenterAlignedTopAppBar(
        title = {
            AnimatedVisibility(
                visible = isTitleVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Text(
                    text = title,
                    style = AppTheme.typography.bodyLarge,
                    color = AppTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    fontSize = 20.sp,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(imageVector = PrimalIcons.ArrowBack, contentDescription = null)
            }
        },
    )
}

private fun LazyListScope.followPackProfiles(
    onProfileClick: (String) -> Unit,
    profiles: List<UserProfileItemUi>,
    onFollowUnfollowClick: (String, Boolean) -> Unit,
) {
    items(
        items = profiles,
        key = { it.profileId },
    ) { profile ->
        ProfileFollowUnfollowListItem(
            data = profile,
            avatarSize = 40.dp,
            onClick = { onProfileClick(profile.profileId) },
            onFollowUnfollowClick = { following ->
                onFollowUnfollowClick(
                    profile.profileId,
                    following,
                )
            },
        )
    }
}

private fun LazyListScope.actionButtonsRow(
    modifier: Modifier = Modifier,
    isFollowAllEnabled: Boolean,
    onShowFeedClick: () -> Unit,
    onFollowAllClick: () -> Unit,
) {
    stickyHeader(
        key = "actionButtons",
    ) {
        Row(
            modifier = modifier
                .background(AppTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PrimalFilledButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                height = 40.dp,
                onClick = onShowFeedClick,
            ) {
                Text(
                    text = stringResource(id = R.string.follow_pack_show_feed_button),
                    style = AppTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }

            PrimalFilledButton(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                height = 40.dp,
                onClick = onFollowAllClick,
                enabled = isFollowAllEnabled,
            ) {
                Text(
                    text = stringResource(id = R.string.follow_pack_follow_all_button),
                    style = AppTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
        PrimalDivider(
            modifier = Modifier
                .background(AppTheme.colorScheme.background)
                .padding(top = 16.dp),
        )
    }
}

private fun LazyListScope.followPackAuthor(
    modifier: Modifier = Modifier,
    data: UserProfileItemUi,
    onProfileClick: (String) -> Unit,
) {
    item(key = "author") {
        val interactionSource = remember { MutableInteractionSource() }
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clickable(
                    indication = null,
                    interactionSource = interactionSource,
                ) { onProfileClick(data.profileId) }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            UniversalAvatarThumbnail(
                avatarSize = 40.dp,
                avatarCdnImage = data.avatarCdnImage,
                legendaryCustomization = data.legendaryCustomization,
                onClick = { onProfileClick(data.profileId) },
            )

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = stringResource(id = R.string.follow_pack_created_by),
                    style = AppTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NostrUserText(
                        displayName = data.displayName,
                        displayNameColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        displayNameFontWeight = FontWeight.SemiBold,
                        style = AppTheme.typography.bodyMedium,
                        internetIdentifier = data.internetIdentifier,
                        legendaryCustomization = data.legendaryCustomization,
                    )

                    data.internetIdentifier?.let { internetIdentifier ->
                        Text(
                            text = internetIdentifier,
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.followPackTitleDescription(
    modifier: Modifier = Modifier,
    title: String,
    description: String?,
) {
    item(key = "title") {
        Text(
            modifier = modifier.padding(top = 16.dp),
            text = title,
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
    description?.let {
        item(key = "description") {
            Text(
                modifier = modifier.padding(top = 8.dp),
                text = description,
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }
    }
}

@Preview
@Composable
private fun FollowPackScreenPreview() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        FollowPackScreen(
            eventPublisher = {},
            onShowFeedClick = { _, _, _ -> },
            onProfileClick = {},
            onClose = {},
            state = FollowPackContract.UiState(
                loading = false,
                followPack =
                FollowPackUi(
                    identifier = "",
                    coverCdnImage = CdnImage("https://placehold.co/600x400"),
                    title = "Freedom Tech Signal",
                    description = "high signal accounts focused on bitcoin, nostr, and other open source projects",
                    authorProfileData = UserProfileItemUi(
                        profileId = "",
                        displayName = "ODELL",
                        internetIdentifier = "odell@primal.net",
                    ),
                    profiles = listOf(
                        UserProfileItemUi(
                            profileId = "profile1",
                            displayName = "ODELL",
                            followersCount = 100_000,
                            isFollowed = false,
                        ),
                        UserProfileItemUi(
                            profileId = "profile2",
                            displayName = "ODELL",
                            followersCount = 10_000,
                            isFollowed = true,
                            internetIdentifier = "odell@primal.net",
                        ),
                        UserProfileItemUi(
                            profileId = "profile3",
                            displayName = "jack",
                            followersCount = 50_000,
                            isFollowed = true,
                        ),
                        UserProfileItemUi(
                            profileId = "profile4",
                            displayName = "miljan",
                            followersCount = null,
                            isFollowed = true,
                            legendaryCustomization = LegendaryCustomization(
                                avatarGlow = true,
                                customBadge = true,
                                legendaryStyle = LegendaryStyle.BLUE,
                            ),
                        ),
                        UserProfileItemUi(
                            profileId = "profile5",
                            displayName = "someSuperLongDisplayNameToSeeEllipsis",
                            followersCount = 100_000,
                            isFollowed = false,
                            internetIdentifier = "someSuperLongDisplayNameToSeeEllipsis@primal.net",
                        ),
                    ),
                    profilesCount = 148,
                    highlightedProfiles = listOf(
                        UserProfileItemUi(
                            profileId = "",
                            displayName = "ODELL",
                        ),
                        UserProfileItemUi(
                            profileId = "",
                            displayName = "ODELL",
                        ),
                    ),
                    updatedAt = Instant.now(),
                    authorId = "profileId",
                ),
            ),
        )
    }
}
