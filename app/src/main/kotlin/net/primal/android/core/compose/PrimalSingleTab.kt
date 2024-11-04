package net.primal.android.core.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun PrimalSingleTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = AppTheme.typography.bodySmall,
) {
    Tab(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        selectedContentColor = Color.Unspecified,
    ) {
        Text(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxWidth(),
            text = text,
            textAlign = TextAlign.Center,
            style = textStyle,
            color = AppTheme.colorScheme.onPrimary,
            fontWeight = if (selected) FontWeight.SemiBold else null,
        )
    }
}
