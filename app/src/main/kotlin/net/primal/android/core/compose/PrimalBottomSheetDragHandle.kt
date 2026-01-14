package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import net.primal.android.theme.AppTheme

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PrimalBottomSheetDragHandle() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppTheme.extraColorScheme.surfaceVariantAlt2),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BottomSheetDefaults.DragHandle()
    }
}
