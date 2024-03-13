package net.primal.android.attachments.gallery

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.HorizontalPagerIndicator
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.core.compose.dropdown.DropdownPrimalMenuItem
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.theme.AppTheme

@Composable
fun MediaGalleryScreen(viewModel: MediaGalleryViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    val uiScope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.effects.collect {
            when (it) {
                MediaGalleryContract.SideEffect.MediaSaved -> uiScope.launch {
                    Toast.makeText(
                        context,
                        context.getString(R.string.media_gallery_toast_photo_saved),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        }
    }

    MediaGalleryScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MediaGalleryScreen(
    state: MediaGalleryContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (MediaGalleryContract.UiEvent) -> Unit,
) {
    val imageAttachments = state.attachments
    val imagesCount = imageAttachments.size
    val pagerState = rememberPagerState { imagesCount }

    fun currentImage() = imageAttachments.getOrNull(pagerState.currentPage)

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is MediaGalleryContract.UiState.MediaGalleryError.FailedToSaveMedia ->
                    context.getString(R.string.media_gallery_error_photo_not_saved)
            }
        },
        actionLabel = stringResource(id = R.string.media_gallery_retry_save),
        onErrorDismiss = { eventPublisher(MediaGalleryContract.UiEvent.DismissError) },
        onActionPerformed = { currentImage()?.let { eventPublisher(MediaGalleryContract.UiEvent.SaveMedia(it.url)) } },
    )

    Scaffold(
        contentColor = AppTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    AppBarIcon(
                        icon = PrimalIcons.ArrowBack,
                        onClick = onClose,
                        appBarIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppTheme.colorScheme.surface.copy(alpha = 0.2f),
                    scrolledContainerColor = AppTheme.colorScheme.surface.copy(alpha = 0.2f),
                ),
                actions = {
                    GalleryDropdownMenu(
                        onSaveClick = {
                            currentImage()?.let { eventPublisher(MediaGalleryContract.UiEvent.SaveMedia(it.url)) }
                        },
                    )
                },
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .systemBarsPadding(),
                contentAlignment = Alignment.TopStart,
            ) {
                if (imageAttachments.isNotEmpty()) {
                    AttachmentsHorizontalPager(
                        modifier = Modifier.fillMaxSize(),
                        imageAttachments = imageAttachments,
                        pagerState = pagerState,
                        initialIndex = state.initialAttachmentIndex,
                    )
                }

                if (imagesCount > 1) {
                    HorizontalPagerIndicator(
                        modifier = Modifier
                            .height(32.dp)
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        pagesCount = imagesCount,
                        currentPage = pagerState.currentPage,
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun GalleryDropdownMenu(onSaveClick: () -> Unit) {
    var menuVisible by remember { mutableStateOf(false) }

    AppBarIcon(
        icon = PrimalIcons.More,
        onClick = { menuVisible = true },
        appBarIconContentDescription = stringResource(id = R.string.accessibility_media_drop_down),
    )

    DropdownPrimalMenu(
        expanded = menuVisible,
        onDismissRequest = { menuVisible = false },
    ) {
        SaveMediaMenuItem(
            onSaveClick = {
                menuVisible = false
                onSaveClick()
            },
        )
    }
}

@Composable
private fun SaveMediaMenuItem(onSaveClick: () -> Unit) {
    val context = LocalContext.current

    val hasExternalStoragePermission by remember {
        val permission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        mutableStateOf(permission == PackageManager.PERMISSION_GRANTED)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> if (granted) onSaveClick() },
    )

    DropdownPrimalMenuItem(
        trailingIconVector = Icons.Default.FileDownload,
        text = stringResource(id = R.string.media_gallery_context_save),
        onClick = {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || hasExternalStoragePermission) {
                onSaveClick()
            } else {
                launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        },
    )
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
            contentDescription = stringResource(id = R.string.accessibility_warning),
            modifier = Modifier.size(48.dp),
        )
    }
}

private const val IMAGE_CROSSFADE_DURATION = 300
