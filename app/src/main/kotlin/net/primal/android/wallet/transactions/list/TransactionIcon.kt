package net.primal.android.wallet.transactions.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun TransactionIcon(
    background: Color = AppTheme.extraColorScheme.surfaceVariantAlt1,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .background(
                color = background,
                shape = CircleShape,
            )
            .size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
