package net.primal.android.thread.articles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.halilibo.richtext.commonmark.CommonmarkAstNodeParser
import com.halilibo.richtext.commonmark.MarkdownParseOptions
import com.halilibo.richtext.markdown.BasicMarkdown
import com.halilibo.richtext.ui.RichTextStyle
import com.halilibo.richtext.ui.material3.RichText
import com.halilibo.richtext.ui.resolveDefaults
import net.primal.android.R
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.feed.note.events.InvoicePayClickEvent
import net.primal.android.core.compose.feed.note.events.MediaClickEvent
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.theme.AppTheme
import net.primal.android.thread.articles.ArticleDetailsContract.UiEvent
import net.primal.android.thread.articles.ArticleDetailsContract.UiState.ArticleDetailsError

@Composable
fun ArticleDetailsScreen(
    viewModel: ArticleDetailsViewModel,
    onClose: () -> Unit,
    onNoteClick: (noteId: String) -> Unit,
    onNoteReplyClick: (String) -> Unit,
    onNoteQuoteClick: (content: TextFieldValue) -> Unit,
    onProfileClick: (profileId: String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
    onGoToWallet: () -> Unit,
    onReactionsClick: (noteId: String) -> Unit,
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
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ArticleDetailsScreen(
    state: ArticleDetailsContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
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
        },
        onErrorDismiss = {
            eventPublisher(UiEvent.DismissErrors)
        },
    )

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            PrimalTopAppBar(
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
                showDivider = true,
            )
        },
        content = { paddingValues ->
            val highlightColor = AppTheme.colorScheme.secondary
            val richTextStyle by remember {
                val default = RichTextStyle().resolveDefaults()
                mutableStateOf(
                    default.copy(
                        stringStyle = default.stringStyle?.copy(
                            linkStyle = SpanStyle(color = highlightColor),
                        ),
                    ),
                )
            }
            val markdownParseOptions by remember { mutableStateOf(MarkdownParseOptions.Default) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(state = rememberScrollState()),
            ) {
                if (state.markdown != null) {
                    val parser = remember(markdownParseOptions, state.markdown) {
                        CommonmarkAstNodeParser(markdownParseOptions)
                    }

                    val astNode = remember(parser) {
                        parser.parse(state.markdown)
                    }

                    SelectionContainer {
                        RichText(
                            modifier = Modifier.padding(all = 16.dp),
                            style = richTextStyle,
                        ) {
                            BasicMarkdown(astNode = astNode)
                        }
                    }
                } else {
                    PrimalLoadingSpinner()
                }

//                    val html by remember(state.rawContent) {
//                        val html = ARTICLE_BASE_HTML
//                            .replace("{{ THEME }}", "huge")
//                            .replace("{{ CONTENT }}",
//                                state.rawContent.substring(startIndex = 6, endIndex = state.rawContent.length - 7)
//                            )
//                        Timber.e(html)
//                        mutableStateOf(html)
//                    }
//
//
//                    AndroidView(
//                        modifier = Modifier
//                            .fillMaxSize(),
//                        factory = { context ->
//                            WebView(context).apply {
//                                var started: Long? = null
//                                webViewClient = object : WebViewClient() {
//                                    override fun shouldOverrideUrlLoading(
//                                        view: WebView?,
//                                        request: WebResourceRequest?,
//                                    ): Boolean {
//                                        Timber.e(request?.url.toString())
//                                        return true
//                                    }
//
//                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//                                        super.onPageStarted(view, url, favicon)
//                                        started = System.currentTimeMillis()
//                                    }
//
//                                    override fun onPageFinished(view: WebView?, url: String?) {
//                                        super.onPageFinished(view, url)
//                                        Timber.w(
//                                            "Rendering: " + (System.currentTimeMillis() - (started
//                                                ?: 0L)) + " msec",
//                                        )
//                                    }
//                                }
//                            }
//                        },
//                        update = {
//                            it.loadData(html, "text/html", "utf8")
//                        },
//                    )
//            }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}
