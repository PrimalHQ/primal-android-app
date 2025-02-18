package net.primal.android.core.compose.fab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun NewPostFloatingActionButton(onNewPostClick: (content: TextFieldValue?) -> Unit) {
    FloatingActionButton(
        onClick = { onNewPostClick(null) },
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(color = AppTheme.colorScheme.primary, shape = CircleShape),
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
        containerColor = Color.Unspecified,
        content = {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(id = R.string.accessibility_new_post),
                tint = Color.White,
            )
        },
    )
}
