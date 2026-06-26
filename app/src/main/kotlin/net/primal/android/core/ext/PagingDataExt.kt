package net.primal.android.core.ext

import androidx.paging.PagingData
import androidx.paging.asItemSnapshotListFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect

/**
 * Keeps a `cachedIn` paging flow continuously presented so its latest [PagingData] generation stays
 * loaded even when no UI is collecting it. Without an active presenter a new generation never loads,
 * so a recreated collector (e.g. a screen returning from the back stack) briefly shows an empty list;
 * keeping it loaded lets that collector replay cached items instantly instead.
 */
suspend fun <T : Any> Flow<PagingData<T>>.keepLoaded() {
    asItemSnapshotListFlow().collect()
}
