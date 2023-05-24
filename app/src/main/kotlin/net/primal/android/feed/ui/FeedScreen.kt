package net.primal.android.feed.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.flow
import net.primal.android.core.compose.ToolbarIcon
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Discuss
import net.primal.android.core.compose.icons.primaliconpack.FeedPicker
import net.primal.android.core.compose.icons.primaliconpack.Messages
import net.primal.android.core.compose.icons.primaliconpack.Notifications
import net.primal.android.core.compose.icons.primaliconpack.Read
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.compose.icons.primaliconpack.Settings
import net.primal.android.core.compose.isEmpty
import net.primal.android.feed.FeedContract
import net.primal.android.feed.FeedViewModel
import net.primal.android.feed.ui.post.FeedPostListItem
import net.primal.android.theme.PrimalTheme

@Composable
fun FeedScreen(
    viewModel: FeedViewModel
) {

    val uiState = viewModel.state.collectAsState()

    FeedScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    state: FeedContract.UiState,
    eventPublisher: (FeedContract.UiEvent) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppToolbar(
                eventPublisher = eventPublisher,
                scrollBehavior = scrollBehavior,
            )
        },
        content = { paddingValues ->
            val pagingItems = state.posts.collectAsLazyPagingItems()

            when {
                pagingItems.isEmpty() -> {
                    Box(
                        modifier = Modifier.padding(paddingValues),
                    ) {
                        Text(
                            text = "Empty feed.",
                            modifier = Modifier
                                .fillMaxSize()
                                .align(alignment = Alignment.Center)
                                .padding(all = 16.dp),
                        )
                    }
                }

                else -> {
                    val listState = rememberLazyListState()
                    FeedList(
                        contentPadding = paddingValues,
                        pagingItems = pagingItems,
                        listState = listState,
                    )
                }
            }
        },
        bottomBar = {
            PrimalNavigationBar()
        }
    )
}

@ExperimentalMaterial3Api
@Composable
fun TopAppToolbar(
    eventPublisher: (FeedContract.UiEvent) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    CenterAlignedTopAppBar(
        navigationIcon = {
             ToolbarIcon(
                 icon = PrimalIcons.Settings,
                 onClick = { },
             )
        },
        title = {
            Text(text = "Primal")
        },
        actions = {
            ToolbarIcon(
                icon = PrimalIcons.FeedPicker,
                onClick = { },
            )
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun FeedList(
    contentPadding: PaddingValues,
    pagingItems: LazyPagingItems<FeedPostUi>,
    listState: LazyListState,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = listState,
    ) {
        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey(key = { it.postId }),
            contentType = pagingItems.itemContentType()
        ) { index ->
            val item = pagingItems[index]

            if (item != null) {
                FeedPostListItem(data = item)
            } else {
                // Placeholder
            }
        }
    }
}

@Composable
fun PrimalNavigationBar() {
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .height(64.dp)
    ) {
        PrimalNavigationBarItem(
            selected = true,
            onClick = {},
            icon = PrimalIcons.Discuss,
        )

        PrimalNavigationBarItem(
            selected = false,
            onClick = {},
            icon = PrimalIcons.Read,
        )

        PrimalNavigationBarItem(
            selected = false,
            onClick = {},
            icon = PrimalIcons.Search,
        )

        PrimalNavigationBarItem(
            selected = false,
            onClick = {},
            icon = PrimalIcons.Messages,
        )

        PrimalNavigationBarItem(
            selected = false,
            onClick = {},
            icon = PrimalIcons.Notifications,
        )
    }
}

@Composable
fun RowScope.PrimalNavigationBarItem(
    selected: Boolean,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(imageVector = icon, contentDescription = null) },
    )
}

@Preview
@Composable
fun FeedScreenPreview() {
    PrimalTheme {
        FeedScreen(
            state = FeedContract.UiState(
                posts = flow { }
            ),
            eventPublisher = {},
        )
    }

}