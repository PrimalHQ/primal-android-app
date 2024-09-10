package net.primal.android.explore.asearch

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailsRow
import net.primal.android.core.compose.PrimalIconTextField
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.explore.asearch.AdvancedSearchContract.SearchKind
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
    state: AdvancedSearchContract.UiState,
    eventPublisher: (AdvancedSearchContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.asearch_top_app_bar_title),
                textColor = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                showDivider = false,
            )
        },
        bottomBar = {
            PrimalLoadingButton(
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                text = stringResource(id = R.string.asearch_search_button),
                onClick = { eventPublisher(AdvancedSearchContract.UiEvent.OnSearch) },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            IncludedWordsTextField(
                includedWords = state.includedWords,
                onValueChange = { eventPublisher(AdvancedSearchContract.UiEvent.IncludedWordsValueChanged(it)) },
            )
            ExcludedWordsTextField(
                excludedWords = state.excludedWords,
                onValueChange = { eventPublisher(AdvancedSearchContract.UiEvent.ExcludedWordsValueChanged(it)) },
            )
            Column {
                SearchKindPicker(
                    searchKind = state.searchKind,
                    onSearchKindChanged = { eventPublisher(AdvancedSearchContract.UiEvent.SearchKindChanged(it)) },
                )

                PostedByPicker(
                    postedBy = state.postedBy,
                    onUsersSelected = { eventPublisher(AdvancedSearchContract.UiEvent.PostedBySelectUsers(it)) },
                )

                ReplyingToPicker(
                    replyingTo = state.replyingTo,
                    onUsersSelected = { eventPublisher(AdvancedSearchContract.UiEvent.ReplyingToSelectUsers(it)) },
                )

                ZappedByPicker(
                    zappedBy = state.zappedBy,
                    onUsersSelected = { eventPublisher(AdvancedSearchContract.UiEvent.ZappedBySelectUsers(it)) },
                )

                TimePostedPicker(
                    timePosted = state.timePosted,
                    onTimePostedChanged = { eventPublisher(AdvancedSearchContract.UiEvent.TimePostedChanged(it)) },
                )

                SearchScopePicker(
                    scope = state.scope,
                    onScopeChanged = { eventPublisher(AdvancedSearchContract.UiEvent.ScopeChanged(it)) },
                )

                SearchFilterPicker(
                    searchFilter = state.filter,
                    searchKind = state.searchKind,
                    onFilterChanged = { eventPublisher(AdvancedSearchContract.UiEvent.SearchFilterChanged(it)) },
                )
                OrderByPicker(
                    orderBy = state.orderBy,
                    onOrderByChanged = { eventPublisher(AdvancedSearchContract.UiEvent.OrderByChanged(it)) },
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
private fun OrderByPicker(
    orderBy: AdvancedSearchContract.SearchOrderBy,
    onOrderByChanged: (AdvancedSearchContract.SearchOrderBy) -> Unit,
) {
    var showOrderByBottomSheetPicker by rememberSaveable { mutableStateOf(false) }
    if (showOrderByBottomSheetPicker) {
        SingleChoicePicker(
            items = AdvancedSearchContract.SearchOrderBy.entries,
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
    searchFilter: AdvancedSearchContract.SearchFilter,
    searchKind: SearchKind,
    onFilterChanged: (AdvancedSearchContract.SearchFilter) -> Unit,
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
                text = searchFilter.toDisplayName(),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScopePicker(
    scope: AdvancedSearchContract.SearchScope,
    onScopeChanged: (AdvancedSearchContract.SearchScope) -> Unit,
) {
    var showScopeBottomSheetPicker by rememberSaveable { mutableStateOf(false) }
    if (showScopeBottomSheetPicker) {
        SingleChoicePicker(
            items = AdvancedSearchContract.SearchScope.entries,
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
            items = AdvancedSearchContract.SearchKind.entries,
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
fun TimePostedPicker(
    timePosted: AdvancedSearchContract.TimeModifier,
    onTimePostedChanged: (AdvancedSearchContract.TimeModifier) -> Unit,
) {
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
            Text(
                text = timePosted.toDisplayName(),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
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
                    overlapAvatars = false,
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
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = label,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                style = AppTheme.typography.bodyMedium,
            )
        },
        trailingContent = {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp),
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
        },
    )
}

@Composable
private fun AdvancedSearchContract.SearchKind.toDisplayName(): String =
    when (this) {
        AdvancedSearchContract.SearchKind.Notes -> stringResource(id = R.string.asearch_search_kind_notes)
        AdvancedSearchContract.SearchKind.Reads -> stringResource(id = R.string.asearch_search_kind_reads)
        AdvancedSearchContract.SearchKind.Images -> stringResource(id = R.string.asearch_search_kind_images)
        AdvancedSearchContract.SearchKind.Videos -> stringResource(id = R.string.asearch_search_kind_videos)
        AdvancedSearchContract.SearchKind.Sound -> stringResource(id = R.string.asearch_search_kind_sound)
        AdvancedSearchContract.SearchKind.NoteReplies -> stringResource(id = R.string.asearch_search_kind_note_replies)
        AdvancedSearchContract.SearchKind.ReadsComments -> stringResource(
            id = R.string.asearch_search_kind_reads_comments,
        )
    }

@Composable
fun AdvancedSearchContract.TimeModifier.toDisplayName(): String =
    when (this) {
        AdvancedSearchContract.TimeModifier.Anytime -> stringResource(id = R.string.asearch_time_posted_anytime)
        AdvancedSearchContract.TimeModifier.Today -> stringResource(id = R.string.asearch_time_posted_today)
        AdvancedSearchContract.TimeModifier.Week -> stringResource(id = R.string.asearch_time_posted_week)
        AdvancedSearchContract.TimeModifier.Month -> stringResource(id = R.string.asearch_time_posted_month)
        AdvancedSearchContract.TimeModifier.Year -> stringResource(id = R.string.asearch_time_posted_year)
        is AdvancedSearchContract.TimeModifier.Custom -> stringResource(id = R.string.asearch_time_posted_custom)
    }

@Composable
private fun AdvancedSearchContract.SearchScope.toDisplayName(): String =
    when (this) {
        AdvancedSearchContract.SearchScope.Global -> stringResource(id = R.string.asearch_search_scope_global)
        AdvancedSearchContract.SearchScope.MyFollows -> stringResource(id = R.string.asearch_search_scope_my_follows)
        AdvancedSearchContract.SearchScope.MyNetwork -> stringResource(id = R.string.asearch_search_scope_my_network)
        AdvancedSearchContract.SearchScope.MyFollowsInteractions -> stringResource(
            id = R.string.asearch_search_scope_my_follows_interactions,
        )
    }

@Composable
fun AdvancedSearchContract.SearchFilter.toDisplayName(): String =
    if (this == AdvancedSearchContract.SearchFilter()) {
        stringResource(id = R.string.asearch_filters_none)
    } else {
        stringResource(id = R.string.asearch_filters_custom)
    }

@Composable
private fun AdvancedSearchContract.SearchOrderBy.toDisplayName(): String =
    when (this) {
        AdvancedSearchContract.SearchOrderBy.Time -> stringResource(id = R.string.asearch_search_order_by_time)
        AdvancedSearchContract.SearchOrderBy.ContentScore -> stringResource(
            id = R.string.asearch_search_order_by_content_score,
        )
    }
