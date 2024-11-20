package net.primal.android.premium.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.theme.AppTheme

@Composable
fun ManagePremiumTableRow(
    firstColumn: String,
    firstColumnWeight: Float,
    secondColumn: String,
    secondColumnWeight: Float,
    thirdColumn: String,
    thirdColumnWeight: Float,
    thirdColumnTextAlign: TextAlign = TextAlign.Start,
    thirdColumnTextColor: Color = AppTheme.colorScheme.onSurface,
    thirdColumnOnClick: (() -> Unit)? = null,
    fontWeight: FontWeight = FontWeight.Normal,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        Text(
            modifier = Modifier.weight(firstColumnWeight),
            text = firstColumn,
            style = AppTheme.typography.bodyMedium,
            fontSize = 15.sp,
            color = AppTheme.colorScheme.onSurface,
            fontWeight = fontWeight,
        )

        Text(
            modifier = Modifier
                .weight(secondColumnWeight)
                .padding(start = 16.dp, end = 32.dp),
            text = secondColumn,
            style = AppTheme.typography.bodyMedium,
            fontSize = 15.sp,
            color = AppTheme.colorScheme.onSurface,
            fontWeight = fontWeight,
        )

        Text(
            modifier = Modifier
                .weight(thirdColumnWeight)
                .clickable(
                    enabled = thirdColumnOnClick != null,
                    onClick = { thirdColumnOnClick?.invoke() },
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ),
            text = thirdColumn,
            style = AppTheme.typography.bodyMedium,
            fontSize = 15.sp,
            color = thirdColumnTextColor,
            fontWeight = fontWeight,
            textAlign = thirdColumnTextAlign,
        )
    }
}
