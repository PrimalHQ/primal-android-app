package net.primal.android.editor.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import kotlinx.coroutines.flow.filter
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.model.toNoteContentUi
import net.primal.android.core.compose.feed.note.FeedNoteHeader
import net.primal.android.core.compose.feed.note.NoteContent
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromGallery
import net.primal.android.editor.NoteEditorContract
import net.primal.android.editor.NoteEditorContract.UiEvent
import net.primal.android.editor.NoteEditorContract.UiState.NewPostError
import net.primal.android.editor.NoteEditorViewModel
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.theme.AppTheme

@Composable
fun NoteEditorScreen(viewModel: NoteEditorViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    LaunchedEffect(viewModel, onClose) {
        viewModel.effect.collect {
            when (it) {
                NoteEditorContract.SideEffect.PostPublished -> onClose()
            }
        }
    }

    NoteEditorScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    state: NoteEditorContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    var content by rememberSaveable { mutableStateOf(state.preFillContent ?: "") }

    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }
    val outlineColor = AppTheme.colorScheme.outline

    val editorListState = rememberLazyListState()
    val keyboardVisible = keyboardVisibilityAsState()

    LaunchedEffect(onClose, eventPublisher, editorListState) {
        snapshotFlow { keyboardVisible.value }
            .filter { it }
            .collect {
                editorListState.animateScrollToItem(state.conversation.size)
            }
    }

    NewPostPublishErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Scaffold(
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding(),
        topBar = {
            PrimalTopAppBar(
                title = "",
                navigationIcon = Icons.Outlined.Close,
                onNavigationIconClick = onClose,
                showDivider = true,
                actions = {
                    val text = when {
                        state.publishing -> if (state.isReply) {
                            stringResource(id = R.string.note_editor_reply_publishing_button)
                        } else {
                            stringResource(id = R.string.note_editor_post_publishing_button)
                        }

                        state.uploadingAttachments -> stringResource(
                            id = R.string.note_editor_uploading_attachments,
                        )
                        else -> if (state.isReply) {
                            stringResource(id = R.string.note_editor_reply_publish_button)
                        } else {
                            stringResource(id = R.string.note_editor_post_publish_button)
                        }
                    }

                    PrimalLoadingButton(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(34.dp)
                            .wrapContentWidth(),
                        text = text,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        contentPadding = PaddingValues(
                            horizontal = 24.dp,
                            vertical = 0.dp,
                        ),
                        enabled = !state.publishing && !state.uploadingAttachments &&
                            content.isNotBlank() &&
                            state.attachments.none { it.uploadError != null },
                        onClick = {
                            eventPublisher(UiEvent.PublishPost(content = content))
                        },
                    )
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { paddingValues ->
            var noteEditorMaxHeightPx by remember { mutableIntStateOf(0) }
            var replyNoteHeightPx by remember { mutableIntStateOf(0) }
            val replyingToPaddingTop = 8.dp
            var replyingToNoticeHeightPx by remember { mutableIntStateOf(0) }

            var extraSpacing by remember { mutableStateOf(0.dp) }
            extraSpacing = if (state.isReply) {
                with(LocalDensity.current) {
                    val replyHeight = replyNoteHeightPx.toDp() + replyingToNoticeHeightPx.toDp() + replyingToPaddingTop
                    val noteEditorMaxHeight = noteEditorMaxHeightPx.toDp()
                    noteEditorMaxHeight - replyHeight - attachmentsHeightDp
                }
            } else {
                0.dp
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .onSizeChanged { noteEditorMaxHeightPx = it.height },
                state = editorListState,
            ) {
                items(
                    items = state.conversation,
                    key = { it.postId },
                ) {
                    ReplyToNote(
                        replyToNote = it,
                        connectionLineColor = outlineColor,
                    )
                }

                item("reply") {
                    Column {
                        if (state.isReply && state.replyToNote != null) {
                            ReplyingToText(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = replyingToPaddingTop, start = avatarsColumnWidthDp, end = 16.dp)
                                    .onSizeChanged { replyingToNoticeHeightPx = it.height },
                                replyToUsername = state.replyToNote.authorHandle,
                            )
                        }

                        Row {
                            AvatarThumbnail(
                                modifier = Modifier
                                    .drawWithCache {
                                        onDrawBehind {
                                            if (state.isReply) {
                                                drawLine(
                                                    color = outlineColor,
                                                    start = Offset(
                                                        x = connectionLineOffsetXDp.toPx(),
                                                        y = (-32).dp.toPx(),
                                                    ),
                                                    end = Offset(
                                                        x = connectionLineOffsetXDp.toPx(),
                                                        y = size.height / 2,
                                                    ),
                                                    strokeWidth = 2.dp.toPx(),
                                                    cap = StrokeCap.Square,
                                                )
                                            }
                                        }
                                    }
                                    .padding(start = 16.dp)
                                    .padding(top = 8.dp),
                                avatarSize = avatarSizeDp,
                                avatarCdnImage = state.activeAccountAvatarCdnImage,
                            )

                            OutlinedTextField(
                                modifier = Modifier
                                    .offset(x = (-8).dp)
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                                    .focusRequester(focusRequester)
                                    .onSizeChanged { replyNoteHeightPx = it.height },
                                value = content,
                                onValueChange = { content = it },
                                enabled = !state.publishing,
                                placeholder = {
                                    Text(
                                        text = stringResource(
                                            id = R.string.note_editor_content_placeholder,
                                        ),
                                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                                        style = AppTheme.typography.bodyMedium,
                                    )
                                },
                                textStyle = AppTheme.typography.bodyMedium.copy(
                                    lineHeight = 20.sp,
                                ),
                                colors = PrimalDefaults.outlinedTextFieldColors(
                                    focusedContainerColor = Color.Unspecified,
                                    unfocusedContainerColor = Color.Unspecified,
                                    focusedBorderColor = Color.Unspecified,
                                    unfocusedBorderColor = Color.Unspecified,
                                    errorBorderColor = Color.Unspecified,
                                    disabledBorderColor = Color.Unspecified,
                                ),
                            )
                        }
                    }
                }

                item(key = "attachments") {
                    NoteAttachmentsLazyRow(
                        attachments = state.attachments,
                        onDiscard = {
                            eventPublisher(UiEvent.DiscardNoteAttachment(attachmentId = it))
                        },
                        onRetryUpload = {
                            eventPublisher(UiEvent.RetryUpload(attachmentId = it))
                        },
                    )
                }

                item(key = "extraSpacing") {
                    Spacer(modifier = Modifier.height(extraSpacing))
                }
            }
        },
        bottomBar = {
            Column {
                Divider(
                    color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                )
                NoteActionRow(
                    onPhotosImported = { photoUris ->
                        eventPublisher(UiEvent.ImportLocalFiles(uris = photoUris))
                    },
                )
            }
        },
    )
}

@Composable
private fun NoteAttachmentsLazyRow(
    attachments: List<NoteAttachment>,
    onDiscard: (UUID) -> Unit,
    onRetryUpload: (UUID) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .height(attachmentsHeightDp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Spacer(modifier = Modifier.width(avatarsColumnWidthDp - 8.dp))
        }

        items(
            items = attachments,
            key = { it.id },
        ) { attachment ->
            NoteAttachmentPreview(
                attachment = attachment,
                onDiscard = onDiscard,
                onRetryUpload = onRetryUpload,
            )
        }
    }
}

@Composable
private fun ReplyToNote(replyToNote: FeedPostUi, connectionLineColor: Color) {
    Column(
        modifier = Modifier.drawWithCache {
            onDrawBehind {
                drawLine(
                    color = connectionLineColor,
                    start = Offset(
                        x = connectionLineOffsetXDp.toPx(),
                        y = connectionLineOffsetXDp.toPx(),
                    ),
                    end = Offset(
                        x = connectionLineOffsetXDp.toPx(),
                        y = size.height + 16.dp.toPx(),
                    ),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Square,
                )
            }
        },
    ) {
        FeedNoteHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 12.dp,
                    bottom = 8.dp,
                    end = 24.dp,
                ),
            authorDisplayName = replyToNote.authorName,
            postTimestamp = replyToNote.timestamp,
            authorAvatarCdnImage = replyToNote.authorAvatarCdnImage,
            authorInternetIdentifier = replyToNote.authorInternetIdentifier,
            onAuthorAvatarClick = {},
        )

        NoteContent(
            modifier = Modifier.padding(
                start = avatarsColumnWidthDp,
                end = 16.dp,
            ),
            data = replyToNote.toNoteContentUi(),
            expanded = true,
            onProfileClick = {},
            onPostClick = {},
            onClick = {},
            onUrlClick = {},
            onHashtagClick = {},
            onMediaClick = { _, _ -> },
        )
    }
}

@Composable
private fun NoteActionRow(maxItems: Int = 5, onPhotosImported: (List<Uri>) -> Unit) {
    val multiplePhotosImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxItems),
    ) { uris -> onPhotosImported(uris) }

    Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        IconButton(
            onClick = {
                multiplePhotosImportLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                    ),
                )
            },
        ) {
            Icon(
                imageVector = PrimalIcons.ImportPhotoFromGallery,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }
    }
}

@Composable
private fun NewPostPublishErrorHandler(error: NewPostError?, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        val errorMessage = when (error) {
            is NewPostError.MissingRelaysConfiguration -> context.getString(
                R.string.app_missing_relays_config,
            )
            is NewPostError.PublishError -> context.getString(
                R.string.note_editor_nostr_publish_error,
            )
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

private val avatarSizeDp = 42.dp
private val connectionLineOffsetXDp = 36.dp
private val attachmentsHeightDp = 160.dp
private val avatarsColumnWidthDp = avatarSizeDp + 24.dp
