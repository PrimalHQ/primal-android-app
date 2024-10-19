package net.primal.android.explore.asearch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.explore.asearch.AdvancedSearchContract.Orientation
import net.primal.android.explore.asearch.AdvancedSearchContract.SearchFilter
import net.primal.android.explore.asearch.AdvancedSearchContract.UiEvent
import net.primal.android.explore.asearch.AdvancedSearchContract.UiState
import net.primal.android.explore.feed.ExploreFeedContract
import net.primal.android.navigation.initialQuery

@HiltViewModel
class AdvancedSearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    companion object {
        private const val DATE_TIME_FORMAT = "yyyy-MM-dd_HH:mm"
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }

    private val dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withZone(ZoneId.systemDefault())
    private val dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(ZoneId.systemDefault())

    private val _state = MutableStateFlow(
        UiState(
            includedWords = savedStateHandle.initialQuery,
        ),
    )
    val state = _state.asStateFlow()
    private fun setState(reducer: UiState.() -> UiState) = _state.getAndUpdate { it.reducer() }

    private val events: MutableSharedFlow<UiEvent> = MutableSharedFlow()
    fun setEvent(event: UiEvent) = viewModelScope.launch { events.emit(event) }

    private val _effects = Channel<AdvancedSearchContract.SideEffect>()
    val effects = _effects.receiveAsFlow()
    private fun setEffect(effect: AdvancedSearchContract.SideEffect) = viewModelScope.launch { _effects.send(effect) }

    init {
        observeEvents()
    }

    private fun observeEvents() =
        viewModelScope.launch {
            events.collect {
                when (it) {
                    is UiEvent.ExcludedWordsValueChanged -> setState { copy(excludedWords = it.query) }
                    is UiEvent.IncludedWordsValueChanged -> setState { copy(includedWords = it.query) }
                    is UiEvent.OrderByChanged -> setState { copy(orderBy = it.orderBy) }
                    is UiEvent.PostedBySelectUsers -> setState { copy(postedBy = it.users) }
                    is UiEvent.ReplyingToSelectUsers -> setState { copy(replyingTo = it.users) }
                    is UiEvent.ZappedBySelectUsers -> setState { copy(zappedBy = it.users) }
                    is UiEvent.ScopeChanged -> setState { copy(scope = it.scope) }
                    is UiEvent.SearchFilterChanged -> setState { copy(filter = it.filter) }
                    is UiEvent.SearchKindChanged -> {
                        setState {
                            copy(
                                filter = filter.copy(
                                    orientation = null,
                                    minReadTime = 0,
                                    maxReadTime = 0,
                                    minDuration = 0,
                                    maxDuration = 0,
                                ),
                                searchKind = it.kind,
                            )
                        }
                    }

                    is UiEvent.TimePostedChanged -> setState { copy(timePosted = it.timePosted) }
                    UiEvent.OnSearch -> onSearch()
                }
            }
        }

    private fun onSearch() =
        viewModelScope.launch {
            val uiState = state.value
            val searchParams = listOf(
                uiState.searchKind.toSearchCommand(),
                uiState.includedWords,
                uiState.excludedWords?.let { it.split(" ").map { "-$it" }.joinToString(separator = " ") },
                uiState.postedBy.joinWithPrefix("from:"),
                uiState.replyingTo.joinWithPrefix("to:"),
                uiState.zappedBy.joinWithPrefix("zappedby:"),
                uiState.timePosted.toSearchCommand(),
                uiState.scope.toSearchCommand(),
                uiState.filter.toSearchCommand(),
                uiState.orderBy.toSearchCommand(),
            )

            val searchCommand = searchParams.filterNot { it.isNullOrEmpty() }.joinToString(separator = " ")
            val renderType = if (uiState.searchKind.isImages() || uiState.searchKind.isVideos()) {
                ExploreFeedContract.RenderType.Grid
            } else {
                ExploreFeedContract.RenderType.List
            }

            if (uiState.searchKind.isReads()) {
                setEffect(
                    AdvancedSearchContract.SideEffect.NavigateToExploreArticleFeed(
                        feedSpec = searchCommand.buildFeedSpec(),
                    ),
                )
            } else {
                setEffect(
                    AdvancedSearchContract.SideEffect.NavigateToExploreNoteFeed(
                        feedSpec = searchCommand.buildFeedSpec(),
                        renderType = renderType,
                    ),
                )
            }
        }

    private fun String.buildFeedSpec(): String = """{"id":"advsearch","query":"$this"}""".trimIndent()

    private fun AdvancedSearchContract.SearchKind.toSearchCommand() =
        when (this) {
            AdvancedSearchContract.SearchKind.Notes -> "kind:1"
            AdvancedSearchContract.SearchKind.Reads -> "kind:30023"
            AdvancedSearchContract.SearchKind.Images -> "filter:image"
            AdvancedSearchContract.SearchKind.Videos -> "filter:video"
            AdvancedSearchContract.SearchKind.Sound -> "filter:audio"
            AdvancedSearchContract.SearchKind.NoteReplies -> "kind:1 repliestokind:1"
            AdvancedSearchContract.SearchKind.ReadsComments -> "kind:1 repliestokind:30023"
        }

    private fun AdvancedSearchContract.SearchOrderBy.toSearchCommand() =
        when (this) {
            AdvancedSearchContract.SearchOrderBy.Time -> ""
            AdvancedSearchContract.SearchOrderBy.ContentScore -> "orderby:score"
            AdvancedSearchContract.SearchOrderBy.Replies -> "orderby:replies"
            AdvancedSearchContract.SearchOrderBy.SatsZapped -> "orderby:satszapped"
            AdvancedSearchContract.SearchOrderBy.Interactions -> "orderby:likes"
        }

    private fun AdvancedSearchContract.TimeModifier.toSearchCommand(): String =
        when (this) {
            AdvancedSearchContract.TimeModifier.Anytime -> ""
            AdvancedSearchContract.TimeModifier.Today ->
                "since:" + ZonedDateTime.now().minusDays(1).toInstant().toCommandFormattedDateTimeString()

            AdvancedSearchContract.TimeModifier.Week ->
                "since:" + ZonedDateTime.now().minusDays(7).toInstant().toCommandFormattedDateTimeString()

            AdvancedSearchContract.TimeModifier.Month ->
                "since:" + ZonedDateTime.now().minusMonths(1).toInstant().toCommandFormattedDateTimeString()

            AdvancedSearchContract.TimeModifier.Year ->
                "since:" + ZonedDateTime.now().minusYears(1).toInstant().toCommandFormattedDateTimeString()

            is AdvancedSearchContract.TimeModifier.Custom ->
                "since:${this.startDate.toCommandFormattedDateString()} " +
                    "until:${this.endDate.toCommandFormattedDateString()}"
        }

    private fun AdvancedSearchContract.SearchScope.toSearchCommand() =
        when (this) {
            AdvancedSearchContract.SearchScope.Global -> ""
            AdvancedSearchContract.SearchScope.MyFollows -> "scope:myfollows"
            AdvancedSearchContract.SearchScope.MyNetwork -> "scope:mynetwork"
            AdvancedSearchContract.SearchScope.MyFollowsInteractions -> "scope:myfollowinteractions"
            AdvancedSearchContract.SearchScope.MyNetworkInteractions -> "scope:mynetworkinteractions"
            AdvancedSearchContract.SearchScope.NotMyFollows -> "scope:notmyfollows"
        }

    private fun SearchFilter.toSearchCommand() =
        if (this.isEmpty()) {
            ""
        } else {
            val stringFilters = arrayOf(
                this.orientation.toFilterQuery(),
                this.minReadTime.times(238).toFilterQueryOrEmpty("minwords", delimiter = ":"),
                this.maxReadTime.times(238).toFilterQueryOrEmpty("maxwords", delimiter = ":"),
                this.minDuration.toFilterQueryOrEmpty("minduration", delimiter = ":"),
                this.maxDuration.toFilterQueryOrEmpty("maxduration", delimiter = ":"),
                this.minContentScore.toFilterQueryOrEmpty("minscore", delimiter = ":"),
                this.minInteractions.toFilterQueryOrEmpty("mininteractions", delimiter = ":"),
                this.minLikes.toFilterQueryOrEmpty("minlikes", delimiter = ":"),
                this.minZaps.toFilterQueryOrEmpty("minzaps", delimiter = ":"),
                this.minReplies.toFilterQueryOrEmpty("minreplies", delimiter = ":"),
                this.minReposts.toFilterQueryOrEmpty("minreposts", delimiter = ":"),
            )

            stringFilters.filter { it.isNotEmpty() }.joinToString(" ")
        }

    private fun Instant.toCommandFormattedDateTimeString() = dateTimeFormatter.format(this)
    private fun Instant.toCommandFormattedDateString() = dateFormatter.format(this)

    private fun Set<UserProfileItemUi>.joinWithPrefix(prefix: String) =
        if (this.isEmpty()) {
            ""
        } else {
            "(" + this.joinToString(separator = " OR ") { prefix + it.profileId.hexToNpubHrp() } + ")"
        }

    private fun Orientation?.toFilterQuery() =
        when (this) {
            Orientation.Any, null -> ""
            Orientation.Horizontal -> "orientation:horizontal"
            Orientation.Vertical -> "orientation:vertical"
        }
}
