package net.primal.android.explore.asearch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.aakira.napier.Napier
import java.time.Instant
import java.time.ZoneId
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
import net.primal.android.core.compose.profile.model.asUserProfileItemUi
import net.primal.android.explore.asearch.AdvancedSearchContract.Orientation
import net.primal.android.explore.asearch.AdvancedSearchContract.SearchFilter
import net.primal.android.explore.asearch.AdvancedSearchContract.UiEvent
import net.primal.android.explore.asearch.AdvancedSearchContract.UiState
import net.primal.android.explore.feed.ExploreFeedContract
import net.primal.android.navigation.advSearchScope
import net.primal.android.navigation.editingFeedSpec
import net.primal.android.navigation.initialQuery
import net.primal.android.navigation.postedBy
import net.primal.android.navigation.searchKind
import net.primal.domain.feeds.AdvancedSearchParsedQuery
import net.primal.domain.feeds.FeedsRepository
import net.primal.domain.feeds.extractAdvancedSearchQuery
import net.primal.domain.nostr.cryptography.utils.hexToNpubHrp
import net.primal.domain.profile.ProfileRepository

@HiltViewModel
class AdvancedSearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val profileRepository: ProfileRepository,
    private val feedsRepository: FeedsRepository,
) : ViewModel() {

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }

    private val dateFormatter = DateTimeFormatter.ofPattern(DATE_FORMAT).withZone(ZoneId.systemDefault())
    private val initialPostedBy = savedStateHandle.postedBy
    private val initialSearchKind = savedStateHandle.searchKind ?: AdvancedSearchContract.SearchKind.Notes
    private val initialSearchScope = savedStateHandle.advSearchScope ?: AdvancedSearchContract.SearchScope.Global
    private val editingFeedSpec = savedStateHandle.editingFeedSpec

    private val _state = MutableStateFlow(
        UiState(
            includedWords = savedStateHandle.initialQuery,
            searchKind = initialSearchKind,
            scope = initialSearchScope,
            editingFeedSpec = editingFeedSpec,
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
        fetchInitialPostedBy()
        if (editingFeedSpec != null) {
            parseEditingFeedSpec()
        }
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

    private fun fetchInitialPostedBy() =
        viewModelScope.launch {
            initialPostedBy?.let {
                val profiles = profileRepository.findProfileData(initialPostedBy)
                setState { copy(postedBy = profiles.map { it.asUserProfileItemUi() }.toSet()) }
            }
        }

    private fun parseEditingFeedSpec() =
        viewModelScope.launch {
            val query = editingFeedSpec?.extractAdvancedSearchQuery() ?: return@launch
            setState { copy(loading = true) }
            runCatching { feedsRepository.getAdvancedSearchQuery(query = query) }
                .mapCatching { parsed -> applyParsedQuery(parsed) }
                .onFailure {
                    Napier.w(throwable = it) { "Failed to parse advanced search query." }
                    setEffect(AdvancedSearchContract.SideEffect.NavigateBack)
                }
            setState { copy(loading = false) }
        }

    private suspend fun applyParsedQuery(parsed: AdvancedSearchParsedQuery) {
        val includedWords = buildString {
            if (parsed.includes.isNotEmpty()) append(parsed.includes)
            if (parsed.hashtags.isNotEmpty()) {
                val tags = parsed.hashtags.split(
                    "\\s+".toRegex(),
                ).filter { it.isNotBlank() }.joinToString(" ") { "#$it" }
                if (isNotEmpty()) append(" ")
                append(tags)
            }
        }.ifEmpty { null }

        setState {
            copy(
                includedWords = includedWords,
                excludedWords = parsed.excludes.ifEmpty { null },
                searchKind = parsed.kind.toSearchKind(),
                timePosted = parsed.toTimeModifier(),
                scope = parsed.scope.toSearchScope(),
                orderBy = parsed.sortBy.toSearchOrderBy(),
                filter = parsed.toSearchFilter(),
            )
        }

        val postedByProfiles = fetchProfiles(parsed.postedBy)
        val replyingToProfiles = fetchProfiles(parsed.replyingTo)
        val zappedByProfiles = fetchProfiles(parsed.zappedBy)

        setState {
            copy(
                postedBy = postedByProfiles,
                replyingTo = replyingToProfiles,
                zappedBy = zappedByProfiles,
            )
        }
    }

    private suspend fun fetchProfiles(pubkeys: List<String>): Set<UserProfileItemUi> {
        if (pubkeys.isEmpty()) return emptySet()
        return profileRepository.findProfileData(pubkeys)
            .map { it.asUserProfileItemUi() }
            .toSet()
    }

    private fun onSearch() =
        viewModelScope.launch {
            val uiState = state.value
            val searchParams = listOf(
                uiState.searchKind.toSearchCommand(),
                uiState.includedWords,
                uiState.excludedWords?.let { it.split(" ").joinToString(separator = " ") { "-$it" } },
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
                        editingFeedSpec = editingFeedSpec,
                    ),
                )
            } else {
                setEffect(
                    AdvancedSearchContract.SideEffect.NavigateToExploreNoteFeed(
                        feedSpec = searchCommand.buildFeedSpec(),
                        renderType = renderType,
                        editingFeedSpec = editingFeedSpec,
                    ),
                )
            }
        }

    private fun String.buildFeedSpec(): String = """{"id":"advsearch","query":"$this pas:1"}""".trimIndent()

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
            AdvancedSearchContract.TimeModifier.Today -> "since:yesterday"

            AdvancedSearchContract.TimeModifier.Week -> "since:lastweek"

            AdvancedSearchContract.TimeModifier.Month -> "since:lastmonth"

            AdvancedSearchContract.TimeModifier.Year -> "since:lastyear"

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
            AdvancedSearchContract.SearchScope.MyNotifications -> "scope:mynotifications"
        }

    private fun SearchFilter.toSearchCommand() =
        if (this.isEmpty()) {
            ""
        } else {
            val stringFilters = arrayOf(
                this.orientation.toFilterQuery(),
                this.minReadTime.times(other = 238).toFilterQueryOrEmpty("minwords", delimiter = ":"),
                this.maxReadTime.times(other = 238).toFilterQueryOrEmpty("maxwords", delimiter = ":"),
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

    private fun String.toSearchKind(): AdvancedSearchContract.SearchKind =
        when (this) {
            "Notes" -> AdvancedSearchContract.SearchKind.Notes
            "Reads" -> AdvancedSearchContract.SearchKind.Reads
            "Images" -> AdvancedSearchContract.SearchKind.Images
            "Videos" -> AdvancedSearchContract.SearchKind.Videos
            "Sound" -> AdvancedSearchContract.SearchKind.Sound
            "NoteReplies" -> AdvancedSearchContract.SearchKind.NoteReplies
            "ReadsComments" -> AdvancedSearchContract.SearchKind.ReadsComments
            else -> AdvancedSearchContract.SearchKind.Notes
        }

    private fun String.toSearchScope(): AdvancedSearchContract.SearchScope =
        when (this) {
            "Global" -> AdvancedSearchContract.SearchScope.Global
            "MyFollows" -> AdvancedSearchContract.SearchScope.MyFollows
            "MyNetwork" -> AdvancedSearchContract.SearchScope.MyNetwork
            "MyNotifications" -> AdvancedSearchContract.SearchScope.MyNotifications
            "MyFollowsInteractions" -> AdvancedSearchContract.SearchScope.MyFollowsInteractions
            "MyNetworkInteractions" -> AdvancedSearchContract.SearchScope.MyNetworkInteractions
            "NotMyFollows" -> AdvancedSearchContract.SearchScope.NotMyFollows
            else -> AdvancedSearchContract.SearchScope.Global
        }

    private fun String.toSearchOrderBy(): AdvancedSearchContract.SearchOrderBy =
        when (this) {
            "Time" -> AdvancedSearchContract.SearchOrderBy.Time
            "ContentScore" -> AdvancedSearchContract.SearchOrderBy.ContentScore
            "Replies" -> AdvancedSearchContract.SearchOrderBy.Replies
            "SatsZapped" -> AdvancedSearchContract.SearchOrderBy.SatsZapped
            "Interactions" -> AdvancedSearchContract.SearchOrderBy.Interactions
            else -> AdvancedSearchContract.SearchOrderBy.Time
        }

    private fun AdvancedSearchParsedQuery.toTimeModifier(): AdvancedSearchContract.TimeModifier {
        if (customTimeframeSince.isNotEmpty() && customTimeframeUntil.isNotEmpty()) {
            val since = runCatching { Instant.parse(customTimeframeSince) }.getOrNull()
            val until = runCatching { Instant.parse(customTimeframeUntil) }.getOrNull()
            if (since != null && until != null) {
                return AdvancedSearchContract.TimeModifier.Custom(startDate = since, endDate = until)
            }
        }
        return when (timeframe) {
            "Anytime" -> AdvancedSearchContract.TimeModifier.Anytime
            "Today" -> AdvancedSearchContract.TimeModifier.Today
            "Week" -> AdvancedSearchContract.TimeModifier.Week
            "Month" -> AdvancedSearchContract.TimeModifier.Month
            "Year" -> AdvancedSearchContract.TimeModifier.Year
            else -> AdvancedSearchContract.TimeModifier.Anytime
        }
    }

    private fun AdvancedSearchParsedQuery.toSearchFilter(): SearchFilter =
        SearchFilter(
            orientation = when (orientation) {
                "Horizontal" -> Orientation.Horizontal
                "Vertical" -> Orientation.Vertical
                else -> null
            },
            minReadTime = if (minWords > 0) (minWords + 237) / 238 else 0,
            maxReadTime = if (maxWords > 0) (maxWords + 237) / 238 else 0,
            minDuration = minDuration,
            maxDuration = maxDuration,
            minContentScore = minScore,
            minInteractions = minInteractions,
            minLikes = minLikes,
            minZaps = minZaps,
            minReplies = minReplies,
            minReposts = minReposts,
        )
}
