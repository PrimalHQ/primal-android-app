package net.primal.android.notes.feed.note

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import net.primal.android.R
import net.primal.android.notes.feed.note.NoteContract.SideEffect.NoteError

suspend fun showNoteErrorSnackbar(
    context: Context,
    error: NoteError,
    snackbarHostState: SnackbarHostState,
): SnackbarResult {
    val errorMessage = when (error) {
        is NoteError.InvalidZapRequest -> context.getString(
            R.string.post_action_invalid_zap_request,
        )

        is NoteError.MissingLightningAddress -> context.getString(
            R.string.post_action_missing_lightning_address,
        )

        is NoteError.FailedToPublishZapEvent -> context.getString(
            R.string.post_action_zap_failed,
        )

        is NoteError.FailedToPublishLikeEvent -> context.getString(
            R.string.post_action_like_failed,
        )

        is NoteError.FailedToPublishRepostEvent -> context.getString(
            R.string.post_action_repost_failed,
        )

        is NoteError.MissingRelaysConfiguration -> context.getString(
            R.string.app_missing_relays_config,
        )

        is NoteError.FailedToMuteUser -> context.getString(
            R.string.app_error_muting_user,
        )
    }

    return snackbarHostState.showSnackbar(
        message = errorMessage,
        duration = SnackbarDuration.Short,
    )
}
