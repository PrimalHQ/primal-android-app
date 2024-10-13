package net.primal.android.core.compose

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.theme.AppTheme


@Composable
fun HeightAdjustableLoadingListPlaceholder(
    modifier: Modifier = Modifier,
    itemPadding: PaddingValues = PaddingValues(all = 0.dp),
    contentPaddingValues: PaddingValues = PaddingValues(all = 0.dp),
    clipShape: Shape = AppTheme.shapes.small,
    repeat: Int = 10,
    height: Dp = 100.dp,
) {
    val animationRawResId = when (LocalPrimalTheme.current.isDarkTheme) {
        true -> R.raw.primal_loader_generic_square
        false -> R.raw.primal_loader_generic_square_light
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .clipToBounds()
            .padding(16.dp)
            .padding(contentPaddingValues),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        repeat(times = repeat) {
            Box(
                modifier = Modifier
                    .clipToBounds()
                    .clip(clipShape)
                    .fillMaxWidth()
                    .height(height),
            ) {
                InfiniteLottieAnimation(
                    modifier = Modifier
                        .scale(10f)
                        .padding(itemPadding),
                    resId = animationRawResId,
                )
            }
        }
    }
}
