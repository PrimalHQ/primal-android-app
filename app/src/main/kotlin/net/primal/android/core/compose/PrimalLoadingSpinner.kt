package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.theme.domain.PrimalAccent

@Composable
fun PrimalLoadingSpinner(size: Dp = 96.dp) {
    val primalTheme = LocalPrimalTheme.current
    val animationRawResId = remember(primalTheme) {
        when (primalTheme.accent) {
            PrimalAccent.Summer -> R.raw.primal_loading_spinner_summer
            PrimalAccent.Winter -> R.raw.primal_loading_spinner_winter
        }
    }
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(animationRawResId),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE,
        speed = 1.0f,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(),
    ) {
        LottieAnimation(
            modifier = Modifier.size(size),
            composition = composition,
            progress = { progress },
        )
    }
}
