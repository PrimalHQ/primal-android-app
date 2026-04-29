package net.primal.android.main.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopLevelAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AdvancedSearch
import net.primal.android.core.compose.icons.primaliconpack.SearchFilled
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.main.explore.feeds.ExploreFeeds
import net.primal.android.main.explore.landing.ExploreLanding
import net.primal.android.main.explore.people.ExplorePeople
import net.primal.android.main.explore.section.ExploreSection
import net.primal.android.main.explore.section.toAppBarPages
import net.primal.android.main.explore.section.toSubtitle
import net.primal.android.main.explore.section.toTitle
import net.primal.android.main.explore.zaps.ExploreZaps
import net.primal.android.notes.feed.grid.MediaFeedGrid
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.feeds.exploreMediaFeedSpec
import net.primal.domain.links.CdnImage

@Composable
internal fun ExploreHomeContent(
    pagerState: PagerState,
    paddingValues: PaddingValues,
    noteCallbacks: NoteCallbacks,
    snackbarHostState: SnackbarHostState,
    onFollowPackClick: (profileId: String, identifier: String) -> Unit,
    onRecentSearchEditClick: (query: String) -> Unit,
    onRecentSearchExecuteClick: (query: String) -> Unit,
    onGoToWallet: () -> Unit,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()

    HorizontalPager(state = pagerState) { pageIndex ->
        when (ExploreSection.entries[pageIndex]) {
            ExploreSection.Explore -> ExploreLanding(
                modifier = Modifier.background(color = AppTheme.colorScheme.surfaceVariant),
                paddingValues = paddingValues,
                onProfileClick = { noteCallbacks.onProfileClick?.invoke(it) },
                onRecentSearchEditClick = onRecentSearchEditClick,
                onRecentSearchExecuteClick = onRecentSearchExecuteClick,
            )

            ExploreSection.FeedGallery -> ExploreFeeds(
                modifier = Modifier.background(color = AppTheme.colorScheme.surfaceVariant),
                paddingValues = paddingValues,
                onGoToWallet = onGoToWallet,
                onUiError = { uiError: UiError ->
                    uiScope.launch {
                        snackbarHostState.showSnackbar(
                            message = uiError.resolveUiErrorMessage(context),
                            duration = SnackbarDuration.Short,
                        )
                    }
                },
            )

            ExploreSection.FollowPacks -> ExplorePeople(
                modifier = Modifier.background(color = AppTheme.colorScheme.surfaceVariant),
                paddingValues = paddingValues,
                onProfileClick = { noteCallbacks.onProfileClick?.invoke(it) },
                onFollowPackClick = onFollowPackClick,
            )

            ExploreSection.Zaps -> ExploreZaps(
                modifier = Modifier.background(color = AppTheme.colorScheme.surfaceVariant),
                paddingValues = paddingValues,
                noteCallbacks = noteCallbacks,
            )

            ExploreSection.Media -> MediaFeedGrid(
                feedSpec = exploreMediaFeedSpec,
                contentPadding = paddingValues,
                onNoteClick = { noteCallbacks.onNoteClick?.invoke(it) },
                onGetPrimalPremiumClick = { noteCallbacks.onGetPrimalPremiumClick?.invoke() },
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
internal fun ExploreTopAppBar(
    activeSection: ExploreSection,
    onExploreSectionPickerRequest: () -> Unit,
    onSearchClick: () -> Unit,
    onAdvancedSearchClick: () -> Unit,
    avatarCdnImage: CdnImage?,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier,
    pagerState: PagerState? = null,
    onAvatarSwipeDown: (() -> Unit)? = null,
    avatarLegendaryCustomization: LegendaryCustomization? = null,
    avatarBlossoms: List<String> = emptyList(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    chevronExpanded: Boolean = false,
    titleOverride: String? = null,
    subtitleOverride: String? = null,
) {
    val sectionPages = ExploreSection.entries.toAppBarPages()
    Column(
        modifier = modifier
            .background(AppTheme.colorScheme.background)
            .wrapContentHeight(),
    ) {
        PrimalTopLevelAppBar(
            title = activeSection.toTitle(),
            subtitle = activeSection.toSubtitle(),
            titleOverride = titleOverride,
            subtitleOverride = subtitleOverride,
            showTitleChevron = true,
            chevronExpanded = chevronExpanded,
            onTitleClick = { onExploreSectionPickerRequest() },
            avatarCdnImage = avatarCdnImage,
            avatarBlossoms = avatarBlossoms,
            avatarLegendaryCustomization = avatarLegendaryCustomization,
            onAvatarClick = onAvatarClick,
            onAvatarSwipeDown = onAvatarSwipeDown,
            showDivider = false,
            scrollBehavior = scrollBehavior,
            pagerState = pagerState,
            pages = sectionPages,
        )
        ExploreSearchNavBar(
            onSearchClick = onSearchClick,
            onAdvancedSearchClick = onAdvancedSearchClick,
        )
        Spacer(modifier = Modifier.height(16.dp))
        PrimalDivider()
    }
}

@Composable
private fun ExploreSearchNavBar(onSearchClick: () -> Unit, onAdvancedSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 16.dp)
            .clip(AppTheme.shapes.extraLarge)
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.extraLarge,
            )
            .clickable(onClick = onSearchClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconText(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            leadingIcon = PrimalIcons.SearchFilled,
            leadingIconTintColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            text = stringResource(id = R.string.explore_search_bar_hint),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
        )
        Icon(
            modifier = Modifier
                .padding(end = 16.dp)
                .size(20.dp)
                .clickable(onClick = onAdvancedSearchClick),
            imageVector = PrimalIcons.AdvancedSearch,
            contentDescription = null,
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewExploreTopAppBar() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            ExploreTopAppBar(
                activeSection = ExploreSection.Explore,
                onExploreSectionPickerRequest = {},
                onSearchClick = {},
                onAdvancedSearchClick = {},
                avatarCdnImage = null,
                onAvatarClick = {},
            )
        }
    }
}
