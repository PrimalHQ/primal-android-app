package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import net.primal.android.LocalPrimalTheme
import net.primal.android.R


@Composable
fun GridLoadingPlaceholder(
    modifier: Modifier = Modifier,
    itemPadding: PaddingValues = PaddingValues(all = 0.dp),
    contentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
    repeat: Int = 10,
    columnCount: Int = 3,
) {
    val animationRawResId = when (LocalPrimalTheme.current.isDarkTheme) {
        true -> R.raw.primal_loader_generic_square
        false -> R.raw.primal_loader_generic_square_light
    }

    LazyVerticalGrid(
        contentPadding = PaddingValues(4.dp),
        columns = GridCells.Fixed(count = columnCount),
        modifier = modifier
            .padding(contentPaddingValues),
    ) {
        items(
            count = repeat * columnCount,
        ) {
            repeat(times = repeat * columnCount) {
                Box(
                    modifier = Modifier
                        .clipToBounds()
                        .fillMaxSize(),
                ) {
                    InfiniteLottieAnimation(
                        modifier = Modifier
                            .padding(itemPadding),
                        resId = animationRawResId,
                    )
                }
            }
        }
    }
}
