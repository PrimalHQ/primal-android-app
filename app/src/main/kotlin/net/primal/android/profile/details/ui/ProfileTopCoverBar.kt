package net.primal.android.profile.details.ui

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Search
import net.primal.android.events.ui.findNearestOrNull
import net.primal.android.profile.details.ProfileDetailsContract
import net.primal.android.theme.AppTheme

data class AvatarValues(
    val avatarSize: Dp,
    val avatarOffsetY: Dp = 0.dp,
    val avatarOffsetX: Dp = 0.dp,
    val avatarPadding: Dp = 0.dp,
)

data class CoverValues(
    val coverHeight: Dp,
    val coverAlpha: Float = 0.0f,
)

private const val MAX_COVER_TRANSPARENCY = 0.70f

@Composable
fun ProfileTopCoverBar(
    listState: LazyListState,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    state: ProfileDetailsContract.UiState,
    callbacks: ProfileDetailsContract.ScreenCallbacks,
    paddingValues: PaddingValues,
) {
    val density = LocalDensity.current

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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun ProfileTopCoverBar(
    state: ProfileDetailsContract.UiState,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    onMediaItemClick: (String) -> Unit,
    onClose: () -> Unit,
    onSearchClick: () -> Unit,
    titleVisible: Boolean,
    coverValues: CoverValues,
    avatarValues: AvatarValues,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
) {
    val isDarkTheme = LocalPrimalTheme.current.isDarkTheme
    val coverBlur = AppTheme.colorScheme.surface.copy(alpha = coverValues.coverAlpha)

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val variant = state.profileDetails?.coverCdnImage?.variants?.findNearestOrNull(
            maxWidthPx = with(LocalDensity.current) { maxWidth.roundToPx() },
        )
        val imageSource = variant?.mediaUrl ?: state.profileDetails?.coverCdnImage?.sourceUrl
        SubcomposeAsyncImage(
            modifier = Modifier
                .clickable { imageSource?.let { onMediaItemClick(it) } }
                .background(color = AppTheme.colorScheme.surface)
                .fillMaxWidth()
                .height(coverValues.coverHeight)
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

        ProfileTopAppBar(
            state = state,
            onClose = onClose,
            titleVisible = titleVisible,
            eventPublisher = eventPublisher,
            paddingValues = paddingValues,
            onSearchClick = onSearchClick,
            onMediaClick = { imageSource?.let { onMediaItemClick(it) } },
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = avatarValues.avatarOffsetY, x = avatarValues.avatarOffsetX)
                .padding(horizontal = 12.dp),
        ) {
            val legendaryCustomization = state.profileDetails?.premiumDetails?.legendaryCustomization
            UniversalAvatarThumbnail(
                modifier = Modifier
                    .padding(
                        top = avatarValues.avatarPadding * 0 / 3,
                        bottom = avatarValues.avatarPadding * 3 / 3,
                        start = avatarValues.avatarPadding * 1 / 8,
                        end = avatarValues.avatarPadding * 7 / 8,
                    ),
                avatarSize = avatarValues.avatarSize,
                onClick = { state.profileDetails?.avatarCdnImage?.sourceUrl?.let { onMediaItemClick(it) } },
                avatarCdnImage = state.profileDetails?.avatarCdnImage,
                avatarBlossoms = state.profileDetails?.profileBlossoms ?: emptyList(),
                fallbackBorderColor = if (isDarkTheme) Color.Black else Color.White,
                borderSizeOverride = if (legendaryCustomization == null) 5.dp else null,
                legendaryCustomization = legendaryCustomization,
                forceAnimationIfAvailable = true,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ProfileTopAppBar(
    state: ProfileDetailsContract.UiState,
    titleVisible: Boolean,
    paddingValues: PaddingValues,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    onClose: () -> Unit,
    onSearchClick: () -> Unit,
    onMediaClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Column(
        modifier = Modifier.padding(paddingValues = paddingValues),
    ) {
        TopAppBar(
            modifier = Modifier.clickable(
                interactionSource = interactionSource,
                indication = null,
            ) { if (!titleVisible) onMediaClick() },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
            navigationIcon = {
                ProfileAppBarIcon(
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
                    visible = titleVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    NostrUserText(
                        modifier = Modifier.padding(top = 4.dp),
                        displayName = state.resolveProfileName(),
                        internetIdentifier = state.profileDetails?.internetIdentifier,
                        internetIdentifierBadgeSize = 20.dp,
                        internetIdentifierBadgeAlign = PlaceholderVerticalAlign.Center,
                        legendaryCustomization = state.profileDetails?.premiumDetails?.legendaryCustomization,
                    )
                }
            },
            actions = {
                val profileName = state.resolveProfileName()
                ProfileAppBarIcon(
                    icon = PrimalIcons.Search,
                    onClick = onSearchClick,
                    appBarIconContentDescription = if (profileName.isEmpty()) {
                        stringResource(id = R.string.accessibility_search)
                    } else {
                        stringResource(id = R.string.accessibility_profile_search, profileName)
                    },
                    enabledBackgroundColor = Color.Black.copy(alpha = 0.5f),
                    tint = Color.White,
                )

                Spacer(modifier = Modifier.width(12.dp))

                if (state.profileId != null) {
                    ProfileDropdownMenu(
                        profileId = state.profileId,
                        isActiveUser = state.isActiveUser == true,
                        isProfileMuted = state.isProfileMuted,
                        isProfileFeedInActiveUserFeeds = state.isProfileFeedInActiveUserFeeds,
                        profileName = profileName,
                        eventPublisher = eventPublisher,
                        primalName = state.profileDetails?.primalName,
                    )
                }
            },
        )

        if (titleVisible) {
            PrimalDivider()
        }
    }
}

@Composable
private fun CoverLoading() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppTheme.colorScheme.surface),
    )
}

@Composable
private fun CoverUnavailable() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = AppTheme.colorScheme.surface),
    )
}
