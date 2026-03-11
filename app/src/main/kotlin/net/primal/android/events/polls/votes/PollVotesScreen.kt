package net.primal.android.events.polls.votes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.LightningBolt
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.model.UserProfileItemUi
import net.primal.android.core.utils.shortened
import net.primal.android.events.polls.votes.model.PollVoteOptionUi
import net.primal.android.events.polls.votes.model.PollVoterUi
import net.primal.android.explore.search.ui.UserProfileListItem
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

private const val SHORTEN_AMOUNT_THRESHOLD = 100_000L

private fun Long.formatSats(): String = if (this >= SHORTEN_AMOUNT_THRESHOLD) this.shortened() else "%,d".format(this)

@Composable
fun PollVotesScreen(viewModel: PollVotesViewModel, callbacks: PollVotesContract.ScreenCallbacks) {
    val state = viewModel.state.collectAsState()

    PollVotesScreen(
        state = state.value,
        callbacks = callbacks,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PollVotesScreen(state: PollVotesContract.UiState, callbacks: PollVotesContract.ScreenCallbacks) {
    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.poll_votes_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = callbacks.onClose,
            )
        },
        content = { paddingValues ->
            when {
                state.loading && state.pollOptions.isEmpty() -> {
                    HeightAdjustableLoadingLazyListPlaceholder(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        contentPaddingValues = PaddingValues(0.dp),
                        repeat = 7,
                    )
                }

                state.error != null -> {
                    ListNoContent(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        noContentText = stringResource(id = R.string.poll_votes_error),
                        refreshButtonVisible = false,
                    )
                }

                state.pollOptions.isEmpty() -> {
                    ListNoContent(
                        modifier = Modifier.fillMaxSize().padding(paddingValues),
                        noContentText = stringResource(id = R.string.poll_votes_no_votes),
                        refreshButtonVisible = false,
                    )
                }

                else -> {
                    PollVotesList(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        pollOptions = state.pollOptions,
                        isZapPoll = state.isZapPoll,
                        onProfileClick = callbacks.onProfileClick,
                    )
                }
            }
        },
    )
}

@Composable
private fun PollVotesList(
    pollOptions: List<PollVoteOptionUi>,
    isZapPoll: Boolean,
    onProfileClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        pollOptions.forEach { option ->
            item(key = "header_${option.id}") {
                PollOptionHeader(option = option, isZapPoll = isZapPoll)
                PrimalDivider(color = AppTheme.colorScheme.outline)
            }

            items(
                items = option.voters,
                key = { voter -> "${option.id}_${voter.eventId}" },
            ) { voter ->
                if (isZapPoll) {
                    ZapVoterListItem(
                        voter = voter,
                        onClick = { onProfileClick(voter.profile.profileId) },
                    )
                } else {
                    UserProfileListItem(
                        data = voter.profile,
                        onClick = { onProfileClick(it.profileId) },
                    )
                }
                PrimalDivider(color = AppTheme.extraColorScheme.surfaceVariantAlt1)
            }
        }
    }
}

@Composable
private fun PollOptionHeader(
    option: PollVoteOptionUi,
    isZapPoll: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = option.title,
            style = AppTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.padding(end = 3.dp),
                text = "${option.voteCount}",
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onBackground,
            )
            Text(
                text = stringResource(id = R.string.poll_votes_suffix),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
            if (isZapPoll) {
                Text(
                    text = " · ",
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                )
                Text(
                    modifier = Modifier.padding(end = 3.dp),
                    text = option.totalSats.formatSats(),
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.colorScheme.onBackground,
                )
                Text(
                    text = stringResource(id = R.string.poll_votes_sats_suffix),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        }
    }
}

@Composable
private fun ZapVoterListItem(
    voter: PollVoterUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier.clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        leadingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(
                    modifier = Modifier.width(48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = PrimalIcons.LightningBolt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    )
                    Spacer(modifier = Modifier.size(7.dp))
                    Text(
                        text = voter.satsZapped.formatSats(),
                        style = AppTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = AppTheme.colorScheme.onBackground,
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                UniversalAvatarThumbnail(
                    avatarCdnImage = voter.profile.avatarCdnImage,
                    avatarSize = 48.dp,
                    avatarBlossoms = voter.profile.avatarBlossoms,
                    onClick = onClick,
                    legendaryCustomization = voter.profile.legendaryCustomization,
                )
            }
        },
        headlineContent = {
            Text(
                text = voter.profile.displayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
            )
        },
        supportingContent = {
            if (!voter.zapComment.isNullOrEmpty()) {
                Text(
                    text = voter.zapComment,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        },
    )
}

// region Previews

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewPollVotesList() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            PollVotesList(
                pollOptions = listOf(
                    PollVoteOptionUi(
                        id = "1",
                        title = "\uD83D\uDC40 Conspiracy Contemplators",
                        voteCount = 5,
                        voters = listOf(
                            PollVoterUi(
                                eventId = "e1",
                                profile = UserProfileItemUi(
                                    profileId = "p1",
                                    displayName = "miljan",
                                    internetIdentifier = "miljan@primal.net",
                                ),
                            ),
                            PollVoterUi(
                                eventId = "e2",
                                profile = UserProfileItemUi(
                                    profileId = "p2",
                                    displayName = "alex",
                                    internetIdentifier = "alex@primal.net",
                                ),
                            ),
                            PollVoterUi(
                                eventId = "e3",
                                profile = UserProfileItemUi(
                                    profileId = "p3",
                                    displayName = "marko",
                                    internetIdentifier = "marko@primal.net",
                                ),
                            ),
                        ),
                    ),
                    PollVoteOptionUi(
                        id = "2",
                        title = "\uD83C\uDF3D Corn Conglomerators",
                        voteCount = 12,
                        voters = listOf(
                            PollVoterUi(
                                eventId = "e4",
                                profile = UserProfileItemUi(
                                    profileId = "p4",
                                    displayName = "nikola",
                                    internetIdentifier = "nikola@primal.net",
                                ),
                            ),
                            PollVoterUi(
                                eventId = "e5",
                                profile = UserProfileItemUi(
                                    profileId = "p5",
                                    displayName = "stefan",
                                    internetIdentifier = "stefan@primal.net",
                                ),
                            ),
                        ),
                    ),
                ),
                isZapPoll = false,
                onProfileClick = {},
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewZapPollVotesList() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            PollVotesList(
                pollOptions = listOf(
                    PollVoteOptionUi(
                        id = "1",
                        title = "\uD83D\uDC40 Conspiracy Contemplat\u2026",
                        voteCount = 5,
                        totalSats = 3_452,
                        voters = listOf(
                            PollVoterUi(
                                eventId = "ze1",
                                profile = UserProfileItemUi(profileId = "p1", displayName = "miljan"),
                                satsZapped = 21_21200,
                                zapComment = "Big day today. One of many to co\u2026",
                            ),
                            PollVoterUi(
                                eventId = "ze2",
                                profile = UserProfileItemUi(profileId = "p2", displayName = "miljan"),
                                satsZapped = 2_12100,
                                zapComment = "Big day today. One of many to co\u2026",
                            ),
                            PollVoterUi(
                                eventId = "ze3",
                                profile = UserProfileItemUi(profileId = "p3", displayName = "miljan"),
                                satsZapped = 2100,
                                zapComment = "Big day today. One of many to co\u2026",
                            ),
                            PollVoterUi(
                                eventId = "ze4",
                                profile = UserProfileItemUi(profileId = "p4", displayName = "miljan"),
                                satsZapped = 210,
                                zapComment = "Big day today. One of many to co\u2026",
                            ),
                        ),
                    ),
                ),
                isZapPoll = true,
                onProfileClick = {},
            )
        }
    }
}

// endregion
