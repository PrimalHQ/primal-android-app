package net.primal.android.core.compose.pulltorefresh

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.PositionalThreshold
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun PrimalIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
    threshold: Dp = PositionalThreshold,
) {
    Indicator(
        modifier = modifier,
        color = AppTheme.colorScheme.primary,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        isRefreshing = isRefreshing,
        state = state,
        threshold = threshold,
    )
}
