package net.primal.android.wallet.transactions.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import net.primal.android.R
import net.primal.android.core.compose.heightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.isEmpty
import net.primal.android.theme.AppTheme
import net.primal.domain.wallet.CurrencyMode

@Composable
fun TransactionsLazyColumn(
    modifier: Modifier,
    pagingItems: LazyPagingItems<TransactionListItemDataUi>,
    listState: LazyListState,
    isRefreshing: Boolean,
    onProfileClick: (String) -> Unit,
    onTransactionClick: (String) -> Unit,
    paddingValues: PaddingValues = PaddingValues(0.dp),
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    currencyMode: CurrencyMode,
    exchangeBtcUsdRate: Double?,
) {
    val today = stringResource(id = R.string.wallet_transactions_today).lowercase()
    val yesterday = stringResource(id = R.string.wallet_transactions_yesterday).lowercase()
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = paddingValues,
    ) {
        if (header != null) {
            item(
                contentType = { "Header" },
            ) {
                header()
            }
        }

        items(
            count = pagingItems.itemCount,
            key = pagingItems.itemKey { it.txId },
            contentType = pagingItems.itemContentType(),
        ) { index ->
            val item = pagingItems[index] ?: return@items
            val previousItem = if (index > 0) pagingItems.peek(index - 1) else null
            val currentDay = item.txUpdatedAt.formatDay(
                todayTranslation = today,
                yesterdayTranslation = yesterday,
            )
            val previousDay = previousItem?.txUpdatedAt?.formatDay(
                todayTranslation = today,
                yesterdayTranslation = yesterday,
            )

            Column {
                if (currentDay != previousDay) {
                    TransactionsHeaderListItem(
                        modifier = Modifier
                            .background(color = AppTheme.colorScheme.surfaceVariant)
                            .fillMaxWidth(),
                        day = currentDay,
                    )
                }

                TransactionListItem(
                    data = item,
                    numberFormat = numberFormat,
                    onAvatarClick = onProfileClick,
                    onClick = onTransactionClick,
                    currencyMode = currencyMode,
                    exchangeBtcUsdRate = exchangeBtcUsdRate,
                )
            }
        }

        if (pagingItems.isEmpty() && (pagingItems.loadState.refresh is LoadState.Loading || isRefreshing)) {
            heightAdjustableLoadingLazyListPlaceholder(
                height = 80.dp,
                showDivider = false,
                itemPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        if (footer != null) {
            item(
                contentType = { "Footer" },
            ) {
                footer()
            }
        }
    }
}

private fun Instant.formatDay(todayTranslation: String, yesterdayTranslation: String): String {
    val zoneId = ZoneId.systemDefault()
    val zonedDateTime: ZonedDateTime = this.atZone(zoneId)
    val now = ZonedDateTime.now(zoneId)

    return if (now.toLocalDate() == zonedDateTime.toLocalDate()) {
        todayTranslation
    } else if (now.minusDays(1).toLocalDate() == zonedDateTime.toLocalDate()) {
        yesterdayTranslation
    } else {
        zonedDateTime.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
    }
}
