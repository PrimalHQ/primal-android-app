package net.primal.android.explore.home.topics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.pulltorefresh.PrimalPullToRefreshBox
import net.primal.android.explore.home.topics.ui.TopicLoadingPlaceholder
import net.primal.android.explore.home.topics.ui.TopicUi
import net.primal.android.theme.AppTheme

@Composable
fun ExploreTopics(
    modifier: Modifier = Modifier,
    onHashtagClick: (String) -> Unit,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    val viewModel: ExploreTopicsViewModel = hiltViewModel()
    val uiState = viewModel.state.collectAsState()

    ExploreTopics(
        modifier = modifier,
        state = uiState.value,
        eventPublisher = viewModel::setEvent,
        onHashtagClick = onHashtagClick,
        paddingValues = paddingValues,
    )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExploreTopics(
    state: ExploreTopicsContract.UiState,
    onHashtagClick: (String) -> Unit,
    eventPublisher: (ExploreTopicsContract.UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state.loading) {
        if (!state.loading) {
            isRefreshing = false
        }
    }

    PrimalPullToRefreshBox(
        isRefreshing = isRefreshing,
        indicatorPaddingValues = paddingValues,
        onRefresh = {
            isRefreshing = true
            eventPublisher(ExploreTopicsContract.UiEvent.RefreshTopics)
        },
    ) {
        if (state.loading && state.topics.isEmpty()) {
            TopicLoadingPlaceholder(
                modifier = modifier,
                repeat = 4,
                contentPadding = paddingValues,
            )
        } else if (state.topics.isEmpty()) {
            ListNoContent(
                modifier = modifier.fillMaxSize(),
                noContentText = stringResource(
                    id = R.string.explore_trending_topics_no_content,
                ),
                refreshButtonVisible = true,
                onRefresh = { eventPublisher(ExploreTopicsContract.UiEvent.RefreshTopics) },
            )
        } else {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = paddingValues,
            ) {
                items(
                    items = state.topics,
                    key = { it.first().name },
                ) {
                    FlowRow(
                        modifier = Modifier
                            .background(color = AppTheme.colorScheme.surfaceVariant)
                            .wrapContentWidth()
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Top,
                    ) {
                        it.forEach {
                            TopicChip(
                                topic = it,
                                onHashtagClick = onHashtagClick,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopicChip(
    modifier: Modifier = Modifier,
    topic: TopicUi,
    onHashtagClick: (String) -> Unit,
) {
    SuggestionChip(
        modifier = modifier
            .height(56.dp)
            .padding(all = 8.dp),
        onClick = { onHashtagClick("#${topic.name}") },
        shape = AppTheme.shapes.extraLarge,
        border = SuggestionChipDefaults.suggestionChipBorder(
            enabled = true,
            borderColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        ),
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            labelColor = AppTheme.colorScheme.onSurface,
        ),
        label = {
            Text(
                modifier = Modifier.padding(all = 4.dp),
                text = topic.name,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )
        },
    )
}
