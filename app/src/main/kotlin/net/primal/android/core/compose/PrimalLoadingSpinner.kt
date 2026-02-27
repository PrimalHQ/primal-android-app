package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import net.primal.android.R

@Composable
fun PrimalLoadingSpinner(size: Dp = 96.dp, paddingValues: PaddingValues = PaddingValues(all = 0.dp)) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.primal_loading_spinner_winter),
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = Int.MAX_VALUE,
        speed = 1.0f,
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize()
            .padding(paddingValues),
    ) {
        LottieAnimation(
            modifier = Modifier.size(size),
            composition = composition,
            progress = { progress },
        )
    }
}
