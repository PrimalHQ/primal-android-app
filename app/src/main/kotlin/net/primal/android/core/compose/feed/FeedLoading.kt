package net.primal.android.core.compose.feed

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.primal.android.core.compose.PrimalLoadingSpinner

@Composable
fun FeedLoading(
    modifier: Modifier,
) {
    Box(modifier = modifier) {
        PrimalLoadingSpinner()
    }
}
