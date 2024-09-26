package net.primal.android.note.reactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import net.primal.android.navigation.noteIdOrThrow
import net.primal.android.note.reactions.ReactionsContract.UiState
import net.primal.android.note.repository.NoteRepository
import net.primal.android.note.ui.asEventZapUiModel
import net.primal.android.user.accounts.active.ActiveAccountStore

@HiltViewModel
class ReactionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    noteRepository: NoteRepository,
    activeAccountStore: ActiveAccountStore,
) : ViewModel() {

    private val noteId = savedStateHandle.noteIdOrThrow

    private val _state = MutableStateFlow(
        UiState(
            zaps = noteRepository.pagedEventZaps(
                userId = activeAccountStore.activeUserId(),
                eventId = noteId,
            )
                .map { it.map { noteZap -> noteZap.asEventZapUiModel() } }
                .cachedIn(viewModelScope),
        ),
    )
    val state = _state.asStateFlow()
}
