package net.primal.android.attachments.gallery

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.imageLoader
import coil.memory.MemoryCache
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.ZoomSpec
import me.saket.telephoto.zoomable.coil.ZoomableAsyncImage
import me.saket.telephoto.zoomable.rememberZoomableImageState
import me.saket.telephoto.zoomable.rememberZoomableState
import net.primal.android.R
import net.primal.android.attachments.domain.NoteAttachmentType
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.HorizontalPagerIndicator
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.attachment.model.NoteAttachmentUi
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.core.compose.dropdown.DropdownPrimalMenuItem
import net.primal.android.core.compose.foundation.KeepScreenOn
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.theme.AppTheme

@Composable
fun MediaGalleryScreen(viewModel: MediaGalleryViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    val uiScope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.effects.collect {
            when (it) {
                is MediaGalleryContract.SideEffect.MediaSaved -> uiScope.launch {
                    val message = when (it.type) {
                        NoteAttachmentType.Image -> context.getString(R.string.media_gallery_toast_photo_saved)
                        NoteAttachmentType.Video -> context.getString(R.string.media_gallery_toast_video_saved)
                        else -> context.getString(R.string.media_gallery_toast_file_saved)
                    }
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MediaGalleryScreen(
    state: MediaGalleryContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (MediaGalleryContract.UiEvent) -> Unit,
) {
    val imageAttachments = state.attachments
    val pagerState = rememberPagerState { imageAttachments.size }

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
        onActionPerformed = { currentImage()?.let { eventPublisher(MediaGalleryContract.UiEvent.SaveMedia(it)) } },
    )

    val containerColor = AppTheme.colorScheme.surface.copy(alpha = 0.21f)

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
                    containerColor = containerColor,
                    scrolledContainerColor = containerColor,
                ),
                actions = {
                    GalleryDropdownMenu(
                        onSaveClick = {
                            currentImage()?.let { eventPublisher(MediaGalleryContract.UiEvent.SaveMedia(it)) }
                        },
                    )
                },
            )
        },
        content = {
            MediaGalleryContent(
                pagerState = pagerState,
                initialAttachmentIndex = state.initialAttachmentIndex,
                initialPositionMs = state.initialPositionMs,
                imageAttachments = imageAttachments,
                pagerIndicatorContainerColor = containerColor,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@ExperimentalFoundationApi
@Composable
private fun MediaGalleryContent(
    pagerState: PagerState,
    initialAttachmentIndex: Int,
    initialPositionMs: Long,
    imageAttachments: List<NoteAttachmentUi>,
    pagerIndicatorContainerColor: Color,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (imageAttachments.isNotEmpty()) {
            AttachmentsHorizontalPager(
                modifier = Modifier.fillMaxSize(),
                imageAttachments = imageAttachments,
                pagerState = pagerState,
                initialIndex = initialAttachmentIndex,
                initialPositionMs = initialPositionMs,
            )
        }

        if (imageAttachments.size > 1) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(color = pagerIndicatorContainerColor, shape = AppTheme.shapes.large)
                    .padding(horizontal = 16.dp),
            ) {
                HorizontalPagerIndicator(
                    modifier = Modifier.height(32.dp),
                    pagesCount = imageAttachments.size,
                    currentPage = pagerState.currentPage,
                )
            }
        }
    }
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
    initialPositionMs: Long = 0,
) {
    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        beyondViewportPageCount = 1,
        flingBehavior = PagerDefaults.flingBehavior(
            state = pagerState,
            snapAnimationSpec = spring(stiffness = Spring.StiffnessLow),
        ),
    ) { index ->
        val attachment = imageAttachments[index]
        Box(modifier = Modifier.fillMaxSize()) {
            when (attachment.type) {
                NoteAttachmentType.Image -> {
                    ImageScreen(
                        modifier = Modifier.fillMaxSize(),
                        attachment = attachment,
                    )
                }

                NoteAttachmentType.Video -> {
                    VideoScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding(),
                        positionMs = initialPositionMs,
                        attachment = attachment,
                        isPageVisible = pagerState.currentPage == index,
                    )
                }

                else -> Unit // Not supported
            }
        }
    }

    LaunchedEffect(initialIndex) {
        pagerState.scrollToPage(initialIndex)
    }
}

@Composable
private fun ImageScreen(attachment: NoteAttachmentUi, modifier: Modifier = Modifier) {
    val zoomSpec = ZoomSpec(maxZoomFactor = 2.5f)

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
            .data(attachment.url)
            .placeholderMemoryCacheKey(keys.lastOrNull())
            .listener(loadingImageListener)
            .crossfade(durationMillis = 300)
            .build(),
        contentDescription = null,
    )

    if (error != null) {
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
        )
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoScreen(
    modifier: Modifier = Modifier,
    positionMs: Long,
    attachment: NoteAttachmentUi,
    isPageVisible: Boolean,
) {
    val context = LocalContext.current

    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val mediaUrl = attachment.variants?.firstOrNull()?.mediaUrl ?: attachment.url
    val mediaSource = MediaItem.fromUri(mediaUrl)
    var playerState by remember { mutableIntStateOf(Player.STATE_IDLE) }

    KeepScreenOn()

    LaunchedEffect(mediaSource) {
        exoPlayer.addListener(
            object : Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    playerState = playbackState
                }
            },
        )
        exoPlayer.setMediaItem(mediaSource)
        exoPlayer.prepare()
        exoPlayer.seekTo(positionMs)
        exoPlayer.playWhenReady = true
        exoPlayer.repeatMode = ExoPlayer.REPEAT_MODE_ALL
        exoPlayer.volume = 1.0f
    }

    LaunchedEffect(isPageVisible) {
        if (!isPageVisible) {
            exoPlayer.pause()
        }
    }

    DisposableLifecycleObserverEffect(mediaSource) {
        if (it == Lifecycle.Event.ON_PAUSE) {
            exoPlayer.pause()
        }
    }

    DisposableEffect(mediaSource) {
        onDispose { exoPlayer.release() }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    setShowNextButton(false)
                    setShowPreviousButton(false)
                    controllerShowTimeoutMs = 1.seconds.inWholeMilliseconds.toInt()
                    useController = true
                    player = exoPlayer
                }
            },
        )
    }
}
