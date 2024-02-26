package net.primal.android.core.compose

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun PrimalDivider(modifier: Modifier = Modifier, thickness: Dp = 0.5.dp) {
    HorizontalDivider(
        modifier = modifier,
        thickness = thickness,
        color = AppTheme.colorScheme.outline,
    )
}
