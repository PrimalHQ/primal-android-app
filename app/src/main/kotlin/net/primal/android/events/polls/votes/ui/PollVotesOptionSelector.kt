package net.primal.android.events.polls.votes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.events.polls.ui.MIN_RESULT_BAR_FRACTION
import net.primal.android.events.polls.ui.PollFooter
import net.primal.android.events.polls.ui.PollOptionBar
import net.primal.android.events.polls.ui.PollPercentageText
import net.primal.android.events.polls.ui.PollWinnerCheckmark
import net.primal.android.notes.feed.model.PollOptionUi
import net.primal.android.notes.feed.model.PollState
import net.primal.android.notes.feed.model.PollType
import net.primal.android.notes.feed.model.PollUi
import net.primal.android.theme.AppTheme

@Composable
fun PollVotesOptionSelector(
    poll: PollUi,
    selectedOptionId: String?,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        poll.options.forEach { option ->
            val isSelected = option.id == selectedOptionId
            val isVotedByUser = poll.selectedOptionIds.contains(option.id)
            SelectablePollOption(
                option = option,
                pollType = poll.pollType,
                isSelected = isSelected,
                isVotedByUser = isVotedByUser,
                hasWinner = poll.state == PollState.Ended && poll.options.any { it.isWinner },
                onClick = { onOptionSelected(option.id) },
            )
        }

        PollFooter(
            totalVotes = poll.totalVotes,
            endsAt = poll.endsAt,
            isEnded = poll.state == PollState.Ended,
        )
    }
}

@Composable
private fun SelectablePollOption(
    option: PollOptionUi,
    pollType: PollType,
    isSelected: Boolean,
    isVotedByUser: Boolean,
    hasWinner: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val barShape = AppTheme.shapes.medium
    val barFillShape = AppTheme.shapes.small
    val progress = option.votePercentage.coerceAtLeast(MIN_RESULT_BAR_FRACTION)

    val progressColor = if (isVotedByUser) {
        AppTheme.colorScheme.primary
    } else {
        AppTheme.colorScheme.outline
    }

    val borderModifier = if (isSelected) {
        Modifier.border(
            width = 1.dp,
            color = AppTheme.colorScheme.primary,
            shape = barShape,
        )
    } else {
        Modifier
    }

    val backgroundColor = if (isSelected) {
        AppTheme.extraColorScheme.surfaceVariantAlt3
    } else {
        Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(barShape)
            .background(backgroundColor)
            .then(borderModifier)
            .clickable(onClick = onClick)
            .padding(10.dp)
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PollOptionBar(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            progress = progress,
            progressColor = progressColor,
            barShape = barFillShape,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = option.label,
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = if (option.isWinner) FontWeight.SemiBold else FontWeight.Normal,
                    color = AppTheme.colorScheme.onSurface,
                )

                if (option.isWinner) {
                    Spacer(modifier = Modifier.width(4.dp))
                    PollWinnerCheckmark()
                }
            }
        }

        OptionTrailingContent(
            option = option,
            pollType = pollType,
            hasWinner = hasWinner,
        )
    }
}

@Composable
private fun OptionTrailingContent(
    option: PollOptionUi,
    pollType: PollType,
    hasWinner: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        PollPercentageText(
            option = option,
            pollType = pollType,
            hasWinner = hasWinner,
            style = AppTheme.typography.bodySmall,
        )
        Text(
            text = stringResource(R.string.poll_votes_see_votes),
            style = AppTheme.typography.labelSmall,
            color = AppTheme.colorScheme.primary,
        )
    }
}
