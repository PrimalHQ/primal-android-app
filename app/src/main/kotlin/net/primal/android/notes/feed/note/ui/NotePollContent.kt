package net.primal.android.notes.feed.note.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import java.time.Duration
import java.time.Instant
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.LightningBolt
import net.primal.android.core.compose.preview.PrimalPreview
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
import net.primal.android.theme.domain.PrimalTheme

private const val PROGRESS_ANIMATION_DURATION_MS = 400
private const val STATE_TRANSITION_DURATION_MS = 300
private const val RESULT_TEXT_TARGET_BIAS = -0.9f

@Composable
fun NotePollContent(
    poll: PollUi,
    modifier: Modifier = Modifier,
    onOptionSelected: (optionId: String) -> Unit = {},
    onVotesClick: (() -> Unit)? = null,
) {
    val initialState = remember { poll.state }

    Box(modifier = modifier) {
        AnimatedContent(
            targetState = poll.state,
            transitionSpec = {
                fadeIn(tween(STATE_TRANSITION_DURATION_MS)) togetherWith
                    fadeOut(tween(STATE_TRANSITION_DURATION_MS)) using
                    SizeTransform(clip = false)
            },
            label = "pollStateTransition",
        ) { state ->
            when (state) {
                PollState.Pending -> PollPendingContent(
                    poll = poll,
                    onVote = { selectedIds -> onOptionSelected(selectedIds.first()) },
                    onVotesClick = onVotesClick,
                )

                PollState.Voted, PollState.Ended -> PollResultsContent(
                    poll = poll,
                    animate = initialState == PollState.Pending,
                    onVotesClick = onVotesClick,
                )
            }
        }
    }
}

@Composable
private fun PollPendingContent(
    poll: PollUi,
    onVote: (Set<String>) -> Unit,
    onVotesClick: (() -> Unit)?,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        poll.options.forEach { option ->
            PollPendingOption(
                option = option,
                pollType = poll.pollType,
                onClick = { onVote(setOf(option.id)) },
            )
        }

        PollFooter(
            totalVotes = poll.totalVotes,
            endsAt = poll.endsAt,
            isEnded = false,
            onVotesClick = onVotesClick,
        )
    }
}

@Composable
private fun PollPendingOption(
    option: PollOptionUi,
    pollType: PollType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(AppTheme.shapes.extraLarge)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt2)
            .border(
                width = 1.dp,
                color = AppTheme.colorScheme.outline,
                shape = AppTheme.shapes.extraLarge,
            )
            .clickable(onClick = onClick)
            .height(36.dp)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = option.label,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        if (pollType == PollType.Zap) {
            Icon(
                imageVector = PrimalIcons.LightningBolt,
                contentDescription = null,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.CenterEnd),
                tint = AppTheme.colorScheme.outline,
            )
        }
    }
}

@Composable
private fun PollResultsContent(
    poll: PollUi,
    animate: Boolean,
    onVotesClick: (() -> Unit)?,
) {
    var animationStarted by remember { mutableStateOf(!animate) }
    if (animate) {
        LaunchedEffect(Unit) { animationStarted = true }
    }

    val hasWinner = poll.state == PollState.Ended && poll.options.any { it.isWinner }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        poll.options.forEach { option ->
            val isUserChoice = option.id in poll.selectedOptionIds
            val showCheckmark = poll.state == PollState.Ended && option.isWinner

            PollResultOption(
                option = option,
                pollType = poll.pollType,
                isUserChoice = isUserChoice,
                showCheckmark = showCheckmark,
                hasWinner = hasWinner,
                animationStarted = animationStarted,
            )
        }

        PollFooter(
            totalVotes = poll.totalVotes,
            endsAt = poll.endsAt,
            isEnded = poll.state == PollState.Ended,
            onVotesClick = onVotesClick,
        )
    }
}

@Composable
private fun PollResultOption(
    option: PollOptionUi,
    pollType: PollType,
    isUserChoice: Boolean,
    showCheckmark: Boolean,
    hasWinner: Boolean,
    animationStarted: Boolean,
    modifier: Modifier = Modifier,
) {
    val targetProgress = if (animationStarted) {
        option.votePercentage.coerceAtLeast(MIN_RESULT_BAR_FRACTION)
    } else {
        0f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = PROGRESS_ANIMATION_DURATION_MS),
        label = "pollProgress",
    )

    val isDarkTheme = isAppInDarkPrimalTheme()
    val progressColor = if (isUserChoice) {
        AppTheme.colorScheme.primary.let { if (!isDarkTheme) it.copy(alpha = 0.50f) else it }
    } else {
        AppTheme.colorScheme.outline
    }

    val barShape = AppTheme.shapes.small

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(barShape)
            .height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PollOptionBar(
            modifier = Modifier
                .weight(1f)
                .height(36.dp),
            progress = animatedProgress,
            progressColor = progressColor,
            barShape = barShape,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                val offsetProgress by animateFloatAsState(
                    targetValue = if (animationStarted) 1f else 0f,
                    animationSpec = tween(durationMillis = PROGRESS_ANIMATION_DURATION_MS),
                    label = "textAlignment",
                )

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = BiasAlignment(
                        horizontalBias = lerp(0f, RESULT_TEXT_TARGET_BIAS, offsetProgress),
                        verticalBias = 0f,
                    ),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = option.label,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = AppTheme.typography.bodyMedium,
                            fontWeight = if (option.isWinner) FontWeight.SemiBold else FontWeight.Normal,
                            color = AppTheme.colorScheme.onSurface,
                        )

                        if (showCheckmark) {
                            Spacer(modifier = Modifier.width(4.dp))
                            PollWinnerCheckmark()
                        }
                    }
                }
            }
        }

        PollPercentageText(
            modifier = Modifier.padding(horizontal = 16.dp),
            option = option,
            pollType = pollType,
            hasWinner = hasWinner,
            style = AppTheme.typography.bodyMedium,
        )
    }
}

// region Previews

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewPollPending() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            NotePollContent(
                modifier = Modifier.padding(16.dp),
                poll = PollUi(
                    options = listOf(
                        PollOptionUi(
                            id = "1",
                            label = "\uD83D\uDC40 Conspiracy Contemplators",
                        ),
                        PollOptionUi(
                            id = "2",
                            label = "\uD83C\uDF3D Corn Conglomerators",
                        ),
                        PollOptionUi(
                            id = "3",
                            label = "\uD83E\uDD65 Coconut Connoiseurs",
                        ),
                        PollOptionUi(
                            id = "4",
                            label = "\uD83D\uDC47 All of the above",
                        ),
                    ),

                    endsAt = Instant.now().plus(
                        Duration.ofDays(2).plusMinutes(56),
                    ),
                    state = PollState.Pending,
                ),
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewPollVoted() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            NotePollContent(
                modifier = Modifier.padding(16.dp),
                poll = PollUi(
                    options = listOf(
                        PollOptionUi(
                            id = "1",
                            label = "\uD83D\uDC40 Conspiracy Contemplators",
                            votePercentage = 0f,
                        ),
                        PollOptionUi(
                            id = "2",
                            label = "\uD83C\uDF3D Corn Conglomerators",
                            votePercentage = 0.222f,
                        ),
                        PollOptionUi(
                            id = "3",
                            label = "\uD83E\uDD65 Coconut Connoiseurs",
                            votePercentage = 0.111f,
                        ),
                        PollOptionUi(
                            id = "4",
                            label = "\uD83D\uDC47 All of the above",
                            votePercentage = 0.666f,
                            isWinner = true,
                        ),
                    ),

                    endsAt = Instant.now().plus(
                        Duration.ofDays(2).plusMinutes(56),
                    ),
                    state = PollState.Voted,
                    selectedOptionIds = setOf("4"),
                ),
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewPollEnded() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            NotePollContent(
                modifier = Modifier.padding(16.dp),
                poll = PollUi(
                    options = listOf(
                        PollOptionUi(
                            id = "1",
                            label = "\uD83D\uDC40 Conspiracy Contemplators",
                            votePercentage = 0.035f,
                        ),
                        PollOptionUi(
                            id = "2",
                            label = "\uD83C\uDF3D Corn Conglomerators",
                            votePercentage = 0.243f,
                        ),
                        PollOptionUi(
                            id = "3",
                            label = "\uD83E\uDD65 Coconut Connoiseurs",
                            votePercentage = 0.093f,
                        ),
                        PollOptionUi(
                            id = "4",
                            label = "\uD83D\uDC47 All of the above",
                            votePercentage = 0.629f,
                            isWinner = true,
                        ),
                    ),

                    endsAt = Instant.now().minus(Duration.ofDays(1)),
                    state = PollState.Ended,
                    selectedOptionIds = setOf("4"),
                ),
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewPollEndedUserLost() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            NotePollContent(
                modifier = Modifier.padding(16.dp),
                poll = PollUi(
                    options = listOf(
                        PollOptionUi(
                            id = "1",
                            label = "\uD83D\uDC40 Conspiracy Contemplators",
                            votePercentage = 0.035f,
                        ),
                        PollOptionUi(
                            id = "2",
                            label = "\uD83C\uDF3D Corn Conglomerators",
                            votePercentage = 0.243f,
                        ),
                        PollOptionUi(
                            id = "3",
                            label = "\uD83E\uDD65 Coconut Connoiseurs",
                            votePercentage = 0.093f,
                        ),
                        PollOptionUi(
                            id = "4",
                            label = "\uD83D\uDC47 All of the above",
                            votePercentage = 0.629f,
                            isWinner = true,
                        ),
                    ),

                    endsAt = Instant.now().minus(Duration.ofDays(1)),
                    state = PollState.Ended,
                    selectedOptionIds = setOf("1"),
                ),
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewZapPollPending() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            NotePollContent(
                modifier = Modifier.padding(16.dp),
                poll = PollUi(
                    pollType = PollType.Zap,
                    options = listOf(
                        PollOptionUi(id = "1", label = "Phoenix"),
                        PollOptionUi(id = "2", label = "Muun"),
                        PollOptionUi(id = "3", label = "Blue Wallet"),
                        PollOptionUi(id = "4", label = "Primal"),
                    ),

                    endsAt = Instant.now().plus(Duration.ofDays(1)),
                    state = PollState.Pending,
                    valueMinimum = 21,
                    valueMaximum = 21_000,
                ),
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewZapPollVoted() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            NotePollContent(
                modifier = Modifier.padding(16.dp),
                poll = PollUi(
                    pollType = PollType.Zap,
                    options = listOf(
                        PollOptionUi(
                            id = "1",
                            label = "Phoenix",
                            votePercentage = 0.291f,
                            satsZapped = 45_000,
                        ),
                        PollOptionUi(
                            id = "2",
                            label = "Muun",
                            votePercentage = 0.078f,
                            satsZapped = 12_000,
                        ),
                        PollOptionUi(
                            id = "3",
                            label = "Blue Wallet",
                            votePercentage = 0.055f,
                            satsZapped = 8_500,
                        ),
                        PollOptionUi(
                            id = "4",
                            label = "Primal",
                            votePercentage = 0.576f,
                            satsZapped = 89_000,
                            isWinner = true,
                        ),
                    ),

                    endsAt = Instant.now().plus(Duration.ofDays(1)),
                    state = PollState.Voted,
                    selectedOptionIds = setOf("4"),
                    valueMinimum = 21,
                    valueMaximum = 21_000,
                ),
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewZapPollEnded() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            NotePollContent(
                modifier = Modifier.padding(16.dp),
                poll = PollUi(
                    pollType = PollType.Zap,
                    options = listOf(
                        PollOptionUi(
                            id = "1",
                            label = "Phoenix",
                            votePercentage = 0.029f,
                            satsZapped = 210,
                        ),
                        PollOptionUi(
                            id = "2",
                            label = "Muun",
                            votePercentage = 0.238f,
                            satsZapped = 1_711,
                        ),
                        PollOptionUi(
                            id = "3",
                            label = "Blue Wallet",
                            votePercentage = 0.114f,
                            satsZapped = 820,
                        ),
                        PollOptionUi(
                            id = "4",
                            label = "Primal",
                            votePercentage = 0.618f,
                            satsZapped = 4_441,
                            isWinner = true,
                        ),
                    ),

                    endsAt = Instant.now().minus(Duration.ofDays(1)),
                    state = PollState.Ended,
                    selectedOptionIds = setOf("4"),
                    valueMinimum = 21,
                    valueMaximum = 21_000,
                ),
            )
        }
    }
}

// endregion
