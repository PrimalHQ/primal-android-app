package net.primal.android.media

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil3.imageLoader
import coil3.request.ErrorResult
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.crossfade
import coil3.toBitmap
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.immersive.rememberImmersiveModeState
import net.primal.android.core.utils.copyBitmapToClipboard
import net.primal.android.core.utils.copyText
import net.primal.android.events.gallery.GalleryDropdownMenu
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalSharedTransitionApi
@Composable
fun MediaItemScreen(
    viewModel: MediaItemViewModel,
    onClose: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val uiState = viewModel.state.collectAsState()

    val uiScope = rememberCoroutineScope()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.effects.collect {
            when (it) {
                MediaItemContract.SideEffect.MediaSaved -> uiScope.launch {
                    Toast.makeText(context, context.getString(R.string.media_item_saved), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    MediaItemScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = viewModel::setEvent,
        sharedTransitionScope = sharedTransitionScope,
        animatedVisibilityScope = animatedVisibilityScope,
    )
}

@ExperimentalMaterial3Api
@ExperimentalSharedTransitionApi
@Composable
private fun MediaItemScreen(
    state: MediaItemContract.UiState,
    eventPublisher: (MediaItemContract.UiEvent) -> Unit,
    onClose: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val window = LocalActivity.current?.window
    val immersiveMode = window?.let { rememberImmersiveModeState(window = window) }

    var mediaItemBitmap by remember { mutableStateOf<Bitmap?>(null) }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is MediaItemContract.UiState.MediaItemError.FailedToSaveMedia ->
                    stringResource(id = R.string.media_item_error_on_save_message)
            }
        },
        actionLabel = stringResource(id = R.string.media_item_error_on_save_try_again),
        onErrorDismiss = { eventPublisher(MediaItemContract.UiEvent.DismissError) },
        onActionPerformed = { eventPublisher(MediaItemContract.UiEvent.SaveMedia) },
    )
    val containerColor = AppTheme.colorScheme.surface.copy(alpha = 0.21f)

    Scaffold(
        contentColor = AppTheme.colorScheme.background,
        topBar = {
            AnimatedVisibility(
                visible = immersiveMode?.isImmersive != true,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                ) + fadeOut(),
            ) {
                MediaItemTopAppBar(
                    onClose = onClose,
                    containerColor = containerColor,
                    eventPublisher = eventPublisher,
                    state = state,
                    mediaItemBitmap = mediaItemBitmap,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) {
        with(sharedTransitionScope) {
            MediaItemContent(
                modifier = Modifier.padding(it),
                mediaUrl = state.mediaUrl,
                animatedVisibilityScope = animatedVisibilityScope,
                onMediaLoaded = { mediaItemBitmap = it },
                onToggleImmersiveMode = { immersiveMode?.toggle() },
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun MediaItemTopAppBar(
    onClose: () -> Unit,
    containerColor: Color,
    eventPublisher: (MediaItemContract.UiEvent) -> Unit,
    state: MediaItemContract.UiState,
    mediaItemBitmap: Bitmap?,
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
                onSaveClick = { eventPublisher(MediaItemContract.UiEvent.SaveMedia) },
                onMediaUrlCopyClick = {
                    copyText(context = context, text = state.mediaUrl)
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
            )
        },
    )
}

@ExperimentalSharedTransitionApi
@Composable
fun SharedTransitionScope.MediaItemContent(
    modifier: Modifier = Modifier,
    mediaUrl: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onToggleImmersiveMode: () -> Unit,
    onMediaLoaded: ((Bitmap) -> Unit),
) {
    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var error by remember { mutableStateOf<ErrorResult?>(null) }
    val loadingImageListener = remember {
        object : ImageRequest.Listener {
            override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                error = null
                loadedBitmap = result.image.toBitmap()
            }

            override fun onError(request: ImageRequest, result: ErrorResult) {
                error = result
            }
        }
    }
    val imageLoader = LocalContext.current.imageLoader

    LaunchedEffect(loadedBitmap) {
        loadedBitmap?.let { onMediaLoaded(it) }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CoilZoomAsyncImage(
            onTap = { onToggleImmersiveMode() },
            modifier = Modifier
                .sharedElement(
                    sharedContentState = rememberSharedContentState(key = "mediaItem"),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                .fillMaxSize(),
            imageLoader = imageLoader,
            model = ImageRequest.Builder(LocalContext.current)
                .data(mediaUrl)
                .listener(loadingImageListener)
                .crossfade(durationMillis = 300)
                .build(),
            contentDescription = null,
        )
    }
}
