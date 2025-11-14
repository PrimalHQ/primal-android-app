package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Check
import net.primal.android.theme.AppTheme

@Composable
fun PrimalCheckBox(
    modifier: Modifier = Modifier,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    size: DpSize,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .run {
                if (checked) {
                    this.background(AppTheme.colorScheme.primary)
                } else {
                    this
                        .background(AppTheme.colorScheme.surfaceVariant)
                        .border(
                            width = 1.dp,
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                            shape = CircleShape,
                        )
                }
            }
            .clickable { onCheckedChange(!checked) }
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                tint = Color.White,
                modifier = Modifier.fillMaxSize(),
                imageVector = PrimalIcons.Check,
                contentDescription = null,
            )
        }
    }
}
