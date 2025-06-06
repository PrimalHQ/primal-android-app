package net.primal.android.feeds.list.ui

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.ConfirmActionAlertDialog
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.theme.AppTheme
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedList(
    title: String,
    feeds: List<FeedUi>,
    activeFeed: FeedUi?,
    onFeedClick: (feed: FeedUi) -> Unit,
    onRestoreDefaultPrimalFeeds: () -> Unit,
    modifier: Modifier = Modifier,
    enableEditMode: Boolean = false,
    isEditMode: Boolean = false,
    onEditFeedClick: (() -> Unit)? = null,
    onAddFeedClick: (() -> Unit)? = null,
    onEditDoneClick: (() -> Unit)? = null,
    onFeedReordered: ((feeds: List<FeedUi>) -> Unit)? = null,
    onFeedEnabled: ((feed: FeedUi, enabled: Boolean) -> Unit)? = null,
    onFeedRemoved: ((feed: FeedUi) -> Unit)? = null,
) {
    var data by remember(feeds) { mutableStateOf(feeds) }
    val haptic = rememberReorderHapticFeedback()
    val feedsListState = rememberLazyListState()
    val reorderableFeedsListState = rememberReorderableLazyListState(lazyListState = feedsListState) { from, to ->
        val newData = data.toMutableList().apply {
            val removed = removeAt(from.index)
            add(to.index, removed)
        }.toList()
        data = newData
        onFeedReordered?.invoke(newData)
        haptic.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
    }

    var deleteFeedDialogVisible by remember { mutableStateOf<FeedUi?>(null) }

    if (deleteFeedDialogVisible != null) {
        ConfirmActionAlertDialog(
            confirmText = stringResource(id = R.string.feed_list_dialog_confirm),
            dismissText = stringResource(id = R.string.feed_list_dialog_dismiss),
            dialogTitle = stringResource(R.string.feed_list_remove_feed_title),
            dialogText = stringResource(R.string.feed_list_remove_feed_text, deleteFeedDialogVisible?.title ?: ""),
            onConfirmation = {
                if (onFeedRemoved != null) {
                    deleteFeedDialogVisible?.let(onFeedRemoved)
                }
                deleteFeedDialogVisible = null
            },
            onDismissRequest = {
                deleteFeedDialogVisible = null
            },
        )
    }

    var restoreDefaultDialogVisible by remember { mutableStateOf(false) }
    if (restoreDefaultDialogVisible) {
        ConfirmActionAlertDialog(
            confirmText = stringResource(id = R.string.feed_list_dialog_confirm),
            dismissText = stringResource(id = R.string.feed_list_dialog_dismiss),
            dialogTitle = stringResource(id = R.string.feed_list_restore_default_feeds),
            dialogText = stringResource(id = R.string.feed_list_restore_default_feeds_prompt_text),
            onConfirmation = {
                onRestoreDefaultPrimalFeeds()
                restoreDefaultDialogVisible = false
            },
            onDismissRequest = {
                restoreDefaultDialogVisible = false
            },
        )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
                ),
                title = {
                    Text(text = title)
                },
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .background(color = AppTheme.extraColorScheme.surfaceVariantAlt2)
                    .padding(paddingValues)
                    .fillMaxSize(),
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
                        FeedListItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(AppTheme.shapes.large)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = LocalIndication.current,
                                    onClick = { onFeedClick(item) },
                                ),
                            data = item,
                            selected = item.spec == activeFeed?.spec,
                            editOptions = {
                                if (isEditMode) {
                                    Row {
                                        PrimalSwitch(
                                            checked = item.enabled,
                                            onCheckedChange = { enabled ->
                                                if (!enabled && item.deletable) {
                                                    deleteFeedDialogVisible = item
                                                } else {
                                                    onFeedEnabled?.invoke(item, enabled)
                                                }
                                            },
                                        )
                                        FeedDragHandle(
                                            haptic = haptic,
                                            interactionSource = interactionSource,
                                        )
                                    }
                                }
                            },
                        )
                    }
                }
                if (isEditMode) {
                    item {
                        val interactionSource = remember { MutableInteractionSource() }
                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(AppTheme.shapes.large)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = LocalIndication.current,
                                    onClick = { restoreDefaultDialogVisible = true },
                                ),
                            headlineContent = {
                                Text(
                                    color = AppTheme.colorScheme.primary,
                                    modifier = Modifier,
                                    text = stringResource(id = R.string.feed_list_restore_default_feeds),
                                    style = AppTheme.typography.bodySmall.copy(fontSize = 15.sp),
                                )
                            },
                        )
                    }
                }
            }
        },
        bottomBar = {
            if (enableEditMode) {
                PrimalDivider()
                if (!isEditMode) {
                    RegularBottomBar(onEditFeedClick = { onEditFeedClick?.invoke() })
                } else {
                    EditModeBottomBar(
                        onAddFeedClick = { onAddFeedClick?.invoke() },
                        onDoneClick = { onEditDoneClick?.invoke() },
                    )
                }
            }
        },
    )
}

@Composable
private fun RegularBottomBar(onEditFeedClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(onClick = onEditFeedClick) {
            Text(text = stringResource(id = R.string.feed_list_edit))
        }
    }
}

@Composable
private fun EditModeBottomBar(onAddFeedClick: () -> Unit, onDoneClick: () -> Unit) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TextButton(onClick = onAddFeedClick) {
            Text(text = stringResource(id = R.string.feed_list_add_feed))
        }

        TextButton(onClick = onDoneClick) {
            Text(text = stringResource(id = R.string.feed_list_done))
        }
    }
}

private fun FeedUi.uniqueKey() = "$spec;$title"

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
            imageVector = Icons.Rounded.Menu,
            contentDescription = stringResource(id = R.string.accessibility_feed_reorder_drag_handle),
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
