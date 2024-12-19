package net.primal.android.messages.conversation

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.asBeforeNowFormat
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.heightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.NewDM
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.compose.isNotEmpty
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.core.utils.parseHashtags
import net.primal.android.messages.conversation.MessageConversationListContract.UiEvent
import net.primal.android.messages.conversation.MessageConversationListContract.UiEvent.ChangeRelation
import net.primal.android.messages.conversation.MessageConversationListContract.UiEvent.MarkAllConversationsAsRead
import net.primal.android.messages.conversation.model.MessageConversationUi
import net.primal.android.messages.domain.ConversationRelation
import net.primal.android.notes.feed.model.NoteContentUi
import net.primal.android.notes.feed.note.ui.renderContentAsAnnotatedString
import net.primal.android.theme.AppTheme

@Composable
fun MessageListScreen(
    viewModel: MessageConversationListViewModel,
    onConversationClick: (String) -> Unit,
    onNewMessageClick: () -> Unit,
    onProfileClick: (profileId: String) -> Unit,
    onClose: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> {
                viewModel.setEvent(UiEvent.ConversationsSeen)
            }

            else -> Unit
        }
    }

    MessageListScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onConversationClick = onConversationClick,
        onNewMessageClick = onNewMessageClick,
        onProfileClick = onProfileClick,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageListScreen(
    state: MessageConversationListContract.UiState,
    eventPublisher: (UiEvent) -> Unit,
    onConversationClick: (String) -> Unit,
    onNewMessageClick: () -> Unit,
    onProfileClick: (profileId: String) -> Unit,
    onClose: () -> Unit,
) {
    val conversations = state.conversations.collectAsLazyPagingItems()
    val listState = conversations.rememberLazyListStatePagingWorkaround()

    val firstConversationId = if (conversations.isNotEmpty()) conversations[0]?.participantId else null
    LaunchedEffect(firstConversationId) {
        if (listState.firstVisibleItemIndex <= 1) {
            listState.animateScrollToItem(index = 0)
        }
    }

    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.messages_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                footer = {
                    MessagesTabs(
                        relation = state.activeRelation,
                        onFollowsTabClick = {
                            eventPublisher(ChangeRelation(relation = ConversationRelation.Follows))
                        },
                        onOtherTabClick = {
                            eventPublisher(ChangeRelation(relation = ConversationRelation.Other))
                        },
                        onMarkAllRead = {
                            eventPublisher(MarkAllConversationsAsRead)
                        },
                    )
                },
            )
        },
        content = { paddingValues ->
            ConversationsList(
                loading = state.loading,
                conversations = conversations,
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .fillMaxSize(),
                state = listState,
                contentPadding = paddingValues,
                onConversationClick = onConversationClick,
                onProfileClick = onProfileClick,
                onRefreshClick = { eventPublisher(UiEvent.RefreshConversations) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewMessageClick,
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(color = AppTheme.colorScheme.primary, shape = CircleShape),
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                containerColor = Color.Unspecified,
                content = {
                    Icon(
                        imageVector = PrimalIcons.NewDM,
                        contentDescription = stringResource(id = R.string.accessibility_new_direct_message),
                        tint = Color.White,
                    )
                },
            )
        },
    )
}

@Composable
private fun ConversationsList(
    loading: Boolean,
    conversations: LazyPagingItems<MessageConversationUi>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    onConversationClick: (String) -> Unit,
    onProfileClick: (profileId: String) -> Unit,
    onRefreshClick: () -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = contentPadding,
        state = state,
    ) {
        items(
            count = conversations.itemCount,
            key = conversations.itemKey { it.participantId },
            contentType = conversations.itemContentType(),
        ) {
            val conversation = conversations[it]

            when {
                conversation != null -> Column {
                    ConversationListItem(
                        conversation = conversation,
                        onConversationClick = onConversationClick,
                        onProfileClick = onProfileClick,
                    )
                    PrimalDivider()
                }

                else -> {}
            }
        }

        if (conversations.isEmpty()) {
            val loadState = conversations.loadState.refresh
            when {
                loadState is LoadState.Loading || loading -> {
                    heightAdjustableLoadingLazyListPlaceholder(
                        height = 48.dp,
                        showDivider = true,
                        itemPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    )
                }

                loadState is LoadState.NotLoading -> {
                    item(contentType = "NoContent") {
                        ListNoContent(
                            modifier = Modifier.fillParentMaxSize(),
                            noContentText = stringResource(id = R.string.messages_no_conversations),
                            refreshButtonVisible = false,
                        )
                    }
                }

                loadState is LoadState.Error -> {
                    item(contentType = "RefreshError") {
                        ListNoContent(
                            modifier = Modifier.fillParentMaxSize(),
                            noContentText = stringResource(
                                id = R.string.messages_conversations_initial_loading_error,
                            ),
                            onRefresh = {
                                onRefreshClick()
                                conversations.refresh()
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationListItem(
    conversation: MessageConversationUi,
    onConversationClick: (String) -> Unit,
    onProfileClick: (profileId: String) -> Unit,
) {
    ListItem(
        modifier = Modifier.clickable {
            onConversationClick(conversation.participantId)
        },
        colors = ListItemDefaults.colors(containerColor = AppTheme.colorScheme.surfaceVariant),
        leadingContent = {
            UniversalAvatarThumbnail(
                avatarCdnImage = conversation.participantAvatarCdnImage,
                onClick = { onProfileClick(conversation.participantId) },
                legendaryCustomization = conversation.participantLegendaryCustomization,
            )
        },
        headlineContent = {
            Row {
                Box(
                    modifier = Modifier.weight(1f),
                ) {
                    val timestamp = conversation.lastMessageAt?.asBeforeNowFormat()
                    val suffixText = buildAnnotatedString {
                        val hasVerifiedBadge = !conversation.participantInternetIdentifier.isNullOrEmpty()
                        if (!hasVerifiedBadge) append(' ')
                        if (timestamp != null) {
                            append(
                                AnnotatedString(
                                    text = "| $timestamp",
                                    spanStyle = SpanStyle(
                                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                                        fontStyle = AppTheme.typography.bodySmall.fontStyle,
                                    ),
                                ),
                            )
                        }
                    }

                    NostrUserText(
                        displayName = conversation.participantUsername,
                        internetIdentifier = conversation.participantInternetIdentifier,
                        annotatedStringSuffixBuilder = { append(suffixText) },
                        legendaryCustomization = conversation.participantLegendaryCustomization,
                        style = AppTheme.typography.bodyMedium,
                    )
                }

                if (conversation.unreadMessagesCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(color = AppTheme.colorScheme.primary, shape = CircleShape),
                    )
                }
            }
        },
        supportingContent = {
            val annotatedContent = renderContentAsAnnotatedString(
                data = NoteContentUi(
                    noteId = conversation.lastMessageId ?: "",
                    content = conversation.lastMessageSnippet ?: conversation.participantInternetIdentifier ?: "",
                    hashtags = conversation.lastMessageSnippet?.parseHashtags() ?: emptyList(),
                    attachments = conversation.lastMessageAttachments,
                    nostrUris = conversation.lastMessageNostrUris,
                ),
                expanded = false,
                seeMoreText = "",
                shouldKeepNostrNoteUris = true,
                highlightColor = AppTheme.colorScheme.primary,
            )

            Text(
                modifier = Modifier.padding(top = 4.dp),
                text = if (conversation.isLastMessageFromUser) {
                    buildAnnotatedString {
                        append(stringResource(id = R.string.chat_message_user_snippet_prefix))
                        append(" ")
                    }.plus(annotatedContent)
                } else {
                    annotatedContent
                },
                overflow = TextOverflow.Ellipsis,
                minLines = 1,
                maxLines = 2,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        },
    )
}

@Composable
private fun MessagesTabs(
    relation: ConversationRelation,
    onFollowsTabClick: () -> Unit,
    onOtherTabClick: () -> Unit,
    onMarkAllRead: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var followsTabWidth by remember { mutableIntStateOf(0) }
        var otherTabWidth by remember { mutableIntStateOf(0) }
        val tabsSpaceWidth = 16.dp

        Column(
            modifier = Modifier.wrapContentWidth(),
        ) {
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MessagesTab(
                    text = stringResource(id = R.string.messages_follows_tab_title).uppercase(),
                    onSizeChanged = { size -> followsTabWidth = size.width },
                    onClick = onFollowsTabClick,
                )

                Spacer(modifier = Modifier.width(tabsSpaceWidth))

                MessagesTab(
                    text = stringResource(id = R.string.messages_other_tab_title).uppercase(),
                    onSizeChanged = { size -> otherTabWidth = size.width },
                    onClick = onOtherTabClick,
                )
            }

            with(LocalDensity.current) {
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(
                            animateIntAsState(
                                targetValue = when (relation) {
                                    ConversationRelation.Follows -> followsTabWidth
                                    ConversationRelation.Other -> otherTabWidth
                                },
                                label = "indicatorWidth",
                            ).value.toDp(),
                        )
                        .offset(
                            y = (-4).dp,
                            x = animateIntAsState(
                                targetValue = when (relation) {
                                    ConversationRelation.Follows -> 0
                                    ConversationRelation.Other -> {
                                        followsTabWidth + tabsSpaceWidth
                                            .toPx()
                                            .toInt()
                                    }
                                },
                                label = "indicatorOffsetX",
                            ).value.toDp(),
                        )
                        .background(
                            color = AppTheme.colorScheme.primary,
                            shape = AppTheme.shapes.small,
                        ),
                )
            }
        }

        Text(
            modifier = Modifier
                .defaultMinSize(minHeight = 32.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMarkAllRead,
                    role = Role.Button,
                ),
            text = stringResource(id = R.string.messages_mark_all_read_button),
            textAlign = TextAlign.End,
            color = AppTheme.colorScheme.primary,
            style = AppTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun MessagesTab(
    text: String,
    onSizeChanged: (IntSize) -> Unit,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .wrapContentWidth()
            .onSizeChanged { onSizeChanged(it) }
            .defaultMinSize(minHeight = 32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                role = Role.Button,
            ),
        text = text,
        style = AppTheme.typography.bodySmall,
        color = AppTheme.colorScheme.onSurface,
    )
}
