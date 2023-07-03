package net.primal.android.explore.feed

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.feed.FeedPostList
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack

@Composable
fun ExploreFeedScreen(
    viewModel: ExploreFeedViewModel,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    ExploreFeedScreen(
        uiState = uiState.value,
        onClose = onClose,
        onPostClick = onPostClick,
        onProfileClick = onProfileClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreFeedScreen(
    uiState: ExploreFeedContract.UiState,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    val listState = rememberLazyListState()


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PrimalTopAppBar(
                title = uiState.title ?: "",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                scrollBehavior = scrollBehavior,
            )
        },
        content = { paddingValues ->
            FeedPostList(
                posts = uiState.posts,
                paddingValues = paddingValues,
                feedListState = listState,
                onPostClick = onPostClick,
                onProfileClick = onProfileClick,
            )
        },
    )
}
