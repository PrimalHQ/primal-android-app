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
                onClick = {
                    eventPublisher(AdvancedSearchContract.UiEvent.OnSearch)
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            PrimalIconTextField(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                value = state.includedWords.orEmpty(),
                onValueChange = { eventPublisher(AdvancedSearchContract.UiEvent.IncludedWordsValueChanged(it)) },
                placeholderText = stringResource(id = R.string.asearch_included_words_placeholder),
                iconImageVector = Icons.Filled.Search,
                focusRequester = null,
                singleLine = true,
            )
            PrimalIconTextField(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                value = state.excludedWords.orEmpty(),
                onValueChange = { eventPublisher(AdvancedSearchContract.UiEvent.ExcludedWordsValueChanged(it)) },
                placeholderText = stringResource(id = R.string.asearch_excluded_words_placeholder),
                iconImageVector = Icons.Filled.Close,
                focusRequester = null,
                singleLine = true,
            )
            Column {
                SearchKindPicker(
                    searchKind = state.searchKind,
                    onSearchKindChanged = { eventPublisher(AdvancedSearchContract.UiEvent.SearchKindChanged(it)) },
                )

                MultipleUserPickerOptionListItem(
                    labelText = stringResource(id = R.string.asearch_posted_by_label),
                    onUsersSelected = {
                        eventPublisher(AdvancedSearchContract.UiEvent.PostedBySelectUsers(it))
                    },
                    placeholderText = stringResource(R.string.asearch_posted_by_placeholder),
                    selectedUsers = state.postedBy,
                    sheetTitle = stringResource(id = R.string.asearch_posted_by_label),
                )

                MultipleUserPickerOptionListItem(
                    labelText = stringResource(id = R.string.asearch_replying_to_label),
                    onUsersSelected = {
                        eventPublisher(AdvancedSearchContract.UiEvent.ReplyingToSelectUsers(it))
                    },
                    placeholderText = stringResource(R.string.asearch_replying_to_placeholder),
                    selectedUsers = state.replyingTo,
                    sheetTitle = stringResource(id = R.string.asearch_replying_to_label),
                )

                MultipleUserPickerOptionListItem(
                    labelText = stringResource(id = R.string.asearch_zapped_by_label),
                    onUsersSelected = {
                        eventPublisher(AdvancedSearchContract.UiEvent.ZappedBySelectUsers(it))
                    },
                    placeholderText = stringResource(R.string.asearch_zapped_by_placeholder),
                    selectedUsers = state.zappedBy,
                    sheetTitle = stringResource(id = R.string.asearch_zapped_by_label),
                )

                var showTimePostedBottomSheetPicker by rememberSaveable { mutableStateOf(false) }
                OptionListWithBottomSheetItem(
                    label = stringResource(id = R.string.asearch_time_posted_label),
                    onClick = { showTimePostedBottomSheetPicker = true },
                    selectedContent = {
                        Text(
                            text = state.timePosted.toDisplayName(),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        )
                    },
                    bottomSheet = {
                        TimeModifierPicker(
                            titleText = stringResource(id = R.string.asearch_time_posted_label),
                            onDismissRequest = { showTimePostedBottomSheetPicker = false },
                            onItemSelected = {
                                eventPublisher(AdvancedSearchContract.UiEvent.TimePostedChanged(it))
                            },
                            selectedItem = state.timePosted,
                        )
                    },
                    isBottomSheetVisible = showTimePostedBottomSheetPicker,
                )
                var showScopeBottomSheetPicker by rememberSaveable { mutableStateOf(false) }
                OptionListWithBottomSheetItem(
                    label = stringResource(id = R.string.asearch_scope_label),
                    onClick = { showScopeBottomSheetPicker = true },
                    selectedContent = {
                        Text(
                            text = state.scope.toDisplayName(),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        )
                    },
                    bottomSheet = {
                        SingleChoicePicker(
                            items = AdvancedSearchContract.SearchScope.entries,
                            itemDisplayName = { toDisplayName() },
                            onDismissRequest = { showScopeBottomSheetPicker = false },
                            onItemSelected = { eventPublisher(AdvancedSearchContract.UiEvent.ScopeChanged(it)) },
                            titleText = stringResource(id = R.string.asearch_scope_label),
                            selectedItem = state.scope,
                        )
                    },
                    isBottomSheetVisible = showScopeBottomSheetPicker,
                )
                var showSearchFilterBottomSheetPicker by rememberSaveable { mutableStateOf(false) }
                OptionListWithBottomSheetItem(
                    label = stringResource(id = R.string.asearch_filter_label),
                    onClick = { showSearchFilterBottomSheetPicker = true },
                    selectedContent = {
                        Text(
                            text = state.filter.toDisplayName(),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        )
                    },
                    bottomSheet = {
                        FilterPicker(
                            filterSelected = {
                                eventPublisher(AdvancedSearchContract.UiEvent.SearchFilterChanged(it))
                            },
                            onDismissRequest = { showSearchFilterBottomSheetPicker = false },
                            searchKind = state.searchKind,
                            startState = state.filter,
                        )
                    },
                    isBottomSheetVisible = showSearchFilterBottomSheetPicker,
                )
                var showOrderByBottomSheetPicker by rememberSaveable { mutableStateOf(false) }
                OptionListWithBottomSheetItem(
                    label = stringResource(id = R.string.asearch_order_by_label),
                    onClick = { showOrderByBottomSheetPicker = true },
                    selectedContent = {
                        Text(
                            text = state.orderBy.toDisplayName(),
                            style = AppTheme.typography.bodyMedium,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        )
                    },
                    bottomSheet = {
                        SingleChoicePicker(
                            items = AdvancedSearchContract.SearchOrderBy.entries,
                            itemDisplayName = { toDisplayName() },
                            onDismissRequest = { showOrderByBottomSheetPicker = false },
                            onItemSelected = { eventPublisher(AdvancedSearchContract.UiEvent.OrderByChanged(it)) },
                            titleText = stringResource(id = R.string.asearch_order_by_label),
                            selectedItem = state.orderBy,
                        )
                    },
                    isBottomSheetVisible = showOrderByBottomSheetPicker,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchKindPicker(searchKind: SearchKind, onSearchKindChanged: (kind: SearchKind) -> Unit) {
    var showSearchBottomSheetPicker by rememberSaveable { mutableStateOf(false) }
    OptionListWithBottomSheetItem(
        label = stringResource(id = R.string.asearch_search_kind_label),
        onClick = { showSearchBottomSheetPicker = true },
        selectedContent = {
            Text(
                text = searchKind.toDisplayName(),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        },
        bottomSheet = {
            SingleChoicePicker(
                items = AdvancedSearchContract.SearchKind.entries,
                itemDisplayName = { toDisplayName() },
                onDismissRequest = { showSearchBottomSheetPicker = false },
                onItemSelected = onSearchKindChanged,
                titleText = stringResource(id = R.string.asearch_search_kind_label),
                selectedItem = searchKind,
            )
        },
        isBottomSheetVisible = showSearchBottomSheetPicker,
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
    OptionListWithBottomSheetItem(
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
        bottomSheet = {
            MultipleUserPicker(
                onDismissRequest = { bottomSheetVisibility = false },
                onUsersSelected = onUsersSelected,
                placeholderText = placeholderText,
                sheetTitle = sheetTitle,
                startingSelectedUsers = selectedUsers,
            )
        },
        isBottomSheetVisible = bottomSheetVisibility,
    )
}

@Composable
private fun OptionListWithBottomSheetItem(
    modifier: Modifier = Modifier,
    label: String,
    onClick: () -> Unit,
    selectedContent: @Composable (RowScope.() -> Unit),
    isBottomSheetVisible: Boolean,
    bottomSheet: @Composable () -> Unit,
) {
    if (isBottomSheetVisible) {
        bottomSheet()
    }

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
