package net.primal.android.feeds.list.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import net.primal.android.core.compose.picker.BasePickerListItem
import net.primal.android.core.compose.picker.PickerListItemCheckIcon
import net.primal.android.feeds.list.ui.model.FeedUi

@Composable
fun FeedListItem(
    modifier: Modifier,
    data: FeedUi,
    selected: Boolean,
    isEditMode: Boolean = false,
    editOptions: @Composable () -> Unit,
) {
    val effectiveSelected = selected && !isEditMode
    BasePickerListItem(
        modifier = modifier,
        title = data.title,
        subtitle = data.description.ifEmpty { null },
        selected = effectiveSelected,
        titleAlignment = if (data.description.isNotEmpty()) TextAlign.Start else TextAlign.Center,
        trailingContent = {
            if (isEditMode) {
                editOptions()
            } else if (selected) {
                PickerListItemCheckIcon()
            }
        },
    )
}
