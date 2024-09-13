package net.primal.android.explore.asearch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.format.FormatStyle
import net.primal.android.R
import net.primal.android.core.compose.AvatarOverlap
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.PrimalIconTextField
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.core.utils.formatToDefaultDateFormat
import net.primal.android.explore.asearch.AdvancedSearchContract.Orientation
import net.primal.android.explore.asearch.AdvancedSearchContract.SearchFilter
import net.primal.android.explore.asearch.AdvancedSearchContract.SearchKind
import net.primal.android.explore.asearch.AdvancedSearchContract.SearchOrderBy
import net.primal.android.explore.asearch.AdvancedSearchContract.SearchScope
import net.primal.android.explore.asearch.AdvancedSearchContract.TimeModifier
import net.primal.android.explore.asearch.AdvancedSearchContract.UiEvent
import net.primal.android.explore.asearch.AdvancedSearchContract.UiState
import net.primal.android.explore.asearch.ui.FilterPicker
import net.primal.android.explore.asearch.ui.MultipleUserPicker
import net.primal.android.explore.asearch.ui.SingleChoicePicker
import net.primal.android.explore.asearch.ui.TimeModifierPicker
import net.primal.android.theme.AppTheme

@Composable
fun AdvancedSearchScreen(viewModel: AdvancedSearchViewModel, onClose: () -> Unit) {
    val state = viewModel.state.collectAsState()

    AdvancedSearchScreen(
        state = state.value,
        eventPublisher = viewModel::setEvent,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvancedSearchScreen(
    state: UiState,
    eventPublisher: (UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.asearch_top_app_bar_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                showDivider = false,
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier.background(AppTheme.colorScheme.background),
            ) {
                PrimalLoadingButton(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .fillMaxWidth(),
                    text = stringResource(id = R.string.asearch_search_button),
                    onClick = { eventPublisher(UiEvent.OnSearch) },
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            IncludedWordsTextField(
                includedWords = state.includedWords,
                onValueChange = { eventPublisher(UiEvent.IncludedWordsValueChanged(it)) },
            )
            ExcludedWordsTextField(
                excludedWords = state.excludedWords,
                onValueChange = { eventPublisher(UiEvent.ExcludedWordsValueChanged(it)) },
            )
            Column {
                SearchKindPicker(
                    searchKind = state.searchKind,
                    onSearchKindChanged = { eventPublisher(UiEvent.SearchKindChanged(it)) },
                )

                PostedByPicker(
                    postedBy = state.postedBy,
                    onUsersSelected = { eventPublisher(UiEvent.PostedBySelectUsers(it)) },
                )

                ReplyingToPicker(
                    replyingTo = state.replyingTo,
                    onUsersSelected = { eventPublisher(UiEvent.ReplyingToSelectUsers(it)) },
                )

                ZappedByPicker(
                    zappedBy = state.zappedBy,
                    onUsersSelected = { eventPublisher(UiEvent.ZappedBySelectUsers(it)) },
                )

                TimePostedPicker(
                    timePosted = state.timePosted,
                    onTimePostedChanged = { eventPublisher(UiEvent.TimePostedChanged(it)) },
                )

                SearchScopePicker(
                    scope = state.scope,
                    onScopeChanged = { eventPublisher(UiEvent.ScopeChanged(it)) },
                )

                SearchFilterPicker(
                    searchFilter = state.filter,
                    searchKind = state.searchKind,
                    onFilterChanged = { eventPublisher(UiEvent.SearchFilterChanged(it)) },
                )
                OrderByPicker(
                    orderBy = state.orderBy,
                    onOrderByChanged = { eventPublisher(UiEvent.OrderByChanged(it)) },
                )
            }
        }
    }
}

@Composable
private fun ExcludedWordsTextField(excludedWords: String?, onValueChange: (String) -> Unit) {
    PrimalIconTextField(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
        value = excludedWords.orEmpty(),
        onValueChange = onValueChange,
        placeholderText = stringResource(id = R.string.asearch_excluded_words_placeholder),
        iconImageVector = Icons.Filled.Close,
        focusRequester = null,
        singleLine = true,
    )
}

@Composable
private fun IncludedWordsTextField(includedWords: String?, onValueChange: (String) -> Unit) {
    PrimalIconTextField(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .fillMaxWidth(),
        value = includedWords.orEmpty(),
        onValueChange = onValueChange,
        placeholderText = stringResource(id = R.string.asearch_included_words_placeholder),
        iconImageVector = Icons.Filled.Search,
        focusRequester = null,
        singleLine = true,
    )
}

@Composable
private fun ZappedByPicker(zappedBy: Set<UserProfileItemUi>, onUsersSelected: (Set<UserProfileItemUi>) -> Unit) {
    MultipleUserPickerOptionListItem(
        labelText = stringResource(id = R.string.asearch_zapped_by_label),
        onUsersSelected = onUsersSelected,
        placeholderText = stringResource(R.string.asearch_zapped_by_placeholder),
        selectedUsers = zappedBy,
        sheetTitle = stringResource(id = R.string.asearch_zapped_by_label),
    )
}

@Composable
private fun ReplyingToPicker(replyingTo: Set<UserProfileItemUi>, onUsersSelected: (Set<UserProfileItemUi>) -> Unit) {
    MultipleUserPickerOptionListItem(
        labelText = stringResource(id = R.string.asearch_replying_to_label),
        onUsersSelected = onUsersSelected,
        placeholderText = stringResource(R.string.asearch_replying_to_placeholder),
        selectedUsers = replyingTo,
        sheetTitle = stringResource(id = R.string.asearch_replying_to_label),
    )
}

@Composable
private fun PostedByPicker(postedBy: Set<UserProfileItemUi>, onUsersSelected: (Set<UserProfileItemUi>) -> Unit) {
    MultipleUserPickerOptionListItem(
        labelText = stringResource(id = R.string.asearch_posted_by_label),
        onUsersSelected = onUsersSelected,
        placeholderText = stringResource(R.string.asearch_posted_by_placeholder),
        selectedUsers = postedBy,
        sheetTitle = stringResource(id = R.string.asearch_posted_by_label),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderByPicker(orderBy: SearchOrderBy, onOrderByChanged: (SearchOrderBy) -> Unit) {
    var showOrderByBottomSheetPicker by rememberSaveable { mutableStateOf(false) }
    if (showOrderByBottomSheetPicker) {
        SingleChoicePicker(
            items = SearchOrderBy.entries,
            itemDisplayName = { toDisplayName() },
            onDismissRequest = { showOrderByBottomSheetPicker = false },
            onItemSelected = onOrderByChanged,
            titleText = stringResource(id = R.string.asearch_order_by_label),
            selectedItem = orderBy,
        )
    }
    OptionListItem(
        label = stringResource(id = R.string.asearch_order_by_label),
        onClick = { showOrderByBottomSheetPicker = true },
        selectedContent = {
            Text(
                text = orderBy.toDisplayName(),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchFilterPicker(
    searchFilter: SearchFilter,
    searchKind: SearchKind,
    onFilterChanged: (SearchFilter) -> Unit,
) {
    var showSearchFilterBottomSheetPicker by rememberSaveable { mutableStateOf(false) }
    if (showSearchFilterBottomSheetPicker) {
        FilterPicker(
            filterSelected = onFilterChanged,
            onDismissRequest = { showSearchFilterBottomSheetPicker = false },
            searchKind = searchKind,
            startState = searchFilter,
        )
    }
    OptionListItem(
        label = stringResource(id = R.string.asearch_filter_label),
        onClick = { showSearchFilterBottomSheetPicker = true },
        selectedContent = {
            Text(
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1.0f),
                text = searchFilter.toDisplayName(),
                style = AppTheme.typography.bodyMedium,
                color = if (searchFilter.isEmpty()) {
                    AppTheme.extraColorScheme.onSurfaceVariantAlt1
                } else {
                    AppTheme.colorScheme.secondary
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScopePicker(scope: SearchScope, onScopeChanged: (SearchScope) -> Unit) {
    var showScopeBottomSheetPicker by rememberSaveable { mutableStateOf(false) }
    if (showScopeBottomSheetPicker) {
        SingleChoicePicker(
            items = SearchScope.entries,
            itemDisplayName = { toDisplayName() },
            onDismissRequest = { showScopeBottomSheetPicker = false },
            onItemSelected = onScopeChanged,
            titleText = stringResource(id = R.string.asearch_scope_label),
            selectedItem = scope,
        )
    }
    OptionListItem(
        label = stringResource(id = R.string.asearch_scope_label),
        onClick = { showScopeBottomSheetPicker = true },
        selectedContent = {
            Text(
                text = scope.toDisplayName(),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchKindPicker(searchKind: SearchKind, onSearchKindChanged: (kind: SearchKind) -> Unit) {
    var showSearchBottomSheetPicker by rememberSaveable { mutableStateOf(false) }

    if (showSearchBottomSheetPicker) {
        SingleChoicePicker(
            items = SearchKind.entries,
            itemDisplayName = { toDisplayName() },
            onDismissRequest = { showSearchBottomSheetPicker = false },
            onItemSelected = onSearchKindChanged,
            titleText = stringResource(id = R.string.asearch_search_kind_label),
            selectedItem = searchKind,
        )
    }

    OptionListItem(
        label = stringResource(id = R.string.asearch_search_kind_label),
        onClick = { showSearchBottomSheetPicker = true },
        selectedContent = {
            Text(
                text = searchKind.toDisplayName(),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePostedPicker(timePosted: TimeModifier, onTimePostedChanged: (TimeModifier) -> Unit) {
    var showTimePostedBottomSheetPicker by rememberSaveable { mutableStateOf(false) }
    if (showTimePostedBottomSheetPicker) {
        TimeModifierPicker(
            onDismissRequest = { showTimePostedBottomSheetPicker = false },
            onItemSelected = onTimePostedChanged,
            selectedItem = timePosted,
        )
    }
    OptionListItem(
        label = stringResource(id = R.string.asearch_time_posted_label),
        onClick = { showTimePostedBottomSheetPicker = true },
        selectedContent = {
            if (timePosted is TimeModifier.Custom) {
                Text(
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(1.0f),
                    text = "${
                        timePosted.startDate.formatToDefaultDateFormat(
                            FormatStyle.MEDIUM,
                        )
                    } - ${timePosted.endDate.formatToDefaultDateFormat(FormatStyle.MEDIUM)}",
                    color = AppTheme.colorScheme.secondary,
                    style = AppTheme.typography.bodyMedium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                )
            } else {
                Text(
                    text = timePosted.toDisplayName(),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultipleUserPickerOptionListItem(
    labelText: String,
    placeholderText: String,
    sheetTitle: String,
    selectedUsers: Set<UserProfileItemUi>,
    onUsersSelected: (Set<UserProfileItemUi>) -> Unit,
) {
    var bottomSheetVisibility by rememberSaveable { mutableStateOf(false) }
    if (bottomSheetVisibility) {
        MultipleUserPicker(
            onDismissRequest = { bottomSheetVisibility = false },
            onUsersSelected = onUsersSelected,
            placeholderText = placeholderText,
            sheetTitle = sheetTitle,
            startingSelectedUsers = selectedUsers,
        )
    }
    OptionListItem(
        label = labelText,
        onClick = { bottomSheetVisibility = true },
        selectedContent = {
            if (selectedUsers.isEmpty()) {
                Text(
                    text = stringResource(id = R.string.asearch_multiselect_anyone),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                )
            } else {
                AvatarThumbnailsRow(
                    avatarCdnImages = selectedUsers.map { it.avatarCdnImage },
                    onClick = {},
                    avatarOverlap = AvatarOverlap.None,
                    maxAvatarsToShow = 4,
                    avatarBorderColor = Color.Transparent,
                )
            }
        },
    )
}

@Composable
private fun OptionListItem(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
    selectedContent: @Composable (RowScope.() -> Unit),
) {
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = label,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    style = AppTheme.typography.bodyMedium,
                    maxLines = 1,
                )
                Row(
                    modifier = Modifier
                        .weight(1.0f)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                ) {
                    selectedContent()
                    Icon(
                        tint = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        modifier = Modifier.size(12.dp),
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                    )
                }
            }
        },
    )
}

@Composable
private fun SearchKind.toDisplayName(): String =
    when (this) {
        SearchKind.Notes -> stringResource(id = R.string.asearch_search_kind_notes)
        SearchKind.Reads -> stringResource(id = R.string.asearch_search_kind_reads)
        SearchKind.Images -> stringResource(id = R.string.asearch_search_kind_images)
        SearchKind.Videos -> stringResource(id = R.string.asearch_search_kind_videos)
        SearchKind.Sound -> stringResource(id = R.string.asearch_search_kind_sound)
        SearchKind.NoteReplies -> stringResource(id = R.string.asearch_search_kind_note_replies)
        SearchKind.ReadsComments -> stringResource(
            id = R.string.asearch_search_kind_reads_comments,
        )
    }

@Composable
fun TimeModifier.toDisplayName(): String =
    when (this) {
        TimeModifier.Anytime -> stringResource(id = R.string.asearch_time_posted_anytime)
        TimeModifier.Today -> stringResource(id = R.string.asearch_time_posted_today)
        TimeModifier.Week -> stringResource(id = R.string.asearch_time_posted_week)
        TimeModifier.Month -> stringResource(id = R.string.asearch_time_posted_month)
        TimeModifier.Year -> stringResource(id = R.string.asearch_time_posted_year)
        is TimeModifier.Custom -> stringResource(id = R.string.asearch_time_posted_custom)
    }

@Composable
private fun SearchScope.toDisplayName(): String =
    when (this) {
        SearchScope.Global -> stringResource(id = R.string.asearch_search_scope_global)
        SearchScope.MyFollows -> stringResource(id = R.string.asearch_search_scope_my_follows)
        SearchScope.MyNetwork -> stringResource(id = R.string.asearch_search_scope_my_network)
        SearchScope.MyFollowsInteractions -> stringResource(
            id = R.string.asearch_search_scope_my_follows_interactions,
        )
    }

@Composable
fun SearchFilter.toDisplayName(): String =
    if (this.isEmpty()) {
        stringResource(id = R.string.asearch_filters_none)
    } else {
        val stringFilters = arrayOf(
            this.orientation.toDisplayName(),
            this.minReadTime.toFilterQueryOrEmpty(stringResource(id = R.string.asearch_filter_min_read_time_shorthand)),
            this.maxReadTime.toFilterQueryOrEmpty(stringResource(id = R.string.asearch_filter_max_read_time_shorthand)),
            this.minDuration.toFilterQueryOrEmpty(stringResource(id = R.string.asearch_filter_min_duration_shorthand)),
            this.maxDuration.toFilterQueryOrEmpty(stringResource(id = R.string.asearch_filter_max_duration_shorthand)),
            this.minContentScore.toFilterQueryOrEmpty(stringResource(id = R.string.asearch_filter_min_content_score)),
            this.minInteractions.toFilterQueryOrEmpty(stringResource(id = R.string.asearch_filter_min_interactions)),
            this.minLikes.toFilterQueryOrEmpty(stringResource(id = R.string.asearch_filter_min_likes)),
            this.minZaps.toFilterQueryOrEmpty(stringResource(id = R.string.asearch_filter_min_zaps)),
            this.minReplies.toFilterQueryOrEmpty(stringResource(id = R.string.asearch_filter_min_replies)),
            this.minReposts.toFilterQueryOrEmpty(stringResource(id = R.string.asearch_filter_min_reposts)),
        )

        stringFilters.filter { it.isNotEmpty() }.joinToString("; ")
    }

@Composable
private fun SearchOrderBy.toDisplayName(): String =
    when (this) {
        SearchOrderBy.Time -> stringResource(id = R.string.asearch_search_order_by_time)
        SearchOrderBy.ContentScore -> stringResource(
            id = R.string.asearch_search_order_by_content_score,
        )
    }

@Composable
private fun Orientation?.toDisplayName(): String =
    when (this) {
        null, Orientation.Any -> ""
        Orientation.Horizontal ->
            "${
                stringResource(
                    id = R.string.asearch_filter_orientation,
                )
            }=${stringResource(id = R.string.asearch_filter_orientation_horizontal)}"

        Orientation.Vertical ->
            "${
                stringResource(
                    id = R.string.asearch_filter_orientation,
                )
            }=${stringResource(id = R.string.asearch_filter_orientation_vertical)}"
    }

fun Int.toFilterQueryOrEmpty(queryProperty: String, delimiter: String = "="): String =
    this.run { if (this != 0) "$queryProperty$delimiter$this" else "" }

fun SearchFilter.isEmpty(): Boolean =
    (this.orientation == null || this.orientation == Orientation.Any) &&
        this.minReadTime == 0 &&
        this.maxReadTime == 0 &&
        this.minDuration == 0 &&
        this.maxDuration == 0 &&
        this.minContentScore == 0 &&
        this.minInteractions == 0 &&
        this.minLikes == 0 &&
        this.minZaps == 0 &&
        this.minReplies == 0 &&
        this.minReposts == 0
