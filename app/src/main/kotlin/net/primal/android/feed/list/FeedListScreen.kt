package net.primal.android.feed.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.feed.ui.model.FeedUi


@Composable
fun FeedListScreen(
    viewModel: FeedListViewModel,
    onFeedSelected: (String) -> Unit,
) {

    val uiState = viewModel.state.collectAsState()

    FeedListScreen(
        state = uiState.value,
        onFeedClick = { onFeedSelected(it.directive) },
    )
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedListScreen(
    state: FeedListContract.UiState,
    onFeedClick: (FeedUi) -> Unit,
) {
    Surface {
        LazyColumn(
            modifier = Modifier.navigationBarsPadding(),
        ) {
            stickyHeader {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    CenterAlignedTopAppBar(title = {
                        Text(text = stringResource(id = R.string.feed_list_title))
                    })
                    BottomSheetDefaults.DragHandle(height = 3.dp)
                }
            }

            items(
                items = state.feeds,
                key = { it.directive },
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onFeedClick(it) }
                ) {
                    Text(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        text = it.name,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}