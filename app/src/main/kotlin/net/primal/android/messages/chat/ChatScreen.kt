package net.primal.android.messages.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.AvatarThumbnailListItemImage
import net.primal.android.core.compose.ListLoading
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.feed.FeedPostContent
import net.primal.android.core.compose.foundation.rememberLazyListStatePagingWorkaround
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.isEmpty
import net.primal.android.core.ext.findByUrl
import net.primal.android.core.utils.asEllipsizedNpub
import net.primal.android.messages.chat.model.ChatMessageUi
import net.primal.android.theme.AppTheme

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit,
) {
    val state = viewModel.state.collectAsState()

    ChatScreen(
        state = state.value,
        onClose = onClose,
        onProfileClick = onProfileClick,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    state: ChatContract.UiState,
    onClose: () -> Unit,
    onProfileClick: (String) -> Unit,
    eventPublisher: (ChatContract.UiEvent) -> Unit,
) {

    val messagesPagingItems = state.messages.collectAsLazyPagingItems()
    val listState = messagesPagingItems.rememberLazyListStatePagingWorkaround()

    Scaffold(
        modifier = Modifier.navigationBarsPadding(),
        topBar = {
            PrimalTopAppBar(
                title = state.participantProfile?.userDisplayName
                    ?: state.participantId.asEllipsizedNpub(),
                subtitle = state.participantProfile?.internetIdentifier,
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(CircleShape)
                    ) {
                        val resource =
                            state.participantMediaResources.findByUrl(url = state.participantProfile?.avatarUrl)
                        val variant = resource?.variants?.minByOrNull { it.width }
                        val imageSource = variant?.mediaUrl ?: state.participantProfile?.avatarUrl
                        AvatarThumbnailListItemImage(
                            source = imageSource,
                            modifier = Modifier.size(32.dp),
                            onClick = { onProfileClick(state.participantId) },
                        )
                    }
                }
            )
        },
        content = { contentPadding ->
            ChatList(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .fillMaxSize(),
                state = listState,
                contentPadding = contentPadding,
                messages = messagesPagingItems,
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier.background(color = AppTheme.colorScheme.surface),
            ) {
                PrimalDivider()
                MessageOutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(vertical = 8.dp)
                        .imePadding(),
                    value = "",
                    onValueChange = { },
                )
            }
        },
    )
}

@Composable
private fun ChatList(
    messages: LazyPagingItems<ChatMessageUi>,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = contentPadding,
        reverseLayout = true,
    ) {
        items(
            count = messages.itemCount,
            key = messages.itemKey { it.messageId },
        ) {
            val chatMessage = messages[it]

            when {
                chatMessage != null -> {
                    ChatMessageListItem(
                        chatMessage = chatMessage,
                    )
                }

                else -> {}
            }
        }

        if (messages.isEmpty()) {
            when (messages.loadState.refresh) {
                LoadState.Loading -> {
                    item(contentType = "LoadingRefresh") {
                        ListLoading(
                            modifier = Modifier.fillParentMaxSize(),
                        )
                    }
                }

                is LoadState.NotLoading -> {
                    item(contentType = "NoContent") {
                        ListNoContent(
                            modifier = Modifier.fillParentMaxSize(),
                            noContentText = "",
                            refreshButtonVisible = false,
                        )
                    }
                }

                is LoadState.Error -> {
                    item(contentType = "RefreshError") {
                        ListNoContent(
                            modifier = Modifier.fillParentMaxSize(),
                            noContentText = stringResource(id = R.string.messages_chat_initial_loading_error),
                            onRefresh = { messages.refresh() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatMessageListItem(
    chatMessage: ChatMessageUi,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (chatMessage.isUserMessage) {
            Alignment.TopEnd
        } else {
            Alignment.TopStart
        }
    ) {
        FeedPostContent(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .background(
                    color = if (chatMessage.isUserMessage) {
                        AppTheme.colorScheme.primary
                    } else {
                        AppTheme.extraColorScheme.surfaceVariantAlt
                    },
                    shape = AppTheme.shapes.small,
                )
                .fillMaxWidth(fraction = 0.7f)
                .wrapContentHeight()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            content = chatMessage.content,
            expanded = true,
            hashtags = chatMessage.hashtags,
            mediaResources = chatMessage.mediaResources,
            nostrResources = chatMessage.nostrResources,
            onClick = { },
            onProfileClick = {

            },
            onPostClick = {

            },
            onUrlClick = {

            },
            onHashtagClick = {

            },
        )
    }
}

@Composable
private fun MessageOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        maxLines = 10,
        enabled = true,
        placeholder = {
            Text(
                text = stringResource(
                    id = R.string.chat_message_hint,
                    "qauser"
                ),
                maxLines = 1,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodyMedium,
            )
        },
        trailingIcon = {
            AppBarIcon(
                icon = Icons.Outlined.ArrowUpward,
                onClick = {},
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        },
        textStyle = AppTheme.typography.bodyMedium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt,
            focusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt,
            focusedBorderColor = Color.Unspecified,
            unfocusedBorderColor = Color.Unspecified,
            errorBorderColor = Color.Unspecified,
            disabledBorderColor = Color.Unspecified
        ),
    )
}