package net.primal.android.core.compose.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.AdjustTemporarilySystemBarColors
import net.primal.android.core.compose.IconText
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Quote
import net.primal.android.core.compose.icons.primaliconpack.Repost
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun RepostOrQuoteBottomSheet(
    onDismiss: () -> Unit,
    onRepostClick: () -> Unit,
    onPostQuoteClick: () -> Unit,
) {
    AdjustTemporarilySystemBarColors(
        navigationBarColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    )
    ModalBottomSheet(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        tonalElevation = 0.dp,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ActionButton(
                text = stringResource(id = R.string.post_repost_button_confirmation),
                leadingIcon = PrimalIcons.Repost,
                onClick = {
                    onDismiss()
                    onRepostClick()
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            ActionButton(
                text = stringResource(id = R.string.post_quote_button_confirmation),
                leadingIcon = PrimalIcons.Quote,
                onClick = {
                    onDismiss()
                    onPostQuoteClick()
                },
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    leadingIcon: ImageVector,
    onClick: () -> Unit,
) {
    PrimalFilledButton(
        modifier = Modifier
            .width(240.dp)
            .height(56.dp),
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        contentColor = AppTheme.colorScheme.onSurface,
        textStyle = AppTheme.typography.titleMedium.copy(
            fontSize = 18.sp,
        ),
        onClick = onClick,
    ) {
        IconText(
            text = text,
            leadingIcon = leadingIcon,
            leadingIconSize = 32.sp,
        )
    }
}
