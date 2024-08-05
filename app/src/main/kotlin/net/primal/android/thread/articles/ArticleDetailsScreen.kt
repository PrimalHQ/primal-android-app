package net.primal.android.thread.articles

import android.content.Context
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import java.time.Instant
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.note.FeedNoteCard
import net.primal.android.core.compose.feed.note.ReferencedNoteCard
import net.primal.android.core.compose.feed.note.events.InvoicePayClickEvent
import net.primal.android.core.compose.feed.note.events.MediaClickEvent
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.ext.openUriSafely
import net.primal.android.nostr.ext.isNEvent
import net.primal.android.nostr.ext.isNEventUri
import net.primal.android.nostr.ext.isNostrUri
import net.primal.android.nostr.ext.isNote
import net.primal.android.nostr.ext.takeAsNoteHexIdOrNull
import net.primal.android.nostr.utils.Nip19TLV.toNaddrString
import net.primal.android.theme.AppTheme
import net.primal.android.thread.articles.ArticleDetailsContract.ArticleDetailsError
import net.primal.android.thread.articles.ArticleDetailsContract.ArticlePartRender
import net.primal.android.thread.articles.ArticleDetailsContract.UiEvent
import net.primal.android.thread.articles.ui.ArticleAuthorRow
import net.primal.android.thread.articles.ui.ArticleDetailsHeader
import net.primal.android.thread.articles.ui.ArticleHashtags
import net.primal.android.thread.articles.ui.ArticleTopZapsSection
import net.primal.android.thread.articles.ui.FloatingArticlePill
import net.primal.android.thread.articles.ui.rendering.HtmlRenderer
import net.primal.android.thread.articles.ui.rendering.MarkdownRenderer
import net.primal.android.thread.articles.ui.rendering.replaceProfileNostrUrisWithMarkdownLinks
import net.primal.android.thread.articles.ui.rendering.splitIntoParagraphs
import net.primal.android.thread.articles.ui.rendering.splitMarkdownByNostrUris

@Composable
fun ArticleDetailsScreen(
    viewModel: ArticleDetailsViewModel,
    onClose: () -> Unit,
    onNoteClick: (noteId: String) -> Unit,
    onNoteReplyClick: (String) -> Unit,
    onNoteQuoteClick: (content: TextFieldValue) -> Unit,
    onProfileClick: (profileId: String) -> Unit,
    onArticleClick: (naddr: String) -> Unit,
    onArticleCommentClick: (naddr: String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onPayInvoiceClick: (InvoicePayClickEvent) -> Unit,
    onGoToWallet: () -> Unit,
    onReactionsClick: (eventId: String) -> Unit,
) {
    val uiState by viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> viewModel.setEvent(UiEvent.UpdateContent)
            else -> Unit
        }
    }

    ArticleDetailsScreen(
        state = uiState,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
        onNoteClick = onNoteClick,
        onProfileClick = onProfileClick,
        onArticleClick = onArticleClick,
        onArticleCommentClick = onArticleCommentClick,
        onMediaClick = onMediaClick,
        onPayInvoiceClick = onPayInvoiceClick,
        onReactionsClick = onReactionsClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleDetailsScreen(
    state: ArticleDetailsContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    onClose: () -> Unit,
    onNoteClick: (noteId: String) -> Unit,
    onProfileClick: (profileId: String) -> Unit,
    onArticleClick: (naddr: String) -> Unit,
    onArticleCommentClick: (naddr: String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onReactionsClick: (eventId: String) -> Unit,
    onPayInvoiceClick: (InvoicePayClickEvent) -> Unit,
) {
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val listState = rememberLazyListState()
    val scrolledToTop by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    val articleParts by remember(state.markdownContent) {
        mutableStateOf(
            state.markdownContent
                .splitMarkdownByNostrUris()
                .flatMap { it.splitIntoParagraphs() }
                .replaceProfileNostrUrisWithMarkdownLinks(npubToDisplayNameMap = state.npubToDisplayNameMap)
                .buildArticleRenderParts(referencedNotes = state.referencedNotes),
        )
    }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveErrorMessage(context = context) },
        onErrorDismiss = { eventPublisher(UiEvent.DismissErrors) },
    )

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            PrimalTopAppBar(
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
                showDivider = state.authorDisplayName == null || !scrolledToTop,
            )
        },
        content = { paddingValues ->
            if (state.markdownContent.isEmpty()) {
                PrimalLoadingSpinner()
            } else {
                ArticleContent(
                    state = state,
                    articleParts = articleParts,
                    listState = listState,
                    paddingValues = paddingValues,
                    onNoteClick = onNoteClick,
                    onProfileClick = onProfileClick,
                    onArticleClick = onArticleClick,
                    onArticleCommentClick = onArticleCommentClick,
                    onMediaClick = onMediaClick,
                    onReactionsClick = onReactionsClick,
                    onPayInvoiceClick = onPayInvoiceClick,
                )
            }
        },
        floatingActionButton = {
            if (state.markdownContent.isNotEmpty()) {
                FloatingArticlePill(
                    commentsCount = state.eventStatsUi?.repliesCount,
                    satsZapped = state.eventStatsUi?.satsZapped,
                    onCommentsClick = {
                        uiScope.launch {
                            listState.animateScrollToItem(
                                index = state.calculateCommentsHeaderIndex(partsSize = articleParts.size),
                            )
                        }
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ArticleContent(
    state: ArticleDetailsContract.UiState,
    articleParts: List<ArticlePartRender>,
    listState: LazyListState = rememberLazyListState(),
    paddingValues: PaddingValues,
    onNoteClick: (noteId: String) -> Unit,
    onProfileClick: (profileId: String) -> Unit,
    onArticleClick: (naddr: String) -> Unit,
    onArticleCommentClick: (naddr: String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onReactionsClick: (eventId: String) -> Unit,
    onPayInvoiceClick: (InvoicePayClickEvent) -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        state = listState,
    ) {
        if (state.authorDisplayName != null) {
            item(
                key = "ArticleAuthor",
                contentType = "ArticleAuthor",
            ) {
                ArticleAuthorRow(
                    modifier = Modifier
                        .background(color = AppTheme.colorScheme.surface)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    authorCdnImage = state.authorCdnImage,
                    authorDisplayName = state.authorDisplayName,
                    authorInternetIdentifier = state.authorInternetIdentifier,
                    onAuthorAvatarClick = { state.authorId?.let(onProfileClick) },
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
                title = state.title,
                date = state.timestamp?.let { Instant.ofEpochSecond(it) },
                cover = state.coverCdnImage,
                summary = state.summary,
            )
        }

        if (state.topZap != null || state.otherZaps.isNotEmpty()) {
            item(
                key = "TopZapSection",
                contentType = "TopZapSection",
            ) {
                ArticleTopZapsSection(
                    modifier = Modifier
                        .background(color = AppTheme.colorScheme.surfaceVariant)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    topZap = state.topZap,
                    otherZaps = state.otherZaps,
                    onZapsClick = { state.eventId?.let(onReactionsClick) },
                    onZapClick = {},
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
                        onProfileClick = onProfileClick,
                        onNoteClick = onNoteClick,
                        onArticleClick = onArticleClick,
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
                        onProfileClick = onProfileClick,
                        onNoteClick = onNoteClick,
                        onArticleClick = onArticleClick,
                        onUrlClick = { url -> uriHandler.openUriSafely(url) },
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
                            onPostClick = onNoteClick,
                            onProfileClick = onProfileClick,
                            onArticleClick = onArticleClick,
                            onMediaClick = onMediaClick,
                            onPayInvoiceClick = onPayInvoiceClick,
                        )
                    }
                }
            }
        }

        item(contentType = "Hashtags") {
            ArticleHashtags(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 10.dp)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                hashtags = state.hashtags,
            )
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
                    onProfileClick = onProfileClick,
                    onPostClick = onNoteClick,
                    onArticleClick = onArticleClick,
                    onMediaClick = onMediaClick,
                    onPayInvoiceClick = onPayInvoiceClick,
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
    var count = 2
    if (authorDisplayName != null) count++
    if (topZap != null || otherZaps.isNotEmpty()) count++
    count += partsSize
    return count
}

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

private fun ArticleDetailsError.resolveErrorMessage(context: Context): String {
    return when (this) {
        ArticleDetailsError.InvalidNaddr -> context.getString(
            R.string.long_form_thread_invalid_naddr,
        )

        is ArticleDetailsError.InvalidZapRequest -> context.getString(
            R.string.post_action_invalid_zap_request,
        )

        is ArticleDetailsError.MissingLightningAddress -> context.getString(
            R.string.post_action_missing_lightning_address,
        )

        is ArticleDetailsError.FailedToPublishZapEvent -> context.getString(
            R.string.post_action_zap_failed,
        )

        is ArticleDetailsError.FailedToPublishLikeEvent -> context.getString(
            R.string.post_action_like_failed,
        )

        is ArticleDetailsError.FailedToPublishRepostEvent -> context.getString(
            R.string.post_action_repost_failed,
        )

        is ArticleDetailsError.MissingRelaysConfiguration -> context.getString(
            R.string.app_missing_relays_config,
        )
    }
}
