package net.primal.android.discuss.post

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailListItemImage
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.theme.AppTheme

@Composable
fun NewPostScreen(
    viewModel: NewPostViewModel,
    onClose: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, onClose) {
        viewModel.effect.collect {
            when (it) {
                NewPostContract.SideEffect.PostPublished -> onClose()
            }
        }
    }

    NewPostScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewPostScreen(
    state: NewPostContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (NewPostContract.UiEvent) -> Unit,
) {
    var content by rememberSaveable { mutableStateOf(state.preFillContent ?: "") }

    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    NewPostPublishErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            PrimalTopAppBar(
                title = "",
                navigationIcon = Icons.Outlined.Close,
                onNavigationIconClick = onClose,
                actions = {
                    val text = if (state.publishing) {
                        stringResource(id = R.string.new_post_publishing_button)
                    } else {
                        stringResource(id = R.string.new_post_publish_button)
                    }

                    PrimalLoadingButton(
                        modifier = Modifier
                            .height(36.dp)
                            .wrapContentWidth(),
                        text = text,
                        fontSize = 16.sp,
                        shape = AppTheme.shapes.small,
                        contentPadding = PaddingValues(
                            horizontal = 24.dp,
                            vertical = 0.dp,
                        ),
                        enabled = !state.publishing,
                        onClick = {
                            eventPublisher(NewPostContract.UiEvent.PublishPost(content = content))
                        }
                    )
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { paddingValues ->
            Surface(
                modifier = Modifier.padding(paddingValues)
            ) {
                Row {
                    AvatarThumbnailListItemImage(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(top = 8.dp),
                        source = state.activeAccountAvatarUrl,
                    )

                    OutlinedTextField(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .fillMaxSize()
                            .focusRequester(focusRequester),
                        value = content,
                        onValueChange = { content = it },
                        enabled = !state.publishing,
                        placeholder = {
                            Text(
                                text = stringResource(id = R.string.new_post_content_placeholder),
                                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Unspecified,
                            unfocusedBorderColor = Color.Unspecified,
                            errorBorderColor = Color.Unspecified,
                            disabledBorderColor = Color.Unspecified
                        )
                    )

                }
            }
        },
    )
}

@Composable
private fun NewPostPublishErrorHandler(
    error: NewPostContract.UiState.PublishError?,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        if (error != null) {
            snackbarHostState.showSnackbar(
                message = context.getString(R.string.new_post_nostr_publish_error),
                duration = SnackbarDuration.Short,
            )
        }
    }
}
