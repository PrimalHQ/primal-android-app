package net.primal.android.thread

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.feed.FeedPostListItem
import net.primal.android.core.compose.feed.RepostOrQuoteBottomSheet
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack

@Composable
fun ThreadScreen(
    viewModel: ThreadViewModel,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {

    val uiState = viewModel.state.collectAsState()

    ThreadScreen(
        state = uiState.value,
        onClose = onClose,
        onPostClick = onPostClick,
        onProfileClick = onProfileClick,
        eventPublisher = { viewModel.setEvent(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    state: ThreadContract.UiState,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    eventPublisher: (ThreadContract.UiEvent) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    val listState = rememberLazyListState()

    var repostQuotePostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (repostQuotePostConfirmation != null) repostQuotePostConfirmation?.let { post ->
        RepostOrQuoteBottomSheet(
            onDismiss = { repostQuotePostConfirmation = null },
            onRepost = {
                eventPublisher(
                    ThreadContract.UiEvent.RepostAction(
                        postId = post.postId,
                        postAuthorId = post.authorId,
                        postNostrEvent = post.rawNostrEventJson,
                    )
                )
            },
            onQuote = { },
        )
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = paddingValues,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                state = listState,
            ) {
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
                        val connected = index in 0 until state.highlightPostIndex

                        FeedPostListItem(
                            data = item,
                            onPostClick = { postId ->
                                if (index != state.highlightPostIndex) {
                                    onPostClick(postId)
                                }
                            },
                            onProfileClick = { profileId -> onProfileClick(profileId) },
                            onPostAction = {
                                when (it) {
                                    FeedPostAction.Reply -> Unit
                                    FeedPostAction.Zap -> Unit
                                    FeedPostAction.Like -> {
                                        eventPublisher(
                                            ThreadContract.UiEvent.PostLikeAction(
                                                postId = item.postId,
                                                postAuthorId = item.authorId,
                                            )
                                        )
                                    }

                                    FeedPostAction.Repost -> {
                                        repostQuotePostConfirmation = item
                                    }
                                }
                            },
                            shouldIndentContent = shouldIndentContent,
                            highlighted = highlighted,
                            connected = connected,
                        )

                    }
                }
            }
        },
    )
}
