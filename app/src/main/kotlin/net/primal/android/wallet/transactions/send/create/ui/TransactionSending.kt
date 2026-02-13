package net.primal.android.wallet.transactions.send.create.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AnimatedRawResImage
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.wallet.dashboard.ui.BtcAmountText
import net.primal.core.utils.CurrencyConversionUtils.toBtc

private enum class AnimationPhase { START, LOOP, END }

private const val ICON_START_SCALE = 0.3f
private const val ICON_END_SCALE = 1f
private const val ICON_START_Y_OFFSET = 100f
private const val ICON_END_Y_OFFSET = 0f
private const val ICON_ANIMATION_DURATION_MS = 300

@Composable
fun TransactionSending(
    modifier: Modifier,
    amountInSats: Long,
    sendingCompleted: Boolean,
    onAnimationFinished: () -> Unit,
    btcAmountModifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.spacedBy(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BtcAmountText(
                modifier = Modifier
                    .padding(start = 32.dp, top = 80.dp)
                    .height(72.dp)
                    .then(btcAmountModifier),
                amountInBtc = amountInSats.toBigDecimal().toBtc(),
                textSize = 48.sp,
            )

            val iconScale = remember { Animatable(ICON_START_SCALE) }
            val iconOffsetY = remember { Animatable(ICON_START_Y_OFFSET) }

            LaunchedEffect(Unit) {
                launch { iconScale.animateTo(ICON_END_SCALE, tween(ICON_ANIMATION_DURATION_MS, easing = EaseOutCubic)) }
                launch {
                    iconOffsetY.animateTo(
                        ICON_END_Y_OFFSET,
                        tween(ICON_ANIMATION_DURATION_MS, easing = EaseOutCubic),
                    )
                }
            }

            Box(
                modifier = Modifier.graphicsLayer {
                    scaleX = iconScale.value
                    scaleY = iconScale.value
                    translationY = iconOffsetY.value
                },
            ) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    AnimatedLightningSendingIcon(
                        sendingCompleted = sendingCompleted,
                        onAnimationFinished = onAnimationFinished,
                    )
                } else {
                    Box(modifier = Modifier.size(160.dp)) {
                        PrimalLoadingSpinner(size = 160.dp)
                    }
                    LaunchedEffect(sendingCompleted) {
                        if (sendingCompleted) onAnimationFinished()
                    }
                }
            }
        }

        PrimalLoadingButton(
            modifier = Modifier
                .width(200.dp)
                .padding(bottom = 16.dp)
                .alpha(0f),
        )
    }
}

@RequiresApi(Build.VERSION_CODES.P)
@Composable
private fun AnimatedLightningSendingIcon(sendingCompleted: Boolean, onAnimationFinished: () -> Unit) {
    var phase by remember { mutableStateOf(AnimationPhase.START) }
    var loopKey by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 16.dp),
    ) {
        key(phase) {
            when (phase) {
                AnimationPhase.START -> {
                    AnimatedRawResImage(
                        resId = R.raw.wallet_sending_start,
                        repeatCount = 0,
                        onAnimationEnd = {
                            phase = if (sendingCompleted) AnimationPhase.END else AnimationPhase.LOOP
                        },
                    )
                }

                AnimationPhase.LOOP -> {
                    key(loopKey) {
                        AnimatedRawResImage(
                            resId = R.raw.wallet_sending_loop,
                            repeatCount = 0,
                            onAnimationEnd = {
                                if (sendingCompleted) {
                                    phase = AnimationPhase.END
                                } else {
                                    loopKey++
                                }
                            },
                        )
                    }
                }

                AnimationPhase.END -> {
                    AnimatedRawResImage(
                        resId = R.raw.wallet_sending_end,
                        repeatCount = 0,
                        onAnimationEnd = onAnimationFinished,
                    )
                }
            }
        }
    }
}
