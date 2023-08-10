package net.primal.android.core.compose.button

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme

@Composable
fun PrimalCallToActionButton(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val buttonShape = AppTheme.shapes.small
    val borderGradientColors = listOf(
        AppTheme.extraColorScheme.brand1,
        AppTheme.extraColorScheme.brand2,
    )
    Button(
        modifier = modifier.then(
            Modifier
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(borderGradientColors),
                    shape = buttonShape,
                )
                .shadow(
                    elevation = 16.dp,
                    shape = buttonShape,
                    ambientColor = AppTheme.colorScheme.primary,
                    spotColor = AppTheme.colorScheme.primary,
                )
        ),
        onClick = onClick,
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AppTheme.colorScheme.surface,
            contentColor = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
        ),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 8.dp)
                .wrapContentWidth()
        ) {
            Text(
                text = title,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = AppTheme.colorScheme.onSurface,
            )

            Text(
                text = subtitle,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
            )
        }

        Image(
            modifier = Modifier
                .size(36.dp)
                .align(CenterVertically),
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            colorFilter = ColorFilter.tint(color = AppTheme.extraColorScheme.onSurfaceVariantAlt1)
        )
    }
}


@Preview
@Composable
fun PreviewButton() {
    PrimalTheme(theme = PrimalTheme.Sunset) {
        PrimalCallToActionButton(
            title = "Sign in",
            subtitle = "Already have a Nostr account? Sign in with your Nostr key.",
            onClick = {},
        )
    }
}