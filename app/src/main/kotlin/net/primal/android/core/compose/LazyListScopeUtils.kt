package net.primal.android.core.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import net.primal.android.R
import timber.log.Timber

fun <T : Any> LazyListScope.handleRefreshLoadState(
    pagingItems: LazyPagingItems<T>,
    noContentText: String,
    noContentVerticalArrangement: Arrangement.Vertical = Arrangement.Center,
    noContentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
) {
    when (val refreshLoadState = pagingItems.loadState.refresh) {
        LoadState.Loading -> {
            heightAdjustableLoadingLazyListPlaceholder()
        }

        is LoadState.NotLoading -> {
            item(contentType = "NoContent") {
                ListNoContent(
                    modifier = Modifier.fillParentMaxSize(),
                    noContentText = noContentText,
                    onRefresh = { pagingItems.refresh() },
                    verticalArrangement = noContentVerticalArrangement,
                    contentPadding = noContentPaddingValues,
                )
            }
        }

        is LoadState.Error -> {
            val error = refreshLoadState.error
            Timber.w(error)
            item(contentType = "RefreshError") {
                ListNoContent(
                    modifier = Modifier.fillParentMaxSize(),
                    noContentText = stringResource(id = R.string.feed_error_loading),
                    onRefresh = { pagingItems.refresh() },
                    verticalArrangement = noContentVerticalArrangement,
                    contentPadding = noContentPaddingValues,
                )
            }
        }
    }
}
