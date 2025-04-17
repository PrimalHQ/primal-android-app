package net.primal.android.settings.muted.tabs

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import net.primal.android.notes.feed.list.NoteFeedList
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.settings.muted.MutedSettingsContract

@Composable
fun MuteThreads(
    state: MutedSettingsContract.UiState,
    noteCallbacks: NoteCallbacks,
    onGoToWallet: () -> Unit,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    NoteFeedList(
        feedSpec = state.defaultMuteThreadsFeedSpec,
        noteCallbacks = noteCallbacks,
        onGoToWallet = onGoToWallet,
        contentPadding = paddingValues,
        pollingEnabled = false,
    )
}
