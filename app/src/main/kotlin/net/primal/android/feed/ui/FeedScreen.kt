package net.primal.android.feed.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import net.primal.android.feed.FeedContract
import net.primal.android.feed.FeedViewModel
import net.primal.android.theme.PrimalTheme

@Composable
fun FeedScreen(
    viewModel: FeedViewModel
) {

    val uiState = viewModel.state.collectAsState()

    FeedScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) }
    )
}


@Composable
fun FeedScreen(
    state: FeedContract.UiState,
    eventPublisher: (FeedContract.UiEvent) -> Unit,
) {

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Nostr events:\n${state.eventCount}",
            textAlign = TextAlign.Center,
            style = PrimalTheme.typography.titleLarge,
        )
    }

}

