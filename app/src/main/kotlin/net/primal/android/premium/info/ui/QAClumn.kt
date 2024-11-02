package net.primal.android.premium.info.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.theme.AppTheme

@Composable
fun QAColumn(
    modifier: Modifier = Modifier,
    question: String,
    answer: String,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        Text(
            text = question,
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodyLarge,
            fontSize = 18.sp,
            textAlign = TextAlign.Justify,
        )
        Text(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            style = AppTheme.typography.bodyLarge,
            lineHeight = 24.sp,
            fontSize = 18.sp,
            textAlign = TextAlign.Justify,
            text = answer,
        )
    }
}

