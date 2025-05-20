package net.primal.android.media

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import coil.imageLoader
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
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
            )
        }
    }
}

@ExperimentalSharedTransitionApi
@Composable
fun SharedTransitionScope.MediaItemContent(
    modifier: Modifier = Modifier,
    mediaUrl: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onMediaLoaded: ((Bitmap) -> Unit),
) {
    val zoomSpec = ZoomSpec(maxZoomFactor = 2f)
    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var error by remember { mutableStateOf<ErrorResult?>(null) }
    val loadingImageListener = remember {
        object : ImageRequest.Listener {
            override fun onSuccess(request: ImageRequest, result: SuccessResult) {
                error = null
                loadedBitmap = (result.drawable as? BitmapDrawable)?.bitmap
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
        ZoomableAsyncImage(
            modifier = Modifier
                .sharedElement(
                    state = rememberSharedContentState(key = "mediaItem"),
                    animatedVisibilityScope = animatedVisibilityScope,
                )
                .fillMaxSize(),
            state = rememberZoomableImageState(rememberZoomableState(zoomSpec = zoomSpec)),
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
