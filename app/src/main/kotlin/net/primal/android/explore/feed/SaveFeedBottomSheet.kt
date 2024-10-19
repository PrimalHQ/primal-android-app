package net.primal.android.explore.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.feeds.domain.FeedSpecKind
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveFeedBottomSheet(
    initialTitle: String,
    initialDescription: String,
    feedSpecKind: FeedSpecKind,
    onDismissRequest: () -> Unit,
    onAddToUserFeed: (title: String, description: String) -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()
    var title by rememberSaveable { mutableStateOf(initialTitle) }
    var description by rememberSaveable { mutableStateOf(initialDescription) }

    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        tonalElevation = 0.dp,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        contentColor = AppTheme.colorScheme.onSurfaceVariant,
    ) {
        CenterAlignedTopAppBar(
            title = {
                val titleResourceId = when (feedSpecKind) {
                    FeedSpecKind.Reads -> stringResource(id = R.string.explore_feeds_save_reads_feed_bottom_sheet_title)
                    FeedSpecKind.Notes -> stringResource(id = R.string.explore_feeds_save_home_feed_bottom_sheet_title)
                }
                Text(text = titleResourceId)
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
            ),
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            TextFieldColumn(
                value = title,
                onValueChange = { title = it },
                label = stringResource(id = R.string.explore_feeds_save_feed_title),
                clearValue = { title = "" },
            )
            TextFieldColumn(
                value = description,
                onValueChange = { description = it },
                label = stringResource(id = R.string.explore_feeds_save_feed_description),
                clearValue = { description = "" },
            )
        }

        PrimalFilledButton(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(vertical = 32.dp)
                .fillMaxWidth(),
            enabled = title.isNotEmpty() && description.isNotEmpty(),
            onClick = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismissRequest()
                    }

                    onAddToUserFeed(title, description)
                }
            },
        ) {
            Text(text = stringResource(id = R.string.explore_feeds_save_feed_save_button))
        }
    }
}

@Composable
fun TextFieldColumn(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    clearValue: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            style = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp),
        )
        TextField(
            shape = AppTheme.shapes.medium,
            colors = PrimalDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            textStyle = AppTheme.typography.bodyMedium.copy(fontSize = 16.sp),
            maxLines = 2,
            trailingIcon = {
                ClearButton(
                    onClick = {
                        clearValue()
                        focusRequester.requestFocus()
                    },
                )
            },
        )
    }
}

@Composable
private fun ClearButton(onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = null,
            tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        )
    }
}
