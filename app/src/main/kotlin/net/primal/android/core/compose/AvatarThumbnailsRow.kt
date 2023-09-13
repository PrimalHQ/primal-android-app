package net.primal.android.core.compose

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AvatarThumbnailsRow(
    avatarUrls: List<Any?>,
    onClick: () -> Unit,
) {
    avatarUrls.forEachIndexed { index, imageUrl ->
        Row {
            Spacer(
                modifier = Modifier
                    .height(10.dp)
                    .width((index * 24).dp)
            )

            AvatarThumbnailListItemImage(
                modifier = Modifier.size(32.dp),
                source = imageUrl,
                hasBorder = true,
                borderGradientColors = listOf(
                    Color.White,
                    Color.White
                ),
                onClick = onClick,
            )
        }
    }
}
