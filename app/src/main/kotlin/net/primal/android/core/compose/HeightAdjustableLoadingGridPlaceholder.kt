package net.primal.android.core.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.activity.LocalPrimalTheme

@Composable
fun HeightAdjustableLoadingGridPlaceholder(
    rows: Int,
    columns: Int,
    cellHeight: Dp,
    modifier: Modifier = Modifier,
    horizontalSpacing: Dp = 8.dp,
    verticalSpacing: Dp = 16.dp,
) {
    val animationRawResId = when (LocalPrimalTheme.current.isDarkTheme) {
        true -> R.raw.primal_loader_generic_square_dark
        false -> R.raw.primal_loader_generic_square_light
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(verticalSpacing),
    ) {
        repeat(rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cellHeight),
                horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            ) {
                repeat(columns) {
                    InfiniteLottieAnimation(
                        modifier = Modifier.weight(1f),
                        resId = animationRawResId,
                    )
                }
            }
        }
    }
}
