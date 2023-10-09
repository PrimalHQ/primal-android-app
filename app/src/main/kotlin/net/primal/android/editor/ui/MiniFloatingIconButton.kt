package net.primal.android.editor.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun MiniFloatingIconButton(
    modifier: Modifier,
    imageVector: ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .padding(all = 4.dp)
            .size(32.dp)
            .clip(CircleShape)
            .background(color = Color.Black.copy(alpha = 0.5f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = Color.White,
        )
    }
}
