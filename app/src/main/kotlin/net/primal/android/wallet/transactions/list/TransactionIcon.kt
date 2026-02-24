package net.primal.android.wallet.transactions.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.LightningBlue
import net.primal.android.core.compose.icons.primaliconpack.WalletBitcoinPayment
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.walletTransactionIconBackgroundBrush
import net.primal.android.wallet.walletTransactionIconBackgroundColor

@Composable
fun TransactionIcon(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isOnChainPayment: Boolean,
    isPending: Boolean = false,
    isPendingContent: @Composable () -> Unit = {},
) {
    if (isOnChainPayment) {
        OnChainTransactionIcon(
            modifier = modifier,
            size = size,
            isPending = isPending,
            isPendingContent = isPendingContent,
        )
    } else {
        LightningTransactionIcon(
            modifier = modifier,
            size = size,
            isPending = isPending,
            isPendingContent = isPendingContent,
        )
    }
}

@Composable
fun OnChainTransactionIcon(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isPending: Boolean = false,
    isPendingContent: @Composable () -> Unit = {},
) {
    TransactionIconBackgroundWithContent(
        modifier = modifier,
        size = size,
        backgroundBrush = null,
        backgroundColor = walletTransactionIconBackgroundColor,
    ) {
        Image(
            imageVector = PrimalIcons.WalletBitcoinPayment,
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = AppTheme.extraColorScheme.zapped),
        )

        if (isPending) {
            isPendingContent()
        }
    }
}

@Composable
fun LightningTransactionIcon(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isPending: Boolean = false,
    isPendingContent: @Composable () -> Unit = {},
) {
    TransactionIconBackgroundWithContent(
        modifier = modifier,
        size = size,
        backgroundBrush = walletTransactionIconBackgroundBrush,
        backgroundColor = null,
    ) {
        Image(
            modifier = Modifier.padding(8.dp),
            imageVector = PrimalIcons.LightningBlue,
            contentDescription = null,
        )

        if (isPending) {
            isPendingContent()
        }
    }
}

@Composable
private fun TransactionIconBackgroundWithContent(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    backgroundBrush: Brush?,
    backgroundColor: Color?,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .run {
                when {
                    backgroundColor != null ->
                        this.background(
                            color = backgroundColor,
                            shape = CircleShape,
                        )

                    backgroundBrush != null ->
                        this.background(
                            brush = backgroundBrush,
                            shape = CircleShape,
                            alpha = 0.33f,
                        )

                    else -> this
                }
            }
            .size(size),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
