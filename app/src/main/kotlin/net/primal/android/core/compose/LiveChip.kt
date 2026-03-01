package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

private object LiveChipDefaults {
    const val DOT_SIZE_SCALE_FACTOR = 0.080f
    const val VERTICAL_PADDING_SCALE_FACTOR = 0.036f
    const val HORIZONTAL_PADDING_SCALE_FACTOR = 0.090f
    const val TEXT_START_PADDING_SCALE_FACTOR = 0.052f
    const val FONT_SIZE_SCALE_FACTOR = 0.140f
    const val LETTER_SPACING_SCALE_FACTOR = 0.011f
}

@Composable
fun LiveChip(
    modifier: Modifier = Modifier,
    avatarSize: Dp,
    canDownscaleToZero: Boolean = false,
) {
    val density = LocalDensity.current
    val dynamicMinWidth = avatarSize + 13.dp

    val calculationWidth = if (canDownscaleToZero) {
        avatarSize
    } else {
        dynamicMinWidth
    }

    val dotSize = calculationWidth * LiveChipDefaults.DOT_SIZE_SCALE_FACTOR
    val verticalPadding = calculationWidth * LiveChipDefaults.VERTICAL_PADDING_SCALE_FACTOR
    val horizontalPadding = calculationWidth * LiveChipDefaults.HORIZONTAL_PADDING_SCALE_FACTOR
    val textStartPadding = calculationWidth * LiveChipDefaults.TEXT_START_PADDING_SCALE_FACTOR

    val fontSize = with(density) { (calculationWidth * LiveChipDefaults.FONT_SIZE_SCALE_FACTOR).toSp() }
    val letterSpacing = with(density) { (calculationWidth * LiveChipDefaults.LETTER_SPACING_SCALE_FACTOR).toSp() }

    Row(
        modifier = modifier
            .clip(AppTheme.shapes.extraLarge)
            .background(color = Color.Black)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .background(color = Color.Red, shape = CircleShape),
        )

        Text(
            modifier = Modifier.padding(start = textStartPadding),
            text = stringResource(id = R.string.live_stream_chip_title).uppercase(),
            style = AppTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                fontSize = fontSize,
                letterSpacing = letterSpacing,
            ),
        )
    }
}

@Preview(name = "Default (uses dynamic min width)")
@Composable
fun LiveChipDefaultBehaviorPreview() {
    PrimalTheme(PrimalTheme.Midnight) {
        Box(modifier = Modifier.size(40.dp)) {
            LiveChip(
                modifier = Modifier.align(Alignment.BottomCenter),
                avatarSize = 32.dp,
                canDownscaleToZero = false,
            )
        }
    }
}

@Preview(name = "Can Downscale to Zero (ignores min width)")
@Composable
fun LiveChipCanDownscalePreview() {
    PrimalTheme(PrimalTheme.Midnight) {
        Box(modifier = Modifier.size(40.dp)) {
            LiveChip(
                modifier = Modifier.align(Alignment.BottomCenter),
                avatarSize = 32.dp,
                canDownscaleToZero = true,
            )
        }
    }
}

@Preview(name = "Large Size (default behavior)")
@Composable
fun LiveChipLargePreview() {
    PrimalTheme(PrimalTheme.Midnight) {
        Box(modifier = Modifier.size(120.dp)) {
            LiveChip(
                modifier = Modifier.align(Alignment.BottomCenter),
                avatarSize = 120.dp,
            )
        }
    }
}
