package net.primal.android.explore.asearch.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SingleChoicePicker(
    modifier: Modifier = Modifier,
    items: List<T>,
    titleText: String,
    onDismissRequest: () -> Unit,
    onItemSelected: (T) -> Unit,
    itemDisplayName: @Composable T.() -> String,
    selectedItem: T? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        tonalElevation = 0.dp,
        modifier = modifier.statusBarsPadding(),
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    ) {
        CenterAlignedTopAppBar(
            title = { Text(text = titleText) },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
            ),
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
        ) {
            items(items = items) { item ->
                ListItem(
                    modifier = Modifier
                        .clickable {
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        onDismissRequest()
                                    }
                                    onItemSelected(item)
                                }
                        },
                    colors = ListItemDefaults.colors(
                        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
                    ),
                    headlineContent = {
                        Text(
                            text = item.itemDisplayName(),
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                            style = AppTheme.typography.bodyMedium,
                        )
                    },
                    trailingContent = {
                        if (item == selectedItem) {
                            Icon(imageVector = Icons.Filled.Check, contentDescription = null)
                        }
                    },
                )
                PrimalDivider()
            }
        }
    }
}
