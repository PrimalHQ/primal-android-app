package net.primal.android.explore.asearch

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

@HiltViewModel
class AdvancedSearchViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val DATE_TIME_FORMAT = "yyyy-MM-dd_HH:mm"
    }

    private val formatter = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT).withZone(ZoneId.systemDefault())

    private val _state = MutableStateFlow(UiState())
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
                uiState.includedWords,
                uiState.excludedWords?.let { "-$this" },
                uiState.searchKind.toSearchCommand(),
                uiState.postedBy.joinWithPrefix("from:"),
                uiState.replyingTo.joinWithPrefix("to:"),
                uiState.zappedBy.joinWithPrefix("zappedby:"),
                uiState.timePosted.toSearchCommand(),
                uiState.scope.toSearchCommand(),
                uiState.filter.toSearchCommand(),
                uiState.orderBy.toSearchCommand(),
            )

            val searchCommand = searchParams.filterNot { it.isNullOrEmpty() }.joinToString(separator = " ")

            setEffect(AdvancedSearchContract.SideEffect.NavigateToExploreFeed(searchCommand.buildFeedSpec()))
        }

    private fun String.buildFeedSpec(): String = "{\"id\":\"advsearch\",\"query\":\"$this\"}"

    private fun AdvancedSearchContract.SearchKind.toSearchCommand() =
        when (this) {
            AdvancedSearchContract.SearchKind.Notes -> "kind:1"
            AdvancedSearchContract.SearchKind.Reads -> "kind:30023"
            AdvancedSearchContract.SearchKind.Images -> "filter:image"
            AdvancedSearchContract.SearchKind.Videos -> "filter:video"
            AdvancedSearchContract.SearchKind.Sound -> "filter:audio"
            AdvancedSearchContract.SearchKind.NoteReplies -> "kind:1 filter:replies"
            AdvancedSearchContract.SearchKind.ReadsComments -> "kind:30023 filter:replies"
        }

    private fun AdvancedSearchContract.SearchOrderBy.toSearchCommand() =
        when (this) {
            AdvancedSearchContract.SearchOrderBy.Time -> ""
            AdvancedSearchContract.SearchOrderBy.ContentScore -> "orderby:score"
        }

    private fun AdvancedSearchContract.TimeModifier.toSearchCommand(): String =
        when (this) {
            AdvancedSearchContract.TimeModifier.Anytime -> ""
            AdvancedSearchContract.TimeModifier.Today ->
                "since:" + ZonedDateTime.now().minusDays(1).toInstant().toCommandFormattedString()

            AdvancedSearchContract.TimeModifier.Week ->
                "since:" + ZonedDateTime.now().minusDays(7).toInstant().toCommandFormattedString()

            AdvancedSearchContract.TimeModifier.Month ->
                "since:" + ZonedDateTime.now().minusMonths(1).toInstant().toCommandFormattedString()

            AdvancedSearchContract.TimeModifier.Year ->
                "since:" + ZonedDateTime.now().minusYears(1).toInstant().toCommandFormattedString()

            is AdvancedSearchContract.TimeModifier.Custom ->
                "since:${this.startDate.toCommandFormattedString()} before:${this.endDate.toCommandFormattedString()}"
        }

    private fun AdvancedSearchContract.SearchScope.toSearchCommand() =
        when (this) {
            AdvancedSearchContract.SearchScope.Global -> ""
            AdvancedSearchContract.SearchScope.MyFollows -> "scope:myfollows"
            AdvancedSearchContract.SearchScope.MyNetwork -> "scope:mynetwork"
            AdvancedSearchContract.SearchScope.MyFollowsInteractions -> "scope:myfollowinteractions"
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

    private fun Instant.toCommandFormattedString() = formatter.format(this)

    private fun Set<UserProfileItemUi>.joinWithPrefix(prefix: String) =
        this.joinToString(separator = " ") { prefix + it.profileId.hexToNpubHrp() }

    private fun Orientation?.toFilterQuery() =
        when (this) {
            Orientation.Any, null -> ""
            Orientation.Horizontal -> "orientation:horizontal"
            Orientation.Vertical -> "orientation:vertical"
        }
}
