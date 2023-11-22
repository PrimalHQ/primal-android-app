package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun HorizontalPagerIndicator(
    modifier: Modifier,
    imagesCount: Int,
    currentPage: Int,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(imagesCount) { iteration ->
            val color = if (currentPage == iteration) {
                AppTheme.colorScheme.primary
            } else {
                AppTheme.colorScheme.onPrimary
            }
            Box(
                modifier = Modifier
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(color)
                    .size(8.dp),
            )
        }
    }
}
