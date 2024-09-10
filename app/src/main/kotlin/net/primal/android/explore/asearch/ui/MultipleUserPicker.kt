package net.primal.android.explore.asearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalIconTextField
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.explore.search.SearchContract
import net.primal.android.explore.search.SearchViewModel
import net.primal.android.explore.search.ui.UserProfileListItem
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultipleUserPicker(
    modifier: Modifier = Modifier,
    sheetTitle: String,
    placeholderText: String,
    onDismissRequest: () -> Unit,
    onUsersSelected: (Set<UserProfileItemUi>) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    startingSelectedUsers: Set<UserProfileItemUi>,
) {
    val scope = rememberCoroutineScope()
    val viewModel = hiltViewModel<SearchViewModel>()
    val state = viewModel.state.collectAsState()
    val lazyListState = rememberLazyListState()

    var selectedUsers: Set<UserProfileItemUi> by remember { mutableStateOf(startingSelectedUsers.toSet()) }

    ModalBottomSheet(
        tonalElevation = 0.dp,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                MultipleUserPickerTopAppBar(
                    sheetTitle = sheetTitle,
                    onDismissRequest = onDismissRequest,
                    lazyListState = lazyListState,
                    selectedUsers = selectedUsers,
                    onUserClick = { user -> selectedUsers = selectedUsers - user },
                )
            },
            bottomBar = {
                MultipleUserPickerBottomAppBar(
                    sheetState = sheetState,
                    onDismissRequest = onDismissRequest,
                    onApplyClick = { onUsersSelected(selectedUsers) },
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues),
            ) {
                SearchBar(
                    placeholderText = placeholderText,
                    onSearchQueryChange = { viewModel.setEvent(SearchContract.UiEvent.SearchQueryUpdated(it)) },
                    searchQuery = state.value.searchQuery,
                )

                LazyColumn {
                    items(
                        items = state.value.searchResults.ifEmpty {
                            when (state.value.searchQuery.isEmpty()) {
                                true -> state.value.recommendedUsers
                                false -> when (state.value.searching) {
                                    true -> state.value.recommendedUsers
                                    false -> state.value.searchResults
                                }
                            }
                        },
                        key = { item -> item.profileId },
                    ) {
                        UserProfileListItem(
                            data = it,
                            onClick = { item ->
                                selectedUsers = selectedUsers + item
                                scope.launch {
                                    lazyListState.animateScrollToItem(selectedUsers.size - 1)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MultipleUserPickerBottomAppBar(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onApplyClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    PrimalLoadingButton(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        text = stringResource(id = R.string.asearch_multiselect_apply),
        onClick = {
            scope.launch { sheetState.hide() }
                .invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismissRequest()
                    }
                    onApplyClick()
                }
        },
    )
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    placeholderText: String,
) {
    PrimalIconTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp),
        value = searchQuery,
        focusRequester = null,
        onValueChange = onSearchQueryChange,
        placeholderText = placeholderText,
        iconImageVector = PrimalIcons.Search,
    )
    PrimalDivider()
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MultipleUserPickerTopAppBar(
    sheetTitle: String,
    onDismissRequest: () -> Unit,
    lazyListState: LazyListState,
    selectedUsers: Set<UserProfileItemUi>,
    onUserClick: (UserProfileItemUi) -> Unit,
) {
    Column {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = sheetTitle,
                )
            },
            navigationIcon = {
                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = PrimalIcons.ArrowBack, contentDescription = null)
                }
            },
        )
        SelectedUsersIndicator(
            lazyListState = lazyListState,
            selectedUsers = selectedUsers,
            onUserClick = onUserClick,
        )
    }
}

@Composable
private fun SelectedUsersIndicator(
    modifier: Modifier = Modifier,
    selectedUsers: Set<UserProfileItemUi>,
    onUserClick: (UserProfileItemUi) -> Unit,
    lazyListState: LazyListState = rememberLazyListState(),
) {
    LazyRow(
        state = lazyListState,
        modifier = modifier
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selectedUsers.isEmpty()) {
            item {
                Text(
                    modifier = Modifier
                        .padding(top = 7.dp, bottom = 8.dp)
                        .background(color = Color.Unspecified, shape = AppTheme.shapes.large)
                        .border(width = (0.5).dp, color = AppTheme.colorScheme.outline, shape = AppTheme.shapes.large)
                        .padding(all = 12.dp),
                    text = stringResource(id = R.string.asearch_multiselect_anyone),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                )
            }
        } else {
            items(items = selectedUsers.toList()) { user ->
                Box(
                    modifier = Modifier.size(54.dp),
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    AvatarThumbnail(
                        modifier = Modifier.offset(x = (-6).dp, y = (-6).dp),
                        avatarSize = 48.dp,
                        avatarCdnImage = user.avatarCdnImage,
                        onClick = { onUserClick(user) },
                    )
                    Icon(
                        modifier = Modifier
                            .clipToBounds()
                            .clip(CircleShape)
                            .clickable { onUserClick(user) }
                            .background(AppTheme.extraColorScheme.surfaceVariantAlt2),
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                    )
                }
            }
        }
    }
}
