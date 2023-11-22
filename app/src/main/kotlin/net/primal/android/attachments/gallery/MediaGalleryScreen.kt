package net.primal.android.attachments.gallery

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.HorizontalPagerIndicator
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MediaGalleryScreen(state: MediaGalleryContract.UiState, onClose: () -> Unit) {
    Surface(color = AppTheme.colorScheme.background) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            contentAlignment = Alignment.TopStart,
        ) {
            val imageAttachments = state.attachments
            val imagesCount = imageAttachments.size

            val pagerState = rememberPagerState { imagesCount }
            if (imageAttachments.isNotEmpty()) {
                AttachmentsHorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    imageAttachments = imageAttachments,
                    pagerState = pagerState,
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

            if (imagesCount > 1) {
                HorizontalPagerIndicator(
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    imagesCount = imagesCount,
                    currentPage = pagerState.currentPage,
                )
            }
        }
    }
}

@ExperimentalFoundationApi
@Composable
private fun AttachmentsHorizontalPager(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    imageAttachments: List<NoteAttachmentUi>,
    initialIndex: Int = 0,
) {
    val zoomSpec = ZoomSpec(maxZoomFactor = 2.5f)

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        beyondBoundsPageCount = 1,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = spring(stiffness = Spring.StiffnessLow),
        ),
    ) { index ->
        val attachment = imageAttachments[index]
        val imageUrl = attachment.url
        Box(modifier = Modifier.fillMaxSize()) {
            var error by remember { mutableStateOf<ErrorResult?>(null) }
            val loadingImageListener = remember {
                object : ImageRequest.Listener {
                    override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                        error = null
                    }

                    override fun onError(request: ImageRequest, result: ErrorResult) {
                        error = result
                    }
                }
            }

            val imageLoader = LocalContext.current.imageLoader
            val keys = attachment.variants.orEmpty()
                .sortedBy { it.width }
                .mapNotNull {
                    val cacheKey = MemoryCache.Key(it.mediaUrl)
                    if (imageLoader.memoryCache?.keys?.contains(cacheKey) == true) cacheKey else null
                }

            ZoomableAsyncImage(
                modifier = modifier,
                state = rememberZoomableImageState(rememberZoomableState(zoomSpec = zoomSpec)),
                imageLoader = imageLoader,
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .placeholderMemoryCacheKey(keys.lastOrNull())
                    .listener(loadingImageListener)
                    .crossfade(IMAGE_CROSSFADE_DURATION)
                    .build(),
                contentDescription = null,
            )

            if (error != null) {
                AttachmentLoadingError()
            }
        }
    }

    LaunchedEffect(initialIndex) {
        pagerState.scrollToPage(initialIndex)
    }
}

@Composable
private fun AttachmentLoadingError() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
        )
    }
}

private const val IMAGE_CROSSFADE_DURATION = 300
