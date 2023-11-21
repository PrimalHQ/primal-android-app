package net.primal.android.thread

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import java.time.Instant
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.feed.RepostOrQuoteBottomSheet
import net.primal.android.core.compose.feed.ZapBottomSheet
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.note.FeedNoteCard
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromGallery
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.editor.ui.ReplyingToText
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.thread.ThreadContract.UiState.ThreadError

@Composable
fun ThreadScreen(
    viewModel: ThreadViewModel,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onWalletUnavailable: () -> Unit,
    onReplyInNoteEditor: (String, Uri?, String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect {
        when (it) {
            Lifecycle.Event.ON_START -> viewModel.setEvent(
                ThreadContract.UiEvent.UpdateConversation,
            )
            else -> Unit
        }
    }

    ThreadScreen(
        state = uiState.value,
        onClose = onClose,
        onPostClick = onPostClick,
        onPostReplyClick = onPostReplyClick,
        onPostQuoteClick = onPostQuoteClick,
        onProfileClick = onProfileClick,
        onHashtagClick = onHashtagClick,
        onMediaClick = onMediaClick,
        onWalletUnavailable = onWalletUnavailable,
        onReplyInNoteEditor = onReplyInNoteEditor,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    state: ThreadContract.UiState,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onWalletUnavailable: () -> Unit,
    onReplyInNoteEditor: (String, Uri?, String) -> Unit,
    eventPublisher: (ThreadContract.UiEvent) -> Unit,
) {
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(topAppBarState)
    val listState = rememberLazyListState()

    var repostQuotePostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (repostQuotePostConfirmation != null) {
        repostQuotePostConfirmation?.let { post ->
            RepostOrQuoteBottomSheet(
                onDismiss = { repostQuotePostConfirmation = null },
                onRepostClick = {
                    eventPublisher(
                        ThreadContract.UiEvent.RepostAction(
                            postId = post.postId,
                            postAuthorId = post.authorId,
                            postNostrEvent = post.rawNostrEventJson,
                        ),
                    )
                },
                onPostQuoteClick = {
                    onPostQuoteClick("\n\nnostr:${post.postId.hexToNoteHrp()}")
                },
            )
        }
    }

    var zapOptionsPostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (zapOptionsPostConfirmation != null) {
        zapOptionsPostConfirmation?.let { post ->
            ZapBottomSheet(
                onDismissRequest = { zapOptionsPostConfirmation = null },
                receiverName = post.authorName,
                defaultZapAmount = state.defaultZapAmount ?: 42.toULong(),
                userZapOptions = state.zapOptions,
                onZap = { zapAmount, zapDescription ->
                    eventPublisher(
                        ThreadContract.UiEvent.ZapAction(
                            postId = post.postId,
                            postAuthorId = post.authorId,
                            zapAmount = zapAmount,
                            zapDescription = zapDescription,
                        ),
                    )
                },
            )
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    ErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

    var topBarMaxHeightPx by remember { mutableIntStateOf(0) }
    var bottomBarMaxHeightPx by remember { mutableIntStateOf(0) }
    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .navigationBarsPadding()
            .imePadding(),
        topBar = {
            PrimalTopAppBar(
                modifier = Modifier.onSizeChanged { topBarMaxHeightPx = it.height },
                title = stringResource(id = R.string.thread_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                showDivider = true,
                scrollBehavior = scrollBehavior,
            )
        },
        content = { paddingValues ->
            var threadListMaxHeightPx by remember { mutableIntStateOf(0) }
            var highlightPostHeightPx by remember { mutableIntStateOf(0) }
            var repliesHeightPx by remember { mutableStateOf(mapOf<Int, Int>()) }

            var extraSpacing by remember { mutableStateOf(0.dp) }
            extraSpacing = with(LocalDensity.current) {
                threadListMaxHeightPx.toDp() - highlightPostHeightPx.toDp() -
                    bottomBarMaxHeightPx.toDp() - topBarMaxHeightPx.toDp() -
                    repliesHeightPx.values.sum().toDp()
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { threadListMaxHeightPx = it.height },
                contentPadding = paddingValues,
                state = listState,
            ) {
                itemsIndexed(
                    items = state.conversation,
                    key = { _, item -> item.postId },
                    contentType = { index, _ ->
                        if (index == state.highlightPostIndex) "root" else "reply"
                    },
                ) { index, item ->
                    val highlightPost = index == state.highlightPostIndex
                    val shouldIndentContent = index != state.highlightPostIndex
                    val highlighted = index == state.highlightPostIndex
                    val connectedToPreviousNote = state.highlightPostIndex > 0 &&
                        index in 1 until state.highlightPostIndex + 1
                    val connectedToNextNote = index in 0 until state.highlightPostIndex
                    val isReply = index > state.highlightPostIndex

                    Column(
                        modifier = Modifier.onSizeChanged {
                            if (highlighted) {
                                highlightPostHeightPx = it.height
                            } else if (isReply) {
                                repliesHeightPx = repliesHeightPx.toMutableMap().apply {
                                    this[index] = it.height
                                }
                            }
                        },
                    ) {
                        FeedNoteCard(
                            data = item,
                            shape = RectangleShape,
                            cardPadding = PaddingValues(all = 0.dp),
                            expanded = true,
                            fullWidthContent = highlightPost,
                            headerSingleLine = !highlightPost,
                            forceContentIndent = shouldIndentContent,
                            drawLineAboveAvatar = connectedToPreviousNote,
                            drawLineBelowAvatar = connectedToNextNote,
                            onPostClick = { postId ->
                                if (state.highlightPostId != postId) {
                                    onPostClick(postId)
                                }
                            },
                            onProfileClick = { profileId -> onProfileClick(profileId) },
                            onPostAction = {
                                when (it) {
                                    FeedPostAction.Reply -> onPostReplyClick(item.postId)

                                    FeedPostAction.Zap -> {
                                        if (state.walletConnected) {
                                            eventPublisher(
                                                ThreadContract.UiEvent.ZapAction(
                                                    postId = item.postId,
                                                    postAuthorId = item.authorId,
                                                    zapAmount = null,
                                                    zapDescription = null,
                                                ),
                                            )
                                        } else {
                                            onWalletUnavailable()
                                        }
                                    }

                                    FeedPostAction.Like -> {
                                        eventPublisher(
                                            ThreadContract.UiEvent.PostLikeAction(
                                                postId = item.postId,
                                                postAuthorId = item.authorId,
                                            ),
                                        )
                                    }

                                    FeedPostAction.Repost -> {
                                        repostQuotePostConfirmation = item
                                    }
                                }
                            },
                            onPostLongClickAction = {
                                when (it) {
                                    FeedPostAction.Zap -> {
                                        if (state.walletConnected) {
                                            zapOptionsPostConfirmation = item
                                        } else {
                                            onWalletUnavailable()
                                        }
                                    }

                                    else -> Unit
                                }
                            },
                            onHashtagClick = onHashtagClick,
                            onMediaClick = onMediaClick,
                            onMuteUserClick = {
                                eventPublisher(ThreadContract.UiEvent.MuteAction(item.authorId))
                            },
                        )

                        if (!connectedToNextNote) {
                            PrimalDivider()
                        }
                    }
                }

                if (state.conversation.isNotEmpty()) {
                    item(key = "extraSpacing") {
                        Spacer(
                            modifier = Modifier.height(
                                height = extraSpacing.coerceAtLeast(50.dp),
                            ),
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .onSizeChanged { bottomBarMaxHeightPx = it.height },
                    publishingReply = state.publishingReply,
                    replyToAuthorName = replyToPost.authorName,
                    replyToAuthorHandle = replyToPost.authorHandle,
                    replyTextProvider = { state.replyText },
                    onPublishReplyClick = {
                        eventPublisher(
                            ThreadContract.UiEvent.ReplyToAction(
                                rootPostId = rootPost.postId,
                                replyToPostId = replyToPost.postId,
                                replyToAuthorId = replyToPost.authorId,
                            ),
                        )
                    },
                    onReplyUpdated = { content ->
                        eventPublisher(ThreadContract.UiEvent.UpdateReply(newReply = content))
                    },
                    onPhotoImported = { photoUri ->
                        onReplyInNoteEditor(state.highlightPostId, photoUri, state.replyText)
                    },
                    onExpand = {
                        onReplyInNoteEditor(state.highlightPostId, null, state.replyText)
                    },
                )
            }
        },
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ReplyToBottomBar(
    modifier: Modifier,
    publishingReply: Boolean,
    replyToAuthorName: String,
    replyToAuthorHandle: String,
    replyTextProvider: () -> String,
    onPublishReplyClick: () -> Unit,
    onReplyUpdated: (String) -> Unit,
    onPhotoImported: (Uri) -> Unit,
    onExpand: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardVisible by keyboardVisibilityAsState()

    Surface(
        modifier = modifier,
        color = AppTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            PrimalDivider()

            AnimatedVisibility(visible = isKeyboardVisible) {
                ReplyingToText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp),
                    replyToUsername = replyToAuthorHandle,
                )
            }

            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp)
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
                                replyToAuthorName,
                            ),
                            maxLines = 1,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                            style = AppTheme.typography.bodyMedium,
                        )
                    }
                },
                trailingIcon = {
                    AnimatedVisibility(visible = isKeyboardVisible) {
                        AppBarIcon(
                            icon = Icons.Outlined.OpenInFull,
                            onClick = {
                                onExpand()
                                keyboardController?.hide()
                            },
                            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        )
                    }
                },
                textStyle = AppTheme.typography.bodyMedium,
                colors = PrimalDefaults.outlinedTextFieldColors(),
                shape = AppTheme.shapes.extraLarge,
            )

            val photoImportLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.PickVisualMedia(),
            ) { uri -> if (uri != null) onPhotoImported.invoke(uri) }

            AnimatedVisibility(visible = isKeyboardVisible) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 0.dp, end = 16.dp)
                        .padding(top = 0.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    ) {
                        IconButton(
                            onClick = {
                                photoImportLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                                    ),
                                )
                            },
                        ) {
                            Icon(
                                imageVector = PrimalIcons.ImportPhotoFromGallery,
                                contentDescription = null,
                                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                            )
                        }
                    }

                    PrimalLoadingButton(
                        modifier = Modifier
                            .wrapContentWidth()
                            .height(34.dp),
                        text = if (publishingReply) {
                            stringResource(id = R.string.thread_publishing_button)
                        } else {
                            stringResource(id = R.string.thread_publish_button)
                        },
                        enabled = !publishingReply && replyTextProvider().isNotBlank(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        onClick = {
                            onPublishReplyClick()
                            keyboardController?.hide()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorHandler(error: ThreadError?, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        val errorMessage = when (error) {
            is ThreadError.InvalidZapRequest -> context.getString(
                R.string.post_action_invalid_zap_request,
            )
            is ThreadError.MissingLightningAddress -> context.getString(
                R.string.post_action_missing_lightning_address,
            )
            is ThreadError.FailedToPublishZapEvent -> context.getString(
                R.string.post_action_zap_failed,
            )
            is ThreadError.FailedToPublishLikeEvent -> context.getString(
                R.string.post_action_like_failed,
            )
            is ThreadError.FailedToPublishRepostEvent -> context.getString(
                R.string.post_action_repost_failed,
            )
            is ThreadError.FailedToPublishReplyEvent -> context.getString(
                R.string.post_action_reply_failed,
            )
            is ThreadError.MissingRelaysConfiguration -> context.getString(
                R.string.app_missing_relays_config,
            )
            is ThreadError.FailedToMuteUser -> context.getString(R.string.app_error_muting_user)
            null -> return@LaunchedEffect
        }

        snackbarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Short,
        )
    }
}

@Preview
@Composable
fun ThreadScreenPreview() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        ThreadScreen(
            state = ThreadContract.UiState(
                highlightPostId = "",
                conversation = listOf(
                    FeedPostUi(
                        postId = "random",
                        repostId = null,
                        authorId = "id",
                        authorName = "alex",
                        authorHandle = "alex",
                        authorInternetIdentifier = "alex@primal.net",
                        content = "Hello #nostr!",
                        authorAvatarCdnImage = null,
                        attachments = emptyList(),
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
                        authorName = "nikola",
                        authorHandle = "nikola",
                        authorInternetIdentifier = "nikola@primal.net",
                        content = "#nostr rocks!",
                        authorAvatarCdnImage = null,
                        attachments = emptyList(),
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
            onPostReplyClick = {},
            onPostQuoteClick = {},
            onProfileClick = {},
            onHashtagClick = {},
            onMediaClick = { _, _ -> },
            onWalletUnavailable = {},
            onReplyInNoteEditor = { _, _, _ -> },
            eventPublisher = {},
        )
    }
}
