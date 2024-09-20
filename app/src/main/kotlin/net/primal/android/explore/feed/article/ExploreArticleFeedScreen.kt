package net.primal.android.explore.feed.article

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.articles.feed.ArticleFeedList
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack


@Composable
fun ExploreArticleFeedScreen(
    viewModel: ExploreArticleFeedViewModel,
    onArticleClick: (naddr: String) -> Unit,
    onClose: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ExploreArticleFeedScreen(
        state = uiState.value,
        onArticleClick = onArticleClick,
        onClose = onClose,
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExploreArticleFeedScreen(
    state: ExploreArticleFeedContract.UiState,
    onArticleClick: (naddr: String) -> Unit,
    onClose: () -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.explore_fallback_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
            )

        }
    ) { paddingValues ->
        ArticleFeedList(
            modifier = Modifier.padding(paddingValues),
            feedSpec = state.feedSpec,
            onArticleClick = onArticleClick,
        )
    }
}
