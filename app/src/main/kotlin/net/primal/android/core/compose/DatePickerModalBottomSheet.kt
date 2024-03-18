package net.primal.android.core.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerFormatter
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun DatePickerModalBottomSheet(
    state: DatePickerState,
    dateFormatter: DatePickerFormatter = remember { DatePickerDefaults.dateFormatter() },
    showModeToggle: Boolean = true,
    colors: DatePickerColors = DatePickerDefaults.colors(
        selectedDayContainerColor = AppTheme.colorScheme.primary,
    ),
    onDismissRequest: () -> Unit,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        scrimColor = scrimColor,
        tonalElevation = 0.dp,
        onDismissRequest = onDismissRequest,
        windowInsets = WindowInsets(
            left = 0,
            top = BottomSheetDefaults.windowInsets.getTop(LocalDensity.current),
            right = 0,
            bottom = 0,
        ),
    ) {
        DatePicker(
            state = state,
            modifier = Modifier.padding(bottom = 24.dp),
            dateFormatter = dateFormatter,
            showModeToggle = showModeToggle,
            colors = colors,
        )
    }
}
