package net.primal.android.core.compose

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Remembers a derived [State] that indicates whether an item with the specified [key] is currently
 * visible in the viewport of a lazy list (e.g., LazyColumn or LazyRow).
 *
 * This function observes the [LazyListState]'s visible items and recomputes the visibility status
 * whenever the layout info changes. It returns a read-only [State] that updates automatically
 * and can be used to drive reactive UI behavior.
 *
 * If the visible items are not yet available (e.g., during the first composition frame), the provided [fallback]
 * value will be returned until visibility data becomes available.
 *
 * @param key The key of the item to track for visibility.
 * @param fallback The value to return when the list is not yet composed or visible items are not available.
 * @return A [State] containing `true` if the item with the given [key] is currently visible, or `false` otherwise.
 */
@Composable
fun LazyListState.rememberIsItemVisible(key: Any?, fallback: Boolean): State<Boolean> {
    return remember(this, key, fallback) {
        derivedStateOf {
            if (layoutInfo.visibleItemsInfo.isEmpty()) {
                fallback
            } else {
                layoutInfo.visibleItemsInfo.firstOrNull { it.key == key } != null
            }
        }
    }
}

@Composable
fun rememberFirstVisibleItemIndex(listState: LazyListState): MutableState<Int?> {
    val currentlyFirstVisibleIndex = remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val fullyVisibleItems = layoutInfo.visibleItemsInfo
                    .filter {
                        val itemTop = it.offset
                        val itemBottom = it.offset + it.size
                        itemTop >= layoutInfo.viewportStartOffset && itemBottom <= layoutInfo.viewportEndOffset
                    }
                fullyVisibleItems.firstOrNull()?.index
            }
            .distinctUntilChanged()
            .collect { visibleIndex ->
                currentlyFirstVisibleIndex.value = visibleIndex
            }
    }

    return currentlyFirstVisibleIndex
}
