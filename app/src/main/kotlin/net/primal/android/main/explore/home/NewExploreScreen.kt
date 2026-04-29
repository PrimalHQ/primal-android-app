package net.primal.android.main.explore.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopLevelAppBar
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
