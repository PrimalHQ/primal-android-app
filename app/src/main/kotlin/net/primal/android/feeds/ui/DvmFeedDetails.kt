package net.primal.android.feeds.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.AppBarIcon
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.feeds.domain.DvmFeed
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DvmFeedDetails(
    dvmFeed: DvmFeed?,
    onClose: () -> Unit,
    onGoToWallet: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    addedToFeeds: Boolean = false,
    onAddOrRemoveFeed: (() -> Unit)? = null,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
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
                    Text(text = stringResource(id = R.string.feed_details_title))
                },
            )
        },
        content = { paddingValues ->
            if (dvmFeed != null) {
                DvmHeaderAndFeedList(
                    modifier = Modifier.padding(paddingValues),
                    dvmFeed = dvmFeed,
                    onGoToWallet = onGoToWallet,
                )
            }
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                PrimalLoadingButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = onAddOrRemoveFeed != null,
                    text = if (addedToFeeds) {
                        stringResource(id = R.string.feed_details_remove_feed)
                    } else {
                        stringResource(id = R.string.feed_details_add_feed)
                    },
                    onClick = { onAddOrRemoveFeed?.invoke() },
                )
            }
        },
    )
}
