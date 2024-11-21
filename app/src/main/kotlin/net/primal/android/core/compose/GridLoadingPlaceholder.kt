package net.primal.android.core.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.LocalPrimalTheme
import net.primal.android.R

@Composable
fun GridLoadingPlaceholder(
    repeat: Int,
    columnCount: Int,
    modifier: Modifier = Modifier,
    itemPadding: Dp = 1.dp,
    contentPadding: PaddingValues = PaddingValues(all = 0.dp),
) {
    val animationRawResId = when (LocalPrimalTheme.current.isDarkTheme) {
        true -> R.raw.primal_loader_generic_square_dark
        false -> R.raw.primal_loader_generic_square_light
    }

    BoxWithConstraints {
        val height = maxWidth.div(other = 3)
        Column(
            modifier = modifier
                .verticalScroll(state = rememberScrollState())
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(itemPadding),
        ) {
            repeat(times = repeat) {
                Row(
                    modifier = Modifier
                        .height(height)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(itemPadding),
                ) {
                    repeat(columnCount) {
                        InfiniteLottieAnimation(
                            modifier = Modifier.weight(weight = 1f),
                            resId = animationRawResId,
                        )
                    }
                }
            }
        }
    }
}
