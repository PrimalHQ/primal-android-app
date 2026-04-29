package net.primal.android.core.compose

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import net.primal.android.R

@Composable
fun PrimalOverlayCloseButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val closeContentDescription = stringResource(id = R.string.accessibility_close)
    TextButton(
        onClick = onClick,
        modifier = modifier.semantics { contentDescription = closeContentDescription },
    ) {
        Text(text = stringResource(id = R.string.overlay_action_close))
    }
}
