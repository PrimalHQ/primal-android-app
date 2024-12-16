package net.primal.android.thread.articles.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import java.text.NumberFormat
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.articles.feed.ui.ArticleDropdownMenuIcon
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Bookmarks
import net.primal.android.core.compose.icons.primaliconpack.BookmarksFilled
import net.primal.android.core.compose.icons.primaliconpack.FeedNewZapFilled
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.compose.profile.approvals.ApproveBookmarkAlertDialog
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.compose.zaps.ArticleTopZapsSection
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.core.ext.openUriSafely
import net.primal.android.nostr.ext.isNEvent
import net.primal.android.nostr.ext.isNEventUri
import net.primal.android.nostr.ext.isNostrUri
import net.primal.android.nostr.ext.isNote
import net.primal.android.nostr.ext.takeAsNoteHexIdOrNull
import net.primal.android.nostr.utils.Nip19TLV.toNaddrString
import net.primal.android.notes.feed.NoteRepostOrQuoteBottomSheet
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.model.FeedPostAction
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.note.FeedNoteCard
import net.primal.android.notes.feed.note.ui.FeedNoteActionsRow
import net.primal.android.notes.feed.note.ui.ReferencedNoteCard
import net.primal.android.notes.feed.note.ui.ThreadNoteStatsRow
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.notes.feed.zaps.UnableToZapBottomSheet
import net.primal.android.notes.feed.zaps.ZapBottomSheet
import net.primal.android.profile.report.ReportType
import net.primal.android.theme.AppTheme
import net.primal.android.thread.articles.ArticleContract
import net.primal.android.thread.articles.ArticleViewModel
import net.primal.android.thread.articles.details.ArticleDetailsContract.ArticlePartRender
import net.primal.android.thread.articles.details.ArticleDetailsContract.UiEvent
import net.primal.android.thread.articles.details.ui.ArticleAuthorRow
import net.primal.android.thread.articles.details.ui.ArticleDetailsHeader
import net.primal.android.thread.articles.details.ui.ArticleHashtags
import net.primal.android.thread.articles.details.ui.FloatingArticlePill
import net.primal.android.thread.articles.details.ui.HighlightActivityBottomSheetHandler
import net.primal.android.thread.articles.details.ui.rendering.HtmlRenderer
import net.primal.android.thread.articles.details.ui.rendering.MarkdownRenderer
import net.primal.android.thread.articles.details.ui.rendering.replaceProfileNostrUrisWithMarkdownLinks
import net.primal.android.thread.articles.details.ui.rendering.splitIntoParagraphs
import net.primal.android.thread.articles.details.ui.rendering.splitMarkdownByNostrUris
import net.primal.android.wallet.zaps.canZap

@Composable
fun ArticleDetailsScreen(
    viewModel: ArticleDetailsViewModel,
    onClose: () -> Unit,
    onArticleHashtagClick: (hashtag: String) -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
) {
    val detailsState by viewModel.state.collectAsState()
    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> viewModel.setEvent(UiEvent.UpdateContent)
            else -> Unit
        }
    }

    val articleViewModel = hiltViewModel<ArticleViewModel>()
    val articleState by articleViewModel.state.collectAsState()

    ArticleDetailsScreen(
        detailsState = detailsState,
        articleState = articleState,
        detailsEventPublisher = viewModel::setEvent,
        articleEventPublisher = articleViewModel::setEvent,
        onClose = onClose,
        onArticleHashtagClick = onArticleHashtagClick,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleDetailsScreen(
    detailsState: ArticleDetailsContract.UiState,
    articleState: ArticleContract.UiState,
    detailsEventPublisher: (UiEvent) -> Unit,
    articleEventPublisher: (ArticleContract.UiEvent) -> Unit,
    onArticleHashtagClick: (hashtag: String) -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val listState = rememberLazyListState()
    val scrolledToTop by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    val articleParts by remember(detailsState.article?.content) {
        mutableStateOf(
            (detailsState.article?.content ?: "")
                .splitMarkdownByNostrUris()
                .flatMap { it.splitIntoParagraphs() }
                .replaceProfileNostrUrisWithMarkdownLinks(npubToDisplayNameMap = detailsState.npubToDisplayNameMap)
                .buildArticleRenderParts(referencedNotes = detailsState.referencedNotes),
        )
    }

    var showCantZapWarning by remember { mutableStateOf(false) }
    if (showCantZapWarning) {
        UnableToZapBottomSheet(
            zappingState = detailsState.zappingState,
            onDismissRequest = { showCantZapWarning = false },
            onGoToWallet = onGoToWallet,
        )
    }

    var showZapOptions by remember { mutableStateOf(false) }
    if (showZapOptions && detailsState.article != null) {
        ZapBottomSheet(
            onDismissRequest = { showZapOptions = false },
            receiverName = detailsState.article.authorDisplayName,
            zappingState = detailsState.zappingState,
            onZap = { zapAmount, zapDescription ->
                if (detailsState.zappingState.canZap(zapAmount)) {
                    detailsEventPublisher(
                        UiEvent.ZapArticle(
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

    fun invokeZapOptionsOrShowWarning() {
        if (detailsState.zappingState.walletConnected) {
            showZapOptions = true
        } else {
            showCantZapWarning = true
        }
    }

    var showRepostOrQuoteConfirmation by remember { mutableStateOf(false) }
    if (showRepostOrQuoteConfirmation) {
        NoteRepostOrQuoteBottomSheet(
            onDismiss = { showRepostOrQuoteConfirmation = false },
            onRepostClick = { detailsEventPublisher(UiEvent.RepostAction) },
            onPostQuoteClick = {
                detailsState.naddr?.toNaddrString()?.let { noteCallbacks.onArticleQuoteClick?.invoke(it) }
            },
        )
    }

    HighlightActivityBottomSheetHandler(
        selectedHighlight = detailsState.selectedHighlight,
        dismissSelection = { detailsEventPublisher(UiEvent.DismissSelectedHighlight) },
        isHighlighted = detailsState.isHighlighted,
        onSaveHighlightClick = { detailsEventPublisher(UiEvent.PublishSelectedHighlight) },
        onDeleteHighlightClick = { detailsEventPublisher(UiEvent.DeleteSelectedHighlight) },
        isWorking = detailsState.isWorking,
    )

    if (articleState.shouldApproveBookmark && detailsState.article != null) {
        ApproveBookmarkAlertDialog(
            onBookmarkConfirmed = {
                articleEventPublisher(
                    ArticleContract.UiEvent.BookmarkAction(
                        forceUpdate = true,
                        articleATag = detailsState.article.aTag,
                    ),
                )
            },
            onClose = {
                articleEventPublisher(ArticleContract.UiEvent.DismissBookmarkConfirmation)
            },
        )
    }

    SnackbarErrorHandler(
        error = detailsState.error ?: articleState.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context = context) },
        onErrorDismiss = {
            detailsEventPublisher(UiEvent.DismissErrors)
            articleEventPublisher(ArticleContract.UiEvent.DismissError)
        },
    )

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            ArticleDetailsTopAppBar(
                state = detailsState,
                scrolledToTop = scrolledToTop,
                onClose = onClose,
                onToggleHighlightsClick = { detailsEventPublisher(UiEvent.ToggleHighlights) },
                onBookmarkClick = {
                    if (detailsState.article != null) {
                        articleEventPublisher(
                            ArticleContract.UiEvent.BookmarkAction(articleATag = detailsState.article.aTag),
                        )
                    }
                },
                onMuteUserClick = {
                    if (detailsState.article != null) {
                        articleEventPublisher(
                            ArticleContract.UiEvent.MuteAction(userId = detailsState.article.authorId),
                        )
                    }
                },
                onReportContentClick = { reportType ->
                    if (detailsState.article != null) {
                        articleEventPublisher(
                            ArticleContract.UiEvent.ReportAbuse(
                                reportType = reportType,
                                authorId = detailsState.article.authorId,
                                eventId = detailsState.article.eventId,
                                articleId = detailsState.article.articleId,
                            ),
                        )
                    }
                },
            )
        },
        content = { paddingValues ->
            if (detailsState.article == null) {
                PrimalLoadingSpinner()
            } else {
                ArticleContentWithComments(
                    state = detailsState,
                    articleParts = articleParts,
                    listState = listState,
                    paddingValues = paddingValues,
                    showHighlights = detailsState.showHighlights,
                    onArticleCommentClick = {
                        detailsState.naddr?.toNaddrString()?.let { noteCallbacks.onArticleReplyClick?.invoke(it) }
                    },
                    onArticleHashtagClick = onArticleHashtagClick,
                    onZapOptionsClick = { invokeZapOptionsOrShowWarning() },
                    onGoToWallet = onGoToWallet,
                    noteCallbacks = noteCallbacks,
                    onHighlightClick = { detailsEventPublisher(UiEvent.SelectHighlight(it)) },
                    onFollowUnfollowClick = { detailsEventPublisher(UiEvent.ToggleAuthorFollows) },
                    onPostAction = { action ->
                        when (action) {
                            FeedPostAction.Reply -> {
                                detailsState.naddr?.toNaddrString()
                                    ?.let { noteCallbacks.onArticleReplyClick?.invoke(it) }
                            }

                            FeedPostAction.Zap -> {
                                if (detailsState.zappingState.canZap()) {
                                    detailsEventPublisher(UiEvent.ZapArticle())
                                } else {
                                    showCantZapWarning = true
                                }
                            }

                            FeedPostAction.Like -> detailsEventPublisher(UiEvent.LikeArticle)

                            FeedPostAction.Repost -> {
                                showRepostOrQuoteConfirmation = true
                            }

                            FeedPostAction.Bookmark -> articleEventPublisher(
                                ArticleContract.UiEvent.BookmarkAction(articleATag = detailsState.article.aTag),
                            )
                        }
                    },
                    onPostLongPressAction = { action ->
                        when (action) {
                            FeedPostAction.Zap -> invokeZapOptionsOrShowWarning()
                            else -> Unit
                        }
                    },
                    onUiError = { uiError ->
                        uiScope.launch {
                            snackbarHostState.showSnackbar(
                                message = uiError.resolveUiErrorMessage(context),
                                duration = SnackbarDuration.Short,
                            )
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            if (detailsState.article != null) {
                FloatingArticlePill(
                    commentsCount = detailsState.article.eventStatsUi.repliesCount,
                    satsZapped = detailsState.article.eventStatsUi.satsZapped,
                    onCommentsClick = {
                        uiScope.launch {
                            listState.animateScrollToItem(
                                index = detailsState.calculateCommentsHeaderIndex(partsSize = articleParts.size),
                            )
                        }
                    },
                    onZapClick = { invokeZapOptionsOrShowWarning() },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ArticleDetailsTopAppBar(
    state: ArticleDetailsContract.UiState,
    scrolledToTop: Boolean,
    onClose: () -> Unit,
    onToggleHighlightsClick: (() -> Unit)? = null,
    onBookmarkClick: (() -> Unit)? = null,
    onMuteUserClick: (() -> Unit)? = null,
    onReportContentClick: ((reportType: ReportType) -> Unit)? = null,
) {
    PrimalTopAppBar(
        navigationIcon = PrimalIcons.ArrowBack,
        navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
        onNavigationIconClick = onClose,
        showDivider = state.article?.authorDisplayName == null || !scrolledToTop,
        actions = {
            if (state.article != null) {
                AppBarIcon(
                    icon = if (state.article.isBookmarked) PrimalIcons.BookmarksFilled else PrimalIcons.Bookmarks,
                    iconSize = 20.dp,
                    appBarIconContentDescription = stringResource(id = R.string.accessibility_bookmark),
                    onClick = { onBookmarkClick?.invoke() },
                )

                ArticleDropdownMenuIcon(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape),
                    articleId = state.article.articleId,
                    articleContent = state.article.content,
                    articleRawData = state.article.eventRawNostrEvent,
                    authorId = state.article.authorId,
                    isBookmarked = state.article.isBookmarked,
                    showHighlights = state.showHighlights,
                    onToggleHighlightsClick = onToggleHighlightsClick,
                    onBookmarkClick = onBookmarkClick,
                    onMuteUserClick = onMuteUserClick,
                    onReportContentClick = onReportContentClick,
                    icon = {
                        Icon(
                            modifier = Modifier.padding(top = 10.dp),
                            imageVector = PrimalIcons.More,
                            contentDescription = stringResource(id = R.string.accessibility_article_drop_down),
                        )
                    },
                )
            }
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ArticleContentWithComments(
    state: ArticleDetailsContract.UiState,
    articleParts: List<ArticlePartRender>,
    listState: LazyListState = rememberLazyListState(),
    showHighlights: Boolean,
    paddingValues: PaddingValues,
    onArticleCommentClick: (naddr: String) -> Unit,
    onArticleHashtagClick: (hashtag: String) -> Unit,
    onZapOptionsClick: () -> Unit,
    onHighlightClick: (String) -> Unit,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    onPostAction: ((FeedPostAction) -> Unit)? = null,
    onPostLongPressAction: ((FeedPostAction) -> Unit)? = null,
    onFollowUnfollowClick: (() -> Unit)? = null,
    onUiError: ((UiError) -> Unit)? = null,
) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        state = listState,
    ) {
        if (state.article != null) {
            item(
                key = "ArticleAuthor",
                contentType = "ArticleAuthor",
            ) {
                ArticleAuthorRow(
                    modifier = Modifier
                        .background(color = AppTheme.colorScheme.surface)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    authorFollowed = state.isAuthorFollowed,
                    authorCdnImage = state.article.authorAvatarCdnImage,
                    authorDisplayName = state.article.authorDisplayName,
                    authorInternetIdentifier = state.article.authorInternetIdentifier,
                    authorLegendaryCustomization = state.article.authorLegendaryCustomization,
                    onAuthorAvatarClick = { noteCallbacks.onProfileClick?.invoke(state.article.authorId) },
                    onFollowUnfollowClick = onFollowUnfollowClick,
                )
                PrimalDivider()
            }
        }

        item(
            key = "ArticleHeader",
            contentType = "ArticleHeader",
        ) {
            ArticleDetailsHeader(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                title = state.article?.title ?: "",
                date = state.article?.publishedAt,
                cover = state.article?.coverImageCdnImage,
                summary = state.article?.summary,
            )
        }

        if (state.topZaps.isNotEmpty()) {
            item(
                key = "TopZapSection",
                contentType = "TopZapSection",
            ) {
                ArticleTopZapsSection(
                    modifier = Modifier
                        .background(color = AppTheme.colorScheme.surfaceVariant)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    topZaps = state.topZaps,
                    onTopZapsClick = {
                        state.article?.eventId?.let { noteCallbacks.onEventReactionsClick?.invoke(it) }
                    },
                    onZapClick = onZapOptionsClick,
                )
            }
        }

        items(
            count = articleParts.size,
            key = { index -> "${articleParts[index]}#$index" },
            contentType = { index ->
                when (articleParts[index]) {
                    is ArticlePartRender.HtmlRender -> "HtmlRender"
                    is ArticlePartRender.MarkdownRender -> "MarkdownRender"
                    is ArticlePartRender.NoteRender -> "NoteRender"
                }
            },
        ) { index ->
            when (val part = articleParts[index]) {
                is ArticlePartRender.HtmlRender -> {
                    HtmlRenderer(
                        modifier = Modifier
                            .background(color = AppTheme.colorScheme.surfaceVariant)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(horizontal = 8.dp),
                        html = part.html,
                        onProfileClick = noteCallbacks.onProfileClick,
                        onNoteClick = noteCallbacks.onNoteClick,
                        onArticleClick = noteCallbacks.onArticleClick,
                        onUrlClick = { url -> uriHandler.openUriSafely(url) },
                    )
                }

                is ArticlePartRender.MarkdownRender -> {
                    MarkdownRenderer(
                        modifier = Modifier
                            .background(color = AppTheme.colorScheme.surfaceVariant)
                            .fillMaxWidth()
                            .padding(all = 16.dp),
                        markdown = part.markdown,
                        showHighlights = showHighlights,
                        highlights = state.highlights,
                        onProfileClick = noteCallbacks.onProfileClick,
                        onNoteClick = noteCallbacks.onNoteClick,
                        onArticleClick = noteCallbacks.onArticleClick,
                        onUrlClick = { url -> uriHandler.openUriSafely(url) },
                        onHighlightClick = onHighlightClick,
                    )
                }

                is ArticlePartRender.NoteRender -> {
                    Box(
                        modifier = Modifier
                            .background(color = AppTheme.colorScheme.surfaceVariant)
                            .fillMaxWidth(),
                    ) {
                        ReferencedNoteCard(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            data = part.note,
                            noteCallbacks = noteCallbacks,
                        )
                    }
                }
            }
        }

        if (state.article?.hashtags?.isNotEmpty() == true) {
            item(contentType = "Hashtags") {
                ArticleHashtags(
                    modifier = Modifier
                        .background(color = AppTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 10.dp)
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    hashtags = state.article.hashtags,
                    onHashtagClick = onArticleHashtagClick,
                )
            }
        }

        if (state.article?.eventStatsUi != null) {
            item(contentType = "Stats") {
                Column(
                    modifier = Modifier
                        .background(color = AppTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp),
                ) {
                    if (state.article.eventStatsUi.satsZapped > 0) {
                        val numberFormat = NumberFormat.getNumberInstance()
                        Row(
                            modifier = Modifier.clickable(
                                enabled = noteCallbacks.onEventReactionsClick != null,
                                onClick = { noteCallbacks.onEventReactionsClick?.invoke(state.article.eventId) },
                            ),
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            IconText(
                                modifier = Modifier,
                                text = numberFormat.format(state.article.eventStatsUi.satsZapped),
                                style = AppTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                leadingIcon = PrimalIcons.FeedNewZapFilled,
                                leadingIconTintColor = AppTheme.extraColorScheme.zapped,
                                iconSize = 16.sp,
                            )

                            Spacer(modifier = Modifier.width(4.dp))

                            Text(
                                text = "sats",
                                style = AppTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 1.dp),
                            )
                        }
                    }

                    if (state.article.eventStatsUi.hasAnyCount()) {
                        ThreadNoteStatsRow(
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 8.dp)
                                .clickable(
                                    enabled = noteCallbacks.onEventReactionsClick != null,
                                    onClick = { noteCallbacks.onEventReactionsClick?.invoke(state.article.eventId) },
                                ),
                            eventStats = state.article.eventStatsUi,
                        )
                    }

                    PrimalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    FeedNoteActionsRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 16.dp),
                        eventStats = state.article.eventStatsUi,
                        isBookmarked = state.article.isBookmarked,
                        showCounts = false,
                        showBookmark = true,
                        highlightedNote = true,
                        onPostAction = onPostAction,
                        onPostLongPressAction = onPostLongPressAction,
                    )
                }
            }
        }

        item(contentType = "CommentsHeader") {
            PrimalDivider()
            CommentsHeaderSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(68.dp)
                    .background(color = AppTheme.extraColorScheme.surfaceVariantAlt1)
                    .padding(horizontal = 16.dp),
                onPostCommentClick = { state.naddr?.toNaddrString()?.let(onArticleCommentClick) },
            )
            PrimalDivider()
        }

        items(
            items = state.comments,
            key = { it.postId },
            contentType = { "NoteComment" },
        ) {
            Column(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .fillMaxWidth(),
            ) {
                FeedNoteCard(
                    data = it,
                    shape = RectangleShape,
                    cardPadding = PaddingValues(vertical = 4.dp),
                    headerSingleLine = true,
                    showReplyTo = false,
                    noteCallbacks = noteCallbacks,
                    onGoToWallet = onGoToWallet,
                    onUiError = onUiError,
                )
                PrimalDivider()
            }
        }

        item(contentType = "Spacer") {
            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}

private fun ArticleDetailsContract.UiState.calculateCommentsHeaderIndex(partsSize: Int): Int {
    var count = 1
    if (article != null) count++
    if (topZaps.isNotEmpty()) count++
    count += partsSize
    if (article?.hashtags?.isNotEmpty() == true) count++
    if (article?.eventStatsUi != null) count++
    return count
}

private fun EventStatsUi.hasAnyCount() = repliesCount > 0 || zapsCount > 0 || likesCount > 0 || repostsCount > 0

@Composable
private fun CommentsHeaderSection(modifier: Modifier, onPostCommentClick: () -> Unit) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(id = R.string.article_details_comments_section_title),
            style = LocalTextStyle.current.copy(
                fontSize = 24.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onSurface,
            ),
        )
        PrimalFilledButton(
            height = 38.dp,
            onClick = onPostCommentClick,
        ) {
            Text(
                text = stringResource(id = R.string.article_details_post_comment_button),
                style = AppTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

private fun List<String>.buildArticleRenderParts(referencedNotes: List<FeedPostUi>): List<ArticlePartRender> {
    return this.map { part ->
        when {
            part.isNostrNote() -> {
                referencedNotes.find { it.postId == part.takeAsNoteHexIdOrNull() }
                    ?.let { ArticlePartRender.NoteRender(note = it) }
                    ?: ArticlePartRender.MarkdownRender(markdown = part)
            }

            else -> ArticlePartRender.MarkdownRender(markdown = part)
        }
    }
}

private fun String.isNostrNote() = isNote() || isNostrUri() || isNEvent() || isNEventUri()
