package net.primal.android.core.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun DeleteListItemImage(modifier: Modifier = Modifier, isRemovable: Boolean = true) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Spacer(
            modifier = Modifier
                .size(16.dp)
                .background(color = Color.White, shape = CircleShape),
        )

        Image(
            imageVector = Icons.Outlined.RemoveCircle,
            contentDescription = null,
            colorFilter = ColorFilter.tint(
                color = if (isRemovable) {
                    AppTheme.colorScheme.error
                } else {
                    AppTheme.colorScheme.outline
                },
            ),
        )
    }
}
