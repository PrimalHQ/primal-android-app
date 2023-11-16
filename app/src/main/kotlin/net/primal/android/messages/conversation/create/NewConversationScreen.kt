package net.primal.android.messages.conversation.create

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.explore.search.SearchContract
import net.primal.android.explore.search.SearchViewModel
import net.primal.android.explore.search.ui.UserProfileListItem
import net.primal.android.theme.AppTheme

@Composable
fun NewConversationScreen(
    viewModel: SearchViewModel,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val state = viewModel.state.collectAsState()
    NewConversationScreen(
        state = state.value,
        onClose = onClose,
        onProfileClick = onProfileClick,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NewConversationScreen(
    state: SearchContract.UiState,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit,
    eventPublisher: (SearchContract.UiEvent) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.new_message_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                footer = {
                    SearchBar(
                        query = state.searchQuery,
                        onQueryChange = {
                            eventPublisher(SearchContract.UiEvent.SearchQueryUpdated(query = it))
                        },
                    )
                },
            )
        },
        content = { contentPadding ->
            LazyColumn(
                contentPadding = contentPadding,
            ) {
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
                    key = { item -> item.profileId },
                ) {
                    UserProfileListItem(
                        data = it,
                        onClick = { profileId ->
                            keyboardController?.hide()
                            onProfileClick(profileId)
                        },
                    )
                }
            }
        },
    )
}

@Composable
fun SearchBar(
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
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp),
        value = query,
        onValueChange = onQueryChange,
        shape = AppTheme.shapes.medium,
        colors = PrimalDefaults.outlinedTextFieldColors(),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
        ),
        leadingIcon = {
            Icon(
                imageVector = PrimalIcons.Search,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        },
        placeholder = {
            Text(
                text = stringResource(id = R.string.new_message_search_hint),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodyMedium,
            )
        },
    )
}
