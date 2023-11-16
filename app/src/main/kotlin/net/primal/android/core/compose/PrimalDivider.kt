package net.primal.android.core.compose

import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun PrimalDivider(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier,
        thickness = 0.5.dp,
        color = AppTheme.colorScheme.outline,
    )
}
