package net.primal.android.thread.blogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.Lifecycle
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.feed.note.events.InvoicePayClickEvent
import net.primal.android.core.compose.feed.note.events.MediaClickEvent
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.thread.blogs.LongFormThreadContract.UiEvent
import net.primal.android.thread.blogs.LongFormThreadContract.UiState.LongFormThreadError

@Composable
fun LongFormThreadScreen(
    viewModel: LongFormThreadViewModel,
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
            Lifecycle.Event.ON_START -> viewModel.setEvent(
                UiEvent.UpdateContent,
            )

            else -> Unit
        }
    }

    LongFormThreadScreen(
        state = uiState,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LongFormThreadScreen(
    state: LongFormThreadContract.UiState,
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
                LongFormThreadError.InvalidNaddr -> context.getString(
                    R.string.long_form_thread_invalid_naddr,
                )

                is LongFormThreadError.InvalidZapRequest -> context.getString(
                    R.string.post_action_invalid_zap_request,
                )

                is LongFormThreadError.MissingLightningAddress -> context.getString(
                    R.string.post_action_missing_lightning_address,
                )

                is LongFormThreadError.FailedToPublishZapEvent -> context.getString(
                    R.string.post_action_zap_failed,
                )

                is LongFormThreadError.FailedToPublishLikeEvent -> context.getString(
                    R.string.post_action_like_failed,
                )

                is LongFormThreadError.FailedToPublishRepostEvent -> context.getString(
                    R.string.post_action_repost_failed,
                )

                is LongFormThreadError.MissingRelaysConfiguration -> context.getString(
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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter,
            ) {
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}
