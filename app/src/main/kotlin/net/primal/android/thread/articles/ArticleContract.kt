package net.primal.android.thread.articles

import net.primal.android.core.errors.UiError
import net.primal.domain.nostr.ReportType

interface ArticleContract {

    data class UiState(
        val shouldApproveBookmark: Boolean = false,
        val error: UiError? = null,
    )

    sealed class UiEvent {
        data class MuteAction(
            val userId: String,
        ) : UiEvent()

        data class ReportAbuse(
            val reportType: ReportType,
            val authorId: String,
            val eventId: String,
            val articleId: String,
        ) : UiEvent()

        data class BookmarkAction(
            val articleATag: String,
            val forceUpdate: Boolean = false,
        ) : UiEvent()

        data object DismissBookmarkConfirmation : UiEvent()

        data object DismissError : UiEvent()
    }
}
