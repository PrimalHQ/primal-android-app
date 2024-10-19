package net.primal.android.explore.asearch

import java.time.Instant
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.explore.feed.ExploreFeedContract

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
        val filter: SearchFilter = SearchFilter(),
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
        data class SearchFilterChanged(val filter: SearchFilter) : UiEvent()
        data class OrderByChanged(val orderBy: SearchOrderBy) : UiEvent()

        data object OnSearch : UiEvent()
    }

    sealed class SideEffect {
        data class NavigateToExploreNoteFeed(
            val feedSpec: String,
            val renderType: ExploreFeedContract.RenderType,
        ) : SideEffect()
        data class NavigateToExploreArticleFeed(
            val feedSpec: String,
        ) : SideEffect()
    }

    sealed class TimeModifier {
        data object Anytime : TimeModifier()
        data object Today : TimeModifier()
        data object Week : TimeModifier()
        data object Month : TimeModifier()
        data object Year : TimeModifier()
        data class Custom(val startDate: Instant, val endDate: Instant) : TimeModifier()
    }

    data class SearchFilter(
        val orientation: Orientation? = null,
        val minReadTime: Int = 0,
        val maxReadTime: Int = 0,
        val minDuration: Int = 0,
        val maxDuration: Int = 0,
        val minContentScore: Int = 0,
        val minInteractions: Int = 0,
        val minLikes: Int = 0,
        val minZaps: Int = 0,
        val minReplies: Int = 0,
        val minReposts: Int = 0,
    )

    enum class Orientation {
        Any,
        Horizontal,
        Vertical,
    }

    enum class SearchKind {
        Notes,
        Reads,
        NoteReplies,
        ReadsComments,
        Images,
        Videos,
        Sound,
        ;

        fun isReads() = this == Reads
        fun isImages() = this == Images
        fun isVideos() = this == Videos
        fun isSound() = this == Sound
    }

    enum class SearchScope {
        Global,
        MyFollows,
        MyNetwork,
        MyFollowsInteractions,
        MyNetworkInteractions,
        NotMyFollows,
    }

    enum class SearchOrderBy {
        Time,
        ContentScore,
        Replies,
        SatsZapped,
        Interactions,
    }
}
