package net.primal.android.notes.feed.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

private const val VISIBILITY_THRESHOLD = 0.3F

@Composable
fun rememberFirstVisibleVideoPlayingItemIndex(
    listState: LazyListState,
    hasVideo: (index: Int) -> Boolean = { false },
): MutableState<Int?> {
    val currentlyFirstVisibleIndex = remember { mutableStateOf<Int?>(null) }
    LaunchedEffect(listState, hasVideo) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                layoutInfo.visibleItemsInfo
                    .asSequence()
                    .map { item ->
                        val itemStart = item.offset
                        val itemEnd = item.offset + item.size
                        val viewportStart = layoutInfo.viewportStartOffset
                        val viewportEnd = layoutInfo.viewportEndOffset
                        val visiblePartStart = maxOf(itemStart, viewportStart)
                        val visiblePartEnd = minOf(itemEnd, viewportEnd)
                        val visibleHeight = (visiblePartEnd - visiblePartStart).toFloat()

                        val visibilityRatio = if (item.size > 0) {
                            (visibleHeight / item.size).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                        Triple(item.index, visibilityRatio, hasVideo(item.index))
                    }
                    .filter { (_, ratio, _) -> ratio >= VISIBILITY_THRESHOLD }
                    .filter { (_, _, isVideo) -> isVideo }
                    .firstOrNull()
                    ?.first
            }
            .distinctUntilChanged()
            .collect { visibleIndex ->
                currentlyFirstVisibleIndex.value = visibleIndex
            }
    }

    return currentlyFirstVisibleIndex
}
