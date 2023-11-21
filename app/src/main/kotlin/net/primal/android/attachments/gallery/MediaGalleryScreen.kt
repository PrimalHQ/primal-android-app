package net.primal.android.attachments.gallery

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

@Composable
fun MediaGalleryScreen(onClose: () -> Unit, viewModel: MediaGalleryViewModel) {
    val uiState = viewModel.state.collectAsState()
    MediaGalleryScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MediaGalleryScreen(state: MediaGalleryContract.UiState, onClose: () -> Unit) {
    Surface(color = AppTheme.colorScheme.background) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopStart,
        ) {
            if (state.attachments.isNotEmpty()) {
                AttachmentsHorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    imageAttachments = state.attachments,
                    initialIndex = state.initialAttachmentIndex,
                )
            }

            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    AppBarIcon(icon = PrimalIcons.ArrowBack, onClick = onClose)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppTheme.colorScheme.surface.copy(alpha = 0.2f),
                    scrolledContainerColor = AppTheme.colorScheme.surface.copy(alpha = 0.2f),
                ),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AttachmentsHorizontalPager(
    modifier: Modifier = Modifier,
    imageAttachments: List<NoteAttachmentUi>,
    initialIndex: Int = 0,
) {
    val zoomSpec = ZoomSpec(maxZoomFactor = 4.0f)
    val pagerState = rememberPagerState { imageAttachments.size }

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        beyondBoundsPageCount = 1,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = spring(stiffness = Spring.StiffnessLow),
        ),
    ) { index ->
        val imageUrl = imageAttachments[index].url
        Box(modifier = Modifier.fillMaxSize()) {
            val loading by remember { mutableStateOf(true) }

            if (loading) {
                PrimalLoadingSpinner()
            }

            val zoomableImageState = rememberZoomableImageState(rememberZoomableState(zoomSpec = zoomSpec))
            ZoomableAsyncImage(
                modifier = modifier,
                state = zoomableImageState,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .placeholderMemoryCacheKey(imageUrl)
                    .crossfade(IMAGE_CROSSFADE_DURATION)
                    .build(),
                contentDescription = null,
            )
        }
    }

    LaunchedEffect(initialIndex) {
        pagerState.scrollToPage(initialIndex)
    }
}

private const val IMAGE_CROSSFADE_DURATION = 300
