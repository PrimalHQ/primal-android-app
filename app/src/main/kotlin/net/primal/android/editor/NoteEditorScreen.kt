package net.primal.android.editor

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*
import net.primal.android.R
import net.primal.android.articles.feed.ui.FeedArticleListItem
import net.primal.android.articles.feed.ui.FeedArticleUi
import net.primal.android.articles.highlights.HighlightUi
import net.primal.android.core.compose.ImportPhotosIconButton
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.ReplyingToText
import net.primal.android.core.compose.TakePhotoIconButton
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromCamera
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromGallery
import net.primal.android.editor.NoteEditorContract.UiEvent
import net.primal.android.editor.NoteEditorContract.UiState.NoteEditorError
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.editor.ui.NoteAttachmentPreview
import net.primal.android.editor.ui.NoteOutlinedTextField
import net.primal.android.editor.ui.NoteTagUserLazyColumn
import net.primal.android.nostr.mappers.toReferencedHighlight
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.toNoteContentUi
import net.primal.android.notes.feed.note.ui.FeedNoteHeader
import net.primal.android.notes.feed.note.ui.NoteContent
import net.primal.android.notes.feed.note.ui.ReferencedArticleCard
import net.primal.android.notes.feed.note.ui.ReferencedHighlight
import net.primal.android.notes.feed.note.ui.ReferencedNoteCard
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
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
    val snackbarHostState = remember { SnackbarHostState() }

    NewPostPublishErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
    )

    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = "",
                navigationIcon = Icons.Outlined.Close,
                onNavigationIconClick = onClose,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_close),
                showDivider = true,
                actions = {
                    PrimalLoadingButton(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .height(34.dp)
                            .wrapContentWidth(),
                        text = state.resolvePublishNoteButtonText(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
                        enabled = !state.publishing && !state.uploadingAttachments &&
                            state.attachments.none { it.uploadError != null } &&
                            (state.content.text.isNotBlank() || state.attachments.isNotEmpty()),
                        onClick = { eventPublisher(UiEvent.PublishNote) },
                    )
                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        content = { paddingValues ->
            NoteEditorBox(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                state = state,
                eventPublisher = eventPublisher,
                contentPadding = paddingValues,
            )
        },
    )
}

@Composable
private fun NoteEditorContract.UiState.resolvePublishNoteButtonText() =
    when {
        publishing -> if (isQuoting) {
            stringResource(id = R.string.note_editor_quoting_publishing_button)
        } else if (isReply) {
            stringResource(id = R.string.note_editor_reply_publishing_button)
        } else {
            stringResource(id = R.string.note_editor_post_publishing_button)
        }

        uploadingAttachments -> stringResource(id = R.string.note_editor_uploading_attachments)

        else -> if (isQuoting) {
            stringResource(id = R.string.note_editor_quote_publish_button)
        } else if (isReply) {
            stringResource(id = R.string.note_editor_reply_publish_button)
        } else {
            stringResource(id = R.string.note_editor_post_publish_button)
        }
    }

@ExperimentalMaterial3Api
@Composable
private fun NoteEditorBox(
    state: NoteEditorContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val editorListState = rememberLazyListState()
    var noteEditorMaxHeightPx by remember { mutableIntStateOf(0) }
    var replyNoteHeightPx by remember { mutableIntStateOf(0) }
    var quotedEventHeightPx by remember { mutableIntStateOf(0) }
    val replyingToPaddingTop = 8.dp
    var replyingToNoticeHeightPx by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    var extraSpacing by remember { mutableStateOf(0.dp) }
    extraSpacing = if (state.isReply) {
        with(density) {
            val replyHeight = replyNoteHeightPx.toDp() + replyingToNoticeHeightPx.toDp() + replyingToPaddingTop
            val noteEditorMaxHeight = noteEditorMaxHeightPx.toDp()
            noteEditorMaxHeight - replyHeight - attachmentsHeightDp - quotedEventHeightPx.toDp()
        }
    } else {
        0.dp
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .background(color = AppTheme.colorScheme.surfaceVariant)
                .fillMaxSize()
                .padding(contentPadding)
                .onSizeChanged { noteEditorMaxHeightPx = it.height },
            state = editorListState,
        ) {
            if (!state.isQuoting) {
                referencedEventsAndConversationAsReplyTo(
                    modifier = Modifier.padding(all = 16.dp),
                    referencedHighlight = state.referencedHighlight,
                    referencedArticle = state.referencedArticle,
                    conversation = state.conversation,
                )
            }

            item("reply") {
                NoteEditor(
                    state = state,
                    replyingToPaddingTop = replyingToPaddingTop,
                    outlineColor = AppTheme.colorScheme.outline,
                    focusRequester = focusRequester,
                    eventPublisher = eventPublisher,
                    onReplyToNoticeHeightChanged = { replyingToNoticeHeightPx = it },
                    onReplyNoteHeightChanged = { replyNoteHeightPx = it },
                )
            }

            if (state.isQuoting) {
                item {
                    ReferencedEventsAndConversationAsQuote(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 68.dp, end = 16.dp, bottom = 8.dp)
                            .onSizeChanged { quotedEventHeightPx = it.height },
                        referencedNote = state.conversation.lastOrNull(),
                        referencedArticle = state.referencedArticle,
                        referencedHighlight = state.referencedHighlight,
                    )
                }
            }

            item(key = "attachments") {
                NoteAttachmentsLazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(attachmentsHeightDp),
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

        NoteEditorFooter(
            Modifier
                .background(color = AppTheme.colorScheme.surface)
                .navigationBarsPadding(),
            state = state,
            eventPublisher = eventPublisher,
        )
    }
}

@ExperimentalMaterial3Api
private fun LazyListScope.referencedEventsAndConversationAsReplyTo(
    modifier: Modifier = Modifier,
    referencedArticle: FeedArticleUi?,
    referencedHighlight: HighlightUi?,
    conversation: List<FeedPostUi> = emptyList(),
) {
    if (referencedHighlight != null) {
        item(
            key = referencedHighlight.highlightId,
            contentType = "MentionedHighlight",
        ) {
            ReferencedHighlight(
                modifier = modifier,
                highlight = referencedHighlight.toReferencedHighlight(),
                isDarkTheme = isAppInDarkPrimalTheme(),
                onClick = {},
            )
        }
    }

    if (referencedArticle != null) {
        item(
            key = referencedArticle.eventId,
            contentType = "MentionedArticle",
        ) {
            Column {
                FeedArticleListItem(
                    data = referencedArticle,
                    modifier = modifier,
                    enabledDropdownMenu = false,
                )
                PrimalDivider()
            }
        }
    }

    if (conversation.isNotEmpty()) {
        items(
            items = conversation,
            key = { it.postId },
        ) {
            ReplyToNote(
                replyToNote = it,
                connectionLineColor = AppTheme.colorScheme.outline,
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun ReferencedEventsAndConversationAsQuote(
    modifier: Modifier = Modifier,
    referencedNote: FeedPostUi?,
    referencedArticle: FeedArticleUi?,
    referencedHighlight: HighlightUi?,
) {
    Column {
        if (referencedNote != null) {
            ReferencedNoteCard(
                modifier = modifier,
                data = referencedNote,
                noteCallbacks = NoteCallbacks(),
            )
        }

        if (referencedArticle != null || referencedHighlight != null) {
            Column(
                modifier = modifier.padding(top = if (referencedNote != null) 8.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (referencedHighlight != null) {
                    ReferencedHighlight(
                        highlight = referencedHighlight.toReferencedHighlight(),
                        isDarkTheme = isAppInDarkPrimalTheme(),
                        onClick = {},
                    )
                }
                if (referencedArticle != null) {
                    ReferencedArticleCard(data = referencedArticle)
                }
            }
        }
    }
}

@Composable
private fun NoteEditor(
    state: NoteEditorContract.UiState,
    replyingToPaddingTop: Dp,
    outlineColor: Color,
    focusRequester: FocusRequester,
    onReplyNoteHeightChanged: (Int) -> Unit,
    onReplyToNoticeHeightChanged: (Int) -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    Column {
        if (state.isReply && state.replyToNote != null && !state.isQuoting) {
            ReplyingToText(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = replyingToPaddingTop, start = avatarsColumnWidthDp, end = 16.dp)
                    .onSizeChanged { onReplyToNoticeHeightChanged(it.height) },
                replyToUsername = state.replyToNote.authorHandle,
            )
        }

        Row {
            UniversalAvatarThumbnail(
                modifier = Modifier
                    .drawWithCache {
                        onDrawBehind {
                            if (state.isReply && !state.isQuoting) {
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
                avatarBlossoms = state.activeAccountBlossoms,
                legendaryCustomization = state.activeAccountLegendaryCustomization,
            )

            NoteOutlinedTextField(
                modifier = Modifier
                    .offset(x = (-8).dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .focusRequester(focusRequester)
                    .onSizeChanged { onReplyNoteHeightChanged(it.height) },
                value = state.content,
                onValueChange = { eventPublisher(UiEvent.UpdateContent(content = it)) },
                taggedUsers = state.taggedUsers,
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
                textStyle = AppTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                colors = PrimalDefaults.transparentOutlinedTextFieldColors(),
                onUserTaggingModeChanged = {
                    eventPublisher(UiEvent.ToggleSearchUsers(enabled = it))
                },
                onUserTagSearch = {
                    eventPublisher(UiEvent.SearchUsers(query = it))
                },
            )
        }
    }
}

@Composable
private fun NoteEditorFooter(
    modifier: Modifier,
    state: NoteEditorContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = AppTheme.extraColorScheme.surfaceVariantAlt1)

        if (state.userTaggingQuery != null) {
            NoteTagUserLazyColumn(
                modifier = Modifier.heightIn(min = 0.dp, max = 288.dp),
                content = state.content,
                taggedUsers = state.taggedUsers,
                users = state.users.ifEmpty {
                    if (state.userTaggingQuery.isEmpty()) {
                        state.recommendedUsers
                    } else {
                        emptyList()
                    }
                },
                userTaggingQuery = state.userTaggingQuery,
                onUserClick = { newContent, newTaggedUsers ->
                    eventPublisher(UiEvent.UpdateContent(content = newContent))
                    eventPublisher(UiEvent.TagUser(taggedUser = newTaggedUsers.last()))
                    eventPublisher(UiEvent.ToggleSearchUsers(enabled = false))
                },
            )
        } else {
            NoteActionRow(
                onPhotosImported = { photoUris ->
                    eventPublisher(
                        UiEvent.ImportLocalFiles(uris = photoUris),
                    )
                },
                onUserTag = {
                    eventPublisher(UiEvent.AppendUserTagAtSign)
                    eventPublisher(UiEvent.ToggleSearchUsers(enabled = true))
                },
            )
        }
    }
}

@Composable
private fun NoteAttachmentsLazyRow(
    modifier: Modifier,
    attachments: List<NoteAttachment>,
    onDiscard: (UUID) -> Unit,
    onRetryUpload: (UUID) -> Unit,
) {
    LazyRow(
        modifier = modifier,
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
            authorLegendaryCustomization = replyToNote.authorLegendaryCustomization,
            onAuthorAvatarClick = {},
        )

        NoteContent(
            modifier = Modifier.padding(
                start = avatarsColumnWidthDp,
                end = 16.dp,
            ),
            data = replyToNote.toNoteContentUi(),
            expanded = true,
            onClick = {},
            onUrlClick = {},
            noteCallbacks = NoteCallbacks(),
        )
    }
}

@Composable
private fun NoteActionRow(onPhotosImported: (List<Uri>) -> Unit, onUserTag: () -> Unit) {
    Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        ImportPhotosIconButton(
            imageVector = PrimalIcons.ImportPhotoFromGallery,
            contentDescription = stringResource(id = R.string.accessibility_import_photo_from_gallery),
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            onPhotosImported = onPhotosImported,
        )

        TakePhotoIconButton(
            imageVector = PrimalIcons.ImportPhotoFromCamera,
            contentDescription = stringResource(id = R.string.accessibility_take_photo),
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            onPhotoTaken = { uri -> onPhotosImported(listOf(uri)) },
        )

        IconButton(onClick = onUserTag) {
            Icon(
                imageVector = Icons.Default.AlternateEmail,
                contentDescription = stringResource(id = R.string.accessibility_tag_user),
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }
    }
}

@Composable
@Deprecated("Replace with SnackbarErrorHandler")
private fun NewPostPublishErrorHandler(error: NoteEditorError?, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current
    LaunchedEffect(error ?: true) {
        val errorMessage = when (error) {
            is NoteEditorError.MissingRelaysConfiguration -> context.getString(
                R.string.app_missing_relays_config,
            )

            is NoteEditorError.PublishError -> context.getString(
                R.string.note_editor_nostr_publish_error,
            )

            is NoteEditorError.AttachmentUploadFailed ->
                error.cause.message
                    ?: context.getString(R.string.app_error_upload_failed)

            null -> return@LaunchedEffect
        }

        snackbarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Long,
        )
    }
}

private val avatarSizeDp = 42.dp
private val connectionLineOffsetXDp = 40.dp
private val attachmentsHeightDp = 160.dp
private val avatarsColumnWidthDp = avatarSizeDp + 24.dp
