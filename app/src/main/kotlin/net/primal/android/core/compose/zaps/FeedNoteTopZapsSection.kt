package net.primal.android.core.compose.zaps

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.stats.ui.EventZapUiModel

@Composable
fun FeedNoteTopZapsSection(zaps: List<EventZapUiModel>, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        if (zaps.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp),
            ) {
                EventZapItem(
                    noteZap = zaps.first(),
                    showMessage = true,
                )
            }
        }
        if (zaps.size > 1) {
            ZappersAvatarThumbnailRow(
                zaps = zaps.drop(n = 1).take(n = 3),
            )
        }
    }
}
