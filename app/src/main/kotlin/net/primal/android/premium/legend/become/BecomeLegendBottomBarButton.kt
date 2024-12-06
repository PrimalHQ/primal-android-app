package net.primal.android.premium.legend.become

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.button.PrimalLoadingButton

@Composable
fun BecomeLegendBottomBarButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp)
            .padding(bottom = 32.dp, top = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        PrimalLoadingButton(
            modifier = Modifier.fillMaxWidth(),
            text = text,
            onClick = onClick,
        )
    }
}
