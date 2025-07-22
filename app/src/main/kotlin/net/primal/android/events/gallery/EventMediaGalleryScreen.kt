package net.primal.android.events.gallery

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.imageLoader
import coil3.memory.MemoryCache
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.toBitmap
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.github.panpf.zoomimage.rememberCoilZoomState
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.HorizontalPagerIndicator
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.attachment.model.EventUriUi
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.core.compose.dropdown.DropdownPrimalMenuItem
import net.primal.android.core.compose.foundation.KeepScreenOn
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyNoteLink
import net.primal.android.core.compose.icons.primaliconpack.ContextCopyRawData
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.compose.immersive.ImmersiveModeState
import net.primal.android.core.compose.immersive.rememberImmersiveModeState
import net.primal.android.core.utils.copyBitmapToClipboard
import net.primal.android.core.utils.copyText
import net.primal.android.core.video.initializePlayer
import net.primal.android.theme.AppTheme
import net.primal.domain.links.EventUriType

@Composable
fun EventMediaGalleryScreen(viewModel: EventMediaGalleryViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()
    val context = LocalContext.current

    val uiScope = rememberCoroutineScope()
    LaunchedEffect(viewModel) {
        viewModel.effects.collect {
            when (it) {
                is EventMediaGalleryContract.SideEffect.MediaSaved -> uiScope.launch {
                    val message = when (it.type) {
                        EventUriType.Image -> context.getString(R.string.media_gallery_toast_photo_saved)
                        EventUriType.Video -> context.getString(R.string.media_gallery_toast_video_saved)
                        else -> context.getString(R.string.media_gallery_toast_file_saved)
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    EventMediaGalleryScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@androidx.annotation.OptIn(UnstableApi::class)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun EventMediaGalleryScreen(
    state: EventMediaGalleryContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (EventMediaGalleryContract.UiEvent) -> Unit,
) {
    val context = LocalContext.current
    val window = LocalActivity.current?.window
    val immersiveMode = window?.let { rememberImmersiveModeState(window = window) }

    var mediaItemBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val pagerState = rememberPagerState(
        initialPage = state.initialAttachmentIndex,
        pageCount = { state.attachments.size },
    )

    fun currentImage() = state.attachments.getOrNull(pagerState.currentPage)

    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is EventMediaGalleryContract.UiState.MediaGalleryError.FailedToSaveMedia ->
                    context.getString(R.string.media_gallery_error_photo_not_saved)
            }
        },
        actionLabel = stringResource(id = R.string.media_gallery_retry_save),
        onErrorDismiss = { eventPublisher(EventMediaGalleryContract.UiEvent.DismissError) },
        onActionPerformed = { currentImage()?.let { eventPublisher(EventMediaGalleryContract.UiEvent.SaveMedia(it)) } },
    )

    val containerColor = AppTheme.colorScheme.surface.copy(alpha = 0.21f)

    Scaffold(
        contentColor = AppTheme.colorScheme.background,
        topBar = {
            AnimatedVisibility(
                visible = immersiveMode?.isImmersive != true,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            ) {
                MediaGalleryTopAppBar(
                    onClose = onClose,
                    containerColor = containerColor,
                    eventPublisher = eventPublisher,
                    mediaItemBitmap = mediaItemBitmap,
                    getCurrentImage = { currentImage() },
                )
            }
        },
        content = {
            MediaGalleryContent(
                pagerState = pagerState,
                initialAttachmentIndex = state.initialAttachmentIndex,
                initialPositionMs = state.initialPositionMs,
                attachments = state.attachments,
                pagerIndicatorContainerColor = containerColor,
                onCurrentlyVisibleBitmap = { mediaItemBitmap = it },
                immersiveMode = immersiveMode,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
@ExperimentalMaterial3Api
private fun MediaGalleryTopAppBar(
    onClose: () -> Unit,
    containerColor: Color,
    eventPublisher: (EventMediaGalleryContract.UiEvent) -> Unit,
    mediaItemBitmap: Bitmap?,
    getCurrentImage: () -> EventUriUi?,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
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
            containerColor = containerColor,
            scrolledContainerColor = containerColor,
        ),
        actions = {
            GalleryDropdownMenu(
                onSaveClick = {
                    getCurrentImage()?.let { eventPublisher(EventMediaGalleryContract.UiEvent.SaveMedia(it)) }
                },
                onMediaUrlCopyClick = {
                    getCurrentImage()?.url?.let { copyText(context = context, text = it) }
                },
                onMediaCopyClick = {
                    mediaItemBitmap?.let {
                        coroutineScope.launch {
                            copyBitmapToClipboard(
                                context = context,
                                bitmap = it,
                                errorMessage = context.getString(R.string.media_gallery_error_photo_not_copied),
                            )
                        }
                    }
                },
                showCopyMediaMenuItem = getCurrentImage()?.type == EventUriType.Image,
            )
        },
    )
}

@UnstableApi
@ExperimentalFoundationApi
@Composable
private fun MediaGalleryContent(
    pagerState: PagerState,
    initialAttachmentIndex: Int,
    initialPositionMs: Long,
    attachments: List<EventUriUi>,
    pagerIndicatorContainerColor: Color,
    immersiveMode: ImmersiveModeState?,
    onCurrentlyVisibleBitmap: ((Bitmap?) -> Unit)? = null,
) {
    val context = LocalContext.current
    val videoAttachments = remember(attachments) { attachments.filter { it.type == EventUriType.Video } }

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    LifecycleStartEffect(Unit) {
        exoPlayer = initializePlayer(context = context)
        onStopOrDispose {
            exoPlayer?.apply { release() }
            exoPlayer = null
        }
    }

    LaunchedEffect(exoPlayer, videoAttachments) {
        exoPlayer?.let { exoPlayer ->
            if (videoAttachments.isNotEmpty()) {
                val videoMediaItems = videoAttachments.map {
                    MediaItem.Builder()
                        .setMediaId(it.url)
                        .setUri(it.variants?.firstOrNull()?.mediaUrl ?: it.url)
                        .build()
                }

                val initialAttachment = attachments.getOrNull(initialAttachmentIndex)
                val startVideoIndex = if (initialAttachment?.type == EventUriType.Video) {
                    videoAttachments.indexOf(initialAttachment).coerceAtLeast(0)
                } else {
                    0
                }

                exoPlayer.setMediaItems(videoMediaItems, startVideoIndex, C.TIME_UNSET)
                exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
                exoPlayer.prepare()
                exoPlayer.playWhenReady = false

                if (initialPositionMs > 0) {
                    exoPlayer.seekTo(startVideoIndex, initialPositionMs)
                }
            }
        }
    }

    LaunchedEffect(exoPlayer, pagerState, videoAttachments) {
        exoPlayer?.let { exoPlayer ->
            snapshotFlow { pagerState.currentPage }.distinctUntilChanged().collect { page ->
                val attachment = attachments.getOrNull(page)
                if (attachment?.type == EventUriType.Video) {
                    val videoIndex = videoAttachments.indexOf(attachment)
                    if (videoIndex != -1 && exoPlayer.currentMediaItemIndex != videoIndex) {
                        exoPlayer.seekTo(videoIndex, C.TIME_UNSET)
                    }
                    exoPlayer.playWhenReady = true
                } else {
                    exoPlayer.pause()
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        exoPlayer?.let { player ->
            if (attachments.isNotEmpty()) {
                AttachmentsHorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    attachments = attachments,
                    pagerState = pagerState,
                    onCurrentlyVisibleBitmap = onCurrentlyVisibleBitmap,
                    immersiveMode = immersiveMode,
                    exoPlayer = player,
                    initialIndex = initialAttachmentIndex,
                    initialPositionMs = initialPositionMs,
                )
            }
        }

        if (attachments.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(color = pagerIndicatorContainerColor, shape = AppTheme.shapes.large)
                    .padding(horizontal = 16.dp),
            ) {
                HorizontalPagerIndicator(
                    modifier = Modifier.height(32.dp),
                    pagesCount = attachments.size,
                    currentPage = pagerState.currentPage,
                )
            }
        }
    }
}

@Composable
fun GalleryDropdownMenu(
    onSaveClick: () -> Unit,
    onMediaUrlCopyClick: () -> Unit,
    onMediaCopyClick: () -> Unit,
    showCopyMediaMenuItem: Boolean = true,
) {
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
        MediaUrlCopyMenuItem(
            onMediaUrlCopyClick = {
                menuVisible = false
                onMediaUrlCopyClick()
            },
        )
        if (showCopyMediaMenuItem) {
            MediaCopyMenuItem(
                onMediaCopyClick = {
                    menuVisible = false
                    onMediaCopyClick()
                },
            )
        }
    }
}

@Composable
private fun SaveMediaMenuItem(onSaveClick: () -> Unit) {
    val context = LocalContext.current

    val hasExternalStoragePermission by remember {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
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
            if (hasExternalStoragePermission) {
                onSaveClick()
            } else {
                launcher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        },
    )
}

@Composable
private fun MediaUrlCopyMenuItem(onMediaUrlCopyClick: () -> Unit) {
    val context = LocalContext.current
    val copyConfirmationText = stringResource(id = R.string.media_gallery_context_copy_url_toast_success)
    val uiScope = rememberCoroutineScope()

    DropdownPrimalMenuItem(
        trailingIconVector = PrimalIcons.ContextCopyNoteLink,
        text = stringResource(id = R.string.media_gallery_context_copy_image_url),
        onClick = {
            uiScope.launch {
                Toast.makeText(
                    context,
                    copyConfirmationText,
                    Toast.LENGTH_SHORT,
                ).show()
            }
            onMediaUrlCopyClick()
        },
    )
}

@Composable
private fun MediaCopyMenuItem(onMediaCopyClick: () -> Unit) {
    val context = LocalContext.current
    val copyConfirmationText = stringResource(id = R.string.media_gallery_context_copy_url_toast_success)
    val uiScope = rememberCoroutineScope()

    DropdownPrimalMenuItem(
        trailingIconVector = PrimalIcons.ContextCopyRawData,
        text = stringResource(id = R.string.media_gallery_context_copy_image),
        onClick = {
            uiScope.launch {
                Toast.makeText(
                    context,
                    copyConfirmationText,
                    Toast.LENGTH_SHORT,
                ).show()
            }
            onMediaCopyClick()
        },
    )
}

@ExperimentalFoundationApi
@Composable
private fun AttachmentsHorizontalPager(
    pagerState: PagerState,
    attachments: List<EventUriUi>,
    immersiveMode: ImmersiveModeState?,
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier,
    initialIndex: Int = 0,
    initialPositionMs: Long = 0,
    onCurrentlyVisibleBitmap: ((Bitmap?) -> Unit)? = null,
) {
    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        beyondViewportPageCount = 0,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = spring(stiffness = Spring.StiffnessLow),
        ),
    ) { index ->
        val attachment = attachments[index]
        Box(modifier = Modifier.fillMaxSize()) {
            when (attachment.type) {
                EventUriType.Image -> {
                    ImageScreen(
                        modifier = Modifier.fillMaxSize(),
                        attachment = attachment,
                        onImageBitmapLoaded = { onCurrentlyVisibleBitmap?.invoke(it) },
                        immersiveMode = immersiveMode,
                    )
                }

                EventUriType.Video -> {
                    VideoScreen(
                        modifier = Modifier.fillMaxSize(),
                        exoPlayer = exoPlayer,
                        positionMs = initialPositionMs,
                        isPageVisible = pagerState.currentPage == index,
                        immersiveMode = immersiveMode,
                    )
                }

                else -> Unit
            }
        }
    }

    LaunchedEffect(initialIndex) {
        pagerState.scrollToPage(initialIndex)
    }
}

@Composable
private fun ImageScreen(
    attachment: EventUriUi,
    onImageBitmapLoaded: (Bitmap?) -> Unit,
    immersiveMode: ImmersiveModeState?,
    modifier: Modifier = Modifier,
) {
    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var error by remember { mutableStateOf<ErrorResult?>(null) }
    val imageLoader = LocalContext.current.imageLoader
    val memoryCache = imageLoader.memoryCache

    val keys = attachment.variants.orEmpty()
        .sortedBy { it.width }
        .map { MemoryCache.Key(it.mediaUrl) }
        .filter { memoryCache?.get(it) != null }

    val highestResMirrorImage = keys.firstOrNull()?.let { memoryCache?.get(it) }?.image
    val placeholderKey = keys.lastOrNull()

    val loadingImageListener = remember {
        object : ImageRequest.Listener {
            override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                error = null
                loadedBitmap = result.image.toBitmap()
            }

            override fun onError(request: ImageRequest, result: ErrorResult) {
                error = result
                loadedBitmap = highestResMirrorImage?.toBitmap()
            }
        }
    }

    LaunchedEffect(loadedBitmap, attachment) {
        onImageBitmapLoaded(loadedBitmap)
    }

    val zoomState = rememberCoilZoomState()
    zoomState.zoomable.keepTransformWhenSameAspectRatioContentSizeChanged = true

    CoilZoomAsyncImage(
        zoomState = zoomState,
        imageLoader = imageLoader,
        modifier = modifier,
        onTap = { immersiveMode?.toggle() },
        model = ImageRequest.Builder(LocalContext.current)
            .data(attachment.url)
            .placeholderMemoryCacheKey(placeholderKey)
            .error(highestResMirrorImage)
            .fallback(highestResMirrorImage)
            .listener(loadingImageListener)
            .build(),
        contentDescription = null,
    )

    if (error != null && highestResMirrorImage == null) {
        AttachmentLoadingError()
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
            tint = Color.White,
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoScreen(
    exoPlayer: ExoPlayer,
    isPageVisible: Boolean,
    positionMs: Long,
    modifier: Modifier = Modifier,
    immersiveMode: ImmersiveModeState?,
) {
    var isBuffering by remember { mutableStateOf(false) }
    if (isPageVisible) {
        KeepScreenOn()
    }

    LaunchedEffect(isPageVisible, immersiveMode) {
        if (isPageVisible) {
            immersiveMode?.hide()
            delay(2.seconds.inWholeMilliseconds)
            immersiveMode?.show()
        }
    }

    LifecycleStartEffect(exoPlayer, isPageVisible) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (isPageVisible) {
                    isBuffering = playbackState == Player.STATE_BUFFERING
                }
            }
        }
        exoPlayer.addListener(listener)
        exoPlayer.seekTo(positionMs)
        onStopOrDispose {
            exoPlayer.removeListener(listener)
        }
    }

    var playerView by remember { mutableStateOf<PlayerView?>(null) }

    LaunchedEffect(playerView, isPageVisible) {
        if (isPageVisible) {
            playerView?.player = exoPlayer
        }
    }

    Box(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                PlayerView(context).apply {
                    useController = true
                    controllerShowTimeoutMs = 1.seconds.inWholeMilliseconds.toInt()
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    setControllerVisibilityListener(
                        PlayerView.ControllerVisibilityListener { visibility ->
                            if (isPageVisible) {
                                if (visibility == View.VISIBLE) {
                                    immersiveMode?.hide()
                                } else {
                                    immersiveMode?.show()
                                }
                            }
                        },
                    )
                }.also {
                    playerView = it
                }
            },
            onRelease = { view ->
                view.player = null
            },
        )

        if (isBuffering && isPageVisible) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}
