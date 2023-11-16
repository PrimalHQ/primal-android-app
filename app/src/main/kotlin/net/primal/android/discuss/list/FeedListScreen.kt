package net.primal.android.discuss.list

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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AdjustTemporarilySystemBarColors
import net.primal.android.discuss.list.model.FeedUi
import net.primal.android.theme.AppTheme

@Composable
fun FeedListScreen(viewModel: FeedListViewModel, onFeedSelected: (String) -> Unit) {
    val uiState = viewModel.state.collectAsState()

    AdjustTemporarilySystemBarColors(
        navigationBarColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    )

    FeedListScreen(
        state = uiState.value,
        onFeedClick = { onFeedSelected(it.directive) },
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FeedListScreen(state: FeedListContract.UiState, onFeedClick: (FeedUi) -> Unit) {
    Surface(
        color = AppTheme.extraColorScheme.surfaceVariantAlt2,
        contentColor = AppTheme.colorScheme.onSurfaceVariant,
    ) {
        LazyColumn(
            modifier = Modifier.navigationBarsPadding(),
        ) {
            stickyHeader {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    CenterAlignedTopAppBar(
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
                        ),
                        title = {
                            Text(text = stringResource(id = R.string.feed_list_title))
                        },
                    )
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
                        .clickable { onFeedClick(it) },
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
