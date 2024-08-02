package net.primal.android.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.QrCode
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun PrimalCircleButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    containerColor: Color = AppTheme.colorScheme.primary,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = containerColor)
            .clickable(
                onClick = onClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = false),
            ),
        contentAlignment = Alignment.Center,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor, content = content)
    }
}

data class PrimalButtonPreviewState(
    val enabled: Boolean,
)

class PrimalStatePreviewProvider : PreviewParameterProvider<PrimalButtonPreviewState> {
    override val values: Sequence<PrimalButtonPreviewState>
        get() = sequenceOf(
            PrimalButtonPreviewState(enabled = true),
            PrimalButtonPreviewState(enabled = false),
        )
}

@Preview
@Composable
fun PrimalButtonPreview(
    @PreviewParameter(PrimalStatePreviewProvider::class)
    state: PrimalButtonPreviewState,
) {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        PrimalCircleButton(
            modifier = Modifier.size(64.dp),
            enabled = state.enabled,
            onClick = { },
        ) {
            Icon(imageVector = PrimalIcons.QrCode, contentDescription = null)
        }
    }
}
