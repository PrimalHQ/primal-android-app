package net.primal.android.auth.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault

val defaultOnboardingAvatarBackground = Color(0xFF7E382C)
val defaultAvatarForeground = Color(0xFFFDB7AB)

@Composable
fun DefaultOnboardingAvatar() {
    Box(
        modifier = Modifier
            .background(color = defaultOnboardingAvatarBackground)
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = PrimalIcons.AvatarDefault,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            tint = defaultAvatarForeground,
        )
    }
}
