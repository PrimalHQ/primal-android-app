package net.primal.android.premium.manage.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.ifNotNull
import net.primal.android.premium.manage.ui.MediaTable
import net.primal.android.premium.manage.ui.UsedStorageBreakdown

@Composable
fun PremiumMediaManagementScreen(viewModel: PremiumMediaManagementViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()

    PremiumMediaManagementScreen(
        state = uiState.value,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumMediaManagementScreen(state: PremiumMediaManagementContract.UiState, onClose: () -> Unit) {
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_media_management_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            ifNotNull(state.usedStorageInBytes, state.maxStorageInBytes) { used, max ->
                UsedStorageBreakdown(
                    usedStorageInBytes = used.times(600_000),
                    maxStorageInBytes = max,
                    imagesInBytes = state.imagesInBytes,
                    videosInBytes = state.videosInBytes,
                    otherInBytes = state.otherInBytes,
                    calculating = state.calculating,
                )
            }
            MediaTable(
                items = state.mediaItems,
                onCopyClick = {},
                onDeleteClick = {},
            )
        }
    }
}
