package net.primal.android.notes.translation

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NoteTranslationHostViewModel @Inject constructor(
    val engine: NoteTranslationEngine,
) : ViewModel()
