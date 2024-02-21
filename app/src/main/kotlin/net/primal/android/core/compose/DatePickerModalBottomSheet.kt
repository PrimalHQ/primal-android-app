package net.primal.android.core.compose

import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@ExperimentalMaterial3Api
@Composable
fun DatePickerModalBottomSheet(
    state: DatePickerState,
    dateFormatter: DatePickerFormatter = remember { DatePickerFormatter() },
    dateValidator: (Long) -> Boolean = { true },
    showModeToggle: Boolean = true,
    colors: DatePickerColors = DatePickerDefaults.colors(
        selectedDayContainerColor = AppTheme.colorScheme.primary,
    ),
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = AppTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        onDismissRequest = onDismissRequest,
    ) {
        DatePicker(
            state = state,
            modifier = Modifier.padding(bottom = 16.dp),
            dateFormatter = dateFormatter,
            dateValidator = dateValidator,
            showModeToggle = showModeToggle,
            colors = colors,
        )
    }
}
