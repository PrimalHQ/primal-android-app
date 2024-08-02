package net.primal.android.profile.details.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.R
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.feed.list.FeedLazyColumn
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.note.ConfirmFirstBookmarkAlertDialog
import net.primal.android.core.compose.feed.note.events.InvoicePayClickEvent
import net.primal.android.core.compose.feed.note.events.MediaClickEvent
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.pulltorefresh.LaunchedPullToRefreshEndingEffect
import net.primal.android.core.compose.pulltorefresh.PrimalPullToRefreshIndicator
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.profile.details.ProfileDetailsContract
import net.primal.android.profile.details.ProfileDetailsContract.UiState.ProfileError
import net.primal.android.profile.details.ProfileDetailsViewModel
import net.primal.android.profile.domain.ProfileFollowsType
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.wallet.domain.DraftTx

@Composable
fun ProfileDetailsScreen(
    viewModel: ProfileDetailsViewModel,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onArticleClick: (naddr: String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (content: TextFieldValue) -> Unit,
    onProfileClick: (String) -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onZapProfileClick: (DraftTx) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
    onFollowsClick: (String, ProfileFollowsType) -> Unit,
    onGoToWallet: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> viewModel.setEvent(ProfileDetailsContract.UiEvent.RequestProfileUpdate)
            else -> Unit
        }
    }

    ProfileDetailsScreen(
        state = uiState.value,
        onClose = onClose,
        onPostClick = onPostClick,
        onArticleClick = onArticleClick,
        onPostReplyClick = onPostReplyClick,
        onPostQuoteClick = onPostQuoteClick,
        onProfileClick = onProfileClick,
        onEditProfileClick = onEditProfileClick,
        onMessageClick = onMessageClick,
        onZapProfileClick = onZapProfileClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        onHashtagClick = onHashtagClick,
        onMediaClick = onMediaClick,
        onPayInvoiceClick = onPayInvoiceClick,
        onGoToWallet = onGoToWallet,
        onFollowsClick = onFollowsClick,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

private const val MAX_COVER_TRANSPARENCY = 0.70f

@Suppress("LongMethod", "CyclomaticComplexMethod")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(
    state: ProfileDetailsContract.UiState,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onArticleClick: (naddr: String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (content: TextFieldValue) -> Unit,
    onProfileClick: (String) -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onZapProfileClick: (DraftTx) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (MediaClickEvent) -> Unit,
    onPayInvoiceClick: ((InvoicePayClickEvent) -> Unit)? = null,
    onGoToWallet: () -> Unit,
    onFollowsClick: (String, ProfileFollowsType) -> Unit,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
) {
    val density = LocalDensity.current

    val maxAvatarSizeDp = 88.dp
    val maxAvatarSizePx = with(density) { maxAvatarSizeDp.roundToPx().toFloat() }
    val avatarSizePx = rememberSaveable { mutableFloatStateOf(maxAvatarSizePx) }

    val maxCoverHeightDp = 112.dp
    val minCoverHeightDp = 64.dp
    val statusBarHeightDp = with(density) {
        WindowInsets.statusBars.getTop(density).toDp()
    }
    val maxCoverHeightPx = with(density) {
        (maxCoverHeightDp + statusBarHeightDp).roundToPx().toFloat()
    }
    val minCoverHeightPx = with(density) {
        (minCoverHeightDp + statusBarHeightDp).roundToPx().toFloat()
    }
    val coverHeightPx = rememberSaveable { mutableFloatStateOf(maxCoverHeightPx) }

    val topBarTitleVisible = rememberSaveable { mutableStateOf(false) }
    val coverTransparency = rememberSaveable { mutableFloatStateOf(0f) }

    val noPagingItems = flowOf<PagingData<FeedPostUi>>().collectAsLazyPagingItems()
    val pagingItems = state.notes.collectAsLazyPagingItems()
    val listState = pagingItems.rememberLazyListStatePagingWorkaround()

    val snackbarHostState = remember { SnackbarHostState() }
    val uiScope = rememberCoroutineScope()
    val context = LocalContext.current

    if (state.confirmBookmarkingNoteId != null) {
        ConfirmFirstBookmarkAlertDialog(
            onBookmarkConfirmed = {
                eventPublisher(
                    ProfileDetailsContract.UiEvent.BookmarkAction(
                        noteId = state.confirmBookmarkingNoteId,
                        forceUpdate = true,
                    ),
                )
            },
            onClose = {
                eventPublisher(ProfileDetailsContract.UiEvent.DismissBookmarkConfirmation)
            },
        )
    }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.asHumanReadableText(context) },
        onErrorDismiss = { eventPublisher(ProfileDetailsContract.UiEvent.DismissError) },
    )

    LaunchedEffect(listState) {
        withContext(Dispatchers.IO) {
            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                .filter { it.first == 0 }
                .map { it.second }
                .collect { scrollOffset ->
                    val newCoverHeight = maxCoverHeightPx - scrollOffset
                    coverHeightPx.floatValue = newCoverHeight.coerceIn(minCoverHeightPx, maxCoverHeightPx)

                    val newAvatarSize = maxAvatarSizePx - (scrollOffset * 1f)
                    avatarSizePx.floatValue = newAvatarSize.coerceIn(0f, maxAvatarSizePx)

                    topBarTitleVisible.value = scrollOffset > maxAvatarSizePx

                    val newCoverAlpha = 0f + scrollOffset / (maxCoverHeightPx - minCoverHeightPx)
                    coverTransparency.floatValue = newCoverAlpha.coerceIn(
                        minimumValue = 0.0f,
                        maximumValue = MAX_COVER_TRANSPARENCY,
                    )
                }
        }
    }

    LaunchedEffect(listState) {
        withContext(Dispatchers.IO) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .collect { visiblePage ->
                    if (visiblePage >= 1) {
                        topBarTitleVisible.value = true
                        coverHeightPx.floatValue = minCoverHeightPx
                        avatarSizePx.floatValue = 0f
                        coverTransparency.floatValue = MAX_COVER_TRANSPARENCY
                    }
                }
        }
    }

    val pullToRefreshState = rememberPullToRefreshState(
        positionalThreshold = PullToRefreshDefaults.PositionalThreshold.times(other = 1.5f),
    )

    LaunchedEffect(pullToRefreshState.isRefreshing) {
        if (pullToRefreshState.isRefreshing) {
            pagingItems.refresh()
            eventPublisher(ProfileDetailsContract.UiEvent.RequestProfileUpdate)
        }
    }

    LaunchedPullToRefreshEndingEffect(
        mediatorLoadStates = pagingItems.loadState.mediator,
        onRefreshEnd = { pullToRefreshState.endRefresh() },
    )

    Surface {
        Box(
            modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection),
        ) {
            FeedLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(0.dp),
                pagingItems = if (!state.isProfileMuted) pagingItems else noPagingItems,
                zappingState = state.zappingState,
                listState = listState,
                onPostClick = onPostClick,
                onArticleClick = onArticleClick,
                onProfileClick = { if (state.profileId != it) onProfileClick(it) },
                onPostReplyClick = onPostReplyClick,
                onZapClick = { post, zapAmount, zapDescription ->
                    eventPublisher(
                        ProfileDetailsContract.UiEvent.ZapAction(
                            postId = post.postId,
                            postAuthorId = post.authorId,
                            zapAmount = zapAmount,
                            zapDescription = zapDescription,
                        ),
                    )
                },
                onPostLikeClick = {
                    eventPublisher(
                        ProfileDetailsContract.UiEvent.PostLikeAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                        ),
                    )
                },
                onRepostClick = {
                    eventPublisher(
                        ProfileDetailsContract.UiEvent.RepostAction(
                            postId = it.postId,
                            postAuthorId = it.authorId,
                            postNostrEvent = it.rawNostrEventJson,
                        ),
                    )
                },
                onPostQuoteClick = { onPostQuoteClick(TextFieldValue(text = "\n\nnostr:${it.postId.hexToNoteHrp()}")) },
                onReportContentClick = { type, profileId, noteId ->
                    eventPublisher(
                        ProfileDetailsContract.UiEvent.ReportAbuse(
                            reportType = type,
                            profileId = profileId,
                            noteId = noteId,
                        ),
                    )
                },
                onHashtagClick = onHashtagClick,
                onMediaClick = onMediaClick,
                onPayInvoiceClick = onPayInvoiceClick,
                onGoToWallet = onGoToWallet,
                onBookmarkClick = { eventPublisher(ProfileDetailsContract.UiEvent.BookmarkAction(noteId = it)) },
                shouldShowLoadingState = false,
                shouldShowNoContentState = false,
                stickyHeader = {
                    ProfileTopCoverBar(
                        state = state,
                        snackbarHostState = snackbarHostState,
                        titleVisible = topBarTitleVisible.value,
                        coverValues = CoverValues(
                            coverHeight = with(density) { coverHeightPx.floatValue.toDp() },
                            coverAlpha = coverTransparency.floatValue,
                        ),
                        avatarValues = AvatarValues(
                            avatarSize = with(density) { avatarSizePx.floatValue.toDp() },
                            avatarPadding = with(density) { (maxAvatarSizePx - avatarSizePx.floatValue).toDp() },
                            avatarOffsetY = with(density) { maxAvatarSizePx.times(other = 0.65f).toDp() },
                        ),
                        eventPublisher = eventPublisher,
                        onClose = onClose,
                    )
                },
                header = {
                    ProfileDetailsHeader(
                        state = state,
                        pagingItems = pagingItems,
                        eventPublisher = eventPublisher,
                        onEditProfileClick = onEditProfileClick,
                        onMessageClick = onMessageClick,
                        onZapProfileClick = onZapProfileClick,
                        onDrawerQrCodeClick = { onDrawerQrCodeClick(state.profileId) },
                        onUnableToZapProfile = {
                            uiScope.launch {
                                snackbarHostState.showSnackbar(
                                    message = context.getString(
                                        R.string.wallet_send_payment_error_nostr_user_without_lightning_address,
                                        state.profileDetails?.authorDisplayName
                                            ?: context.getString(R.string.wallet_send_payment_this_user_chunk),
                                    ),
                                    duration = SnackbarDuration.Short,
                                )
                            }
                        },
                        onFollowsClick = onFollowsClick,
                        onProfileClick = onProfileClick,
                        onHashtagClick = onHashtagClick,
                    )
                },
            )

            PullToRefreshContainer(
                modifier = Modifier.align(Alignment.TopCenter),
                state = pullToRefreshState,
                contentColor = AppTheme.colorScheme.primary,
                indicator = { PrimalPullToRefreshIndicator(state = pullToRefreshState) },
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .navigationBarsPadding(),
            )
        }
    }
}

private fun ProfileError.asHumanReadableText(context: Context): String {
    return when (this) {
        is ProfileError.InvalidZapRequest -> context.getString(
            R.string.post_action_invalid_zap_request,
        )

        is ProfileError.MissingLightningAddress -> context.getString(
            R.string.post_action_missing_lightning_address,
        )

        is ProfileError.FailedToPublishZapEvent -> context.getString(
            R.string.post_action_zap_failed,
        )

        is ProfileError.FailedToPublishLikeEvent -> context.getString(
            R.string.post_action_like_failed,
        )

        is ProfileError.FailedToPublishRepostEvent -> context.getString(
            R.string.post_action_repost_failed,
        )

        is ProfileError.FailedToFollowProfile -> context.getString(
            R.string.profile_error_unable_to_follow,
        )

        is ProfileError.FailedToUnfollowProfile -> context.getString(
            R.string.profile_error_unable_to_unfollow,
        )

        is ProfileError.MissingRelaysConfiguration -> context.getString(
            R.string.app_missing_relays_config,
        )

        is ProfileError.FailedToAddToFeed -> context.getString(
            R.string.app_error_adding_to_feed,
        )

        is ProfileError.FailedToRemoveFeed -> context.getString(
            R.string.app_error_removing_feed,
        )

        is ProfileError.FailedToMuteProfile -> context.getString(R.string.app_error_muting_user)
        is ProfileError.FailedToUnmuteProfile -> context.getString(
            R.string.app_error_unmuting_user,
        )
    }
}

@Preview
@Composable
private fun PreviewProfileScreen() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        ProfileDetailsScreen(
            state = ProfileDetailsContract.UiState(
                profileId = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
                profileDetails = ProfileDetailsUi(
                    pubkey = "b10b0d5e5fae9c6c48a8c77f7e5abd42a79e9480e25a4094051d4ba4ce14456b",
                    authorDisplayName = "alex",
                    userDisplayName = "alex",
                    coverCdnImage = null,
                    avatarCdnImage = null,
                    internetIdentifier = "alex@primal.net",
                    lightningAddress = "alex@primal.net",
                    about = "Primal Android",
                    website = "https://appollo41.com",
                ),
                isProfileFollowed = false,
                isProfileMuted = false,
                isActiveUser = true,
                isProfileFeedInActiveUserFeeds = false,
                notes = emptyFlow(),
            ),
            onClose = {},
            onPostClick = {},
            onArticleClick = {},
            onPostReplyClick = {},
            onPostQuoteClick = {},
            onProfileClick = {},
            onEditProfileClick = {},
            onMessageClick = {},
            onZapProfileClick = {},
            onDrawerQrCodeClick = {},
            onHashtagClick = {},
            onMediaClick = {},
            onFollowsClick = { _, _ -> },
            onGoToWallet = {},
            eventPublisher = {},
        )
    }
}
