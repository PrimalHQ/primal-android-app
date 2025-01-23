package net.primal.android.explore.home.zaps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.primal.android.R
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.zaps.ReferencedNoteZap
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.theme.AppTheme

@Composable
fun ExploreZaps(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    noteCallbacks: NoteCallbacks,
) {
    val viewModel: ExploreZapsViewModel = hiltViewModel()
    val uiState = viewModel.state.collectAsState()

    ExploreZaps(
        modifier = modifier,
        state = uiState.value,
        paddingValues = paddingValues,
        noteCallbacks = noteCallbacks,
        eventPublisher = viewModel::setEvent,
    )
}

@Composable
private fun ExploreZaps(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    noteCallbacks: NoteCallbacks,
    state: ExploreZapsContract.UiState,
    eventPublisher: (ExploreZapsContract.UiEvent) -> Unit,
) {
    when {
        state.loading && state.zaps.isEmpty() -> {
            HeightAdjustableLoadingLazyListPlaceholder(
                modifier = modifier.fillMaxSize(),
                contentPaddingValues = paddingValues,
                clipShape = AppTheme.shapes.large,
                height = 112.dp,
            )
        }

        state.zaps.isEmpty() -> {
            ListNoContent(
                modifier = modifier.fillMaxSize(),
                noContentText = stringResource(id = R.string.explore_trending_zaps_no_content),
                refreshButtonVisible = true,
                onRefresh = { eventPublisher(ExploreZapsContract.UiEvent.RefreshZaps) },
            )
        }

        else -> {
            LazyColumn(
                modifier = modifier.padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = paddingValues,
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }
                items(items = state.zaps) { item ->
                    ReferencedNoteZap(
                        senderId = item.sender?.pubkey,
                        receiverId = item.receiver?.pubkey,
                        noteContentUi = item.noteContentUi,
                        amountInSats = item.amountSats,
                        createdAt = item.createdAt,
                        message = item.zapMessage,
                        senderAvatarCdnImage = item.sender?.avatarCdnImage,
                        senderLegendaryCustomization = item.sender?.premiumDetails?.legendaryCustomization,
                        receiverDisplayName = item.receiver?.authorDisplayName,
                        receiverAvatarCdnImage = item.receiver?.avatarCdnImage,
                        receiverLegendaryCustomization = item.receiver?.premiumDetails?.legendaryCustomization,
                        noteCallbacks = noteCallbacks,
                    )
                }
                item { Spacer(modifier = Modifier.height(4.dp)) }
            }
        }
    }
}
