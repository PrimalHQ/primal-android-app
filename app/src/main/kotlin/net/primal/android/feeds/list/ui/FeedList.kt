package net.primal.android.feeds.list.ui

import android.os.Build
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import net.primal.android.core.compose.PrimalOverlayBottomBar
import net.primal.android.core.compose.PrimalOverlayCloseButton
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.PencilUnderline
import net.primal.android.feeds.list.ui.model.FeedUi
import net.primal.android.theme.AppTheme
import net.primal.domain.feeds.isAdvancedSearchFeedSpec
import sh.calvin.reorderable.ReorderableCollectionItemScope
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedList(
    feeds: List<FeedUi>,
    activeFeed: FeedUi?,
    onFeedClick: (feed: FeedUi) -> Unit,
    onRestoreDefaultPrimalFeeds: () -> Unit,
    modifier: Modifier = Modifier,
    enableEditMode: Boolean = false,
    isEditMode: Boolean = false,
    onEditFeedClick: (() -> Unit)? = null,
    onCloseClick: (() -> Unit)? = null,
    onAddFeedClick: (() -> Unit)? = null,
    onEditDoneClick: (() -> Unit)? = null,
    onFeedReordered: ((feeds: List<FeedUi>) -> Unit)? = null,
    onFeedEnabled: ((feed: FeedUi, enabled: Boolean) -> Unit)? = null,
    onFeedRemoved: ((feed: FeedUi) -> Unit)? = null,
    onEditAdvancedSearchFeedClick: ((feedSpec: String) -> Unit)? = null,
) {
    var data by remember(feeds) { mutableStateOf(feeds) }
    val haptic = rememberReorderHapticFeedback()
    val feedsListState = rememberLazyListState()

    val reorderableFeedsListState = rememberReorderableLazyListState(
        lazyListState = feedsListState,
    ) { from, to ->
        data = data.toMutableList().apply { add(to.index, removeAt(from.index)) }
        onFeedReordered?.invoke(data)
        haptic.performHapticFeedback(ReorderHapticFeedbackType.MOVE)
    }

    var deleteFeedDialogVisible by remember { mutableStateOf<FeedUi?>(null) }
    var restoreDefaultDialogVisible by remember { mutableStateOf(false) }

    FeedListDialogs(
        deleteFeedDialogVisible = deleteFeedDialogVisible,
        restoreDefaultDialogVisible = restoreDefaultDialogVisible,
        onFeedRemoved = onFeedRemoved,
        onRestoreDefaultPrimalFeeds = onRestoreDefaultPrimalFeeds,
        onDeleteDismiss = { deleteFeedDialogVisible = null },
        onRestoreDismiss = { restoreDefaultDialogVisible = false },
    )

    Column(modifier = modifier) {
        LazyColumn(
            modifier = Modifier
                .background(color = AppTheme.extraColorScheme.surfaceVariantAlt2)
                .weight(1f),
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
                        isEditMode = isEditMode,
                        editOptions = {
                            FeedEditOptions(
                                item = item,
                                haptic = haptic,
                                interactionSource = interactionSource,
                                onFeedEnabled = onFeedEnabled,
                                onDeleteRequest = { deleteFeedDialogVisible = item },
                                onEditAdvancedSearchFeedClick = onEditAdvancedSearchFeedClick,
                            )
                        },
                    )
                }
            }
            if (isEditMode) {
                item {
                    RestoreDefaultFeedsItem(onRestoreClick = { restoreDefaultDialogVisible = true })
                }
            }
        }

        FeedListBottomBar(
            enableEditMode = enableEditMode,
            isEditMode = isEditMode,
            onEditFeedClick = onEditFeedClick,
            onCloseClick = onCloseClick,
            onAddFeedClick = onAddFeedClick,
            onEditDoneClick = onEditDoneClick,
        )
    }
}

@Composable
private fun FeedListBottomBar(
    enableEditMode: Boolean,
    isEditMode: Boolean,
    onEditFeedClick: (() -> Unit)?,
    onCloseClick: (() -> Unit)?,
    onAddFeedClick: (() -> Unit)?,
    onEditDoneClick: (() -> Unit)?,
) {
    if (enableEditMode) {
        if (!isEditMode) {
            RegularBottomBar(
                onEditFeedClick = { onEditFeedClick?.invoke() },
                onCloseClick = { onCloseClick?.invoke() },
            )
        } else {
            EditModeBottomBar(
                onAddFeedClick = { onAddFeedClick?.invoke() },
                onDoneClick = { onEditDoneClick?.invoke() },
            )
        }
    }
}

@Composable
private fun FeedListDialogs(
    deleteFeedDialogVisible: FeedUi?,
    restoreDefaultDialogVisible: Boolean,
    onFeedRemoved: ((feed: FeedUi) -> Unit)?,
    onRestoreDefaultPrimalFeeds: () -> Unit,
    onDeleteDismiss: () -> Unit,
    onRestoreDismiss: () -> Unit,
) {
    if (deleteFeedDialogVisible != null) {
        ConfirmActionAlertDialog(
            confirmText = stringResource(id = R.string.feed_list_dialog_confirm),
            dismissText = stringResource(id = R.string.feed_list_dialog_dismiss),
            dialogTitle = stringResource(R.string.feed_list_remove_feed_title),
            dialogText = stringResource(R.string.feed_list_remove_feed_text, deleteFeedDialogVisible.title),
            onConfirmation = {
                onFeedRemoved?.invoke(deleteFeedDialogVisible)
                onDeleteDismiss()
            },
            onDismissRequest = onDeleteDismiss,
        )
    }

    if (restoreDefaultDialogVisible) {
        ConfirmActionAlertDialog(
            confirmText = stringResource(id = R.string.feed_list_dialog_confirm),
            dismissText = stringResource(id = R.string.feed_list_dialog_dismiss),
            dialogTitle = stringResource(id = R.string.feed_list_restore_default_feeds),
            dialogText = stringResource(id = R.string.feed_list_restore_default_feeds_prompt_text),
            onConfirmation = {
                onRestoreDefaultPrimalFeeds()
                onRestoreDismiss()
            },
            onDismissRequest = onRestoreDismiss,
        )
    }
}

@Composable
private fun ReorderableCollectionItemScope.FeedEditOptions(
    item: FeedUi,
    haptic: ReorderHapticFeedback,
    interactionSource: MutableInteractionSource,
    onFeedEnabled: ((feed: FeedUi, enabled: Boolean) -> Unit)?,
    onDeleteRequest: () -> Unit,
    onEditAdvancedSearchFeedClick: ((feedSpec: String) -> Unit)? = null,
) {
    Row {
        if (item.spec.isAdvancedSearchFeedSpec()) {
            IconButton(onClick = { onEditAdvancedSearchFeedClick?.invoke(item.spec) }) {
                Icon(
                    imageVector = PrimalIcons.PencilUnderline,
                    contentDescription = null,
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        }
        PrimalSwitch(
            checked = item.enabled,
            onCheckedChange = { enabled ->
                if (!enabled && item.deletable) {
                    onDeleteRequest()
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

@Composable
private fun RestoreDefaultFeedsItem(onRestoreClick: () -> Unit) {
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
                onClick = onRestoreClick,
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

@Composable
private fun RegularBottomBar(onEditFeedClick: () -> Unit, onCloseClick: () -> Unit) {
    PrimalOverlayBottomBar(
        leading = {
            TextButton(onClick = onEditFeedClick) {
                Text(text = stringResource(id = R.string.feed_list_edit))
            }
        },
        trailing = { PrimalOverlayCloseButton(onClick = onCloseClick) },
    )
}

@Composable
private fun EditModeBottomBar(onAddFeedClick: () -> Unit, onDoneClick: () -> Unit) {
    PrimalOverlayBottomBar(
        leading = {
            TextButton(onClick = onAddFeedClick) {
                Text(text = stringResource(id = R.string.feed_list_add_feed))
            }
        },
        trailing = {
            TextButton(onClick = onDoneClick) {
                Text(text = stringResource(id = R.string.feed_list_done))
            }
        },
    )
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
