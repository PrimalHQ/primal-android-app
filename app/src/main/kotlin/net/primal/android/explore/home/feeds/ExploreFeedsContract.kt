package net.primal.android.explore.home.feeds

import net.primal.android.feeds.domain.DvmFeed

interface ExploreFeedsContract {
    data class UiState(
        val feeds: List<DvmFeed> = emptyList(),
        val loading: Boolean = true,
    )
}
