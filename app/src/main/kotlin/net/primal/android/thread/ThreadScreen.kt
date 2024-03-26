package net.primal.android.thread

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import kotlin.time.Duration.Companion.seconds
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.ReplyingToText
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.feed.RepostOrQuoteBottomSheet
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.note.FeedNoteCard
import net.primal.android.core.compose.feed.zaps.UnableToZapBottomSheet
import net.primal.android.core.compose.feed.zaps.ZapBottomSheet
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromGallery
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.profile.report.OnReportContentClick
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.thread.ThreadContract.UiState.ThreadError
import net.primal.android.wallet.zaps.canZap

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
    onGoToWallet: () -> Unit,
    onReplyInNoteEditor: (String, Uri?, String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
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
        onGoToWallet = onGoToWallet,
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
    onGoToWallet: () -> Unit,
    onReplyInNoteEditor: (String, Uri?, String) -> Unit,
    eventPublisher: (ThreadContract.UiEvent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

    var topBarMaxHeightPx by remember { mutableIntStateOf(0) }
    var bottomBarMaxHeightPx by remember { mutableIntStateOf(0) }
    Scaffold(
        modifier = Modifier
            .navigationBarsPadding()
            .imePadding(),
        topBar = {
            PrimalTopAppBar(
                modifier = Modifier.onSizeChanged { topBarMaxHeightPx = it.height },
                title = stringResource(id = R.string.thread_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
                showDivider = true,
            )
        },
        content = { paddingValues ->
            ThreadScreenContent(
                paddingValues = paddingValues,
                state = state,
                scaffoldBarsMaxHeightPx = bottomBarMaxHeightPx + topBarMaxHeightPx,
                onPostClick = onPostClick,
                onProfileClick = onProfileClick,
                onPostReplyClick = onPostReplyClick,
                onHashtagClick = onHashtagClick,
                onMediaClick = onMediaClick,
                onPostQuoteClick = onPostQuoteClick,
                onGoToWallet = onGoToWallet,
                eventPublisher = eventPublisher,
            )
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

@ExperimentalMaterial3Api
@Composable
private fun ThreadScreenContent(
    paddingValues: PaddingValues,
    state: ThreadContract.UiState,
    scaffoldBarsMaxHeightPx: Int,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onGoToWallet: () -> Unit,
    eventPublisher: (ThreadContract.UiEvent) -> Unit,
) {
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

    var showCantZapWarning by remember { mutableStateOf(false) }
    if (showCantZapWarning) {
        UnableToZapBottomSheet(
            zappingState = state.zappingState,
            onDismissRequest = { showCantZapWarning = false },
            onGoToWallet = onGoToWallet,
        )
    }

    var zapOptionsPostConfirmation by remember { mutableStateOf<FeedPostUi?>(null) }
    if (zapOptionsPostConfirmation != null) {
        zapOptionsPostConfirmation?.let { post ->
            ZapBottomSheet(
                onDismissRequest = { zapOptionsPostConfirmation = null },
                receiverName = post.authorName,
                zappingState = state.zappingState,
                onZap = { zapAmount, zapDescription ->
                    if (state.zappingState.canZap(zapAmount)) {
                        eventPublisher(
                            ThreadContract.UiEvent.ZapAction(
                                postId = post.postId,
                                postAuthorId = post.authorId,
                                zapAmount = zapAmount.toULong(),
                                zapDescription = zapDescription,
                            ),
                        )
                    } else {
                        showCantZapWarning = true
                    }
                },
            )
        }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            eventPublisher(ThreadContract.UiEvent.UpdateConversation)
        }
    }

    LaunchedEffect(!state.fetching) {
        if (!state.fetching) {
            pullToRefreshState.endRefresh()
        }
    }

    Box(
        modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection),
    ) {
        ThreadLazyColumn(
            paddingValues = paddingValues,
            state = state,
            scaffoldBarsMaxHeightPx = scaffoldBarsMaxHeightPx,
            onPostClick = onPostClick,
            onProfileClick = onProfileClick,
            onPostAction = { noteAction, note ->
                when (noteAction) {
                    FeedPostAction.Reply -> onPostReplyClick(note.postId)

                    FeedPostAction.Zap -> {
                        if (state.zappingState.canZap()) {
                            eventPublisher(
                                ThreadContract.UiEvent.ZapAction(
                                    postId = note.postId,
                                    postAuthorId = note.authorId,
                                    zapAmount = null,
                                    zapDescription = null,
                                ),
                            )
                        } else {
                            showCantZapWarning = true
                        }
                    }

                    FeedPostAction.Like -> {
                        eventPublisher(
                            ThreadContract.UiEvent.PostLikeAction(
                                postId = note.postId,
                                postAuthorId = note.authorId,
                            ),
                        )
                    }

                    FeedPostAction.Repost -> {
                        repostQuotePostConfirmation = note
                    }
                }
            },
            onPostLongClickAction = { noteAction, note ->
                when (noteAction) {
                    FeedPostAction.Zap -> {
                        if (state.zappingState.walletConnected) {
                            zapOptionsPostConfirmation = note
                        } else {
                            showCantZapWarning = true
                        }
                    }

                    else -> Unit
                }
            },
            onHashtagClick = onHashtagClick,
            onMediaClick = onMediaClick,
            onMuteUserClick = { authorId -> eventPublisher(ThreadContract.UiEvent.MuteAction(authorId)) },
            onReportContentClick = { reportType, profileId, noteId ->
                eventPublisher(
                    ThreadContract.UiEvent.ReportAbuse(
                        reportType = reportType,
                        profileId = profileId,
                        noteId = noteId,
                    ),
                )
            },
        )

        PullToRefreshContainer(
            modifier = Modifier
                .padding(paddingValues)
                .align(Alignment.TopCenter),
            state = pullToRefreshState,
            contentColor = AppTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun ThreadLazyColumn(
    paddingValues: PaddingValues,
    state: ThreadContract.UiState,
    scaffoldBarsMaxHeightPx: Int,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostAction: ((noteAction: FeedPostAction, note: FeedPostUi) -> Unit)? = null,
    onPostLongClickAction: ((noteAction: FeedPostAction, note: FeedPostUi) -> Unit)? = null,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onMuteUserClick: (authorId: String) -> Unit,
    onReportContentClick: OnReportContentClick,
) {
    var threadListMaxHeightPx by remember { mutableIntStateOf(0) }
    var highlightPostHeightPx by remember { mutableIntStateOf(0) }
    var repliesHeightPx by remember { mutableStateOf(mapOf<Int, Int>()) }

    var extraSpacing by remember { mutableStateOf(0.dp) }
    extraSpacing = with(LocalDensity.current) {
        threadListMaxHeightPx.toDp() - highlightPostHeightPx.toDp() -
            scaffoldBarsMaxHeightPx.toDp() - repliesHeightPx.values.sum().toDp()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { threadListMaxHeightPx = it.height },
        contentPadding = paddingValues,
    ) {
        itemsIndexed(
            items = state.conversation,
            key = { _, item -> item.postId },
            contentType = { index, _ -> if (index == state.highlightPostIndex) "root" else "reply" },
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
                    showReplyTo = false,
                    onPostClick = { postId ->
                        if (state.highlightPostId != postId) {
                            onPostClick(postId)
                        }
                    },
                    onProfileClick = { profileId -> onProfileClick(profileId) },
                    onPostAction = { onPostAction?.invoke(it, item) },
                    onPostLongClickAction = { onPostLongClickAction?.invoke(it, item) },
                    onHashtagClick = onHashtagClick,
                    onMediaClick = onMediaClick,
                    onMuteUserClick = { onMuteUserClick(item.authorId) },
                    onReportContentClick = onReportContentClick,
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
}

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

            AnimatedVisibility(visible = isKeyboardVisible) {
                ReplyToOptions(
                    publishingReply = publishingReply,
                    replyTextProvider = replyTextProvider,
                    onPublishReplyClick = onPublishReplyClick,
                    onPhotoImported = onPhotoImported,
                )
            }
        }
    }
}

@Composable
private fun ReplyToOptions(
    publishingReply: Boolean,
    replyTextProvider: () -> String,
    onPublishReplyClick: () -> Unit,
    onPhotoImported: (Uri) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val photoImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) onPhotoImported.invoke(uri) }

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
                        timestamp = Instant.now().minusSeconds(3600.seconds.inWholeSeconds),
                        stats = FeedPostStatsUi(),
                        hashtags = listOf("#nostr"),
                        rawNostrEventJson = "raaaw",
                        replyToAuthorHandle = "alex",
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
                        replyToAuthorHandle = "alex",
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
            onGoToWallet = {},
            onReplyInNoteEditor = { _, _, _ -> },
            eventPublisher = {},
        )
    }
}
