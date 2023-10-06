package net.primal.android.discuss.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.editor.NoteActionRow
import net.primal.android.core.compose.editor.NoteAttachmentPreview
import net.primal.android.discuss.post.NewPostContract.UiEvent
import net.primal.android.discuss.post.NewPostContract.UiState.NewPostError
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
    eventPublisher: (UiEvent) -> Unit,
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
        modifier = Modifier.imePadding().navigationBarsPadding(),
        topBar = {
            PrimalTopAppBar(
                title = "",
                navigationIcon = Icons.Outlined.Close,
                onNavigationIconClick = onClose,
                actions = {
                    val text = when {
                        state.publishing -> stringResource(id = R.string.new_post_publishing_button)
                        state.uploadingAttachments -> stringResource(id = R.string.new_post_uploading_attachments)
                        else -> stringResource(id = R.string.new_post_publish_button)
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
                        enabled = !state.publishing && !state.uploadingAttachments
                                && state.attachments.none { it.uploadError != null },
                        onClick = {
                            eventPublisher(UiEvent.PublishPost(content = content))
                        }
                    )
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier.padding(paddingValues)
            ) {
                item {
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
                                .fillMaxWidth()
                                .wrapContentHeight()
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

                item {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(160.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item {
                            Spacer(modifier = Modifier.width(48.dp + 32.dp))
                        }

                        items(
                            items = state.attachments,
                            key = { it.id },
                        ) { attachment ->
                            NoteAttachmentPreview(
                                attachment = attachment,
                                onDiscard = {
                                    eventPublisher(UiEvent.DiscardNoteAttachment(attachmentId = it))
                                },
                                onRetryUpload = {
                                    eventPublisher(UiEvent.RetryUpload(attachmentId = attachment.id))
                                },
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column {
                Divider(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt,
                )
                NoteActionRow(
                    onPhotosImported = { photoUris ->
                        eventPublisher(UiEvent.ImportLocalFiles(uris = photoUris))
                    },
                )
            }
        }
    )
}

@Composable
private fun NewPostPublishErrorHandler(
    error: NewPostError?,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        val errorMessage = when (error) {
            is NewPostError.MissingRelaysConfiguration -> context.getString(R.string.app_missing_relays_config)
            is NewPostError.PublishError -> context.getString(R.string.new_post_nostr_publish_error)
            else -> null
        }

        if (errorMessage != null) {
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Short,
            )
        }
    }
}
