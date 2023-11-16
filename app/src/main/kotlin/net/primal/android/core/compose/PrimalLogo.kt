package net.primal.android.core.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import net.primal.android.R

@Composable
fun PrimalLogo(modifier: Modifier = Modifier, showBackground: Boolean = false) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter,
    ) {
        if (showBackground) {
            Image(
                modifier = Modifier.clip(CircleShape),
                painter = painterResource(id = R.drawable.ic_launcher_background),
                contentDescription = null,
            )
        }

        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
        )
    }
}
