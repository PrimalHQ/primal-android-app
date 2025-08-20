package net.primal.android.stream.ui

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer
import java.text.NumberFormat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.PrimalClickableText
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ContextReportContent
import net.primal.android.core.compose.icons.primaliconpack.NavWalletBoltFilled
import net.primal.android.core.compose.profile.approvals.FollowsApprovalAlertDialog
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.core.ext.openUriSafely
import net.primal.android.editor.ui.NoteOutlinedTextField
import net.primal.android.editor.ui.NoteTagUserLazyColumn
import net.primal.android.events.ui.EventZapUiModel
import net.primal.android.notes.feed.model.NoteNostrUriUi
import net.primal.android.notes.feed.zaps.ZapHost
import net.primal.android.notes.feed.zaps.rememberZapHostState
import net.primal.android.profile.report.ReportUserDialog
import net.primal.android.stream.LiveStreamContract
import net.primal.android.stream.player.SEEK_INCREMENT_MS
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_HEIGHT
import net.primal.android.stream.player.VIDEO_ASPECT_RATIO_WIDTH
import net.primal.android.theme.AppTheme
import net.primal.core.utils.detectUrls
import net.primal.domain.links.EventUriNostrType
import net.primal.domain.links.ReferencedUser
import net.primal.domain.nostr.ReactionType
import net.primal.domain.nostr.ReportType
import net.primal.domain.nostr.utils.clearAtSignFromNostrUris
import net.primal.domain.nostr.utils.parseNostrUris
import net.primal.domain.utils.isLightningAddress
import net.primal.domain.wallet.DraftTx

private const val URL_ANNOTATION_TAG = "url"
private val ZapMessageBorderColor = Color(0xFFFFA000)
private val ZapMessageBackgroundColor = Color(0xFFE47C00)
private val ZapMessageProfileHandleColor: Color
    @Composable
    get() = if (LocalPrimalTheme.current.isDarkTheme) {
        Color(0xFFFFA02F)
    } else {
        Color(0xFFE47C00)
    }
private val ReportButtonHandleColor: Color
    @Composable
    get() = if (LocalPrimalTheme.current.isDarkTheme) {
        Color(0xFF333333)
    } else {
        Color(0xFFD5D5D5)
    }

private sealed interface ActiveBottomSheet {
    data object None : ActiveBottomSheet
    data object StreamInfo : ActiveBottomSheet
    data class ChatDetails(val message: ChatMessageUi) : ActiveBottomSheet
    data class ZapDetails(val zap: EventZapUiModel) : ActiveBottomSheet
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStreamScreen(
    state: LiveStreamContract.UiState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    exoPlayer: ExoPlayer,
    callbacks: LiveStreamContract.ScreenCallbacks,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val zapHostState = rememberZapHostState(
        zappingState = state.zappingState,
        receiverName = state.streamInfo?.mainHostProfile?.authorDisplayName,
    )

    ZapHost(
        zapHostState = zapHostState,
        onZap = { zapAmount, zapDescription ->
            eventPublisher(
                LiveStreamContract.UiEvent.ZapStream(
                    zapAmount = zapAmount.toULong(),
                    zapDescription = zapDescription,
                ),
            )
        },
        onGoToWallet = callbacks.onGoToWallet,
    )

    if (state.shouldApproveProfileAction != null) {
        FollowsApprovalAlertDialog(
            followsApproval = state.shouldApproveProfileAction,
            onFollowsActionsApproved = {
                eventPublisher(LiveStreamContract.UiEvent.ApproveFollowsActions(it.actions))
            },
            onClose = { eventPublisher(LiveStreamContract.UiEvent.DismissConfirmFollowUnfollowAlertDialog) },
        )
    }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context) },
        onErrorDismiss = { eventPublisher(LiveStreamContract.UiEvent.DismissError) },
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            BoxWithConstraints {
                val coroutineScope = rememberCoroutineScope()

                val playerHeight = this.maxWidth * (VIDEO_ASPECT_RATIO_HEIGHT / VIDEO_ASPECT_RATIO_WIDTH)
                val topInset = paddingValues.calculateTopPadding()
                val bottomInset = paddingValues.calculateBottomPadding()

                val bottomSheetHeight = remember(this.maxHeight, playerHeight, topInset, bottomInset) {
                    (this.maxHeight - topInset - playerHeight - bottomInset).coerceAtLeast(0.dp)
                }

                var activeBottomSheet by remember { mutableStateOf<ActiveBottomSheet>(ActiveBottomSheet.None) }

                LiveStreamContent(
                    state = state,
                    exoPlayer = exoPlayer,
                    eventPublisher = eventPublisher,
                    paddingValues = paddingValues,
                    callbacks = callbacks,
                    onZapClick = { zapHostState.showZapOptionsOrShowWarning() },
                    onInfoClick = { activeBottomSheet = ActiveBottomSheet.StreamInfo },
                    onChatMessageLongClick = { activeBottomSheet = ActiveBottomSheet.ChatDetails(it) },
                    onZapMessageLongClick = { activeBottomSheet = ActiveBottomSheet.ZapDetails(it) },
                )

                LiveStreamModalBottomSheets(
                    activeSheet = activeBottomSheet,
                    onDismiss = { activeBottomSheet = ActiveBottomSheet.None },
                    state = state,
                    eventPublisher = eventPublisher,
                    callbacks = callbacks,
                    onZapClick = {
                        handleZapProfile(
                            state = state,
                            callbacks = callbacks,
                            coroutineScope = coroutineScope,
                            snackbarHostState = snackbarHostState,
                            context = context,
                        )
                    },
                    bottomSheetHeight = bottomSheetHeight,
                )
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LiveStreamModalBottomSheets(
    activeSheet: ActiveBottomSheet,
    onDismiss: () -> Unit,
    state: LiveStreamContract.UiState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    callbacks: LiveStreamContract.ScreenCallbacks,
    onZapClick: () -> Unit,
    bottomSheetHeight: Dp?,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (activeSheet != ActiveBottomSheet.None) {
        val streamInfo = state.streamInfo ?: return
        val activeUserId = state.activeUserId ?: return

        val isMuteStreamHostButtonVisible = activeSheet is
            ActiveBottomSheet.ChatDetails || activeSheet is ActiveBottomSheet.ZapDetails

        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
            tonalElevation = 0.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (bottomSheetHeight != null) Modifier.height(bottomSheetHeight) else Modifier)
                    .padding(top = 8.dp),
            ) {
                StreamInfoBottomSheet(
                    modifier = Modifier.padding(bottom = 16.dp),
                    isMuteStreamHostButtonVisible = isMuteStreamHostButtonVisible,
                    activeUserId = activeUserId,
                    streamInfo = streamInfo,
                    isLive = state.playerState.isLive,
                    onFollow = { eventPublisher(LiveStreamContract.UiEvent.FollowAction(streamInfo.mainHostId)) },
                    onUnfollow = {
                        eventPublisher(LiveStreamContract.UiEvent.UnfollowAction(streamInfo.mainHostId))
                    },
                    onMute = {
                        eventPublisher(LiveStreamContract.UiEvent.MuteAction(profileId = streamInfo.mainHostId))
                    },
                    onUnmute = {
                        eventPublisher(LiveStreamContract.UiEvent.UnmuteAction(profileId = streamInfo.mainHostId))
                    },
                    onZap = onZapClick,
                    onEditProfileClick = callbacks.onEditProfileClick,
                    onMessageClick = callbacks.onMessageClick,
                    onDrawerQrCodeClick = callbacks.onDrawerQrCodeClick,
                    bottomContent = {
                        when (activeSheet) {
                            is ActiveBottomSheet.StreamInfo -> {
                                StreamDescriptionSection(
                                    streamInfo = streamInfo,
                                    isLive = state.playerState.isLive,
                                    onHashtagClick = callbacks.onHashtagClick,
                                )
                            }

                            is ActiveBottomSheet.ChatDetails -> {
                                ChatDetailsSection(
                                    message = activeSheet.message,
                                    onReport = { reportType ->
                                        eventPublisher(
                                            LiveStreamContract.UiEvent.ReportMessage(
                                                reportType = reportType,
                                                messageId = activeSheet.message.messageId,
                                                authorId = activeSheet.message.authorProfile.pubkey,
                                            ),
                                        )
                                        onDismiss()
                                    },
                                )
                            }

                            is ActiveBottomSheet.ZapDetails -> {
                                ZapDetailsSection(
                                    zap = activeSheet.zap,
                                    onReport = { reportType ->
                                        activeSheet.zap.zapperId.let { zapperId ->
                                            eventPublisher(
                                                LiveStreamContract.UiEvent.ReportMessage(
                                                    reportType = reportType,
                                                    messageId = activeSheet.zap.id,
                                                    authorId = zapperId,
                                                ),
                                            )
                                        }
                                        onDismiss()
                                    },
                                )
                            }
                            is ActiveBottomSheet.None -> Unit
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun StreamPlayer(
    state: LiveStreamContract.UiState,
    streamInfo: LiveStreamContract.StreamInfoUi,
    exoPlayer: ExoPlayer,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    onClose: () -> Unit,
    onQuoteStreamClick: (String) -> Unit,
) {
    LiveStreamPlayer(
        state = state,
        exoPlayer = exoPlayer,
        streamUrl = streamInfo.streamUrl,
        onPlayPauseClick = {
            if (exoPlayer.isPlaying) {
                exoPlayer.pause()
            } else {
                exoPlayer.play()
            }
        },
        onRewind = {
            val newPosition = (exoPlayer.currentPosition - SEEK_INCREMENT_MS).coerceAtLeast(0L)
            exoPlayer.seekTo(newPosition)
        },
        onForward = {
            val newPosition = (exoPlayer.currentPosition + SEEK_INCREMENT_MS)
                .coerceAtMost(state.playerState.totalDuration)
            exoPlayer.seekTo(newPosition)
        },
        onSoundClick = {
            eventPublisher(LiveStreamContract.UiEvent.ToggleMute)
        },
        onClose = onClose,
        onSeek = { positionMs ->
            eventPublisher(LiveStreamContract.UiEvent.OnSeek(positionMs = positionMs))
        },
        onSeekStarted = {
            eventPublisher(LiveStreamContract.UiEvent.OnSeekStarted)
        },
        onQuoteClick = onQuoteStreamClick,
        onMuteUserClick = {
            state.streamInfo?.mainHostId?.let { eventPublisher(LiveStreamContract.UiEvent.MuteAction(it)) }
        },
        onUnmuteUserClick = {
            state.streamInfo?.mainHostId?.let { eventPublisher(LiveStreamContract.UiEvent.UnmuteAction(it)) }
        },
        onReportContentClick = { reportType ->
            eventPublisher(LiveStreamContract.UiEvent.ReportAbuse(reportType))
        },
        onRequestDeleteClick = {
            eventPublisher(LiveStreamContract.UiEvent.RequestDeleteStream)
        },
    )
}

@Composable
private fun StreamInfoAndChatSection(
    modifier: Modifier = Modifier,
    state: LiveStreamContract.UiState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    onZapClick: () -> Unit,
    onInfoClick: () -> Unit,
    onProfileClick: (String) -> Unit,
    onEventReactionsClick: (eventId: String, initialTab: ReactionType, articleATag: String?) -> Unit,
    onChatMessageLongClick: (ChatMessageUi) -> Unit,
    onZapMessageLongClick: (EventZapUiModel) -> Unit,
) {
    val chatListState = rememberSaveable(saver = LazyListState.Saver) {
        LazyListState()
    }
    val isKeyboardVisible by keyboardVisibilityAsState()

    Column(modifier = modifier.fillMaxSize()) {
        StreamInfoDisplay(
            state = state,
            onZapClick = onZapClick,
            isKeyboardVisible = isKeyboardVisible,
            onInfoClick = onInfoClick,
            onEventReactionsClick = onEventReactionsClick,
        )

        LiveChatContent(
            state = state,
            listState = chatListState,
            eventPublisher = eventPublisher,
            onProfileClick = onProfileClick,
            onChatMessageLongClick = onChatMessageLongClick,
            onZapMessageLongClick = onZapMessageLongClick,
        )
    }
}

@Composable
private fun LiveStreamContent(
    state: LiveStreamContract.UiState,
    exoPlayer: ExoPlayer,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    paddingValues: PaddingValues,
    callbacks: LiveStreamContract.ScreenCallbacks,
    onZapClick: () -> Unit,
    onInfoClick: () -> Unit,
    onChatMessageLongClick: (ChatMessageUi) -> Unit,
    onZapMessageLongClick: (EventZapUiModel) -> Unit,
) {
    if (state.loading) {
        PrimalLoadingSpinner()
    }

    val streamInfo = state.streamInfo
    if (streamInfo != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppTheme.colorScheme.surface)
                .padding(paddingValues),
        ) {
            StreamPlayer(
                state = state,
                streamInfo = streamInfo,
                exoPlayer = exoPlayer,
                eventPublisher = eventPublisher,
                onClose = callbacks.onClose,
                onQuoteStreamClick = callbacks.onQuoteStreamClick,
            )

            StreamInfoAndChatSection(
                modifier = Modifier.weight(1f),
                state = state,
                eventPublisher = eventPublisher,
                onZapClick = onZapClick,
                onInfoClick = onInfoClick,
                onProfileClick = callbacks.onProfileClick,
                onEventReactionsClick = callbacks.onEventReactionsClick,
                onChatMessageLongClick = onChatMessageLongClick,
                onZapMessageLongClick = onZapMessageLongClick,
            )
        }
    }
}

@Composable
private fun StreamInfoDisplay(
    state: LiveStreamContract.UiState,
    onZapClick: () -> Unit,
    isKeyboardVisible: Boolean,
    onInfoClick: () -> Unit,
    onEventReactionsClick: (eventId: String, initialTab: ReactionType, articleATag: String?) -> Unit,
) {
    val streamInfo = state.streamInfo ?: return
    val bottomBorderColor = AppTheme.extraColorScheme.surfaceVariantAlt1

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .background(
                AppTheme.extraColorScheme.surfaceVariantAlt2,
            )
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                val y = size.height - strokeWidth / 2f
                drawLine(
                    color = bottomBorderColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidth,
                )
            }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        StreamInfoSection(
            title = streamInfo.title,
            viewers = streamInfo.viewers,
            startedAt = streamInfo.startedAt,
            isLive = state.playerState.isLive,
            onInfoClick = onInfoClick,
            onChatSettingsClick = {},
            isKeyboardVisible = isKeyboardVisible,
        )

        if (!isKeyboardVisible) {
            StreamTopZapsSection(
                modifier = Modifier.fillMaxWidth(),
                topZaps = state.zaps,
                onZapClick = onZapClick,
                onTopZapsClick = {
                    state.streamInfo.atag.let { atag ->
                        onEventReactionsClick(
                            atag,
                            ReactionType.ZAPS,
                            atag,
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun LiveChatListOrSearch(
    modifier: Modifier = Modifier,
    state: LiveStreamContract.UiState,
    listState: LazyListState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    onProfileClick: (String) -> Unit,
    onChatMessageLongClick: (ChatMessageUi) -> Unit,
    onZapMessageLongClick: (EventZapUiModel) -> Unit,
) {
    if (!state.userTaggingState.isUserTaggingActive) {
        if (state.chatItems.isEmpty()) {
            LiveChatEmpty(
                modifier = modifier,
            )
        } else {
            LazyColumn(
                modifier = modifier,
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                reverseLayout = true,
            ) {
                items(
                    items = state.chatItems,
                    key = { it.uniqueId },
                ) { chatItem ->
                    when (chatItem) {
                        is StreamChatItem.ChatMessageItem -> ChatMessageListItem(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            message = chatItem.message,
                            onProfileClick = onProfileClick,
                            onLongClick = { onChatMessageLongClick(chatItem.message) },
                        )

                        is StreamChatItem.ZapMessageItem -> ZapMessageListItem(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            zap = chatItem.zap,
                            onLongClick = { onZapMessageLongClick(chatItem.zap) },
                        )
                    }
                }
            }
        }
    } else {
        NoteTagUserLazyColumn(
            modifier = modifier,
            content = state.comment,
            taggedUsers = state.taggedUsers,
            users = if (state.userTaggingState.userTaggingQuery.isNullOrEmpty()) {
                state.userTaggingState.recommendedUsers
            } else {
                state.userTaggingState.searchResults
            },
            userTaggingQuery = state.userTaggingState.userTaggingQuery ?: "",
            onUserClick = { newContent, newTaggedUsers ->
                eventPublisher(LiveStreamContract.UiEvent.OnCommentValueChanged(newContent))
                eventPublisher(LiveStreamContract.UiEvent.TagUser(taggedUser = newTaggedUsers.last()))
                eventPublisher(LiveStreamContract.UiEvent.ToggleSearchUsers(enabled = false))
            },
        )
    }
}

@Composable
private fun LiveChatContent(
    state: LiveStreamContract.UiState,
    listState: LazyListState,
    eventPublisher: (LiveStreamContract.UiEvent) -> Unit,
    onProfileClick: (String) -> Unit,
    onChatMessageLongClick: (ChatMessageUi) -> Unit,
    onZapMessageLongClick: (EventZapUiModel) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.chatItems.size) {
        if (state.chatItems.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(0)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colorScheme.surface)
            .imePadding(),
    ) {
        LiveChatListOrSearch(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = state,
            listState = listState,
            eventPublisher = eventPublisher,
            onProfileClick = onProfileClick,
            onChatMessageLongClick = onChatMessageLongClick,
            onZapMessageLongClick = onZapMessageLongClick,
        )

        LiveChatCommentInput(
            state = state,
            onCommentChanged = {
                eventPublisher(LiveStreamContract.UiEvent.OnCommentValueChanged(it))
            },
            onSendMessage = {
                eventPublisher(LiveStreamContract.UiEvent.SendMessage(it))
            },
            onUserTaggingModeChanged = { enabled ->
                eventPublisher(LiveStreamContract.UiEvent.ToggleSearchUsers(enabled = enabled))
            },
            onUserTagSearch = { query ->
                eventPublisher(LiveStreamContract.UiEvent.SearchUsers(query = query))
            },
        )
    }
}

@Composable
private fun LiveChatEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxHeight(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = R.string.live_stream_empty_chat),
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 20.sp,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Composable
private fun LiveChatCommentInput(
    state: LiveStreamContract.UiState,
    onCommentChanged: (TextFieldValue) -> Unit,
    onSendMessage: (String) -> Unit,
    onUserTaggingModeChanged: (Boolean) -> Unit,
    onUserTagSearch: (String) -> Unit,
) {
    val borderColor = AppTheme.extraColorScheme.surfaceVariantAlt1
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 1.dp.toPx()
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth,
                )
            }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NoteOutlinedTextField(
            modifier = Modifier.weight(1.0f),
            value = state.comment,
            onValueChange = onCommentChanged,
            maxLines = 3,
            placeholder = {
                Text(
                    text = stringResource(id = R.string.live_stream_send_comment),
                    maxLines = 1,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    style = AppTheme.typography.bodyMedium,
                )
            },
            textStyle = AppTheme.typography.bodyMedium,
            colors = PrimalDefaults.outlinedTextFieldColors(),
            shape = AppTheme.shapes.extraLarge,
            keyboardOptions = KeyboardOptions.Default.copy(
                capitalization = KeyboardCapitalization.Sentences,
            ),
            taggedUsers = state.taggedUsers,
            onUserTaggingModeChanged = onUserTaggingModeChanged,
            onUserTagSearch = onUserTagSearch,
            keyboardActions = KeyboardActions(
                onSend = { onSendMessage(state.comment.text) },
            ),
        )

        if (state.comment.text.isNotBlank()) {
            IconButton(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(46.dp),
                onClick = { onSendMessage(state.comment.text) },
                enabled = !state.sendingMessage,
            ) {
                if (state.sendingMessage) {
                    PrimalLoadingSpinner(size = 24.dp)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = stringResource(id = R.string.live_stream_send_button_title),
                        tint = AppTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChatMessageListItem(
    message: ChatMessageUi,
    onProfileClick: (String) -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val localUriHandler = LocalUriHandler.current

    val authorNameColor = AppTheme.colorScheme.onSurface
    val defaultTextColor = AppTheme.extraColorScheme.onSurfaceVariantAlt1
    val linkStyle = SpanStyle(textDecoration = TextDecoration.Underline)
    val highlightColor = AppTheme.colorScheme.primary

    val annotatedContent = remember(message) {
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = authorNameColor,
                ),
            ) {
                append(message.authorProfile.authorDisplayName)
            }
            append(" ")

            val renderedContent = renderChatMessageContentAsAnnotatedString(
                message = message,
                highlightColor = highlightColor,
            )

            val messageWithLinks = spannableTextWithLinks(
                text = renderedContent,
                defaultColor = defaultTextColor,
                linkStyle = linkStyle,
            )
            append(messageWithLinks)
        }
    }

    Row(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() },
                )
            },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        UniversalAvatarThumbnail(
            avatarCdnImage = message.authorProfile.avatarCdnImage,
            avatarSize = 24.dp,
            legendaryCustomization = message.authorProfile.premiumDetails?.legendaryCustomization,
        )

        PrimalClickableText(
            modifier = Modifier.padding(top = 7.dp),
            text = annotatedContent,
            style = AppTheme.typography.bodyLarge.copy(fontSize = 15.sp),
            onClick = { position, _ ->
                val urlAnnotation = annotatedContent.getStringAnnotations(
                    tag = URL_ANNOTATION_TAG,
                    start = position,
                    end = position,
                ).firstOrNull()

                if (urlAnnotation != null) {
                    localUriHandler.openUriSafely(urlAnnotation.item)
                    return@PrimalClickableText
                }

                val profileAnnotation = annotatedContent.getStringAnnotations(
                    tag = "profileId",
                    start = position,
                    end = position,
                ).firstOrNull()

                if (profileAnnotation != null) {
                    onProfileClick(profileAnnotation.item)
                    return@PrimalClickableText
                }
            },
            onLongClick = onLongClick,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ZapMessageListItem(
    zap: EventZapUiModel,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = ZapMessageBorderColor,
                shape = AppTheme.shapes.medium,
            )
            .clip(AppTheme.shapes.medium)
            .background(color = ZapMessageBackgroundColor.copy(alpha = 0.2f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() },
                )
            }
            .padding(horizontal = 8.dp, vertical = 10.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            UniversalAvatarThumbnail(
                avatarCdnImage = zap.zapperAvatarCdnImage,
                avatarSize = 24.dp,
                legendaryCustomization = zap.zapperLegendaryCustomization,
            )
            ZapMessageContent(zap = zap)
        }
    }
}

@Composable
private fun ZapMessageContent(zap: EventZapUiModel) {
    val localUriHandler = LocalUriHandler.current

    Column(modifier = Modifier.padding(top = 1.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = ZapMessageProfileHandleColor, fontWeight = FontWeight.Bold)) {
                        append(zap.zapperName)
                    }
                    withStyle(style = SpanStyle(color = ZapMessageProfileHandleColor)) {
                        append(" zapped")
                    }
                },
                style = AppTheme.typography.bodyLarge.copy(fontSize = 16.sp),
                lineHeight = 20.sp,
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .background(
                        color = ZapMessageProfileHandleColor,
                        shape = AppTheme.shapes.extraLarge,
                    )
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val numberFormatter = remember { NumberFormat.getInstance() }
                val formattedAmount = remember(zap.amountInSats) {
                    numberFormatter.format(zap.amountInSats.toLong())
                }

                IconText(
                    modifier = Modifier
                        .alignByBaseline()
                        .padding(end = 2.dp, top = 1.dp),
                    text = formattedAmount,
                    fontWeight = FontWeight.Bold,
                    style = AppTheme.typography.bodySmall.copy(
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                    ),
                    leadingIcon = PrimalIcons.NavWalletBoltFilled,
                    iconSize = 16.sp,
                    color = AppTheme.colorScheme.surface,
                )
            }
        }

        if (!zap.message.isNullOrBlank()) {
            val defaultTextColor = AppTheme.colorScheme.onSurface
            val linkStyle = SpanStyle(textDecoration = TextDecoration.Underline)

            val contentText = remember(zap.message, defaultTextColor) {
                spannableTextWithLinks(
                    text = AnnotatedString(zap.message),
                    defaultColor = defaultTextColor,
                    linkStyle = linkStyle,
                )
            }
            PrimalClickableText(
                modifier = Modifier.padding(top = 5.dp),
                text = contentText,
                style = AppTheme.typography.bodyLarge.copy(fontSize = 15.sp),
                onClick = { position, _ ->
                    contentText.getStringAnnotations(
                        tag = URL_ANNOTATION_TAG,
                        start = position,
                        end = position,
                    ).firstOrNull()?.let { annotation ->
                        localUriHandler.openUriSafely(annotation.item)
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailsSection(message: ChatMessageUi, onReport: (ReportType) -> Unit) {
    var reportDialogVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                AppTheme.extraColorScheme.surfaceVariantAlt1,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.live_stream_chat_message),
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 16.sp,
            ),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        ChatMessageListItem(message = message, onProfileClick = {}, onLongClick = {})

        PrimalFilledButton(
            modifier = Modifier.padding(start = 40.dp),
            containerColor = ReportButtonHandleColor,
            contentColor = AppTheme.colorScheme.onSurface,
            textStyle = AppTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp),
            onClick = { reportDialogVisible = true },
            contentPadding = PaddingValues(18.dp, vertical = 1.dp),
            height = 41.dp,
        ) {
            Icon(
                imageVector = PrimalIcons.ContextReportContent,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.live_stream_report_message_button))
        }
    }

    if (reportDialogVisible) {
        ReportUserDialog(
            onDismissRequest = { reportDialogVisible = false },
            onReportClick = { type ->
                reportDialogVisible = false
                onReport(type)
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ZapDetailsSection(zap: EventZapUiModel, onReport: (ReportType) -> Unit) {
    var reportDialogVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                AppTheme.extraColorScheme.surfaceVariantAlt1,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(id = R.string.live_stream_chat_message),
            style = AppTheme.typography.bodyLarge.copy(
                fontSize = 15.sp,
                lineHeight = 16.sp,
            ),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        ZapMessageListItem(zap = zap, onLongClick = {})

        PrimalFilledButton(
            containerColor = ReportButtonHandleColor,
            contentColor = AppTheme.colorScheme.onSurface,
            textStyle = AppTheme.typography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 20.sp),
            onClick = { reportDialogVisible = true },
            contentPadding = PaddingValues(18.dp, vertical = 0.dp),
            height = 41.dp,
        ) {
            Icon(
                imageVector = PrimalIcons.ContextReportContent,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = stringResource(id = R.string.live_stream_report_message_button))
        }
    }

    if (reportDialogVisible) {
        ReportUserDialog(
            onDismissRequest = { reportDialogVisible = false },
            onReportClick = { type ->
                reportDialogVisible = false
                onReport(type)
            },
        )
    }
}

private fun handleZapProfile(
    state: LiveStreamContract.UiState,
    callbacks: LiveStreamContract.ScreenCallbacks,
    coroutineScope: CoroutineScope,
    snackbarHostState: SnackbarHostState,
    context: Context,
) {
    val streamInfo = state.streamInfo ?: return
    val profileDetails = streamInfo.mainHostProfile
    val profileLud16 = profileDetails?.lightningAddress

    if (profileLud16?.isLightningAddress() == true) {
        callbacks.onSendWalletTx(
            DraftTx(
                targetUserId = streamInfo.mainHostId,
                targetLud16 = profileLud16,
            ),
        )
    } else {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(
                message = context.getString(
                    R.string.wallet_send_payment_error_nostr_user_without_lightning_address,
                    profileDetails?.authorDisplayName
                        ?: context.getString(R.string.wallet_send_payment_this_user_chunk),
                ),
                duration = SnackbarDuration.Short,
            )
        }
    }
}

private fun spannableTextWithLinks(
    text: AnnotatedString,
    defaultColor: Color,
    linkStyle: SpanStyle,
): AnnotatedString {
    val uriLinks = text.text.detectUrls() + text.text.parseNostrUris()
        .filterNot { it.contains("nprofile") }

    return buildAnnotatedString {
        withStyle(style = SpanStyle(color = defaultColor)) {
            append(text)
        }

        uriLinks.forEach { url ->
            val startIndex = text.text.indexOf(url)
            if (startIndex != -1) {
                addStyle(
                    style = linkStyle,
                    start = startIndex,
                    end = startIndex + url.length,
                )
                addStringAnnotation(
                    tag = URL_ANNOTATION_TAG,
                    annotation = url,
                    start = startIndex,
                    end = startIndex + url.length,
                )
            }
        }
    }
}

private fun String.replaceNostrProfileUrisWithHandles(resources: List<NoteNostrUriUi>): String {
    var newContent = this
    resources.forEach {
        checkNotNull(it.referencedUser)
        newContent = newContent.replace(
            oldValue = it.uri,
            newValue = it.referencedUser.displayUsername,
            ignoreCase = true,
        )
    }
    return newContent
}

private fun renderChatMessageContentAsAnnotatedString(message: ChatMessageUi, highlightColor: Color): AnnotatedString {
    val mentionedUsers = message.nostrUris.filter { it.type == EventUriNostrType.Profile }

    val refinedContent = message.content
        .clearAtSignFromNostrUris()
        .replaceNostrProfileUrisWithHandles(resources = mentionedUsers)

    return buildAnnotatedString {
        append(refinedContent)

        mentionedUsers.forEach {
            checkNotNull(it.referencedUser)
            addProfileAnnotation(
                referencedUser = it.referencedUser,
                content = refinedContent,
                highlightColor = highlightColor,
            )
        }
    }
}

private fun AnnotatedString.Builder.addProfileAnnotation(
    referencedUser: ReferencedUser,
    content: String,
    highlightColor: Color,
) {
    val displayHandle = referencedUser.displayUsername
    var startIndex = content.indexOf(displayHandle)

    while (startIndex >= 0) {
        val endIndex = startIndex + displayHandle.length

        addStyle(
            style = SpanStyle(color = highlightColor),
            start = startIndex,
            end = endIndex,
        )

        addStringAnnotation(
            tag = "profileId",
            annotation = referencedUser.userId,
            start = startIndex,
            end = endIndex,
        )

        startIndex = content.indexOf(displayHandle, startIndex + 1)
    }
}
