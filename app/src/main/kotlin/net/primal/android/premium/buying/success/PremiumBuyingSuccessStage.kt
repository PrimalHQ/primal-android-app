package net.primal.android.premium.buying.success

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import net.primal.android.theme.AppTheme

@Composable
fun PremiumBuyingSuccessStage(modifier: Modifier = Modifier, onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClose() },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Success! LFG!!!",
            style = AppTheme.typography.bodyLarge,
            fontSize = 28.sp,
        )
    }
}
