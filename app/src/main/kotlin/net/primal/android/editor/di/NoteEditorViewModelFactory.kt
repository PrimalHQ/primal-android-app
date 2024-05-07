package net.primal.android.editor.di

import android.app.Activity
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.assisted.AssistedFactory
import dagger.hilt.android.EntryPointAccessors
import net.primal.android.core.di.ViewModelFactoryProvider
import net.primal.android.editor.NoteEditorViewModel

@AssistedFactory
interface NoteEditorViewModelFactory {
    fun create(
        content: TextFieldValue,
        replyNoteId: String?,
        mediaUri: Uri?,
    ): NoteEditorViewModel
}

@Composable
fun noteEditorViewModel(
    content: TextFieldValue = TextFieldValue(),
    replyNoteId: String? = null,
    mediaUri: String? = null,
): NoteEditorViewModel {
    val factory = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java,
    ).noteEditorViewModelFactory()

    return viewModel(
        key = "noteEditorViewModel/$replyNoteId",
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return factory.create(
                    content = content,
                    replyNoteId = replyNoteId,
                    mediaUri = mediaUri?.let { Uri.parse(it) },
                ) as T
            }
        },
    )
}
