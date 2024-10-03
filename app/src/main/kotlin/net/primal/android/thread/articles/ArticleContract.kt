package net.primal.android.thread.articles

import net.primal.android.core.errors.UiError
import net.primal.android.profile.report.ReportType

interface ArticleContract {

    data class UiState(
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

        data object DismissError : UiEvent()
    }
}
