package net.primal.android.core.compose.dropdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import net.primal.android.theme.AppTheme

@Composable
fun DropdownPrimalMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    offset: DpOffset = DpOffset(0.dp, 0.dp),
    properties: PopupProperties = PopupProperties(focusable = true),
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        modifier = Modifier
            .background(
                color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                shape = AppTheme.shapes.small,
            ),
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = offset,
        properties = properties,
        content = content,
    )
}
