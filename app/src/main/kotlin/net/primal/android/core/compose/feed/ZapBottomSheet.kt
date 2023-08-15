package net.primal.android.core.compose.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZapBottomSheet(
    receiverName: String,
    onDismissRequest: () -> Unit,
    onZap: (Int, String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 32.dp)
                .clickable {
                    onDismissRequest()
                    onZap(42, "Test description.")
                },
            text = "This will be the bottom sheet to zap $receiverName.",
            textAlign = TextAlign.Center,
        )
    }
}
