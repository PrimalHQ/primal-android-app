package net.primal.android.thread.articles.ui

import android.content.Context
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import java.time.Instant
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.feed.model.FeedPostUi
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
import net.primal.android.thread.articles.ArticleDetailsContract
import net.primal.android.thread.articles.ArticleDetailsContract.ArticleDetailsError
import net.primal.android.thread.articles.ArticleDetailsContract.ArticlePartRender
import net.primal.android.thread.articles.ArticleDetailsContract.UiEvent
import net.primal.android.thread.articles.ArticleDetailsViewModel
import net.primal.android.thread.articles.ui.rendering.HtmlRenderer
import net.primal.android.thread.articles.ui.rendering.MarkdownRenderer
import net.primal.android.thread.articles.ui.rendering.replaceProfileNostrUrisWithMarkdownLinks
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
    onMediaClick: (MediaClickEvent) -> Unit,
    onReactionsClick: (eventId: String) -> Unit,
    onPayInvoiceClick: (InvoicePayClickEvent) -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

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
                showDivider = state.authorDisplayName == null,
            )
        },
        content = { paddingValues ->
            if (state.markdownContent.isEmpty()) {
                PrimalLoadingSpinner()
            } else {
                ArticleContent(
                    state = state,
                    paddingValues = paddingValues,
                    onNoteClick = onNoteClick,
                    onProfileClick = onProfileClick,
                    onArticleClick = onArticleClick,
                    onMediaClick = onMediaClick,
                    onReactionsClick = onReactionsClick,
                    onPayInvoiceClick = onPayInvoiceClick,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun ArticleContent(
    state: ArticleDetailsContract.UiState,
    paddingValues: PaddingValues,
    onNoteClick: (noteId: String) -> Unit,
    onProfileClick: (profileId: String) -> Unit,
    onArticleClick: (naddr: String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onReactionsClick: (eventId: String) -> Unit,
    onPayInvoiceClick: (InvoicePayClickEvent) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val renderParts by remember(state.markdownContent) {
        mutableStateOf(
            state.markdownContent
                .splitMarkdownByNostrUris()
                .replaceProfileNostrUrisWithMarkdownLinks(npubToDisplayNameMap = state.npubToDisplayNameMap)
                .buildArticleRenderParts(referencedNotes = state.referencedNotes),
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
    ) {
        if (state.authorDisplayName != null) {
            item {
                ArticleAuthorRow(
                    modifier = Modifier
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

        item {
            ArticleDetailsHeader(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                title = state.title,
                date = state.timestamp?.let { Instant.ofEpochSecond(it) },
                cover = state.coverCdnImage,
                summary = state.summary,
            )
        }

        if (state.topZap != null || state.otherZaps.isNotEmpty()) {
            item {
                ArticleTopZapsSection(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    topZap = state.topZap,
                    otherZaps = state.otherZaps,
                    onZapsClick = { state.eventId?.let(onReactionsClick) },
                    onZapClick = {},
                )
            }
        }

        items(items = renderParts) { part ->
            when (part) {
                is ArticlePartRender.HtmlRender -> {
                    HtmlRenderer(
                        modifier = Modifier
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
                        modifier = Modifier.padding(all = 16.dp),
                        markdown = part.markdown,
                        onProfileClick = onProfileClick,
                        onNoteClick = onNoteClick,
                        onArticleClick = onArticleClick,
                        onUrlClick = { url -> uriHandler.openUriSafely(url) },
                    )
                }

                is ArticlePartRender.NoteRender -> {
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
