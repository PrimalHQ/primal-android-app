package net.primal.android.explore.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AdvancedSearch
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.explore.search.SearchContract
import net.primal.android.explore.search.SearchViewModel
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.utils.takeAsNaddrOrNull
import net.primal.domain.nostr.utils.takeAsNoteHexIdOrNull
import net.primal.domain.nostr.utils.takeAsProfileHexIdOrNull

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    searchScope: SearchScope,
    onClose: () -> Unit,
    onAdvancedSearchClick: (query: String) -> Unit,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onNaddrClick: (String) -> Unit,
    onSearchContent: (scope: SearchScope, query: String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()
    SearchScreen(
        state = uiState.value,
        searchScope = searchScope,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
        onAdvancedSearchClick = onAdvancedSearchClick,
        onProfileClick = onProfileClick,
        onNoteClick = onNoteClick,
        onNaddrClick = onNaddrClick,
        onSearchContent = onSearchContent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchContract.UiState,
    searchScope: SearchScope,
    eventPublisher: (SearchContract.UiEvent) -> Unit,
    onClose: () -> Unit,
    onAdvancedSearchClick: (query: String) -> Unit,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onNaddrClick: (String) -> Unit,
    onSearchContent: (scope: SearchScope, query: String) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    AppBarIcon(
                        icon = PrimalIcons.ArrowBack,
                        onClick = onClose,
                        appBarIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                    )
                },
                title = {
                    SearchTextField(
                        query = state.searchQuery,
                        onQueryChange = {
                            eventPublisher(SearchContract.UiEvent.SearchQueryUpdated(query = it))
                        },
                        onSearch = {
                            keyboardController?.hide()
                            scope.launch {
                                onSearchContent(searchScope, state.searchQuery)
                            }
                        },
                    )
                },
                actions = {
                    AppBarIcon(
                        icon = PrimalIcons.AdvancedSearch,
                        onClick = { onAdvancedSearchClick(state.searchQuery) },
                        appBarIconContentDescription = stringResource(id = R.string.accessibility_search),
                    )
                },
            )
        },
        content = { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues,
            ) {
                item {
                    HorizontalDivider(color = AppTheme.extraColorScheme.surfaceVariantAlt1)
                    SearchContentListItem(
                        hint = state.searchQuery.ifEmpty {
                            stringResource(id = R.string.explore_enter_query)
                        },
                        clickable = state.searchQuery.isNotBlank(),
                        searchScope = searchScope,
                        onClick = {
                            keyboardController?.hide()
                            scope.launch {
                                val query = state.searchQuery
                                val noteId = query.takeAsNoteHexIdOrNull()
                                val profileId = query.takeAsProfileHexIdOrNull()
                                val naddr = query.takeAsNaddrOrNull()
                                when {
                                    noteId != null -> {
                                        delay(KEYBOARD_HIDE_DELAY)
                                        onNoteClick(noteId)
                                    }
                                    profileId != null -> onProfileClick(profileId)
                                    naddr != null -> onNaddrClick(naddr)
                                    else -> onSearchContent(searchScope, query)
                                }
                            }
                        },
                    )
                    HorizontalDivider(color = AppTheme.extraColorScheme.surfaceVariantAlt1)
                }

                items(
                    items = state.searchResults.ifEmpty {
                        when (state.searchQuery.isEmpty()) {
                            true -> state.recommendedUsers
                            false -> when (state.searching) {
                                true -> state.recommendedUsers
                                false -> state.searchResults
                            }
                        }
                    },
                    key = { it.profileId },
                ) {
                    UserProfileListItem(
                        data = it,
                        onClick = { item -> onProfileClick(item.profileId) },
                    )
                }
            }
        },
    )
}

private const val KEYBOARD_HIDE_DELAY = 150L

@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onSearch: () -> Unit,
) {
    var focusRequested by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(focusRequester) {
        if (!focusRequested) {
            focusRequester.requestFocus()
            focusRequested = true
        }
    }

    OutlinedTextField(
        modifier = Modifier.focusRequester(focusRequester),
        value = query,
        onValueChange = onQueryChange,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
            },
        ),
        singleLine = true,
    )
}

@Composable
fun SearchContentListItem(
    hint: String,
    clickable: Boolean,
    onClick: () -> Unit,
    searchScope: SearchScope,
) {
    ListItem(
        modifier = Modifier.clickable(
            enabled = clickable,
            onClick = onClick,
        ),
        leadingContent = {
            Icon(imageVector = PrimalIcons.Search, contentDescription = null)
        },
        headlineContent = {
            Text(
                text = hint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            val resourceId = when (searchScope) {
                SearchScope.Notes -> R.string.explore_search_notes
                SearchScope.Reads -> R.string.explore_search_reads
                SearchScope.MyNotifications -> R.string.explore_search_notifications
            }
            Text(
                text = stringResource(id = resourceId).lowercase(),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            )
        },
    )
}
