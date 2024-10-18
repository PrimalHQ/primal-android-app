package net.primal.android.feeds.list.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.feeds.dvm.ui.DvmFeedListItem
import net.primal.android.feeds.dvm.ui.DvmFeedUi
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DvmFeedMarketplace(
    dvmFeeds: List<DvmFeedUi>,
    modifier: Modifier = Modifier,
    onFeedClick: (dvmFeed: DvmFeedUi) -> Unit,
    onGoToWallet: (() -> Unit)? = null,
    onClose: () -> Unit,
) {
    Column(modifier = modifier) {
        CenterAlignedTopAppBar(
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
            ),
            navigationIcon = {
                AppBarIcon(
                    icon = PrimalIcons.ArrowBack,
                    onClick = onClose,
                )
            },
            title = {
                Text(text = stringResource(id = R.string.feed_marketplace_title))
            },
        )
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            items(
                items = dvmFeeds,
                key = { it.data.dvmId },
            ) {
                Column {
                    DvmFeedListItem(
                        data = it,
                        onFeedClick = onFeedClick,
                        onGoToWallet = onGoToWallet,
                    )
                    PrimalDivider()
                }
            }
        }
    }
}
