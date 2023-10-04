package net.primal.android.core.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun DropdownMenuItemText(
    text: String,
    color: Color = Color.Unspecified,
) {
    Text(
        modifier = Modifier.padding(end = 32.dp),
        text = text,
        color = color,
        style = AppTheme.typography.bodyMedium,
    )
}
