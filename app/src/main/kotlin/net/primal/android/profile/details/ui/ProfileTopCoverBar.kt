package net.primal.android.profile.details.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import coil.compose.SubcomposeAsyncImage
import net.primal.android.R
import net.primal.android.attachments.domain.findNearestOrNull
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.asEllipsizedNpub
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

@Composable
fun ProfileTopCoverBar(
    state: ProfileDetailsContract.UiState,
    snackbarHostState: SnackbarHostState,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    onClose: () -> Unit,
    titleVisible: Boolean,
    coverValues: CoverValues,
    avatarValues: AvatarValues,
) {
    val coverBlur = AppTheme.colorScheme.surface.copy(alpha = coverValues.coverAlpha)

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
            snackbarHostState = snackbarHostState,
            eventPublisher = eventPublisher,
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(y = avatarValues.avatarOffsetY, x = avatarValues.avatarOffsetX)
                .padding(horizontal = 16.dp),
        ) {
            AvatarThumbnail(
                modifier = Modifier
                    .size(avatarValues.avatarSize)
                    .padding(
                        top = avatarValues.avatarPadding * 0 / 3,
                        bottom = avatarValues.avatarPadding * 3 / 3,
                        start = avatarValues.avatarPadding * 1 / 8,
                        end = avatarValues.avatarPadding * 7 / 8,
                    ),
                avatarCdnImage = state.profileDetails?.avatarCdnImage,
                hasBorder = true,
                borderColor = Color.White,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ProfileTopAppBar(
    state: ProfileDetailsContract.UiState,
    titleVisible: Boolean,
    snackbarHostState: SnackbarHostState,
    eventPublisher: (ProfileDetailsContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Column {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
            ),
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
                    visible = titleVisible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    NostrUserText(
                        displayName = state.profileDetails?.authorDisplayName
                            ?: state.profileId.asEllipsizedNpub(),
                        internetIdentifier = state.profileDetails?.internetIdentifier,
                        internetIdentifierBadgeSize = 20.dp,
                        internetIdentifierBadgeAlign = PlaceholderVerticalAlign.Center,
                    )
                }
            },
            actions = {
                ProfileDropdownMenu(
                    profileId = state.profileId,
                    isActiveUser = state.isActiveUser,
                    isProfileMuted = state.isProfileMuted,
                    isProfileFeedInActiveUserFeeds = state.isProfileFeedInActiveUserFeeds,
                    snackbarHostState = snackbarHostState,
                    profileName = state.profileDetails?.authorDisplayName ?: "",
                    eventPublisher = eventPublisher,
                )
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
