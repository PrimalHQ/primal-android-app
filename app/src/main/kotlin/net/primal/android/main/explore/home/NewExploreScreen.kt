package net.primal.android.main.explore.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopLevelAppBar
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AdvancedSearch
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.feeds.dvm.ui.DvmFeedDetailsBottomSheet
import net.primal.android.feeds.dvm.ui.DvmFeedListItem
import net.primal.android.feeds.dvm.ui.DvmFeedUi
import net.primal.android.main.explore.people.model.FollowPackUi
import net.primal.android.main.explore.people.ui.FollowPackListItem
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.theme.AppTheme
import net.primal.domain.feeds.buildSpec
import net.primal.domain.links.CdnImage

private val SECTION_SPACING = 40.dp
private const val HORIZONTAL_CARD_WIDTH_FRACTION = 0.8f

@Composable
fun NewExploreTabContent(
    paddingValues: PaddingValues,
    noteCallbacks: NoteCallbacks,
    onProfileClick: (profileId: String) -> Unit,
    onSearchUsersClick: () -> Unit,
    onAdvancedSearchClick: () -> Unit,
    onFollowPackClick: (profileId: String, identifier: String) -> Unit,
) {
    val viewModel = hiltViewModel<NewExploreViewModel>()
    val uiState by viewModel.state.collectAsStateWithLifecycle()

    NewExploreTabContent(
        state = uiState,
        paddingValues = paddingValues,
        eventPublisher = viewModel::setEvent,
        noteCallbacks = noteCallbacks,
        onProfileClick = onProfileClick,
        onSearchUsersClick = onSearchUsersClick,
        onAdvancedSearchClick = onAdvancedSearchClick,
        onFollowPackClick = onFollowPackClick,
    )
}

@Composable
fun NewExploreTabContent(
    state: NewExploreContract.UiState,
    paddingValues: PaddingValues,
    eventPublisher: (NewExploreContract.UiEvent) -> Unit,
    noteCallbacks: NoteCallbacks,
    onProfileClick: (profileId: String) -> Unit,
    onSearchUsersClick: () -> Unit,
    onAdvancedSearchClick: () -> Unit,
    onFollowPackClick: (profileId: String, identifier: String) -> Unit,
) {
    var dvmFeedToShow by remember { mutableStateOf<DvmFeedUi?>(null) }

    dvmFeedToShow?.let { selectedDvmFeed ->
        val addedToFeed by remember(dvmFeedToShow, state.userFeedSpecs) {
            val kind = dvmFeedToShow?.data?.kind
            mutableStateOf(
                kind?.let { state.userFeedSpecs.contains(dvmFeedToShow?.data?.buildSpec(specKind = kind)) } ?: false,
            )
        }
        DvmFeedDetailsBottomSheet(
            onDismissRequest = {
                dvmFeedToShow?.let { eventPublisher(NewExploreContract.UiEvent.ClearDvmFeed(it)) }
                dvmFeedToShow = null
            },
            dvmFeed = selectedDvmFeed,
            addedToFeed = addedToFeed,
            addToUserFeeds = { eventPublisher(NewExploreContract.UiEvent.AddToUserFeeds(it)) },
            removeFromUserFeeds = { eventPublisher(NewExploreContract.UiEvent.RemoveFromUserFeeds(it)) },
            noteCallbacks = noteCallbacks,
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingValues,
    ) {
        item { Spacer(Modifier.height(SECTION_SPACING)) }
        item {
            UsersSection(
                users = state.recommendedUsers,
                onUserClick = onProfileClick,
                onSearchUsersClick = onSearchUsersClick,
                onAdvancedSearchClick = onAdvancedSearchClick,
            )
        }
        item { Spacer(Modifier.height(SECTION_SPACING)) }
        item {
            FollowPacksSection(
                followPacks = state.followPacks,
                onFollowPackClick = onFollowPackClick,
                onProfileClick = onProfileClick,
            )
        }
        item { Spacer(Modifier.height(SECTION_SPACING)) }
        item {
            FeedsSection(
                feeds = state.feeds,
                onFeedClick = { dvmFeedToShow = it },
                onProfileClick = onProfileClick,
            )
        }
        item { Spacer(Modifier.height(SECTION_SPACING)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewExploreTabTopAppBar(
    avatarCdnImage: CdnImage?,
    onAvatarClick: () -> Unit,
    avatarLegendaryCustomization: LegendaryCustomization? = null,
    avatarBlossoms: List<String> = emptyList(),
    onAvatarSwipeDown: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    titleOverride: String? = null,
    subtitleOverride: String? = null,
) {
    PrimalTopLevelAppBar(
        title = stringResource(id = R.string.explore_title),
        subtitle = stringResource(id = R.string.new_explore_subtitle),
        titleOverride = titleOverride,
        subtitleOverride = subtitleOverride,
        avatarCdnImage = avatarCdnImage,
        avatarBlossoms = avatarBlossoms,
        avatarLegendaryCustomization = avatarLegendaryCustomization,
        onAvatarClick = onAvatarClick,
        onAvatarSwipeDown = onAvatarSwipeDown,
        showDivider = false,
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun UsersSection(
    users: List<UserProfileItemUi>,
    onUserClick: (profileId: String) -> Unit,
    onSearchUsersClick: () -> Unit,
    onAdvancedSearchClick: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(id = R.string.new_explore_users_section_title),
            style = AppTheme.typography.titleLarge.copy(lineHeight = 20.sp),
        )

        PopularUsersGrid(users = users, onUserClick = onUserClick)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedActionButton(
                modifier = Modifier.weight(1f),
                onClick = onSearchUsersClick,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = PrimalIcons.Search,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = R.string.new_explore_search_users_button),
                    style = AppTheme.typography.labelMedium,
                )
            }
            OutlinedActionButton(
                modifier = Modifier.weight(1f),
                onClick = onAdvancedSearchClick,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = PrimalIcons.AdvancedSearch,
                    contentDescription = null,
                )
                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(id = R.string.new_explore_advanced_search_button),
                    style = AppTheme.typography.labelMedium,
                )
            }
        }
    }
}

private const val POPULAR_USERS_COLUMNS = 4
private const val POPULAR_USER_LABEL_ALPHA = 0.7f

@Composable
private fun PopularUsersGrid(users: List<UserProfileItemUi>, onUserClick: (profileId: String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        users.chunked(POPULAR_USERS_COLUMNS).forEach { rowUsers ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                rowUsers.forEach { user ->
                    UserCell(
                        modifier = Modifier.weight(1f),
                        user = user,
                        onClick = { onUserClick(user.profileId) },
                    )
                }
                repeat(POPULAR_USERS_COLUMNS - rowUsers.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun UserCell(
    modifier: Modifier = Modifier,
    user: UserProfileItemUi,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        UniversalAvatarThumbnail(
            avatarCdnImage = user.avatarCdnImage,
            avatarSize = 64.dp,
            avatarBlossoms = user.avatarBlossoms,
            legendaryCustomization = user.legendaryCustomization,
            isLive = user.isLive,
            onClick = onClick,
        )
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            text = user.displayName,
            style = AppTheme.typography.labelMedium,
            color = AppTheme.colorScheme.onSurface.copy(alpha = POPULAR_USER_LABEL_ALPHA),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OutlinedActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    PrimalFilledButton(
        modifier = modifier,
        height = 48.dp,
        containerColor = Color.Transparent,
        contentColor = AppTheme.colorScheme.onSurface,
        border = BorderStroke(width = 1.dp, color = AppTheme.colorScheme.outline),
        onClick = onClick,
        content = content,
    )
}

private val FOLLOW_PACK_CARD_HEIGHT = 270.dp

@Composable
private fun FollowPacksSection(
    followPacks: List<FollowPackUi>,
    onFollowPackClick: (profileId: String, identifier: String) -> Unit,
    onProfileClick: (profileId: String) -> Unit,
) {
    val cardWidth = (LocalConfiguration.current.screenWidthDp * HORIZONTAL_CARD_WIDTH_FRACTION).dp
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(id = R.string.new_explore_follow_packs_section_title),
            style = AppTheme.typography.titleLarge.copy(lineHeight = 20.sp),
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = followPacks,
                key = { "${it.authorId}:${it.identifier}" },
            ) { item ->
                FollowPackListItem(
                    modifier = Modifier
                        .width(cardWidth)
                        .height(FOLLOW_PACK_CARD_HEIGHT),
                    followPack = item,
                    onClick = onFollowPackClick,
                    onProfileClick = onProfileClick,
                )
            }
        }
    }
}

private const val FEEDS_GRID_ROW_COUNT = 3
private val FEEDS_GRID_HEIGHT = 366.dp

@Composable
private fun FeedsSection(
    feeds: List<DvmFeedUi>,
    onFeedClick: (DvmFeedUi) -> Unit,
    onProfileClick: (profileId: String) -> Unit,
) {
    val itemWidth = (LocalConfiguration.current.screenWidthDp * HORIZONTAL_CARD_WIDTH_FRACTION).dp
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(id = R.string.new_explore_feeds_section_title),
            style = AppTheme.typography.titleLarge.copy(lineHeight = 20.sp),
        )

        LazyHorizontalGrid(
            rows = GridCells.Fixed(FEEDS_GRID_ROW_COUNT),
            modifier = Modifier
                .fillMaxWidth()
                .height(FEEDS_GRID_HEIGHT),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = feeds,
                key = { it.data.eventId },
            ) { feed ->
                DvmFeedListItem(
                    modifier = Modifier.width(itemWidth),
                    data = feed,
                    listItemContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt3,
                    showFollowsActionsAvatarRow = true,
                    onFeedClick = onFeedClick,
                    onProfileClick = onProfileClick,
                )
            }
        }
    }
}
