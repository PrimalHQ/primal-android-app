package net.primal.android.explore.asearch

import java.time.Instant
import net.primal.android.core.compose.profile.model.UserProfileItemUi

interface AdvancedSearchContract {

    data class UiState(
        val includedWords: String? = null,
        val excludedWords: String? = null,
        val searchKind: SearchKind = SearchKind.Notes,
        val postedBy: Set<UserProfileItemUi> = emptySet(),
        val replyingTo: Set<UserProfileItemUi> = emptySet(),
        val zappedBy: Set<UserProfileItemUi> = emptySet(),
        val timePosted: TimeModifier = TimeModifier.Anytime,
        val scope: SearchScope = SearchScope.Global,
        val orderBy: SearchOrderBy = SearchOrderBy.Time,

    )

    sealed class UiEvent {
        data class IncludedWordsValueChanged(val query: String) : UiEvent()
        data class ExcludedWordsValueChanged(val query: String) : UiEvent()
        data class SearchKindChanged(val kind: SearchKind) : UiEvent()
        data class PostedBySelectUsers(val users: Set<UserProfileItemUi>) : UiEvent()
        data class ReplyingToSelectUsers(val users: Set<UserProfileItemUi>) : UiEvent()
        data class ZappedBySelectUsers(val users: Set<UserProfileItemUi>) : UiEvent()
        data class TimePostedChanged(val timePosted: TimeModifier) : UiEvent()
        data class ScopeChanged(val scope: SearchScope) : UiEvent()
        data class OrderByChanged(val orderBy: SearchOrderBy) : UiEvent()

        data object OnSearch : UiEvent()
    }

    sealed class TimeModifier {
        data object Anytime : TimeModifier()
        data object Today : TimeModifier()
        data object Week : TimeModifier()
        data object Month : TimeModifier()
        data object Year : TimeModifier()
        data class Custom(val startDate: Instant, val endDate: Instant) : TimeModifier()
    }

    enum class SearchKind {
        Notes,
        Reads,
        Images,
        Videos,
        Sound,
    }

    enum class SearchScope {
        Global,
        MyFollows,
        MyNetwork,
        MyFollowsInteractions,
    }

    enum class SearchOrderBy {
        Time,
        ContentScore,
    }
}
