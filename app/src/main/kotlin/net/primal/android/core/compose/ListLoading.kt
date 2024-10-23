package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Deprecated(
    message = "Use skeleton loaders",
    replaceWith = ReplaceWith("HeightAdjustableLoadingLazyListPlaceholder"),
)
@Composable
fun ListLoading(modifier: Modifier) {
    Box(modifier = modifier) {
        PrimalLoadingSpinner()
    }
}
