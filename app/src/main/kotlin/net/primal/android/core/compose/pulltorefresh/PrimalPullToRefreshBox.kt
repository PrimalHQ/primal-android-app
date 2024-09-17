package net.primal.android.core.compose.pulltorefresh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
@ExperimentalMaterial3Api
fun PrimalPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    indicatorPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
    state: PullToRefreshState = rememberPullToRefreshState(),
    contentAlignment: Alignment = Alignment.TopStart,
    indicator: @Composable BoxScope.() -> Unit = {
        PrimalIndicator(
            modifier = Modifier
                .padding(indicatorPaddingValues)
                .align(Alignment.TopCenter),
            isRefreshing = isRefreshing,
            state = state,
        )
    },
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier.pullToRefresh(state = state, isRefreshing = isRefreshing, onRefresh = onRefresh, enabled = enabled),
        contentAlignment = contentAlignment,
    ) {
        content()
        indicator()
    }
}
