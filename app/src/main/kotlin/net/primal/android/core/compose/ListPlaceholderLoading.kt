package net.primal.android.core.compose

import androidx.annotation.RawRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import net.primal.android.LocalPrimalTheme

@Composable
fun ListPlaceholderLoading(
    modifier: Modifier,
    @RawRes lightAnimationResId: Int,
    @RawRes darkAnimationResId: Int,
    repeat: Int = 10,
) {
    val animationRawResId = when (LocalPrimalTheme.current.isDarkTheme) {
        true -> darkAnimationResId
        false -> lightAnimationResId
    }

    Column(modifier = modifier) {
        repeat(times = repeat) {
            InfiniteLottieAnimation(resId = animationRawResId)
        }
    }
}

@Composable
fun InfiniteLottieAnimation(@RawRes resId: Int) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(resId),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE,
        speed = 1.0f,
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(),
    ) {
        LottieAnimation(
            modifier = Modifier.fillMaxSize(),
            composition = composition,
            progress = { progress },
        )
    }
}
