package net.primal.android.wallet.transactions.send.prepare.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.explore.search.SearchContract
import net.primal.android.explore.search.SearchViewModel
import net.primal.android.explore.search.ui.UserProfileListItem
import net.primal.android.messages.conversation.create.SearchBar

@Composable
fun SendPaymentTabNostr(onProfileClick: (String) -> Unit) {
    val viewModel = hiltViewModel<SearchViewModel>()
    val state = viewModel.state.collectAsState()

    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        SearchBar(
            query = state.value.searchQuery,
            focusRequester = null,
            onQueryChange = {
                viewModel.setEvent(SearchContract.UiEvent.SearchQueryUpdated(it))
            },
        )

        PrimalDivider()

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
                        keyboardController?.hide()
                        onProfileClick(item.profileId)
                        viewModel.setEvent(SearchContract.UiEvent.ProfileSelected(profileId = item.profileId))
                    },
                )
            }
        }
    }
}
