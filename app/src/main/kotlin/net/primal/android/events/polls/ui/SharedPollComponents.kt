package net.primal.android.events.polls.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import java.time.Duration
import java.time.Instant
import net.primal.android.R
import net.primal.android.notes.feed.model.PollOptionUi
import net.primal.android.notes.feed.model.PollType
import net.primal.android.theme.AppTheme

const val MIN_RESULT_BAR_FRACTION = 0.02f
const val PERCENTAGE_MULTIPLIER = 100
const val HOURS_PER_DAY = 24
const val MINUTES_PER_HOUR = 60

@Composable
fun PollWinnerCheckmark(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = null,
        modifier = modifier.size(18.dp),
        tint = AppTheme.colorScheme.onSurface,
    )
}

@Composable
fun formatTimeRemaining(endsAt: Instant): String {
    val now = Instant.now()
    val duration = Duration.between(now, endsAt)
    if (duration.isNegative) {
        return stringResource(R.string.poll_final_results)
    }

    val days = duration.toDays()
    val hours = duration.toHours() % HOURS_PER_DAY
    val minutes = duration.toMinutes() % MINUTES_PER_HOUR

    val parts = buildList {
        if (days > 0) add(pluralStringResource(R.plurals.poll_time_days, days.toInt(), days))
        if (hours > 0) add(pluralStringResource(R.plurals.poll_time_hours, hours.toInt(), hours))
        if (minutes > 0 || (days == 0L && hours == 0L)) {
            add(pluralStringResource(R.plurals.poll_time_minutes, minutes.toInt(), minutes))
        }
    }

    return stringResource(
        R.string.poll_time_remaining,
        parts.joinToString(" " + stringResource(R.string.poll_time_and) + " "),
    )
}

@Composable
fun PollFooter(
    totalVotes: Int,
    endsAt: Instant?,
    isEnded: Boolean,
    modifier: Modifier = Modifier,
    onVotesClick: (() -> Unit)? = null,
) {
    val timeText = if (isEnded) {
        stringResource(R.string.poll_final_results)
    } else {
        endsAt?.let { formatTimeRemaining(it) }
    }

    Text(
        modifier = modifier
            .padding(start = 4.dp, top = 4.dp)
            .then(
                if (onVotesClick != null) {
                    Modifier.clickable(onClick = onVotesClick)
                } else {
                    Modifier
                },
            ),
        text = buildAnnotatedString {
            if (onVotesClick != null) {
                withStyle(SpanStyle(color = AppTheme.colorScheme.primary)) {
                    append(pluralStringResource(R.plurals.poll_votes_count, totalVotes, totalVotes))
                }
            } else {
                append(pluralStringResource(R.plurals.poll_votes_count, totalVotes, totalVotes))
            }
            if (timeText != null) {
                withStyle(
                    SpanStyle(
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    ),
                ) {
                    append(" \u2022 $timeText")
                }
            }
        },
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        style = AppTheme.typography.bodySmall,
    )
}

@Composable
fun PollPercentageText(
    option: PollOptionUi,
    pollType: PollType,
    hasWinner: Boolean,
    modifier: Modifier = Modifier,
    style: TextStyle = AppTheme.typography.bodyMedium,
) {
    Text(
        modifier = modifier,
        text = when (pollType) {
            PollType.Zap -> stringResource(
                R.string.poll_sats_format,
                "%,d".format(option.satsZapped),
            )

            PollType.User -> "%.1f%%".format(option.votePercentage * PERCENTAGE_MULTIPLIER)
        },
        style = style,
        fontWeight = if (option.isWinner) FontWeight.Bold else FontWeight.SemiBold,
        color = if (hasWinner && !option.isWinner) {
            AppTheme.extraColorScheme.onSurfaceVariantAlt2
        } else {
            AppTheme.colorScheme.onSurface
        },
    )
}

@Composable
fun PollOptionBar(
    progress: Float,
    progressColor: androidx.compose.ui.graphics.Color,
    barShape: Shape,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progress)
                .fillMaxHeight()
                .clip(barShape)
                .background(progressColor),
        )

        label()
    }
}
