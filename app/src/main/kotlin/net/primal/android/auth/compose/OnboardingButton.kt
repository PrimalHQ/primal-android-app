package net.primal.android.auth.compose

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.MAX_COMPONENT_WIDTH
import net.primal.android.core.compose.button.PrimalLoadingButton

@Composable
fun OnboardingButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    PrimalLoadingButton(
        modifier = modifier
            .widthIn(240.dp, MAX_COMPONENT_WIDTH.dp)
            .fillMaxWidth(),
        containerColor = Color.Black,
        disabledContainerColor = Color.Black.copy(alpha = 0.20f),
        contentColor = Color.White,
        enabled = enabled,
        loading = loading,
        onClick = onClick,
        text = text,
    )
}
