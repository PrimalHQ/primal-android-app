package net.primal.android.profile.details.ui

import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.R
import net.primal.android.articles.feed.ArticleFeedList
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.notes.feed.NoteFeedList
import net.primal.android.notes.feed.note.ConfirmFirstBookmarkAlertDialog
import net.primal.android.notes.feed.note.events.NoteCallbacks
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
    noteCallbacks: NoteCallbacks,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onZapProfileClick: (DraftTx) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
    onFollowsClick: (String, ProfileFollowsType) -> Unit,
    onArticleClick: (String) -> Unit,
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
        noteCallbacks = noteCallbacks,
        onEditProfileClick = onEditProfileClick,
        onMessageClick = onMessageClick,
        onZapProfileClick = onZapProfileClick,
        onDrawerQrCodeClick = onDrawerQrCodeClick,
        onGoToWallet = onGoToWallet,
        onFollowsClick = onFollowsClick,
        eventPublisher = { viewModel.setEvent(it) },
        onArticleClick = onArticleClick,
    )
}

private const val MAX_COVER_TRANSPARENCY = 0.70f
internal const val NOTES_TAB_INDEX = 0
internal const val REPLIES_TAB_INDEX = 1
internal const val READS_TAB_INDEX = 2
internal const val MEDIA_TAB_INDEX = 3

@Suppress("LongMethod", "CyclomaticComplexMethod")
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileDetailsScreen(
    state: ProfileDetailsContract.UiState,
    onClose: () -> Unit,
    noteCallbacks: NoteCallbacks,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onZapProfileClick: (DraftTx) -> Unit,
    onDrawerQrCodeClick: (String) -> Unit,
    onArticleClick: (String) -> Unit,
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

    val listState = rememberLazyListState()

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

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding(),
            )
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
                                    Offset(
                                        x = 0f,
                                        y = -listState.dispatchRawDelta(-available.y),
                                    )
                                }
                            }
                        }
                    },
                ),
        ) {
            val screenHeight = maxHeight

            LazyColumn(
                state = listState,
            ) {
                stickyHeader {
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
                        paddingValues = paddingValues,
                    )
                }
                item {
                    ProfileDetailsHeader(
                        state = state,
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
                        onProfileClick = { noteCallbacks.onProfileClick?.invoke(it) },
                        onHashtagClick = { noteCallbacks.onHashtagClick?.invoke(it) },
                    )
                }
                item {
                    Column(
                        modifier = Modifier.height(screenHeight),
                    ) {
                        val pagerState = rememberPagerState { PROFILE_TAB_COUNT }
                        ProfileTabs(
                            pagerState = pagerState,
                            modifier = Modifier
                                .background(AppTheme.colorScheme.background)
                                .padding(bottom = 8.dp, top = 8.dp),
                            notesCount = state.profileStats?.notesCount,
                            onNotesCountClick = { },
                            repliesCount = state.profileStats?.repliesCount,
                            onRepliesCountClick = { },
                            readsCount = state.profileStats?.readsCount,
                            onReadsCountClick = {},
                            mediaCount = state.profileStats?.mediaCount,
                            onMediaCountClick = {},
                        )
                        HorizontalPager(
                            state = pagerState,
                        ) { pageIndex ->
                            when {
                                state.isProfileMuted -> {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                    ) {
                                        ProfileMutedNotice(
                                            profileName = state.profileDetails?.authorDisplayName
                                                ?: state.profileId.asEllipsizedNpub(),
                                            onUnmuteClick = {
                                                eventPublisher(
                                                    ProfileDetailsContract.UiEvent.UnmuteAction(state.profileId),
                                                )
                                            },
                                        )
                                    }
                                }

                                pageIndex == NOTES_TAB_INDEX || pageIndex == REPLIES_TAB_INDEX -> {
                                    NoteFeedList(
                                        feedSpec = state.profileFeedSpecs[pageIndex].buildSpec(
                                            profileId = state.profileId,
                                        ),
                                        noteCallbacks = noteCallbacks,
                                        onGoToWallet = onGoToWallet,
                                        pollingEnabled = false,
                                        pullToRefreshEnabled = false,
                                    )
                                }

                                pageIndex == READS_TAB_INDEX -> {
                                    ArticleFeedList(
                                        feedSpec = state.profileFeedSpecs[pageIndex].buildSpec(
                                            profileId = state.profileId,
                                        ),
                                        onArticleClick = onArticleClick,
                                        pullToRefreshEnabled = false,
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
            ),
            onClose = {},
            noteCallbacks = NoteCallbacks(),
            onEditProfileClick = {},
            onMessageClick = {},
            onZapProfileClick = {},
            onDrawerQrCodeClick = {},
            onFollowsClick = { _, _ -> },
            onGoToWallet = {},
            eventPublisher = {},
            onArticleClick = {},
        )
    }
}
