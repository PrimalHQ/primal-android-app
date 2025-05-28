package net.primal.android.editor

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboardManager
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
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.ReplyingToText
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.TakePhotoIconButton
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.foundation.isAppInDarkPrimalTheme
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromCamera
import net.primal.android.core.compose.icons.primaliconpack.ImportPhotoFromGallery
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.editor.NoteEditorContract.UiEvent
import net.primal.android.editor.domain.NoteAttachment
import net.primal.android.editor.ui.NoteAttachmentPreview
import net.primal.android.editor.ui.NoteOutlinedTextField
import net.primal.android.editor.ui.NoteTagUserLazyColumn
import net.primal.android.nostr.mappers.toReferencedHighlight
import net.primal.android.notes.feed.model.FeedPostUi
import net.primal.android.notes.feed.model.toNoteContentUi
import net.primal.android.notes.feed.note.ui.FeedNoteHeader
import net.primal.android.notes.feed.note.ui.NoteContent
import net.primal.android.notes.feed.note.ui.NoteLightningInvoice
import net.primal.android.notes.feed.note.ui.NoteUnknownEvent
import net.primal.android.notes.feed.note.ui.ReferencedArticleCard
import net.primal.android.notes.feed.note.ui.ReferencedHighlight
import net.primal.android.notes.feed.note.ui.ReferencedNoteCard
import net.primal.android.notes.feed.note.ui.events.NoteCallbacks
import net.primal.android.theme.AppTheme
import net.primal.domain.nostr.asATagValue

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
    val context = LocalContext.current

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { eventPublisher(UiEvent.DismissError) },
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
                noteCallbacks = NoteCallbacks(),
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
    noteCallbacks: NoteCallbacks,
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
    var footerHeight by remember { mutableIntStateOf(0) }

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
                    referencedHighlight = state.replyToHighlight,
                    referencedArticle = state.replyToArticle,
                    conversation = state.replyToConversation,
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

            nostrUris(
                nostrUris = state.referencedNostrUris,
                noteCallbacks = noteCallbacks,
                onRetryUriClick = { eventPublisher(UiEvent.RefreshUri(it.uri)) },
                onRemoveUriClick = { eventPublisher(UiEvent.RemoveUri(it)) },
                onRemoveHighlight = { eventPublisher(UiEvent.RemoveHighlightByArticle(it)) },
            )

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

            if (state.attachments.isNotEmpty()) {
                item(key = "attachmentSpacing") {
                    Spacer(
                        modifier = Modifier.height(
                            with(density) { footerHeight.toDp() + 8.dp },
                        ),
                    )
                }
            }
        }

        NoteEditorFooter(
            Modifier
                .background(color = AppTheme.colorScheme.surface)
                .navigationBarsPadding()
                .onSizeChanged { footerHeight = it.height },
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

internal const val ELLIPSIZE_URI_CHAR_COUNT = 16

@OptIn(ExperimentalMaterial3Api::class)
fun LazyListScope.nostrUris(
    onRetryUriClick: (NoteEditorContract.ReferencedUri<*>) -> Unit,
    onRemoveUriClick: (Int) -> Unit,
    onRemoveHighlight: (String) -> Unit,
    nostrUris: List<NoteEditorContract.ReferencedUri<*>>,
    noteCallbacks: NoteCallbacks,
) {
    items(
        count = nostrUris.size,
        contentType = { "ReferencedUri" },
    ) { uriIndex ->
        val uri = nostrUris[uriIndex]
        Box(
            modifier = Modifier
                .padding(start = 68.dp, end = 16.dp, bottom = 8.dp),
        ) {
            if (uri.loading) {
                Box(modifier = Modifier.height(100.dp)) {
                    PrimalLoadingSpinner(size = 48.dp)
                }
            } else if (uri.data != null) {
                when (uri) {
                    is NoteEditorContract.ReferencedUri.Article -> {
                        uri.data?.let { article ->
                            ReferencedArticleCard(
                                data = article,
                            )
                        }
                    }

                    is NoteEditorContract.ReferencedUri.Note -> {
                        uri.data?.let { note ->
                            ReferencedNoteCard(
                                data = note,
                                noteCallbacks = noteCallbacks,
                            )
                        }
                    }

                    is NoteEditorContract.ReferencedUri.LightningInvoice -> {
                        NoteLightningInvoice(invoice = uri.data)
                    }

                    is NoteEditorContract.ReferencedUri.Highlight -> {
                        uri.data?.let {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                ReferencedHighlight(
                                    highlight = uri.data.toReferencedHighlight(),
                                    isDarkTheme = isAppInDarkPrimalTheme(),
                                    onClick = {},
                                )
                            }
                        }
                    }
                }
            } else {
                NoteUnknownEvent(
                    altDescription = stringResource(
                        id = R.string.note_editor_unable_to_locate_event,
                        "${uri.uri.take(ELLIPSIZE_URI_CHAR_COUNT)}...${uri.uri.takeLast(ELLIPSIZE_URI_CHAR_COUNT)}",
                    ),
                    onClick = { onRetryUriClick(uri) },
                )
            }

            if (uri !is NoteEditorContract.ReferencedUri.Highlight) {
                DismissUriFloatingButton(
                    onRemoveUriClick = onRemoveUriClick,
                    uriIndex = uriIndex,
                    uri = uri,
                    onRemoveHighlight = onRemoveHighlight,
                )
            }
        }
    }
}

@Composable
private fun BoxScope.DismissUriFloatingButton(
    onRemoveUriClick: (Int) -> Unit,
    uriIndex: Int,
    uri: NoteEditorContract.ReferencedUri<*>,
    onRemoveHighlight: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(top = 8.dp, end = 8.dp)
            .align(Alignment.TopEnd)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .padding(4.dp),
    ) {
        IconButton(
            modifier = Modifier.size(24.dp),
            onClick = {
                onRemoveUriClick(uriIndex)

                if (uri is NoteEditorContract.ReferencedUri.Article) {
                    onRemoveHighlight(uri.naddr.asATagValue())
                }
            },
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = null,
                tint = Color.White,
            )
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
    val clipboardManager = LocalClipboardManager.current
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
                onValueChange = {
                    val clipboardText = clipboardManager.getText()
                    if (clipboardText != null && it.text.contains(clipboardText.text)) {
                        eventPublisher(UiEvent.PasteContent(content = it))
                    } else {
                        eventPublisher(UiEvent.UpdateContent(content = it))
                    }
                },
                taggedUsers = state.taggedUsers,
                enabled = !state.publishing,
                placeholder = {
                    Text(
                        text = stringResource(id = R.string.note_editor_content_placeholder),
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                        style = AppTheme.typography.bodyMedium,
                    )
                },
                textStyle = AppTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                colors = PrimalDefaults.transparentOutlinedTextFieldColors(),
                onUserTaggingModeChanged = { eventPublisher(UiEvent.ToggleSearchUsers(enabled = it)) },
                onUserTagSearch = { eventPublisher(UiEvent.SearchUsers(query = it)) },
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
    BoxWithConstraints(
        modifier = modifier,
    ) {
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = avatarsColumnWidthDp,
                end = contentEndPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = attachments,
                key = { it.id },
            ) { attachment ->
                NoteAttachmentPreview(
                    attachment = attachment,
                    maxWidth = this@BoxWithConstraints.maxWidth - (avatarsColumnWidthDp + contentEndPadding),
                    onDiscard = onDiscard,
                    onRetryUpload = onRetryUpload,
                )
            }
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

private val avatarSizeDp = 42.dp
private val connectionLineOffsetXDp = 40.dp
private val attachmentsHeightDp = 160.dp
private val avatarsColumnWidthDp = avatarSizeDp + 24.dp
private val contentEndPadding = 16.dp
