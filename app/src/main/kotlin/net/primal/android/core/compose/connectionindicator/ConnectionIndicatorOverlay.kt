package net.primal.android.core.compose.connectionindicator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.activity.LocalPrimalTheme
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.NoConnectionBlack
import net.primal.android.core.compose.icons.primaliconpack.NoConnectionWhite
import net.primal.android.core.compose.indicator.IndicatorOverlay

@Composable
fun ConnectionIndicatorOverlay(viewModel: ConnectionIndicatorViewModel, content: @Composable () -> Unit) {
    val uiState by viewModel.state.collectAsState()
    val isDarkTheme = LocalPrimalTheme.current.isDarkTheme

    IndicatorOverlay(
        showIndicator = !uiState.hasConnection,
        indicatorText = stringResource(id = R.string.app_no_connection_notice),
        indicatorIcon = if (isDarkTheme) PrimalIcons.NoConnectionBlack else PrimalIcons.NoConnectionWhite,
        indicatorIconTint = Color.Unspecified,
        floatingIcon = if (isDarkTheme) PrimalIcons.NoConnectionWhite else PrimalIcons.NoConnectionBlack,
        floatingIconTint = Color.Unspecified,
        floatingIconTopPadding = 16.dp,
        content = content,
    )
}
