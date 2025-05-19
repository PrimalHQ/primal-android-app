package net.primal.android.notes.feed.note.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.notes.feed.model.EventStatsUi
import net.primal.android.notes.feed.note.ui.events.ReactionTab
import net.primal.android.theme.AppTheme

@Composable
fun ThreadNoteStatsRow(
    eventStats: EventStatsUi,
    onStatClick: (ReactionTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier) {
        if (eventStats.repliesCount > 0) {
            SingleNoteStat(
                count = eventStats.repliesCount,
                text = stringResource(R.string.thread_stats_replies),
                onClick = { onStatClick(ReactionTab.REPLIES) },
            )
        }

        if (eventStats.zapsCount > 0) {
            val hasPreviousStats = eventStats.repliesCount > 0
            val startPadding = if (hasPreviousStats) 8.dp else 0.dp
            SingleNoteStat(
                modifier = Modifier.padding(start = startPadding),
                count = eventStats.zapsCount,
                text = stringResource(R.string.thread_stats_zaps),
                onClick = { onStatClick(ReactionTab.ZAPS) },
            )
        }

        if (eventStats.likesCount > 0) {
            val hasPreviousStats = eventStats.repliesCount > 0 || eventStats.zapsCount > 0
            val startPadding = if (hasPreviousStats) 8.dp else 0.dp
            SingleNoteStat(
                modifier = Modifier.padding(start = startPadding),
                count = eventStats.likesCount,
                text = stringResource(R.string.thread_stats_likes),
                onClick = { onStatClick(ReactionTab.LIKES) },
            )
        }

        if (eventStats.repostsCount > 0) {
            val hasPreviousStats = eventStats.repliesCount > 0 || eventStats.zapsCount > 0 || eventStats.likesCount > 0
            val startPadding = if (hasPreviousStats) 8.dp else 0.dp
            SingleNoteStat(
                modifier = Modifier.padding(start = startPadding),
                count = eventStats.repostsCount,
                text = stringResource(R.string.thread_stats_reposts),
                onClick = { onStatClick(ReactionTab.REPLIES) },
            )
        }
    }
}

@Composable
private fun SingleNoteStat(
    count: Long,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = "$count",
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = AppTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        )
    }
}
