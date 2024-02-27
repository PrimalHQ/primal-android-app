package net.primal.android.explore.search.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.crypto.bech32ToHexOrThrow
import net.primal.android.explore.search.SearchContract
import net.primal.android.explore.search.SearchViewModel
import net.primal.android.nostr.ext.isNPub
import net.primal.android.nostr.ext.isNPubUri
import net.primal.android.nostr.ext.isNote
import net.primal.android.nostr.ext.isNoteUri
import net.primal.android.theme.AppTheme
import timber.log.Timber

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onSearchContent: (String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()
    SearchScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
        onProfileClick = onProfileClick,
        onNoteClick = onNoteClick,
        onSearchContent = onSearchContent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    state: SearchContract.UiState,
    eventPublisher: (SearchContract.UiEvent) -> Unit,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit,
    onNoteClick: (String) -> Unit,
    onSearchContent: (String) -> Unit,
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
                    )
                },
                title = {
                    SearchTextField(
                        query = state.searchQuery,
                        onQueryChange = {
                            eventPublisher(SearchContract.UiEvent.SearchQueryUpdated(query = it))
                        },
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
                        onClick = {
                            keyboardController?.hide()
                            scope.launch {
                                val query = state.searchQuery
                                val noteId = query.takeAsNoteHexId()
                                val profileId = query.takeAsProfileHexId()
                                when {
                                    noteId != null -> {
                                        delay(KEYBOARD_HIDE_DELAY)
                                        onNoteClick(noteId)
                                    }
                                    profileId != null -> onProfileClick(profileId)
                                    else -> onSearchContent(query)
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
                        onClick = { profileId -> onProfileClick(profileId) },
                    )
                }
            }
        },
    )
}

private const val KEYBOARD_HIDE_DELAY = 150L
private fun String.takeAsNoteHexId(): String? {
    return if (isNote() || isNoteUri()) {
        try {
            bech32ToHexOrThrow()
        } catch (error: IllegalArgumentException) {
            Timber.w(error)
            null
        }
    } else {
        null
    }
}

private fun String.takeAsProfileHexId(): String? {
    return if (isNPub() || isNPubUri()) {
        try {
            bech32ToHexOrThrow()
        } catch (error: IllegalArgumentException) {
            Timber.w(error)
            null
        }
    } else {
        null
    }
}

@Composable
fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() },
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
            imeAction = ImeAction.Done,
        ),
        singleLine = true,
    )
}

@Composable
fun SearchContentListItem(
    hint: String,
    clickable: Boolean,
    onClick: () -> Unit,
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
            Text(
                text = stringResource(id = R.string.explore_search_nostr).lowercase(),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            )
        },
    )
}
