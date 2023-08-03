package net.primal.android.thread

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalButton
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.feed.FeedPostListItem
import net.primal.android.core.compose.feed.RepostOrQuoteBottomSheet
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import java.time.Instant

@Composable
fun ThreadScreen(
    viewModel: ThreadViewModel,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
) {

    val uiState = viewModel.state.collectAsState()

    ThreadScreen(
        state = uiState.value,
        onClose = onClose,
        onPostClick = onPostClick,
        onPostQuoteClick = onPostQuoteClick,
        onProfileClick = onProfileClick,
        onHashtagClick = onHashtagClick,
        eventPublisher = { viewModel.setEvent(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    state: ThreadContract.UiState,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    eventPublisher: (ThreadContract.UiEvent) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    val listState = rememberLazyListState()

    var repostQuotePostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (repostQuotePostConfirmation != null) repostQuotePostConfirmation?.let { post ->
        RepostOrQuoteBottomSheet(
            onDismiss = { repostQuotePostConfirmation = null },
            onRepostClick = {
                eventPublisher(
                    ThreadContract.UiEvent.RepostAction(
                        postId = post.postId,
                        postAuthorId = post.authorId,
                        postNostrEvent = post.rawNostrEventJson,
                    )
                )
            },
            onPostQuoteClick = {
                onPostQuoteClick("\n\nnostr:${post.postId.hexToNoteHrp()}")
            },
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }

    ReplyPublishingErrorHandler(
        error = state.publishingError,
        snackbarHostState = snackbarHostState,
    )

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .navigationBarsPadding()
            .imePadding(),
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
                            expanded = true,
                            onPostClick = { postId ->
                                if (state.highlightPostId != postId) {
                                    onPostClick(postId)
                                }
                            },
                            onProfileClick = { profileId -> onProfileClick(profileId) },
                            onPostAction = {
                                when (it) {
                                    FeedPostAction.Reply -> {
                                        if (state.highlightPostId != item.postId) {
                                            onPostClick(item.postId)
                                        }
                                    }
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
                            onHashtagClick = onHashtagClick,
                            shouldIndentContent = shouldIndentContent,
                            highlighted = highlighted,
                            connected = connected,
                        )

                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            val rootPost = state.conversation.firstOrNull()
            val replyToPost = state.conversation.getOrNull(state.highlightPostIndex)
            if (rootPost != null && replyToPost != null) {
                ReplyToBottomBar(
                    publishingReply = state.publishingReply,
                    replyToAuthorDisplayName = replyToPost.authorDisplayName,
                    replyToUserDisplayName = replyToPost.userDisplayName,
                    replyTextProvider = { state.replyText },
                    onReplyClick = {
                        eventPublisher(
                            ThreadContract.UiEvent.ReplyToAction(
                                rootPostId = rootPost.postId,
                                replyToPostId = replyToPost.postId,
                                replyToAuthorId = replyToPost.authorId,
                            )
                        )
                    },
                    onReplyUpdated = { content ->
                        eventPublisher(ThreadContract.UiEvent.UpdateReply(newReply = content))
                    }
                )
            }
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReplyToBottomBar(
    publishingReply: Boolean,
    replyToAuthorDisplayName: String,
    replyToUserDisplayName: String,
    replyTextProvider: () -> String,
    onReplyClick: () -> Unit,
    onReplyUpdated: (String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardVisible by keyboardVisibilityAsState()

    val unfocusedColor = AppTheme.extraColorScheme.surfaceVariantAlt
    val focusedColor = AppTheme.colorScheme.surface
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        color = if (isKeyboardVisible) unfocusedColor else focusedColor,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            AnimatedVisibility(visible = isKeyboardVisible) {
                val mention = "@$replyToUserDisplayName"
                val text = stringResource(id = R.string.thread_replying_to, mention)
                val contentText = buildAnnotatedString {
                    append(text)
                    addStyle(
                        style = SpanStyle(
                            color = AppTheme.colorScheme.primary,
                        ),
                        start = text.indexOf(mention),
                        end = text.length,
                    )
                }

                Text(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                    text = contentText,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                )
            }

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .imePadding(),
                value = replyTextProvider(),
                onValueChange = { onReplyUpdated(it) },
                maxLines = 10,
                enabled = !publishingReply,
                placeholder = {
                    AnimatedVisibility(
                        visible = !isKeyboardVisible,
                        exit = fadeOut(),
                        enter = fadeIn(),
                    ) {
                        Text(
                            text = stringResource(
                                id = R.string.thread_reply_to,
                                replyToAuthorDisplayName
                            ),
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = unfocusedColor,
                    focusedContainerColor = if (isKeyboardVisible) focusedColor else unfocusedColor,
                    focusedBorderColor = Color.Unspecified,
                    unfocusedBorderColor = Color.Unspecified,
                    errorBorderColor = Color.Unspecified,
                    disabledBorderColor = Color.Unspecified
                ),
            )

            AnimatedVisibility(visible = isKeyboardVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    PrimalButton(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(40.dp),
                        text = if (publishingReply) {
                            stringResource(id = R.string.thread_publishing_button)
                        } else {
                            stringResource(id = R.string.thread_publish_button)
                        },
                        enabled = !publishingReply,
                        fontSize = 16.sp,
                        onClick = {
                            onReplyClick()
                            keyboardController?.hide()
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun keyboardVisibilityAsState(): State<Boolean> {
    val density = LocalDensity.current
    val imeBottom = WindowInsets.ime.getBottom(density)
    val minKeyboardVisibility = with(density) { 128.dp.toPx() }
    val isImeVisible = imeBottom > minKeyboardVisibility
    return rememberUpdatedState(isImeVisible)
}

@Composable
private fun ReplyPublishingErrorHandler(
    error: ThreadContract.UiState.PublishError?,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        if (error != null) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.thread_reply_nostr_publish_error),
                duration = SnackbarDuration.Short,
            )
        }
    }
}


@Preview()
@Composable
fun ThreadScreenPreview() {
    PrimalTheme {
        ThreadScreen(
            state = ThreadContract.UiState(
                conversation = listOf(
                    FeedPostUi(
                        postId = "random",
                        repostId = null,
                        authorId = "id",
                        authorDisplayName = "alex",
                        userDisplayName = "alex",
                        authorInternetIdentifier = "alex@primal.net",
                        content = "Hello #nostr!",
                        authorResources = emptyList(),
                        postResources = emptyList(),
                        nostrUris = emptyList(),
                        timestamp = Instant.now().minusSeconds(3600),
                        stats = FeedPostStatsUi(),
                        hashtags = listOf("#nostr"),
                        rawNostrEventJson = "raaaw",
                    ),
                    FeedPostUi(
                        postId = "reply",
                        repostId = null,
                        authorId = "id",
                        authorDisplayName = "nikola",
                        userDisplayName = "nikola",
                        authorInternetIdentifier = "nikola@primal.net",
                        content = "#nostr rocks!",
                        authorResources = emptyList(),
                        postResources = emptyList(),
                        nostrUris = emptyList(),
                        timestamp = Instant.now(),
                        stats = FeedPostStatsUi(),
                        hashtags = listOf("#nostr"),
                        rawNostrEventJson = "raaaw",
                    ),
                ),
            ),
            onClose = {},
            onPostClick = {},
            onPostQuoteClick = {},
            onProfileClick = {},
            onHashtagClick = {},
            eventPublisher = {},
        )
    }
}
