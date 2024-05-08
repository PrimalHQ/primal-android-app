package net.primal.android.thread

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.outlined.OpenInFull
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import java.text.NumberFormat
import java.time.Instant
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.ReplyingToText
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.feed.RepostOrQuoteBottomSheet
import net.primal.android.core.compose.feed.model.FeedPostAction
import net.primal.android.core.compose.feed.model.FeedPostStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.note.ConfirmFirstBookmarkAlertDialog
import net.primal.android.core.compose.feed.note.FeedNoteCard
import net.primal.android.core.compose.feed.note.events.MediaClickEvent
import net.primal.android.core.compose.feed.zaps.UnableToZapBottomSheet
import net.primal.android.core.compose.feed.zaps.ZapBottomSheet
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromGallery
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.compose.pulltorefresh.PrimalPullToRefreshIndicator
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.editor.NoteEditorContract
import net.primal.android.editor.di.noteEditorViewModel
import net.primal.android.editor.domain.NoteEditorArgs
import net.primal.android.editor.ui.NoteOutlinedTextField
import net.primal.android.editor.ui.NoteTagUserLazyColumn
import net.primal.android.note.ui.NoteZapUiModel
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
    onPostClick: (noteId: String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (content: TextFieldValue) -> Unit,
    onProfileClick: (profileId: String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onGoToWallet: () -> Unit,
    onExpandReply: (args: NoteEditorArgs) -> Unit,
    onReactionsClick: (noteId: String) -> Unit,
) {
    val uiState by viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> viewModel.setEvent(
                ThreadContract.UiEvent.UpdateConversation,
            )

            else -> Unit
        }
    }

    ThreadScreen(
        state = uiState,
        onClose = onClose,
        onPostClick = onPostClick,
        onPostReplyClick = onPostReplyClick,
        onPostQuoteClick = onPostQuoteClick,
        onProfileClick = onProfileClick,
        onHashtagClick = onHashtagClick,
        onMediaClick = onMediaClick,
        onGoToWallet = onGoToWallet,
        onExpandReply = onExpandReply,
        onReactionsClick = onReactionsClick,
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
    onPostQuoteClick: (content: TextFieldValue) -> Unit,
    onProfileClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onGoToWallet: () -> Unit,
    onExpandReply: (args: NoteEditorArgs) -> Unit,
    onReactionsClick: (noteId: String) -> Unit,
    eventPublisher: (ThreadContract.UiEvent) -> Unit,
) {
    val noteEditorViewModel = noteEditorViewModel(args = NoteEditorArgs(replyToNoteId = state.highlightPostId))
    val replyState by noteEditorViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    ThreadErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

    NoteEditorErrorHandler(
        error = replyState.error,
        snackbarHostState = snackbarHostState,
    )

    var topBarMaxHeightPx by remember { mutableIntStateOf(0) }
    var bottomBarMaxHeightPx by remember { mutableIntStateOf(0) }
    Scaffold(
        modifier = Modifier.imePadding(),
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
                ThreadConversationLazyColumn(
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
                    onReactionsClick = onReactionsClick,
                    eventPublisher = eventPublisher,
                )

                val mentionQuery = replyState.userTaggingQuery
                if (mentionQuery != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(paddingValues),
                    ) {
                        PrimalDivider()
                        NoteTagUserLazyColumn(
                            modifier = Modifier.wrapContentHeight(),
                            content = replyState.content,
                            taggedUsers = replyState.taggedUsers,
                            users = replyState.users.ifEmpty {
                                if (mentionQuery.isEmpty()) {
                                    replyState.recommendedUsers
                                } else {
                                    emptyList()
                                }
                            },
                            userTaggingQuery = mentionQuery,
                            onUserClick = { newContent, newTaggedUsers ->
                                noteEditorViewModel.setEvent(
                                    NoteEditorContract.UiEvent.UpdateContent(content = newContent),
                                )
                                noteEditorViewModel.setEvent(
                                    NoteEditorContract.UiEvent.TagUser(taggedUser = newTaggedUsers.last()),
                                )
                                noteEditorViewModel.setEvent(
                                    NoteEditorContract.UiEvent.ToggleSearchUsers(enabled = false),
                                )
                            },
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        bottomBar = {
            val uiScope = rememberCoroutineScope()
            val replyToPost = state.conversation.getOrNull(state.highlightPostIndex)
            if (replyToPost != null) {
                ReplyToBottomBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onSizeChanged { bottomBarMaxHeightPx = it.height }
                        .navigationBarsPadding(),
                    replyState = replyState,
                    replyToPost = replyToPost,
                    onExpandReply = { mediaUris ->
                        onExpandReply(
                            NoteEditorArgs(
                                replyToNoteId = state.highlightPostId,
                                mediaUris = mediaUris.map { it.toString() },
                                content = replyState.content.text,
                                contentSelectionStart = replyState.content.selection.start,
                                contentSelectionEnd = replyState.content.selection.end,
                                taggedUsers = replyState.taggedUsers,
                            ),
                        )
                        uiScope.launch {
                            delay(250.milliseconds)
                            noteEditorViewModel.setEvent(
                                NoteEditorContract.UiEvent.UpdateContent(content = TextFieldValue()),
                            )
                        }
                    },
                    replyEventPublisher = { noteEditorViewModel.setEvent(it) },
                )
            }
        },
    )
}

@ExperimentalMaterial3Api
@Composable
private fun ThreadConversationLazyColumn(
    paddingValues: PaddingValues,
    state: ThreadContract.UiState,
    scaffoldBarsMaxHeightPx: Int,
    onPostClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onPostQuoteClick: (content: TextFieldValue) -> Unit,
    onGoToWallet: () -> Unit,
    onReactionsClick: (noteId: String) -> Unit,
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
                    onPostQuoteClick(TextFieldValue(text = "\n\nnostr:${post.postId.hexToNoteHrp()}"))
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

    if (state.confirmBookmarkingNoteId != null) {
        ConfirmFirstBookmarkAlertDialog(
            onBookmarkConfirmed = {
                eventPublisher(
                    ThreadContract.UiEvent.BookmarkAction(
                        noteId = state.confirmBookmarkingNoteId,
                        forceUpdate = true,
                    ),
                )
            },
            onClose = {
                eventPublisher(ThreadContract.UiEvent.DismissBookmarkConfirmation)
            },
        )
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
            onBookmarkClick = { noteId -> eventPublisher(ThreadContract.UiEvent.BookmarkAction(noteId = noteId)) },
            onReportContentClick = { reportType, profileId, noteId ->
                eventPublisher(
                    ThreadContract.UiEvent.ReportAbuse(
                        reportType = reportType,
                        profileId = profileId,
                        noteId = noteId,
                    ),
                )
            },
            onReactionsClick = onReactionsClick,
        )

        PullToRefreshContainer(
            modifier = Modifier
                .padding(paddingValues)
                .align(Alignment.TopCenter),
            state = pullToRefreshState,
            contentColor = AppTheme.colorScheme.primary,
            indicator = { PrimalPullToRefreshIndicator(state = pullToRefreshState) },
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
    onMediaClick: (MediaClickEvent) -> Unit,
    onBookmarkClick: (String) -> Unit,
    onMuteUserClick: (authorId: String) -> Unit,
    onReactionsClick: (noteId: String) -> Unit,
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
            .navigationBarsPadding()
            .onSizeChanged { threadListMaxHeightPx = it.height },
        contentPadding = paddingValues,
    ) {
        itemsIndexed(
            items = state.conversation,
            key = { _, item -> item.postId },
            contentType = { index, _ -> if (index == state.highlightPostIndex) "root" else "reply" },
        ) { index, item ->
            val isReply = index > state.highlightPostIndex
            val highlighted = index == state.highlightPostIndex
            Column(
                modifier = Modifier.onSizeChanged {
                    if (highlighted) {
                        highlightPostHeightPx = it.height
                    } else if (isReply) {
                        repliesHeightPx = repliesHeightPx.toMutableMap().apply { this[index] = it.height }
                    }
                },
            ) {
                FeedNoteCard(
                    data = item,
                    shape = RectangleShape,
                    cardPadding = PaddingValues(all = 0.dp),
                    expanded = true,
                    textSelectable = highlighted,
                    fullWidthContent = highlighted,
                    headerSingleLine = !highlighted,
                    forceContentIndent = index != state.highlightPostIndex,
                    drawLineAboveAvatar = isConnectedBackward(index, state.highlightPostIndex),
                    drawLineBelowAvatar = isConnectedForward(index, state.highlightPostIndex),
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
                    onBookmarkClick = { onBookmarkClick(item.postId) },
                    contentFooter = {
                        if (highlighted && (state.topZap != null || state.otherZaps.isNotEmpty())) {
                            TopZapsSection(
                                topZap = state.topZap,
                                otherZaps = state.otherZaps,
                                onClick = { onReactionsClick(item.postId) },
                            )
                        }
                    },
                )

                if (!isConnectedForward(index, state.highlightPostIndex)) {
                    PrimalDivider()
                }
            }
        }

        if (state.conversation.isNotEmpty()) {
            item(key = "extraSpacing") {
                Spacer(modifier = Modifier.height(height = extraSpacing.coerceAtLeast(50.dp)))
            }
        }
    }
}

private fun isConnectedBackward(index: Int, highlightIndex: Int): Boolean {
    return highlightIndex > 0 && index in 1 until highlightIndex + 1
}

private fun isConnectedForward(index: Int, highlightIndex: Int): Boolean {
    return index in 0 until highlightIndex
}

const val MaxOtherZaps = 4

@Composable
private fun TopZapsSection(
    topZap: NoteZapUiModel?,
    otherZaps: List<NoteZapUiModel>,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .animateContentSize()
            .padding(horizontal = 10.dp),
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        if (topZap != null) {
            NoteZapListItem(
                noteZap = topZap,
                showMessage = true,
                onClick = onClick,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (otherZaps.isNotEmpty()) {
            Row {
                otherZaps.take(MaxOtherZaps).forEach {
                    key(it.id) {
                        NoteZapListItem(
                            noteZap = it,
                            showMessage = false,
                            onClick = onClick,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }

                if (otherZaps.size > MaxOtherZaps) {
                    Icon(
                        modifier = Modifier
                            .background(
                                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                                shape = CircleShape,
                            )
                            .size(26.dp)
                            .padding(horizontal = 4.dp)
                            .clickable { onClick() },
                        imageVector = PrimalIcons.More,
                        contentDescription = null,
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun NoteZapListItem(
    noteZap: NoteZapUiModel,
    showMessage: Boolean = false,
    onClick: () -> Unit,
) {
    val numberFormat = NumberFormat.getNumberInstance()
    Row(
        modifier = Modifier
            .height(26.dp)
            .animateContentSize()
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.extraLarge,
            )
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarThumbnail(
            modifier = Modifier.padding(start = 2.dp),
            avatarCdnImage = noteZap.zapperAvatarCdnImage,
            avatarSize = 24.dp,
            onClick = onClick,
        )

        Text(
            modifier = Modifier.padding(horizontal = 8.dp),
            text = numberFormat.format(noteZap.amountInSats.toLong()),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = AppTheme.typography.bodySmall,
            color = AppTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
        )

        if (showMessage && !noteZap.message.isNullOrEmpty()) {
            Text(
                modifier = Modifier.padding(end = 8.dp),
                text = noteZap.message,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                style = AppTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
fun ReplyToBottomBar(
    modifier: Modifier,
    replyState: NoteEditorContract.UiState,
    replyToPost: FeedPostUi,
    onExpandReply: (mediaUris: List<Uri>) -> Unit,
    replyEventPublisher: (NoteEditorContract.UiEvent) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isKeyboardVisible by keyboardVisibilityAsState()

    Column(
        modifier = modifier.background(color = AppTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Bottom,
    ) {
        PrimalDivider()

        AnimatedVisibility(visible = isKeyboardVisible) {
            ReplyingToText(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
                replyToUsername = replyToPost.authorHandle,
            )
        }

        NoteOutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp)
                .imePadding(),
            value = replyState.content,
            onValueChange = { replyEventPublisher(NoteEditorContract.UiEvent.UpdateContent(content = it)) },
            maxLines = 10,
            enabled = !replyState.publishing,
            placeholder = {
                ReplyTextFieldPlaceholder(
                    isKeyboardVisible = isKeyboardVisible,
                    replyToPost = replyToPost,
                )
            },
            trailingIcon = {
                AnimatedVisibility(visible = isKeyboardVisible) {
                    AppBarIcon(
                        icon = Icons.Outlined.OpenInFull,
                        onClick = {
                            onExpandReply(emptyList())
                            keyboardController?.hide()
                        },
                        tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    )
                }
            },
            textStyle = AppTheme.typography.bodyMedium,
            colors = PrimalDefaults.outlinedTextFieldColors(),
            shape = AppTheme.shapes.extraLarge,
            taggedUsers = replyState.taggedUsers,
            onUserTaggingModeChanged = {
                replyEventPublisher(NoteEditorContract.UiEvent.ToggleSearchUsers(enabled = it))
            },
            onUserTagSearch = {
                replyEventPublisher(NoteEditorContract.UiEvent.SearchUsers(query = it))
            },
        )

        AnimatedVisibility(visible = isKeyboardVisible) {
            ReplyToOptions(
                replying = replyState.publishing,
                replyEnabled = !replyState.publishing && replyState.content.text.isNotBlank(),
                onPublishReplyClick = { replyEventPublisher(NoteEditorContract.UiEvent.PublishNote) },
                onPhotoImported = { onExpandReply(listOf(it)) },
                onUserTag = {
                    replyEventPublisher(NoteEditorContract.UiEvent.AppendUserTagAtSign)
                    replyEventPublisher(NoteEditorContract.UiEvent.ToggleSearchUsers(enabled = true))
                },
            )
        }
    }
}

@Composable
private fun ColumnScope.ReplyTextFieldPlaceholder(isKeyboardVisible: Boolean, replyToPost: FeedPostUi) {
    AnimatedVisibility(
        visible = !isKeyboardVisible,
        exit = fadeOut(),
        enter = fadeIn(),
    ) {
        Text(
            text = stringResource(
                id = R.string.thread_reply_to,
                replyToPost.authorName,
            ),
            maxLines = 1,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            style = AppTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ReplyToOptions(
    replying: Boolean,
    replyEnabled: Boolean,
    onPublishReplyClick: () -> Unit,
    onPhotoImported: (Uri) -> Unit,
    onUserTag: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val photoImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) onPhotoImported.invoke(uri)
    }

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
                            mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
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

            IconButton(
                onClick = onUserTag,
            ) {
                Icon(
                    imageVector = Icons.Default.AlternateEmail,
                    contentDescription = stringResource(id = R.string.accessibility_tag_user),
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        }

        PrimalLoadingButton(
            modifier = Modifier
                .wrapContentWidth()
                .height(34.dp),
            text = if (replying) {
                stringResource(id = R.string.thread_publishing_button)
            } else {
                stringResource(id = R.string.thread_publish_button)
            },
            enabled = replyEnabled,
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
private fun ThreadErrorHandler(error: ThreadError?, snackbarHostState: SnackbarHostState) {
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

@Composable
private fun NoteEditorErrorHandler(
    error: NoteEditorContract.UiState.NoteEditorError?,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    LaunchedEffect(error) {
        val errorMessage = when (error) {
            is NoteEditorContract.UiState.NoteEditorError.MissingRelaysConfiguration -> context.getString(
                R.string.app_missing_relays_config,
            )

            is NoteEditorContract.UiState.NoteEditorError.PublishError -> context.getString(
                R.string.post_action_reply_failed,
            )

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
            onMediaClick = {},
            onGoToWallet = {},
            onExpandReply = {},
            onReactionsClick = {},
            eventPublisher = {},
        )
    }
}
