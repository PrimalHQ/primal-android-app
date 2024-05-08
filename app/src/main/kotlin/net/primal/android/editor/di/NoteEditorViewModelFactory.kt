package net.primal.android.editor.di

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.assisted.AssistedFactory
import dagger.hilt.android.EntryPointAccessors
import net.primal.android.core.di.ViewModelFactoryProvider
import net.primal.android.editor.NoteEditorViewModel
import net.primal.android.editor.domain.NoteEditorArgs

@AssistedFactory
interface NoteEditorViewModelFactory {
    fun create(noteEditorArgs: NoteEditorArgs): NoteEditorViewModel
}

@Composable
fun noteEditorViewModel(args: NoteEditorArgs?): NoteEditorViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java,
    ).noteEditorViewModelFactory()

    return viewModel(
        key = "noteEditorViewModel/${args?.replyToNoteId}",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(noteEditorArgs = args ?: NoteEditorArgs()) as T
            }
        },
    )
}
