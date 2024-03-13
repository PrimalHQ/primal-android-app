package net.primal.android.profile.details

import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.SubcomposeAsyncImage
import java.text.NumberFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.attachments.domain.findNearestOrNull
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.core.compose.dropdown.DropdownPrimalMenuItem
import net.primal.android.core.compose.feed.list.FeedLazyColumn
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.ContextMuteUser
import net.primal.android.core.compose.icons.primaliconpack.ContextShare
import net.primal.android.core.compose.icons.primaliconpack.FeedZaps
import net.primal.android.core.compose.icons.primaliconpack.Key
import net.primal.android.core.compose.icons.primaliconpack.Link
import net.primal.android.core.compose.icons.primaliconpack.Message
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.compose.icons.primaliconpack.UserFeedAdd
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.core.utils.copyText
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.core.utils.resolvePrimalProfileLink
import net.primal.android.core.utils.shortened
import net.primal.android.core.utils.systemShareText
import net.primal.android.crypto.hexToNoteHrp
import net.primal.android.crypto.hexToNpubHrp
import net.primal.android.profile.details.ProfileDetailsContract.UiState.ProfileError
import net.primal.android.profile.domain.ProfileFeedDirective
import net.primal.android.profile.domain.ProfileFollowsType
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.wallet.domain.DraftTx
import net.primal.android.wallet.utils.isLightningAddress
import timber.log.Timber

@Composable
fun ProfileDetailsScreen(
    viewModel: ProfileDetailsViewModel,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onZapProfileClick: (DraftTx) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
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
        onPostReplyClick = onPostReplyClick,
        onPostQuoteClick = onPostQuoteClick,
        onProfileClick = onProfileClick,
        onEditProfileClick = onEditProfileClick,
        onMessageClick = onMessageClick,
        onZapProfileClick = onZapProfileClick,
        onHashtagClick = onHashtagClick,
        onMediaClick = onMediaClick,
        onGoToWallet = onGoToWallet,
        onFollowsClick = onFollowsClick,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

private const val MAX_COVER_TRANSPARENCY = 0.70f

@Suppress("MagicNumber")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileDetailsScreen(
    state: ProfileDetailsContract.UiState,
    onClose: () -> Unit,
    onPostClick: (String) -> Unit,
    onPostReplyClick: (String) -> Unit,
    onPostQuoteClick: (String) -> Unit,
    onProfileClick: (String) -> Unit,
    onEditProfileClick: () -> Unit,
    onMessageClick: (String) -> Unit,
    onZapProfileClick: (DraftTx) -> Unit,
    onHashtagClick: (String) -> Unit,
    onMediaClick: (String, String) -> Unit,
    onGoToWallet: () -> Unit,
    onFollowsClick: (String, ProfileFollowsType) -> Unit,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
) {
    val density = LocalDensity.current

    val maxAvatarSizeDp = 80.dp
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

    ErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

    LaunchedEffect(listState) {
        withContext(Dispatchers.IO) {
            snapshotFlow { listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset }
                .filter { it.first == 0 }
                .map { it.second }
                .collect { scrollOffset ->
                    val newCoverHeight = maxCoverHeightPx - scrollOffset
                    coverHeightPx.floatValue =
                        newCoverHeight.coerceIn(minCoverHeightPx, maxCoverHeightPx)

                    val newAvatarSize = maxAvatarSizePx - (scrollOffset * 1f)
                    avatarSizePx.floatValue = newAvatarSize.coerceIn(0f, maxAvatarSizePx)

                    topBarTitleVisible.value = scrollOffset > maxAvatarSizePx

                    val newCoverAlpha = 0f + scrollOffset / (maxCoverHeightPx - minCoverHeightPx)
                    coverTransparency.floatValue = newCoverAlpha.coerceIn(0.0f, MAX_COVER_TRANSPARENCY)
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

    Surface(
        modifier = Modifier.navigationBarsPadding(),
    ) {
        Box {
            FeedLazyColumn(
                contentPadding = PaddingValues(0.dp),
                pagingItems = if (!state.isProfileMuted) pagingItems else noPagingItems,
                zappingState = state.zappingState,
                listState = listState,
                onPostClick = onPostClick,
                onProfileClick = {
                    if (state.profileId != it) {
                        onProfileClick(it)
                    }
                },
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
                onPostQuoteClick = {
                    onPostQuoteClick("\n\nnostr:${it.postId.hexToNoteHrp()}")
                },
                onHashtagClick = onHashtagClick,
                onMediaClick = onMediaClick,
                onGoToWallet = onGoToWallet,
                shouldShowLoadingState = false,
                shouldShowNoContentState = false,
                stickyHeader = {
                    ProfileTopCoverBar(
                        state = state,
                        snackbarHostState = snackbarHostState,
                        uiScope = uiScope,
                        eventPublisher = eventPublisher,
                        coverHeight = with(density) { coverHeightPx.floatValue.toDp() },
                        coverAlpha = coverTransparency.floatValue,
                        avatarSize = with(density) { avatarSizePx.floatValue.toDp() },
                        avatarPadding = with(
                            density,
                        ) { (maxAvatarSizePx - avatarSizePx.floatValue).toDp() },
                        avatarOffsetY = with(density) { (maxAvatarSizePx * 0.65f).toDp() },
                        navigationIcon = {
                            AppBarIcon(
                                icon = PrimalIcons.ArrowBack,
                                appBarIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                                enabledBackgroundColor = Color.Black.copy(alpha = 0.5f),
                                tint = Color.White,
                                onClick = onClose,
                            )
                        },
                        title = {
                            AnimatedVisibility(
                                modifier = Modifier.padding(horizontal = 4.dp),
                                visible = topBarTitleVisible.value,
                                enter = fadeIn(),
                                exit = fadeOut(),
                            ) {
                                NostrUserText(
                                    displayName = state.profileDetails?.authorDisplayName
                                        ?: state.profileId.asEllipsizedNpub(),
                                    internetIdentifier = state.profileDetails?.internetIdentifier,
                                    internetIdentifierBadgeSize = 24.dp,
                                )
                            }
                        },
                    )
                },
                header = {
                    UserProfileDetails(
                        state = state,
                        eventPublisher = eventPublisher,
                        onEditProfileClick = onEditProfileClick,
                        onMessageClick = { onMessageClick(state.profileId) },
                        onZapProfileClick = {
                            val profileLud16 = state.profileDetails?.lightningAddress
                            if (profileLud16?.isLightningAddress() == true) {
                                onZapProfileClick(
                                    DraftTx(targetUserId = state.profileId, targetLud16 = profileLud16),
                                )
                            } else {
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
                            }
                        },
                        onFollow = { eventPublisher(ProfileDetailsContract.UiEvent.FollowAction(state.profileId)) },
                        onUnfollow = { eventPublisher(ProfileDetailsContract.UiEvent.UnfollowAction(state.profileId)) },
                        onFollowsClick = onFollowsClick,
                    )

                    if (state.isProfileMuted) {
                        ProfileMutedNotice(
                            profileName = state.profileDetails?.authorDisplayName
                                ?: state.profileId.asEllipsizedNpub(),
                            onUnmuteClick = {
                                eventPublisher(
                                    ProfileDetailsContract.UiEvent.UnmuteAction(state.profileId),
                                )
                            },
                        )
                    } else {
                        if (pagingItems.isEmpty()) {
                            when (pagingItems.loadState.refresh) {
                                LoadState.Loading -> ListLoading(
                                    modifier = Modifier
                                        .padding(vertical = 64.dp)
                                        .fillMaxWidth(),
                                )

                                is LoadState.NotLoading -> ListNoContent(
                                    modifier = Modifier
                                        .padding(vertical = 64.dp)
                                        .fillMaxWidth(),
                                    noContentText = stringResource(id = R.string.feed_no_content),
                                    onRefresh = { pagingItems.refresh() },
                                )

                                is LoadState.Error -> Unit
                            }
                        }
                    }
                },
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

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ProfileTopCoverBar(
    state: ProfileDetailsContract.UiState,
    snackbarHostState: SnackbarHostState,
    uiScope: CoroutineScope,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    navigationIcon: @Composable () -> Unit,
    title: @Composable () -> Unit,
    avatarSize: Dp,
    coverHeight: Dp,
    coverAlpha: Float = 0.0f,
    avatarOffsetY: Dp = 0.dp,
    avatarOffsetX: Dp = 0.dp,
    avatarPadding: Dp = 0.dp,
) {
    val coverBlur = AppTheme.colorScheme.surface.copy(alpha = coverAlpha)
    val maxCollapsed = coverAlpha == MAX_COVER_TRANSPARENCY
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val variant = state.profileDetails?.coverCdnImage?.variants?.findNearestOrNull(
            maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() },
        )
        val imageSource = variant?.mediaUrl ?: state.profileDetails?.coverCdnImage?.sourceUrl
        SubcomposeAsyncImage(
            modifier = Modifier
                .background(color = AppTheme.colorScheme.surface)
                .fillMaxWidth()
                .height(coverHeight)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(color = coverBlur)
                    }
                },
            model = imageSource,
            loading = { CoverLoading() },
            error = { CoverUnavailable() },
            contentDescription = "Cover",
            contentScale = ContentScale.Crop,
        )

        Column {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
                navigationIcon = navigationIcon,
                title = title,
                actions = {
                    ProfileDropdownMenu(
                        profileId = state.profileId,
                        isActiveUser = state.isActiveUser,
                        isProfileMuted = state.isProfileMuted,
                        isProfileFeedInActiveUserFeeds = state.isProfileFeedInActiveUserFeeds,
                        snackbarHostState = snackbarHostState,
                        uiScope = uiScope,
                        name = state.profileDetails?.authorDisplayName ?: "",
                        eventPublisher = eventPublisher,
                    )
                },
            )

            if (maxCollapsed) {
                PrimalDivider()
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = avatarOffsetY, x = avatarOffsetX)
                .padding(horizontal = 16.dp),
        ) {
            AvatarThumbnail(
                modifier = Modifier
                    .size(avatarSize)
                    .padding(
                        top = avatarPadding * 0 / 3,
                        bottom = avatarPadding * 3 / 3,
                        start = avatarPadding * 1 / 8,
                        end = avatarPadding * 7 / 8,
                    ),
                avatarCdnImage = state.profileDetails?.avatarCdnImage,
            )
        }
    }
}

@Composable
private fun ProfileDropdownMenu(
    profileId: String,
    isActiveUser: Boolean,
    isProfileMuted: Boolean,
    isProfileFeedInActiveUserFeeds: Boolean,
    snackbarHostState: SnackbarHostState,
    uiScope: CoroutineScope,
    name: String,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
) {
    var menuVisible by remember { mutableStateOf(false) }
    val addedToUserFeedsMessage = stringResource(id = R.string.app_added_to_user_feeds)
    val removedFromUserFeedsMessage = stringResource(id = R.string.app_removed_from_user_feeds)

    val context = LocalContext.current

    AppBarIcon(
        icon = PrimalIcons.More,
        onClick = { menuVisible = true },
        appBarIconContentDescription = stringResource(id = R.string.accessibility_profile_drop_down),
    )

    DropdownPrimalMenu(
        expanded = menuVisible,
        onDismissRequest = { menuVisible = false },
    ) {
        if (!isActiveUser) {
            val title = stringResource(id = R.string.profile_user_feed_title, name)
            val itemText = if (isProfileFeedInActiveUserFeeds) {
                stringResource(id = R.string.profile_context_remove_user_feed)
            } else {
                stringResource(id = R.string.profile_context_add_user_feed)
            }

            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.UserFeedAdd,
                text = itemText,
                onClick = {
                    if (isProfileFeedInActiveUserFeeds) {
                        eventPublisher(ProfileDetailsContract.UiEvent.RemoveUserFeedAction(directive = profileId))
                        uiScope.launch {
                            snackbarHostState.showSnackbar(
                                message = removedFromUserFeedsMessage,
                                duration = SnackbarDuration.Short,
                            )
                        }
                    } else {
                        eventPublisher(
                            ProfileDetailsContract.UiEvent.AddUserFeedAction(
                                name = title,
                                directive = profileId,
                            ),
                        )
                        uiScope.launch {
                            snackbarHostState.showSnackbar(
                                message = addedToUserFeedsMessage,
                                duration = SnackbarDuration.Short,
                            )
                        }
                    }
                    menuVisible = false
                },
            )
        }

        DropdownPrimalMenuItem(
            trailingIconVector = PrimalIcons.ContextShare,
            text = stringResource(id = R.string.profile_context_share_profile),
            onClick = {
                systemShareText(context = context, text = resolvePrimalProfileLink(profileId = profileId))
                menuVisible = false
            },
        )

        if (!isActiveUser) {
            val text = if (isProfileMuted) {
                stringResource(id = R.string.context_menu_unmute_user)
            } else {
                stringResource(id = R.string.context_menu_mute_user)
            }

            val action = if (isProfileMuted) {
                ProfileDetailsContract.UiEvent.UnmuteAction(profileId = profileId)
            } else {
                ProfileDetailsContract.UiEvent.MuteAction(profileId = profileId)
            }

            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.ContextMuteUser,
                text = text,
                tint = AppTheme.colorScheme.error,
                onClick = {
                    eventPublisher(action)
                    menuVisible = false
                },
            )
        }
    }
}

@Composable
private fun CoverLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = AppTheme.colorScheme.surface,
            ),
    )
}

@Composable
private fun CoverUnavailable() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = AppTheme.colorScheme.surface,
            ),
    )
}

@Composable
private fun UserProfileDetails(
    state: ProfileDetailsContract.UiState,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    onEditProfileClick: () -> Unit,
    onZapProfileClick: () -> Unit,
    onMessageClick: () -> Unit,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
    onFollowsClick: (String, ProfileFollowsType) -> Unit,
) {
    val localUriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val uiScope = rememberCoroutineScope()
    val keyCopiedText = stringResource(id = R.string.settings_keys_key_copied)
    val protocolPrefix = "http"
    val protocolPrefixReplacement = "https://"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = AppTheme.colorScheme.surfaceVariant),
    ) {
        ProfileActions(
            isFollowed = state.isProfileFollowed,
            isActiveUser = state.isActiveUser,
            onEditProfileClick = onEditProfileClick,
            onMessageClick = onMessageClick,
            onZapProfileClick = onZapProfileClick,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
        )

        NostrUserText(
            modifier = Modifier.padding(horizontal = 16.dp),
            displayName = state.profileDetails?.authorDisplayName ?: state.profileId.asEllipsizedNpub(),
            internetIdentifier = state.profileDetails?.internetIdentifier,
            internetIdentifierBadgeSize = 24.dp,
            style = AppTheme.typography.titleLarge,
        )

        if (state.profileDetails?.internetIdentifier?.isNotEmpty() == true) {
            UserInternetIdentifier(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp),
                internetIdentifier = state.profileDetails.internetIdentifier,
            )
        }

        UserPublicKey(
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
            pubkey = state.profileId,
            onCopyClick = {
                copyText(context = context, text = it)
                uiScope.launch { Toast.makeText(context, keyCopiedText, Toast.LENGTH_SHORT).show() }
            },
        )

        if (state.profileDetails?.about?.isNotEmpty() == true) {
            UserAbout(about = state.profileDetails.about)
        }

        if (state.profileDetails?.website?.isNotEmpty() == true) {
            val websiteWithProtocol = if (state.profileDetails.website.startsWith(protocolPrefix, true)) {
                state.profileDetails.website
            } else {
                protocolPrefixReplacement + state.profileDetails.website
            }

            UserWebsiteText(
                website = state.profileDetails.website,
                onClick = {
                    try {
                        localUriHandler.openUri(websiteWithProtocol)
                    } catch (error: ActivityNotFoundException) {
                        Timber.w(error)
                        uiScope.launch {
                            Toast.makeText(
                                context,
                                "App not found that could open $websiteWithProtocol.",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
                },
            )
        }

        UserTabs(
            modifier = Modifier.padding(bottom = 8.dp),
            feedDirective = state.profileDirective,
            notesCount = state.profileStats?.notesCount,
            onNotesCountClick = {
                eventPublisher(
                    ProfileDetailsContract.UiEvent.ChangeProfileFeed(
                        profileDirective = ProfileFeedDirective.AuthoredNotes,
                    ),
                )
            },
            repliesCount = state.profileStats?.repliesCount,
            onRepliesCountClick = {
                eventPublisher(
                    ProfileDetailsContract.UiEvent.ChangeProfileFeed(
                        profileDirective = ProfileFeedDirective.AuthoredReplies,
                    ),
                )
            },
            followingCount = state.profileStats?.followingCount,
            onFollowingCountClick = { onFollowsClick(state.profileId, ProfileFollowsType.Following) },
            followersCount = state.profileStats?.followersCount,
            onFollowersCountClick = { onFollowsClick(state.profileId, ProfileFollowsType.Followers) },
        )
    }
}

@Composable
private fun UserTabs(
    feedDirective: ProfileFeedDirective,
    notesCount: Int?,
    onNotesCountClick: () -> Unit,
    repliesCount: Int?,
    onRepliesCountClick: () -> Unit,
    followingCount: Int?,
    onFollowingCountClick: () -> Unit,
    followersCount: Int?,
    onFollowersCountClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "-",
) {
    var tabIndex by remember {
        mutableIntStateOf(
            when (feedDirective) {
                ProfileFeedDirective.AuthoredNotes -> 0
                ProfileFeedDirective.AuthoredReplies -> 1
            },
        )
    }

    TabRow(
        modifier = modifier,
        selectedTabIndex = tabIndex,
        containerColor = Color.Transparent,
        divider = { },
        indicator = { tabPositions ->
            if (tabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                    height = 4.dp,
                    color = AppTheme.colorScheme.tertiary,
                )
            }
        },
    ) {
        CustomTab(
            modifier = Modifier.fillMaxWidth(),
            selected = tabIndex == 0,
            onClick = {
                onNotesCountClick()
                tabIndex = 0
            },
            text = notesCount?.asTabText() ?: placeholderText,
            label = stringResource(id = R.string.profile_notes_stat),
        )

        CustomTab(
            modifier = Modifier.fillMaxWidth(),
            selected = tabIndex == 1,
            onClick = {
                onRepliesCountClick()
                tabIndex = 1
            },
            text = repliesCount?.asTabText() ?: placeholderText,
            label = stringResource(id = R.string.profile_replies_stat),
        )

        CustomTab(
            selected = false,
            onClick = onFollowingCountClick,
            text = followingCount?.asTabText() ?: placeholderText,
            label = stringResource(id = R.string.profile_following_stat),
        )

        CustomTab(
            selected = false,
            onClick = onFollowersCountClick,
            text = followersCount?.asTabText() ?: placeholderText,
            label = stringResource(id = R.string.profile_followers_stat),
        )
    }
}

private const val MAX_ORIGINAL_COUNT = 9999

@Composable
private fun Int.asTabText(): String {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    val formattedInt = numberFormat.format(this)
    return if (this > MAX_ORIGINAL_COUNT) this.shortened() else formattedInt
}

@Composable
private fun CustomTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    label: String,
) {
    Tab(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        selectedContentColor = Color.Unspecified,
        content = {
            Column(
                modifier = Modifier.padding(vertical = 12.dp),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = text,
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.bodyLarge.copy(fontSize = 24.sp),
                    color = AppTheme.colorScheme.onPrimary,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = label,
                    style = AppTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                )
            }
        },
    )
}

@Composable
private fun UserInternetIdentifier(modifier: Modifier = Modifier, internetIdentifier: String) {
    Text(
        modifier = modifier,
        text = internetIdentifier.formatNip05Identifier(),
        style = AppTheme.typography.bodySmall,
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
    )
}

@Composable
private fun ProfileActions(
    isFollowed: Boolean,
    isActiveUser: Boolean,
    onEditProfileClick: () -> Unit,
    onMessageClick: () -> Unit,
    onZapProfileClick: () -> Unit,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp)
            .background(AppTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.End,
    ) {
        ActionButton(
            onClick = onZapProfileClick,
            iconVector = PrimalIcons.FeedZaps,
            contentDescription = stringResource(id = R.string.accessibility_profile_send_zap),
        )

        ActionButton(
            onClick = onMessageClick,
            iconVector = PrimalIcons.Message,
            contentDescription = stringResource(id = R.string.accessibility_profile_messages),
        )

        if (!isActiveUser) {
            when (isFollowed) {
                true -> UnfollowButton(onClick = onUnfollow)
                false -> FollowButton(onClick = onFollow)
            }
        } else {
            Spacer(modifier = Modifier.width(8.dp))
            EditProfileButton(
                onClick = {
                    onEditProfileClick()
                },
            )
        }
    }
}

@Composable
fun ActionButton(
    iconVector: ImageVector,
    onClick: () -> Unit,
    contentDescription: String? = null,
) {
    IconButton(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(36.dp),
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            contentColor = AppTheme.colorScheme.onSurface,
        ),
    ) {
        Icon(
            modifier = Modifier.padding(all = 2.dp),
            imageVector = iconVector,
            contentDescription = contentDescription,
        )
    }
}

@Composable
fun FollowButton(onClick: () -> Unit) {
    ProfileButton(
        text = stringResource(id = R.string.profile_follow_button).lowercase(),
        containerColor = AppTheme.colorScheme.onSurface,
        contentColor = AppTheme.colorScheme.surface,
        onClick = onClick,
    )
}

@Composable
private fun UnfollowButton(onClick: () -> Unit) {
    ProfileButton(
        text = stringResource(id = R.string.profile_unfollow_button).lowercase(),
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        contentColor = AppTheme.colorScheme.onSurface,
        onClick = onClick,
    )
}

@Composable
private fun EditProfileButton(onClick: () -> Unit) {
    ProfileButton(
        text = stringResource(id = R.string.profile_edit_profile_button).lowercase(),
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        contentColor = AppTheme.colorScheme.onSurface,
        onClick = onClick,
    )
}

@Composable
private fun ProfileButton(
    text: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
) {
    PrimalFilledButton(
        modifier = Modifier
            .height(36.dp)
            .wrapContentWidth()
            .defaultMinSize(minWidth = 100.dp),
        contentPadding = PaddingValues(
            horizontal = 16.dp,
            vertical = 0.dp,
        ),
        containerColor = containerColor,
        contentColor = contentColor,
        textStyle = AppTheme.typography.titleMedium.copy(
            lineHeight = 18.sp,
        ),
        onClick = onClick,
    ) {
        Text(text = text)
    }
}

@Composable
private fun UserAbout(about: String) {
    Text(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        text = about,
        color = AppTheme.colorScheme.onSurface,
        style = AppTheme.typography.bodySmall,
    )
}

@Composable
private fun UserWebsiteText(website: String, onClick: () -> Unit) {
    IconText(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        text = website,
        style = AppTheme.typography.bodySmall,
        color = AppTheme.colorScheme.secondary,
        leadingIcon = PrimalIcons.Link,
        iconSize = 12.sp,
        leadingIconTintColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
    )
}

@Composable
private fun UserPublicKey(
    modifier: Modifier = Modifier,
    pubkey: String,
    onCopyClick: (String) -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = CenterVertically,
    ) {
        IconText(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .wrapContentWidth(),
            text = pubkey.asEllipsizedNpub(),
            style = AppTheme.typography.bodySmall,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            leadingIconTintColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            leadingIcon = PrimalIcons.Key,
            iconSize = 12.sp,
        )

        Box(
            modifier = Modifier
                .size(20.dp)
                .clickable(
                    onClick = { onCopyClick(pubkey.hexToNpubHrp()) },
                    role = Role.Button,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                imageVector = Icons.Outlined.ContentCopy,
                colorFilter = ColorFilter.tint(color = AppTheme.colorScheme.primary),
                contentDescription = stringResource(id = R.string.accessibility_copy_content),
            )
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

@Composable
private fun ErrorHandler(error: ProfileError?, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        val errorMessage = when (error) {
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

            else -> return@LaunchedEffect
        }

        snackbarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Short,
        )
    }
}

@Preview
@Composable
fun PreviewProfileScreen() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        CompositionLocalProvider(
            LocalPrimalTheme provides PrimalTheme.Sunset,
        ) {
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
                onPostReplyClick = {},
                onPostQuoteClick = {},
                onProfileClick = {},
                onEditProfileClick = {},
                onMessageClick = {},
                onZapProfileClick = {},
                onHashtagClick = {},
                onMediaClick = { _, _ -> },
                onFollowsClick = { _, _ -> },
                onGoToWallet = {},
                eventPublisher = {},
            )
        }
    }
}
