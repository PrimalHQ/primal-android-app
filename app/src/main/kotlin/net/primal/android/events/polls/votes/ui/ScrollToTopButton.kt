package net.primal.android.events.polls.votes.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.primal.android.theme.AppTheme

@Composable
fun BoxScope.ScrollToTopButton(visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.BottomEnd),
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut(),
    ) {
        IconButton(
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp)
                .clip(CircleShape)
                .background(AppTheme.extraColorScheme.surfaceVariantAlt1),
            onClick = onClick,
        ) {
            Icon(
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                modifier = Modifier.padding(8.dp),
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
            )
        }
    }
}
