package net.primal.android.feed.thread

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.feed.shared.ui.FeedPostListItem
import net.primal.android.theme.AppTheme

@Composable
fun ThreadScreen(
    viewModel: ThreadViewModel,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
) {

    val uiState = viewModel.state.collectAsState()

    ThreadScreen(
        state = uiState.value,
        onClose = onClose,
        onPostClick = onPostClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    state: ThreadContract.UiState,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)

    val listState = rememberLazyListState()

    LaunchedEffect(state.highlightPostIndex) {
        if (state.highlightPostIndex > 0) {
            listState.scrollToItem(index = state.highlightPostIndex)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.thread_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                scrollBehavior = scrollBehavior,
            )
        },
        content = { paddingValues ->
            val outlineColor = AppTheme.colorScheme.outline
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(0.dp),
                state = listState,
            ) {
                if (state.conversation.isEmpty() && state.highlightPost != null) {
                    item {
                        FeedPostListItem(
                            data = state.highlightPost,
                            onClick = {},
                            highlighted = true,
                        )
                    }
                } else {
                    itemsIndexed(
                        items = state.conversation,
                        key = { _, item -> item.postId },
                        contentType = { index, _ ->
                            if (index == state.highlightPostIndex) "root" else "reply"
                        },
                    ) { index, item ->
                        Column {
                            val shouldIndentContent = index != state.highlightPostIndex
                            val highlighted = index == state.highlightPostIndex
                            val connectedToPostBefore = index in 1..state.highlightPostIndex
                            val connectedToPostAfter = index in 0 until state.highlightPostIndex

                            FeedPostListItem(
                                data = item,
                                onClick = {
                                    if (index != state.highlightPostIndex) {
                                        onPostClick(item.postId)
                                    }
                                },
                                shouldIndentContent = shouldIndentContent,
                                highlighted = highlighted,
                                connectedToPostBefore = connectedToPostBefore,
                                connectedToPostAfter = connectedToPostAfter,
                            )

                            Spacer(
                                modifier = Modifier
                                    .height(4.dp)
                                    .drawWithCache {
                                        onDrawBehind {
                                            if (connectedToPostAfter) {
                                                drawLine(
                                                    color = outlineColor,
                                                    start = Offset(x = 44.dp.toPx() + 0.5f, y = 0f),
                                                    end = Offset(
                                                        x = 44.dp.toPx() + 0.5f,
                                                        y = size.height
                                                    ),
                                                    strokeWidth = 1.dp.toPx(),
                                                    cap = StrokeCap.Square
                                                )
                                            }
                                        }
                                    }

                            )
                        }
                    }
                }
            }
        },
    )
}