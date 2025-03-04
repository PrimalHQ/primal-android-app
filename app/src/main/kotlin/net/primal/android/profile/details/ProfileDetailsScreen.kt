package net.primal.android.profile.details

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.R
import net.primal.android.articles.feed.ArticleFeedList
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.fab.NewPostFloatingActionButton
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.approvals.ApproveFollowUnfollowProfileAlertDialog
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.pulltorefresh.PrimalPullToRefreshBox
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.errors.UiError
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.notes.feed.grid.MediaFeedGrid
import net.primal.android.notes.feed.list.NoteFeedList
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.notes.feed.zaps.UnableToZapBottomSheet
import net.primal.android.notes.feed.zaps.ZapBottomSheet
import net.primal.android.profile.details.ProfileDetailsContract.UiState.ProfileError
import net.primal.android.profile.details.ui.AvatarValues
import net.primal.android.profile.details.ui.CoverValues
import net.primal.android.profile.details.ui.PROFILE_TAB_COUNT
import net.primal.android.profile.details.ui.ProfileDetailsHeader
import net.primal.android.profile.details.ui.ProfileTabs
import net.primal.android.profile.details.ui.ProfileTopCoverBar
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.WalletPreference
import net.primal.android.wallet.zaps.canZap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(
    viewModel: ProfileDetailsViewModel,
    callbacks: ProfileDetailsContract.ScreenCallbacks,
    noteCallbacks: NoteCallbacks,
) {
    val uiState = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> {
                viewModel.setEvent(ProfileDetailsContract.UiEvent.RequestProfileUpdate)
            }

            else -> Unit
        }
    }

    val uiScope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()
    val pullToRefreshing = remember { mutableStateOf(false) }
    val addedToUserFeedsMessage = stringResource(id = R.string.app_added_to_user_feeds)
    val removedFromUserFeedsMessage = stringResource(id = R.string.app_removed_from_user_feeds)
    val profileZapSentMessage = stringResource(id = R.string.profile_zap_sent_message)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect {
            when (it) {
                ProfileDetailsContract.SideEffect.ProfileUpdateFinished -> pullToRefreshing.value = false
                ProfileDetailsContract.SideEffect.ProfileFeedAdded -> uiScope.launch {
                    snackbarHostState.showSnackbar(message = addedToUserFeedsMessage)
                }

                ProfileDetailsContract.SideEffect.ProfileFeedRemoved -> uiScope.launch {
                    snackbarHostState.showSnackbar(message = removedFromUserFeedsMessage)
                }

                ProfileDetailsContract.SideEffect.ProfileZapSent -> uiScope.launch {
                    snackbarHostState.showSnackbar(message = profileZapSentMessage)
                }
            }
        }
    }

    LaunchedEffect(viewModel, uiState.value.zapError) {
        uiState.value.zapError?.let {
            uiScope.launch {
                snackbarHostState.showSnackbar(
                    message = it.resolveUiErrorMessage(context),
                    duration = SnackbarDuration.Short,
                )
            }
            viewModel.setEvent(ProfileDetailsContract.UiEvent.DismissZapError)
        }
    }

    ProfileDetailsScreen(
        state = uiState.value,
        snackbarHostState = snackbarHostState,
        pullToRefreshState = pullToRefreshState,
        pullToRefreshing = pullToRefreshing,
        callbacks = callbacks,
        noteCallbacks = noteCallbacks,
        eventPublisher = viewModel::setEvent,
    )
}

private const val MAX_COVER_TRANSPARENCY = 0.70f
internal const val NOTES_TAB_INDEX = 0
internal const val REPLIES_TAB_INDEX = 1
internal const val READS_TAB_INDEX = 2
internal const val MEDIA_TAB_INDEX = 3

@Suppress("LongMethod", "CyclomaticComplexMethod")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(
    state: ProfileDetailsContract.UiState,
    snackbarHostState: SnackbarHostState,
    pullToRefreshState: PullToRefreshState,
    pullToRefreshing: MutableState<Boolean>,
    noteCallbacks: NoteCallbacks,
    callbacks: ProfileDetailsContract.ScreenCallbacks,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
) {
    val listState = rememberLazyListState()

    if (state.shouldApproveProfileAction != null) {
        ApproveFollowUnfollowProfileAlertDialog(
            profileApproval = state.shouldApproveProfileAction,
            onFollowApproved = {
                eventPublisher(
                    ProfileDetailsContract.UiEvent.FollowAction(
                        profileId = state.shouldApproveProfileAction.profileId,
                        forceUpdate = true,
                    ),
                )
            },
            onUnfollowApproved = {
                eventPublisher(
                    ProfileDetailsContract.UiEvent.UnfollowAction(
                        profileId = state.shouldApproveProfileAction.profileId,
                        forceUpdate = true,
                    ),
                )
            },
            onClose = {
                eventPublisher(ProfileDetailsContract.UiEvent.DismissConfirmFollowUnfollowAlertDialog)
            },
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding(),
            )
        },
        floatingActionButton = {
            NewPostFloatingActionButton(onNewPostClick = callbacks.onNewPostClick)
        },
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .consumeWindowInsets(paddingValues)
                .nestedScroll(
                    remember {
                        object : NestedScrollConnection {
                            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                                return if (available.y > 0) {
                                    Offset.Zero
                                } else {
                                    Offset(x = 0f, y = -listState.dispatchRawDelta(-available.y))
                                }
                            }
                        }
                    },
                ),
        ) {
            val screenHeight = maxHeight - with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toDp() }

            if (state.isResolvingProfileId) {
                PrimalLoadingSpinner()
            } else if (state.isInvalidProfileId) {
                ListNoContent(
                    modifier = Modifier.fillMaxSize(),
                    noContentText = stringResource(id = R.string.profile_invalid_profile_id),
                    onRefresh = { eventPublisher(ProfileDetailsContract.UiEvent.RequestProfileIdResolution) },
                )
            } else {
                ProfileDetailsContent(
                    pullToRefreshing = pullToRefreshing,
                    pullToRefreshState = pullToRefreshState,
                    paddingValues = paddingValues,
                    eventPublisher = eventPublisher,
                    listState = listState,
                    state = state,
                    callbacks = callbacks,
                    noteCallbacks = noteCallbacks,
                    screenHeight = screenHeight,
                    snackbarHostState = snackbarHostState,
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
@ExperimentalMaterial3Api
fun ProfileDetailsContent(
    pullToRefreshing: MutableState<Boolean>,
    pullToRefreshState: PullToRefreshState,
    paddingValues: PaddingValues,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    listState: LazyListState,
    state: ProfileDetailsContract.UiState,
    callbacks: ProfileDetailsContract.ScreenCallbacks,
    noteCallbacks: NoteCallbacks,
    screenHeight: Dp,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val uiScope = rememberCoroutineScope()

    val maxAvatarSizeDp = 86.dp
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

    var showCantZapWarning by remember { mutableStateOf(false) }
    if (showCantZapWarning) {
        UnableToZapBottomSheet(
            zappingState = state.zappingState,
            onDismissRequest = { showCantZapWarning = false },
            onGoToWallet = { callbacks.onGoToWallet() },
        )
    }

    var showZapOptions by remember { mutableStateOf(false) }
    if (showZapOptions) {
        ZapBottomSheet(
            onDismissRequest = { showZapOptions = false },
            receiverName = state.profileDetails?.userDisplayName
                ?: stringResource(id = R.string.profile_zap_bottom_sheet_fallback_title),
            zappingState = state.zappingState,
            onZap = { zapAmount, zapDescription ->
                if (state.zappingState.canZap(zapAmount) && state.profileId != null) {
                    eventPublisher(
                        ProfileDetailsContract.UiEvent.ZapProfile(
                            profileId = state.profileId,
                            profileLnUrlDecoded = state.profileDetails?.lnUrlDecoded,
                            zapAmount = zapAmount.toULong(),
                            zapDescription = zapDescription,
                        ),
                    )
                } else {
                    showCantZapWarning = true
                }
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

    PrimalPullToRefreshBox(
        isRefreshing = pullToRefreshing.value,
        state = pullToRefreshState,
        indicatorPaddingValues = paddingValues,
        onRefresh = {
            pullToRefreshing.value = true
            eventPublisher(ProfileDetailsContract.UiEvent.RequestProfileUpdate)
        },
    ) {
        LazyColumn(
            state = listState,
        ) {
            stickyHeader {
                ProfileTopCoverBar(
                    state = state,
                    titleVisible = topBarTitleVisible.value,
                    coverValues = CoverValues(
                        coverHeight = with(density) { coverHeightPx.floatValue.toDp() },
                        coverAlpha = coverTransparency.floatValue,
                    ),
                    avatarValues = AvatarValues(
                        avatarSize = with(density) { avatarSizePx.floatValue.toDp() },
                        avatarPadding = with(
                            density,
                        ) { (maxAvatarSizePx - avatarSizePx.floatValue).toDp() },
                        avatarOffsetY = with(density) { maxAvatarSizePx.times(other = 0.65f).toDp() },
                    ),
                    eventPublisher = eventPublisher,
                    onClose = callbacks.onClose,
                    paddingValues = paddingValues,
                    onSearchClick = { state.profileId?.let { callbacks.onSearchClick(state.profileId) } },
                    onMediaItemClick = callbacks.onMediaItemClick,
                )
            }
            item {
                ProfileDetailsHeader(
                    state = state,
                    eventPublisher = eventPublisher,
                    onEditProfileClick = callbacks.onEditProfileClick,
                    onMessageClick = callbacks.onMessageClick,
                    onZapProfileClick = {
                        if (state.zappingState.walletConnected) {
                            if (state.zappingState.walletPreference
                                == WalletPreference.NostrWalletConnect
                            ) {
                                showZapOptions = true
                            } else {
                                callbacks.onSendWalletTx(it)
                            }
                        } else {
                            showCantZapWarning = true
                        }
                    },
                    onDrawerQrCodeClick = { state.profileId?.let { callbacks.onDrawerQrCodeClick(it) } },
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
                    onFollowsClick = callbacks.onFollowsClick,
                    onProfileClick = { noteCallbacks.onProfileClick?.invoke(it) },
                    onHashtagClick = { noteCallbacks.onHashtagClick?.invoke(it) },
                    onPremiumBadgeClick = callbacks.onPremiumBadgeClick,
                )
            }
            item {
                val tabVerticalPadding = 8.dp
                Column(
                    modifier = Modifier
                        .background(AppTheme.colorScheme.surfaceVariant)
                        .height(screenHeight + tabVerticalPadding * 2),
                ) {
                    val pagerState = rememberPagerState { PROFILE_TAB_COUNT }

                    ProfileTabs(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier.padding(vertical = tabVerticalPadding),
                        notesCount = state.profileStats?.notesCount,
                        onNotesCountClick = {
                            uiScope.launch { pagerState.animateScrollToPage(page = NOTES_TAB_INDEX) }
                        },
                        repliesCount = state.profileStats?.repliesCount,
                        onRepliesCountClick = {
                            uiScope.launch { pagerState.animateScrollToPage(page = REPLIES_TAB_INDEX) }
                        },
                        readsCount = state.profileStats?.readsCount,
                        onReadsCountClick = {
                            uiScope.launch { pagerState.animateScrollToPage(page = READS_TAB_INDEX) }
                        },
                        mediaCount = state.profileStats?.mediaCount,
                        onMediaCountClick = {
                            uiScope.launch { pagerState.animateScrollToPage(page = MEDIA_TAB_INDEX) }
                        },
                    )
                    HorizontalPager(state = pagerState) { pageIndex ->
                        when {
                            state.isProfileMuted -> {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    ProfileMutedNotice(
                                        profileName = state.profileDetails?.authorDisplayName
                                            ?: state.profileId?.asEllipsizedNpub() ?: "",
                                        onUnmuteClick = {
                                            state.profileId?.let {
                                                eventPublisher(ProfileDetailsContract.UiEvent.UnmuteAction(it))
                                            }
                                        },
                                    )
                                }
                            }

                            pageIndex == NOTES_TAB_INDEX || pageIndex == REPLIES_TAB_INDEX -> {
                                state.profileId?.let {
                                    NoteFeedList(
                                        feedSpec = state.profileFeedSpecs[pageIndex].buildSpec(profileId = it),
                                        noteCallbacks = noteCallbacks,
                                        onGoToWallet = callbacks.onGoToWallet,
                                        pollingEnabled = pageIndex == NOTES_TAB_INDEX,
                                        pullToRefreshEnabled = false,
                                        showTopZaps = true,
                                        onUiError = { uiError ->
                                            uiScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = uiError.resolveUiErrorMessage(context),
                                                    duration = SnackbarDuration.Short,
                                                )
                                            }
                                        },
                                        noContentVerticalArrangement = Arrangement.Top,
                                        noContentPaddingValues = PaddingValues(top = 16.dp),
                                    )
                                }
                            }

                            pageIndex == READS_TAB_INDEX -> {
                                state.profileId?.let {
                                    ArticleFeedList(
                                        feedSpec = state.profileFeedSpecs[pageIndex].buildSpec(profileId = it),
                                        onArticleClick = { naddr -> noteCallbacks.onArticleClick?.invoke(naddr) },
                                        onGetPremiumClick = { noteCallbacks.onGetPrimalPremiumClick?.invoke() },
                                        pullToRefreshEnabled = false,
                                        noContentVerticalArrangement = Arrangement.Top,
                                        noContentPaddingValues = PaddingValues(top = 16.dp),
                                        onUiError = { uiError: UiError ->
                                            uiScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = uiError.resolveUiErrorMessage(context),
                                                    duration = SnackbarDuration.Short,
                                                )
                                            }
                                        },
                                    )
                                }
                            }

                            pageIndex == MEDIA_TAB_INDEX -> {
                                state.profileId?.let {
                                    MediaFeedGrid(
                                        modifier = Modifier.fillMaxSize(),
                                        feedSpec = state.profileFeedSpecs[pageIndex].buildSpec(profileId = it),
                                        onNoteClick = { naddr -> noteCallbacks.onNoteClick?.let { it(naddr) } },
                                        noContentVerticalArrangement = Arrangement.Top,
                                        noContentPaddingValues = PaddingValues(top = 16.dp),
                                        onGetPrimalPremiumClick = { noteCallbacks.onGetPrimalPremiumClick?.invoke() },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMutedNotice(profileName: String, onUnmuteClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .padding(top = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.profile_user_is_muted, profileName),
            style = AppTheme.typography.bodyLarge,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        )
        TextButton(onClick = onUnmuteClick) {
            Text(
                text = stringResource(id = R.string.context_menu_unmute_user).uppercase(),
            )
        }
    }
}

private fun ProfileError.asHumanReadableText(context: Context): String {
    return when (this) {
        is ProfileError.FailedToFollowProfile -> context.getString(
            R.string.app_error_unable_to_follow_profile,
        )

        is ProfileError.FailedToUnfollowProfile -> context.getString(
            R.string.app_error_unable_to_unfollow_profile,
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

        is ProfileError.FailedToMuteProfile -> context.getString(
            R.string.app_error_muting_user,
        )

        is ProfileError.FailedToUnmuteProfile -> context.getString(
            R.string.app_error_unmuting_user,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            ),
            callbacks = ProfileDetailsContract.ScreenCallbacks(
                onClose = {},
                onSearchClick = {},
                onMediaItemClick = {},
                onEditProfileClick = {},
                onMessageClick = {},
                onSendWalletTx = {},
                onDrawerQrCodeClick = {},
                onFollowsClick = { _, _ -> },
                onGoToWallet = {},
                onPremiumBadgeClick = { _, _ -> },
                onNewPostClick = {},
            ),
            snackbarHostState = SnackbarHostState(),
            noteCallbacks = NoteCallbacks(),
            eventPublisher = {},
            pullToRefreshing = remember { mutableStateOf(false) },
            pullToRefreshState = rememberPullToRefreshState(),
        )
    }
}
