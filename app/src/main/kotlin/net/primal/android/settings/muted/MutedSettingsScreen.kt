package net.primal.android.settings.muted

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState

@Composable
fun MutedSettingsScreen(
    viewModel: MutedSettingsViewModel,
    onClose: () -> Unit
) {
    val state = viewModel.state.collectAsState()

    MutedSettingsScreen(
        state = state.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose
    )
}

@Composable
fun MutedSettingsScreen(
    state: MutedSettingsContract.UiState,
    eventPublisher: (MutedSettingsContract.UiEvent) -> Unit,
    onClose: () -> Unit
) {

}