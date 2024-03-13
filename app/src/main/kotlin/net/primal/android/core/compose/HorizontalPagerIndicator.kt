package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun HorizontalPagerIndicator(
    modifier: Modifier,
    pagesCount: Int,
    currentPage: Int,
    predecessorsColor: Color = AppTheme.colorScheme.onPrimary,
    currentColor: Color = AppTheme.colorScheme.primary,
    successorsColor: Color = AppTheme.colorScheme.onPrimary,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(pagesCount) { iteration ->
            val color = when {
                iteration < currentPage -> predecessorsColor
                iteration == currentPage -> currentColor
                else -> successorsColor
            }
            Box(
                modifier = Modifier
                    .padding(vertical = 2.dp, horizontal = 4.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(8.dp),
            )
        }
    }
}
