package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun ListLoadingError(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Text(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 32.dp, vertical = 16.dp)
                .align(Alignment.Center),
            text = text,
            textAlign = TextAlign.Center,
            style = AppTheme.typography.bodySmall,
        )
    }
}
