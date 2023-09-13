package net.primal.android.notifications.list.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp

@Composable
fun NotificationListItem(
    iconPainter: Painter,
    content: @Composable () -> Unit,
) {
    Row {
        Box(
            modifier = Modifier.padding(all = 16.dp),
        ) {
            Image(
                modifier = Modifier.padding(top = 8.dp),
                painter = iconPainter,
                contentDescription = null,
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            content()
        }
    }
}
