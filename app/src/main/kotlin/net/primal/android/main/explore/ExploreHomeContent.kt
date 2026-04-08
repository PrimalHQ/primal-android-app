package net.primal.android.main.explore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopLevelAppBar
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.main.explore.feeds.ExploreFeeds
import net.primal.android.main.explore.people.ExplorePeople
import net.primal.android.main.explore.topics.ExploreTopics
import net.primal.android.main.explore.ui.EXPLORE_HOME_TAB_COUNT
import net.primal.android.main.explore.ui.ExploreHomeTabs
import net.primal.android.main.explore.ui.FEEDS_INDEX
import net.primal.android.main.explore.ui.MEDIA_INDEX
import net.primal.android.main.explore.ui.PEOPLE_INDEX
import net.primal.android.main.explore.ui.TOPICS_INDEX
import net.primal.android.main.explore.ui.ZAPS_INDEX
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
    onGoToWallet: () -> Unit,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
    ) { pageIndex ->
        when (pageIndex) {
            PEOPLE_INDEX -> {
                ExplorePeople(
                    modifier = Modifier.background(color = AppTheme.colorScheme.surfaceVariant),
                    paddingValues = paddingValues,
                    onProfileClick = { noteCallbacks.onProfileClick?.invoke(it) },
                    onFollowPackClick = onFollowPackClick,
                )
            }

            FEEDS_INDEX -> {
                ExploreFeeds(
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
            }

            ZAPS_INDEX -> {
                ExploreZaps(
                    modifier = Modifier.background(color = AppTheme.colorScheme.surfaceVariant),
                    paddingValues = paddingValues,
                    noteCallbacks = noteCallbacks,
                )
            }

            MEDIA_INDEX -> {
                MediaFeedGrid(
                    feedSpec = exploreMediaFeedSpec,
                    contentPadding = paddingValues,
                    onNoteClick = { noteCallbacks.onNoteClick?.invoke(it) },
                    onGetPrimalPremiumClick = { noteCallbacks.onGetPrimalPremiumClick?.invoke() },
                )
            }

            TOPICS_INDEX -> {
                ExploreTopics(
                    modifier = Modifier.background(color = AppTheme.colorScheme.surfaceVariant),
                    paddingValues = paddingValues,
                    onHashtagClick = { noteCallbacks.onHashtagClick?.invoke(it) },
                )
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
internal fun ExploreTopAppBar(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    avatarCdnImage: CdnImage?,
    onAvatarClick: () -> Unit,
    avatarLegendaryCustomization: LegendaryCustomization? = null,
    avatarBlossoms: List<String> = emptyList(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .background(AppTheme.colorScheme.background)
            .wrapContentHeight(),
    ) {
        PrimalTopLevelAppBar(
            title = stringResource(id = R.string.explore_title),
            subtitle = stringResource(id = R.string.explore_top_app_bar_subtitle),
            avatarCdnImage = avatarCdnImage,
            avatarBlossoms = avatarBlossoms,
            avatarLegendaryCustomization = avatarLegendaryCustomization,
            onAvatarClick = onAvatarClick,
            showDivider = false,
            scrollBehavior = scrollBehavior,
        )
        ExploreHomeTabs(
            selectedTabIndex = pagerState.currentPage,
            onPeopleTabClick = { scope.launch { pagerState.animateScrollToPage(PEOPLE_INDEX) } },
            onFeedsTabClick = { scope.launch { pagerState.animateScrollToPage(FEEDS_INDEX) } },
            onZapsTabClick = { scope.launch { pagerState.animateScrollToPage(ZAPS_INDEX) } },
            onMediaTabClick = { scope.launch { pagerState.animateScrollToPage(MEDIA_INDEX) } },
            onTopicsTabClick = { scope.launch { pagerState.animateScrollToPage(TOPICS_INDEX) } },
        )
        PrimalDivider()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun PreviewExploreTopAppBar() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            ExploreTopAppBar(
                avatarCdnImage = null,
                onAvatarClick = {},
                pagerState = rememberPagerState { EXPLORE_HOME_TAB_COUNT },
            )
        }
    }
}
