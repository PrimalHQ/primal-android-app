package net.primal.android.core.utils

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ExperimentalMaterial3Api
fun SheetState.hideAndRun(
    coroutineScope: CoroutineScope,
    onDismissRequest: () -> Unit,
    block: () -> Unit,
) = coroutineScope.launch {
    hide()
}.invokeOnCompletion {
    if (!isVisible) {
        onDismissRequest()
    }
    block()
}
