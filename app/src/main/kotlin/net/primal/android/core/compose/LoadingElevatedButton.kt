package net.primal.android.core.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import net.primal.android.theme.PrimalTheme

@Composable
fun LoadingElevatedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    colors: ButtonColors = ButtonDefaults.elevatedButtonColors(),
    loadingContent: (@Composable RowScope.() -> Unit) = { DefaultLoadingContent() },
    content: @Composable RowScope.() -> Unit,
) = ElevatedButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled,
    colors = colors,
    content = if (loading) loadingContent else content
)

@Composable
private fun DefaultLoadingContent() {
    CircularProgressIndicator(
        modifier = Modifier.size(12.dp),
        strokeWidth = 2.dp
    )
}

data class LoadingButtonPreviewState(val enabled: Boolean, val loading: Boolean)

class LoadingButtonStatePreviewProvider : PreviewParameterProvider<LoadingButtonPreviewState> {
    override val values: Sequence<LoadingButtonPreviewState>
        get() = sequenceOf(
            LoadingButtonPreviewState(enabled = true, loading = false),
            LoadingButtonPreviewState(enabled = false, loading = true),
            LoadingButtonPreviewState(enabled = false, loading = false),
            LoadingButtonPreviewState(enabled = true, loading = true)
        )
}

@Preview
@Composable
fun LoadingButtonPreview(
    @PreviewParameter(LoadingButtonStatePreviewProvider::class)
    state: LoadingButtonPreviewState
) {
    PrimalTheme {
        LoadingElevatedButton(
            onClick = { },
            enabled = state.enabled,
            loading = state.loading,
            content = {
                Text(text = "Hello Primal!")
            }
        )
    }
}

@Preview
@Composable
fun LoadingButtonCustomLoadingContentPreview(
    @PreviewParameter(LoadingButtonStatePreviewProvider::class)
    state: LoadingButtonPreviewState
) {
    PrimalTheme {
        LoadingElevatedButton(
            onClick = { },
            enabled = state.enabled,
            loading = state.loading,
            loadingContent = { Text(text = "I am the Loading Content") },
            content = { Text(text = "I am the real Content") }
        )
    }
}
