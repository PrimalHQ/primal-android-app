package net.primal.android.settings.feeds

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import net.primal.android.R
import net.primal.android.core.compose.DeleteListItemImage
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.settings.feeds.model.Feed
import net.primal.android.settings.feeds.model.FeedAction
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun FeedsSettingsScreen(viewModel: FeedsSettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_DESTROY -> viewModel.setEvent(FeedsSettingsContract.UiEvent.PersistFeeds)
            else -> Unit
        }
    }

    FeedsSettingsScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsSettingsScreen(
    state: FeedsSettingsContract.UiState,
    eventPublisher: (FeedsSettingsContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        content = { paddingValues ->
            DraggableFeedsLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                state = state,
                eventPublisher = eventPublisher,
            )
        },
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_feeds_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
    )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun DraggableFeedsLazyColumn(
    modifier: Modifier,
    state: FeedsSettingsContract.UiState,
    eventPublisher: (FeedsSettingsContract.UiEvent) -> Unit,
) {
    var feedActionState by remember { mutableStateOf<FeedAction>(FeedAction.Inactive) }
    FeedActionStateHandler(
        state = feedActionState,
        eventPublisher = eventPublisher,
        onStateReset = { feedActionState = FeedAction.Inactive },
    )

    var data by remember(state.feeds) { mutableStateOf(state.feeds) }
    val haptic = rememberReorderHapticFeedback()
    val feedsListState = rememberLazyListState()
    val reorderableFeedsListState = rememberReorderableLazyListState(lazyListState = feedsListState) { from, to ->
        val newData = data.toMutableList().apply {
            val removed = removeAt(from.index)
            add(to.index, removed)
        }.toList()
        data = newData
        eventPublisher(FeedsSettingsContract.UiEvent.FeedReordered(newData))
        haptic.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
    }

    LazyColumn(
        modifier = modifier,
        state = feedsListState,
        // https://github.com/Calvin-LL/Reorderable/issues/32
        contentPadding = PaddingValues(vertical = 1.dp),
    ) {
        itemsIndexed(
            items = data,
            key = { _, item -> item.uniqueKey() },
        ) { _, item ->
            ReorderableItem(
                state = reorderableFeedsListState,
                key = item.uniqueKey(),
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                Card(
                    onClick = {},
                    shape = RectangleShape,
                    interactionSource = interactionSource,
                ) {
                    FeedItem(
                        item = item,
                        dragHandle = {
                            FeedDragHandle(haptic = haptic, interactionSource = interactionSource)
                        },
                        onRemoveFeed = {
                            feedActionState = FeedAction.ConfirmRemove(
                                directive = item.directive,
                                name = item.name,
                            )
                        },
                    )
                    PrimalDivider()
                }
            }
        }
        item {
            RestoreDefaultFeedsListItem(
                onClick = {
                    feedActionState = FeedAction.ConfirmRestoreDefaults
                },
            )
        }
    }
}

@Composable
private fun RestoreDefaultFeedsListItem(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            modifier = Modifier.clickable(onClick = onClick),
            text = stringResource(
                R.string.settings_feeds_restore_default_title,
            ).lowercase(),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun rememberReorderHapticFeedback(): ReorderHapticFeedback {
    val view = LocalView.current
    return remember {
        object : ReorderHapticFeedback {
            override fun performHapticFeedback(type: ReorderHapticFeedbackType) {
                when (type) {
                    ReorderHapticFeedbackType.START ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            view.performHapticFeedback(HapticFeedbackConstants.DRAG_START)
                        }

                    ReorderHapticFeedbackType.MOVE ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
                        }

                    ReorderHapticFeedbackType.END ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            view.performHapticFeedback(HapticFeedbackConstants.GESTURE_END)
                        }
                }
            }
        }
    }
}

private enum class ReorderHapticFeedbackType {
    START,
    MOVE,
    END,
}

private interface ReorderHapticFeedback {
    fun performHapticFeedback(type: ReorderHapticFeedbackType)
}

private fun Feed.uniqueKey() = "$directive;$name"

@Composable
private fun FeedActionStateHandler(
    state: FeedAction,
    eventPublisher: (FeedsSettingsContract.UiEvent) -> Unit,
    onStateReset: () -> Unit,
) {
    when (state) {
        is FeedAction.ConfirmRemove -> {
            ConfirmActionAlertDialog(
                onDismissRequest = onStateReset,
                onConfirmation = {
                    eventPublisher(
                        FeedsSettingsContract.UiEvent.FeedRemoved(
                            name = state.name,
                            directive = state.directive,
                        ),
                    )

                    onStateReset()
                },
                dialogTitle = stringResource(
                    id = R.string.settings_feeds_remove_feed_prompt_title,
                ),
                dialogText = stringResource(
                    id = R.string.settings_feeds_remove_feed_prompt_text,
                    state.name,
                ),
            )
        }

        is FeedAction.ConfirmRestoreDefaults -> {
            ConfirmActionAlertDialog(
                onDismissRequest = onStateReset,
                onConfirmation = {
                    eventPublisher(FeedsSettingsContract.UiEvent.RestoreDefaultFeeds)
                    onStateReset()
                },
                dialogTitle = stringResource(R.string.settings_feeds_restore_default_title),
                dialogText = stringResource(
                    id = R.string.settings_feeds_restore_defaults_prompt_text,
                ),
            )
        }

        is FeedAction.Inactive -> Unit
    }
}

@Composable
private fun FeedItem(
    item: Feed,
    onRemoveFeed: () -> Unit,
    dragHandle: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(
            containerColor = AppTheme.colorScheme.surfaceVariant,
        ),
        headlineContent = {
            Text(
                text = item.name,
                fontWeight = FontWeight.W400,
                fontSize = 16.sp,
                lineHeight = 16.sp,
            )
        },
        leadingContent = {
            DeleteListItemImage(
                modifier = Modifier
                    .clickable(
                        enabled = item.isRemovable,
                        onClick = onRemoveFeed,
                    ),
                isRemovable = item.isRemovable,
            )
        },
        trailingContent = dragHandle,
    )
}

@Composable
private fun ReorderableCollectionItemScope.FeedDragHandle(
    haptic: ReorderHapticFeedback,
    interactionSource: MutableInteractionSource,
) {
    IconButton(
        modifier = Modifier
            .draggableHandle(
                onDragStarted = { haptic.performHapticFeedback(ReorderHapticFeedbackType.START) },
                onDragStopped = { haptic.performHapticFeedback(ReorderHapticFeedbackType.END) },
                interactionSource = interactionSource,
            )
            .clearAndSetSemantics { },
        onClick = {},
        interactionSource = interactionSource,
    ) {
        Icon(
            imageVector = Icons.Rounded.DragHandle,
            contentDescription = stringResource(id = R.string.accessibility_feed_reorder_drag_handle),
        )
    }
}

@Composable
private fun ConfirmActionAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
) {
    AlertDialog(
        containerColor = AppTheme.colorScheme.surfaceVariant,
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirmation() },
            ) {
                Text(text = stringResource(id = R.string.settings_network_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismissRequest() },
            ) {
                Text(text = stringResource(id = R.string.settings_network_dialog_dismiss))
            }
        },
    )
}

@Preview
@Composable
private fun PreviewSettingsScreen() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        FeedsSettingsScreen(
            state = FeedsSettingsContract.UiState(
                feeds = listOf(
                    Feed(
                        name = "Latest",
                        directive = "Latest",
                        isRemovable = false,
                    ),
                    Feed(
                        name = "Latest with Replies",
                        directive = "Latest with Replies",
                        isRemovable = false,
                    ),
                    Feed(
                        name = "Trending 24h",
                        directive = "Trending 24h",
                        isRemovable = true,
                    ),
                    Feed(
                        name = "Most zapped 4h",
                        directive = "Most zapped 4h",
                        isRemovable = true,
                    ),
                    Feed(
                        name = "#photography",
                        directive = "#photography",
                        isRemovable = true,
                    ),
                    Feed(
                        name = "#bitcoin2023",
                        directive = "#bitcoin2023",
                        isRemovable = true,
                    ),
                    Feed(
                        name = "#nostrasia",
                        directive = "#nostrasia",
                        isRemovable = true,
                    ),
                    Feed(
                        name = "#nature",
                        directive = "#nature",
                        isRemovable = true,
                    ),
                    Feed(
                        name = "#food",
                        directive = "#food",
                        isRemovable = true,
                    ),
                ),
            ),
            eventPublisher = {},
            onClose = {},
        )
    }
}
