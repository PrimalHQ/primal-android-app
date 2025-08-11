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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun LiveChip(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(AppTheme.shapes.extraLarge)
            .background(color = Color.Black)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color = Color.Red, shape = CircleShape),
        )

        Text(
            modifier = Modifier.padding(start = 4.dp, top = 1.dp),
            text = stringResource(id = R.string.live_stream_chip_title),
            style = AppTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 1.sp,
                fontSize = 12.sp,
                lineHeight = 11.sp,
            ),
        )
    }
}

@Preview
@Composable
fun LiveChipPreview() {
    PrimalTheme(PrimalTheme.Sunset) {
        LiveChip()
    }
}
