package net.primal.android.wallet.transactions.send.create.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.theme.AppTheme

@Composable
fun TransactionStatusColumn(
    icon: ImageVector?,
    iconTint: Color = LocalContentColor.current,
    headlineText: String,
    supportText: String,
    textColor: Color = LocalContentColor.current,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        if (icon != null) {
            Image(
                modifier = Modifier
                    .size(160.dp)
                    .padding(vertical = 16.dp),
                imageVector = icon,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = iconTint),
            )
        } else {
            Box(modifier = Modifier.size(160.dp)) {
                PrimalLoadingSpinner(size = 160.dp)
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .padding(top = 32.dp, bottom = 8.dp),
            text = headlineText,
            textAlign = TextAlign.Center,
            color = textColor,
            style = AppTheme.typography.headlineSmall,
        )

        Text(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .padding(vertical = 32.dp),
            text = supportText,
            textAlign = TextAlign.Center,
            color = textColor,
            style = AppTheme.typography.bodyLarge.copy(
                lineHeight = 28.sp,
            ),
        )
    }
}
