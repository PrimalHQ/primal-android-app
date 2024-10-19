package net.primal.android.core.compose

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.LocalPrimalTheme
import net.primal.android.R

@Composable
fun GridLoadingPlaceholder(
    modifier: Modifier = Modifier,
    itemPadding: PaddingValues = PaddingValues(all = 1.dp),
    contentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
    repeat: Int = 7,
    columnCount: Int = 3,
) {
    val animationRawResId = when (LocalPrimalTheme.current.isDarkTheme) {
        true -> R.raw.primal_loader_generic_square
        false -> R.raw.primal_loader_generic_square_light
    }

    BoxWithConstraints {
        val itemWidth = this.maxWidth / 3

        LazyVerticalGrid(
            contentPadding = contentPaddingValues,
            columns = GridCells.Fixed(count = columnCount),
            modifier = modifier,
        ) {
            items(
                count = repeat * columnCount,
            ) {
                repeat(times = repeat * columnCount) {
                    InfiniteLottieAnimation(
                        modifier = Modifier
                            .padding(itemPadding)
                            .size(itemWidth),
                        resId = animationRawResId,
                    )
                }
            }
        }
    }
}
