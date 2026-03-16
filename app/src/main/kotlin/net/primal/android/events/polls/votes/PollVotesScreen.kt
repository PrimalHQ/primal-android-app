package net.primal.android.events.polls.votes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.HeightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalScaffold
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.heightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.LightningBolt
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.utils.shortened
import net.primal.android.events.polls.votes.PollVotesContract.UiEvent
import net.primal.android.events.polls.votes.model.PollVoterUi
import net.primal.android.events.polls.votes.ui.PollVotesOptionSelector
import net.primal.android.events.polls.votes.ui.ScrollToTopButton
import net.primal.android.explore.search.ui.UserProfileListItem
import net.primal.android.notes.feed.model.PollOptionUi
import net.primal.android.notes.feed.model.PollState
import net.primal.android.notes.feed.model.PollType
import net.primal.android.notes.feed.model.PollUi
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

private const val SHORTEN_AMOUNT_THRESHOLD = 100_000L

private fun Long.formatSats(): String = if (this >= SHORTEN_AMOUNT_THRESHOLD) this.shortened() else "%,d".format(this)

@Composable
fun PollVotesScreen(viewModel: PollVotesViewModel, callbacks: PollVotesContract.ScreenCallbacks) {
    val state = viewModel.state.collectAsState()
    val votersPagingItems = viewModel.votersPagingData.collectAsLazyPagingItems()

    PollVotesScreen(
        state = state.value,
        votersPagingItems = votersPagingItems,
        eventPublisher = viewModel::setEvent,
        callbacks = callbacks,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PollVotesScreen(
    state: PollVotesContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    callbacks: PollVotesContract.ScreenCallbacks,
    votersPagingItems: LazyPagingItems<PollVoterUi>? = null,
) {
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
                state.loading && state.pollUi == null -> {
                    HeightAdjustableLoadingLazyListPlaceholder(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPaddingValues = PaddingValues(0.dp),
                        repeat = 7,
                    )
                }

                state.error != null -> {
                    ListNoContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        noContentText = stringResource(id = R.string.poll_votes_error),
                        refreshButtonVisible = false,
                    )
                }

                state.pollUi != null -> {
                    val listState = rememberLazyListState()
                    val coroutineScope = rememberCoroutineScope()
                    val showScrollToTop by remember {
                        derivedStateOf { listState.firstVisibleItemIndex > 0 }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    ) {
                        PollVotesContent(
                            poll = state.pollUi,
                            selectedOptionId = state.selectedOptionId,
                            votersPagingItems = votersPagingItems,
                            listState = listState,
                            onOptionSelected = { eventPublisher(UiEvent.SelectOption(it)) },
                            onProfileClick = callbacks.onProfileClick,
                        )

                        ScrollToTopButton(
                            visible = showScrollToTop,
                            onClick = {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(0)
                                }
                            },
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun PollVotesContent(
    poll: PollUi,
    selectedOptionId: String?,
    votersPagingItems: LazyPagingItems<PollVoterUi>?,
    listState: LazyListState,
    onOptionSelected: (String) -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val isZapPoll = poll.pollType == PollType.Zap
    val selectedOption = poll.options.find { it.id == selectedOptionId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
    ) {
        item(key = "poll_options") {
            PollVotesOptionSelector(
                poll = poll,
                selectedOptionId = selectedOptionId,
                onOptionSelected = onOptionSelected,
            )
        }

        item(key = "divider") {
            PrimalDivider()
        }

        stickyHeader(key = "selected_option_header") {
            SelectedOptionHeader(
                option = selectedOption,
                isZapPoll = isZapPoll,
            )
        }

        if (votersPagingItems != null) {
            voterItems(
                votersPagingItems = votersPagingItems,
                isZapPoll = isZapPoll,
                onProfileClick = onProfileClick,
            )
        }
    }
}

private fun LazyListScope.voterItems(
    votersPagingItems: LazyPagingItems<PollVoterUi>,
    isZapPoll: Boolean,
    onProfileClick: (String) -> Unit,
) {
    items(
        count = votersPagingItems.itemCount,
        key = { index -> votersPagingItems.peek(index)?.eventId ?: "voter_$index" },
    ) { index ->
        val voter = votersPagingItems[index]
        if (voter != null) {
            if (isZapPoll) {
                ZapVoterListItem(
                    voter = voter,
                    onClick = { onProfileClick(voter.profile.profileId) },
                )
            } else {
                UserProfileListItem(
                    data = voter.profile,
                    onClick = { onProfileClick(it.profileId) },
                    internetIdentifierColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
            PrimalDivider(color = AppTheme.extraColorScheme.surfaceVariantAlt1)
        }
    }

    if (votersPagingItems.loadState.append is LoadState.Loading) {
        item(key = "loading_indicator") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            }
        }
    }

    when {
        votersPagingItems.loadState.refresh is LoadState.Loading &&
            votersPagingItems.itemCount == 0 -> {
            heightAdjustableLoadingLazyListPlaceholder()
        }

        votersPagingItems.loadState.refresh is LoadState.Error -> {
            item(key = "voters_error") {
                ListNoContent(
                    modifier = Modifier.fillMaxWidth(),
                    noContentText = stringResource(id = R.string.poll_votes_error),
                    refreshButtonVisible = true,
                    onRefresh = { votersPagingItems.refresh() },
                    verticalArrangement = Arrangement.Top,
                    contentPadding = PaddingValues(top = 16.dp),
                )
            }
        }

        votersPagingItems.itemCount == 0 -> {
            item(key = "voters_no_content") {
                ListNoContent(
                    modifier = Modifier.fillMaxWidth(),
                    noContentText = stringResource(id = R.string.poll_votes_no_voters),
                    refreshButtonVisible = false,
                    verticalArrangement = Arrangement.Top,
                    contentPadding = PaddingValues(top = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun SelectedOptionHeader(
    option: PollOptionUi?,
    isZapPoll: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(AppTheme.colorScheme.surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = option?.label.orEmpty(),
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.width(8.dp))

            if (option != null) {
                val suffixColor = AppTheme.extraColorScheme.onSurfaceVariantAlt2
                val valueStyle = SpanStyle(
                    color = AppTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = buildAnnotatedString {
                        withStyle(valueStyle) { append("${option.voteCount}") }
                        withStyle(SpanStyle(color = suffixColor)) {
                            append(
                                " ${
                                    pluralStringResource(
                                        R.plurals.poll_votes_count_suffix,
                                        option.voteCount,
                                        option.voteCount,
                                    )
                                }",
                            )
                        }
                        if (isZapPoll) {
                            withStyle(SpanStyle(color = suffixColor)) { append(" \u2022 ") }
                            withStyle(valueStyle) { append(option.satsZapped.formatSats()) }
                            withStyle(SpanStyle(color = suffixColor)) {
                                append(" ${stringResource(R.string.poll_votes_sats_suffix)}")
                            }
                        }
                    },
                    style = AppTheme.typography.bodySmall,
                )
            }
        }
        PrimalDivider(color = AppTheme.extraColorScheme.surfaceVariantAlt1)
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
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
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
private fun PreviewPollVotesScreenLoading() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            PollVotesScreen(
                state = PollVotesContract.UiState(loading = true),
                eventPublisher = {},
                callbacks = PollVotesContract.ScreenCallbacks(
                    onClose = {},
                    onProfileClick = {},
                ),
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewPollVotesScreenWithContent() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            PollVotesScreen(
                state = PollVotesContract.UiState(
                    loading = false,
                    pollUi = PollUi(
                        options = listOf(
                            PollOptionUi(
                                id = "1",
                                label = "Option A",
                                voteCount = 42,
                                votePercentage = 0.6f,
                                isWinner = true,
                            ),
                            PollOptionUi(
                                id = "2",
                                label = "Option B",
                                voteCount = 28,
                                votePercentage = 0.4f,
                            ),
                        ),
                        state = PollState.Voted,
                        userVotedOptionId = "1",
                    ),
                    selectedOptionId = "1",
                ),
                eventPublisher = {},
                votersPagingItems = flowOf(PagingData.empty<PollVoterUi>()).collectAsLazyPagingItems(),
                callbacks = PollVotesContract.ScreenCallbacks(
                    onClose = {},
                    onProfileClick = {},
                ),
            )
        }
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun PreviewPollVotesScreenZapPoll() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        Surface {
            PollVotesScreen(
                state = PollVotesContract.UiState(
                    loading = false,
                    pollUi = PollUi(
                        pollType = PollType.Zap,
                        options = listOf(
                            PollOptionUi(
                                id = "1",
                                label = "Phoenix",
                                voteCount = 10,
                                votePercentage = 0.576f,
                                satsZapped = 89_000,
                                isWinner = true,
                            ),
                            PollOptionUi(
                                id = "2",
                                label = "Primal",
                                voteCount = 5,
                                votePercentage = 0.291f,
                                satsZapped = 45_000,
                            ),
                        ),
                        state = PollState.Voted,
                        userVotedOptionId = "1",
                    ),
                    selectedOptionId = "1",
                ),
                eventPublisher = {},
                votersPagingItems = flowOf(PagingData.empty<PollVoterUi>()).collectAsLazyPagingItems(),
                callbacks = PollVotesContract.ScreenCallbacks(
                    onClose = {},
                    onProfileClick = {},
                ),
            )
        }
    }
}

// endregion
