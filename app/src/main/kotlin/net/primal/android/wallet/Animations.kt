package net.primal.android.wallet

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith

val numericPadEnterAnimation = (fadeIn(animationSpec = tween(220, delayMillis = 90)) + scaleIn())

val numericPadExitAnimation = fadeOut(animationSpec = tween(90)) + scaleOut()

val numericPadContentTransformAnimation = numericPadEnterAnimation.togetherWith(numericPadExitAnimation)
