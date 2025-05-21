package net.primal.android.thread.notes

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import java.time.Instant
import java.time.format.FormatStyle
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.articles.feed.ui.FeedArticleListItem
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.ImportPhotosIconButton
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.ReplyingToText
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.TakePhotoIconButton
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromCamera
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromGallery
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.pulltorefresh.PrimalIndicator
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.compose.zaps.ThreadNoteTopZapsSection
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.core.utils.formatToDefaultDateFormat
import net.primal.android.core.utils.formatToDefaultTimeFormat
import net.primal.android.editor.NoteEditorContract
import net.primal.android.editor.di.noteEditorViewModel
import net.primal.android.editor.domain.NoteEditorArgs
import net.primal.android.editor.ui.NoteOutlinedTextField
import net.primal.android.editor.ui.NoteTagUserLazyColumn
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.asNeventString
import net.primal.android.notes.feed.note.FeedNoteCard
import net.primal.android.notes.feed.note.ui.ThreadNoteStatsRow
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.domain.nostr.ReactionType

@Composable
fun ThreadScreen(
    viewModel: ThreadViewModel,
    onClose: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onExpandReply: (args: NoteEditorArgs) -> Unit,
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
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
        onExpandReply = onExpandReply,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThreadScreen(
    state: ThreadContract.UiState,
    onClose: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onExpandReply: (args: NoteEditorArgs) -> Unit,
    eventPublisher: (ThreadContract.UiEvent) -> Unit,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val noteEditorViewModel = noteEditorViewModel(
        args = NoteEditorArgs(
            referencedNoteNevent = state.highlightNote?.asNeventString(),
        ),
    )

    val replyState by noteEditorViewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorToShow = replyState.error ?: state.error

    SnackbarErrorHandler(
        error = errorToShow,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = {
            if (replyState.error != null) {
                noteEditorViewModel.setEvent(
                    NoteEditorContract.UiEvent.DismissError,
                )
            }
            if (state.error != null) {
                eventPublisher(ThreadContract.UiEvent.DismissError)
            }
        },
    )

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            PrimalTopAppBar(
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
                    noteCallbacks = noteCallbacks,
                    onGoToWallet = onGoToWallet,
                    eventPublisher = eventPublisher,
                    onRootPostDeleted = onClose,
                    onUiError = { uiError ->
                        uiScope.launch {
                            snackbarHostState.showSnackbar(
                                message = uiError.resolveUiErrorMessage(context),
                                duration = SnackbarDuration.Short,
                            )
                        }
                    },
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
            val replyToPost = state.conversation.getOrNull(state.highlightPostIndex)
            if (replyToPost != null) {
                ReplyToBottomBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                    replyState = replyState,
                    replyToPost = replyToPost,
                    onExpandReply = { mediaUris ->
                        onExpandReply(
                            NoteEditorArgs(
                                referencedNoteNevent = state.highlightNote?.asNeventString(),
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
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onRootPostDeleted: () -> Unit,
    eventPublisher: (ThreadContract.UiEvent) -> Unit,
    onUiError: ((UiError) -> Unit)? = null,
) {
    val pullToRefreshState = rememberPullToRefreshState()
    var pullToRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(!state.fetching) {
        if (!state.fetching) {
            pullToRefreshing = false
        }
    }

    PullToRefreshBox(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        isRefreshing = pullToRefreshing,
        onRefresh = {
            eventPublisher(ThreadContract.UiEvent.UpdateConversation)
            pullToRefreshing = true
        },
        state = pullToRefreshState,
        indicator = {
            PrimalIndicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = pullToRefreshing,
                state = pullToRefreshState,
            )
        },
    ) {
        if (state.conversation.isEmpty()) {
            if (state.fetching) {
                HeightAdjustableLoadingLazyListPlaceholder(
                    firstItemHeight = 250.dp,
                    height = 100.dp,
                )
            } else {
                ListNoContent(
                    modifier = Modifier.fillMaxSize(),
                    noContentText = stringResource(id = R.string.thread_invalid_thread_id),
                    onRefresh = { eventPublisher(ThreadContract.UiEvent.UpdateConversation) },
                )
            }
        } else {
            ThreadLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                onGoToWallet = onGoToWallet,
                noteCallbacks = noteCallbacks,
                onRootPostDeleted = onRootPostDeleted,
                onUiError = onUiError,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ThreadLazyColumn(
    modifier: Modifier,
    state: ThreadContract.UiState,
    noteCallbacks: NoteCallbacks,
    onRootPostDeleted: () -> Unit,
    onGoToWallet: (() -> Unit),
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
    onUiError: ((UiError) -> Unit)? = null,
) {
    var threadListMaxHeightPx by remember { mutableIntStateOf(0) }
    var highlightPostHeightPx by remember { mutableIntStateOf(0) }
    var repliesHeightPx by remember { mutableStateOf(mapOf<Int, Int>()) }

    var extraSpacing by remember { mutableStateOf(0.dp) }
    extraSpacing = with(LocalDensity.current) {
        threadListMaxHeightPx.toDp() - highlightPostHeightPx.toDp() - repliesHeightPx.values.sum().toDp()
    }

    LazyColumn(
        modifier = modifier.onSizeChanged { threadListMaxHeightPx = it.height },
        contentPadding = paddingValues,
    ) {
        if (state.replyToArticle != null) {
            item(
                key = state.replyToArticle.eventId,
                contentType = "MentionedArticle",
            ) {
                Column {
                    FeedArticleListItem(
                        data = state.replyToArticle,
                        modifier = Modifier.padding(all = 16.dp),
                        onClick = noteCallbacks.onArticleClick,
                        isArticleAuthor = state.replyToArticle.authorId == state.activeAccountUserId,
                    )
                    PrimalDivider()
                }
            }
        }

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
                    expanded = true,
                    textSelectable = highlighted,
                    fullWidthContent = highlighted,
                    headerSingleLine = !highlighted,
                    enableTweetsMode = index == state.highlightPostIndex,
                    forceContentIndent = index != state.highlightPostIndex,
                    drawLineAboveAvatar = isConnectedBackward(index, state.highlightPostIndex),
                    drawLineBelowAvatar = isConnectedForward(index, state.highlightPostIndex),
                    showReplyTo = false,
                    showNoteStatCounts = index != state.highlightPostIndex,
                    noteCallbacks = noteCallbacks.copy(
                        onNoteClick = { noteId ->
                            if (state.highlightPostId != noteId) {
                                noteCallbacks.onNoteClick?.invoke(noteId)
                            }
                        },
                    ),
                    onNoteDeleted = {
                        if (!isReply) {
                            onRootPostDeleted()
                        }
                    },
                    onGoToWallet = onGoToWallet,
                    contentFooter = {
                        Column(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .padding(horizontal = 10.dp),
                        ) {
                            if (item.eventZaps.isNotEmpty()) {
                                ThreadNoteTopZapsSection(
                                    zaps = item.eventZaps,
                                    onClick = if (noteCallbacks.onEventReactionsClick != null) {
                                        { noteCallbacks.onEventReactionsClick.invoke(item.postId, ReactionType.ZAPS) }
                                    } else {
                                        null
                                    },
                                )
                            }

                            if (highlighted) {
                                val date = item.timestamp.formatToDefaultDateFormat(FormatStyle.FULL)
                                val time = item.timestamp.formatToDefaultTimeFormat(FormatStyle.SHORT)
                                Text(
                                    modifier = Modifier.padding(top = 14.dp),
                                    text = "$date • $time ",
                                    style = AppTheme.typography.bodyMedium,
                                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                                )

                                if (item.stats.hasAnyCount()) {
                                    ThreadNoteStatsRow(
                                        modifier = Modifier.padding(top = 10.dp),
                                        eventStats = item.stats,
                                        onReactionTypeClick = { tab ->
                                            noteCallbacks.onEventReactionsClick?.invoke(item.postId, tab)
                                        },
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    },
                    onUiError = onUiError,
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

private fun EventStatsUi.hasAnyCount() = repliesCount > 0 || zapsCount > 0 || likesCount > 0 || repostsCount > 0

@Composable
private fun ReplyToBottomBar(
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

        ReplyToOptions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 0.dp, end = 16.dp)
                .padding(top = 0.dp, bottom = 4.dp)
                .then(if (isKeyboardVisible) Modifier.wrapContentHeight() else Modifier.height(0.dp)),
            replying = replyState.publishing,
            replyEnabled = !replyState.publishing && replyState.content.text.isNotBlank(),
            onPublishReplyClick = { replyEventPublisher(NoteEditorContract.UiEvent.PublishNote) },
            onPhotosImported = { uris ->
                onExpandReply(uris)
            },
            onUserTagClick = {
                replyEventPublisher(NoteEditorContract.UiEvent.AppendUserTagAtSign)
                replyEventPublisher(NoteEditorContract.UiEvent.ToggleSearchUsers(enabled = true))
            },
        )
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
    modifier: Modifier,
    replying: Boolean,
    replyEnabled: Boolean,
    onPublishReplyClick: () -> Unit,
    onPhotosImported: (List<Uri>) -> Unit,
    onUserTagClick: () -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            ImportPhotosIconButton(
                imageVector = PrimalIcons.ImportPhotoFromGallery,
                contentDescription = stringResource(id = R.string.accessibility_import_photo_from_gallery),
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                onPhotosImported = onPhotosImported,
            )

            TakePhotoIconButton(
                imageVector = PrimalIcons.ImportPhotoFromCamera,
                contentDescription = stringResource(id = R.string.accessibility_take_photo),
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                onPhotoTaken = { uri -> onPhotosImported(listOf(uri)) },
            )

            IconButton(onClick = onUserTagClick) {
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

@Preview
@Composable
fun ThreadScreenPreview() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        ThreadScreen(
            state = ThreadContract.UiState(
                activeAccountUserId = "",
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
                        uris = emptyList(),
                        nostrUris = emptyList(),
                        timestamp = Instant.now().minusSeconds(3600.seconds.inWholeSeconds),
                        stats = EventStatsUi(),
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
                        uris = emptyList(),
                        nostrUris = emptyList(),
                        timestamp = Instant.now(),
                        stats = EventStatsUi(),
                        hashtags = listOf("#nostr"),
                        rawNostrEventJson = "raaaw",
                        replyToAuthorHandle = "alex",
                    ),
                ),
            ),
            onClose = {},
            noteCallbacks = NoteCallbacks(),
            onGoToWallet = {},
            onExpandReply = {},
            eventPublisher = {},
        )
    }
}
