package net.primal.android.stream.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import net.primal.android.R
import net.primal.android.core.activity.LocalPrimalTheme

@Composable
fun StreamPlayerLoadingIndicator(modifier: Modifier = Modifier, isFullscreen: Boolean) {
    val background by if (LocalPrimalTheme.current.isDarkTheme || isFullscreen) {
        rememberLottieComposition(
            spec = LottieCompositionSpec.RawRes(R.raw.primal_stream_video_loader_background_dark),
        )
    } else {
        rememberLottieComposition(
            spec = LottieCompositionSpec.RawRes(R.raw.primal_stream_video_loader_background_light),
        )
    }

    val backgroundProgress by animateLottieCompositionAsState(
        composition = background,
        iterations = Int.MAX_VALUE,
        speed = 1.0f,
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        LottieAnimation(
            modifier = Modifier.fillMaxSize(),
            composition = background,
            progress = { backgroundProgress },
        )
    }
}
