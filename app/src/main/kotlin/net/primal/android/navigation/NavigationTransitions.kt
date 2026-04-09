package net.primal.android.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

val primalSlideInHorizontallyFromEnd = slideInHorizontally(animationSpec = tween(), initialOffsetX = { it })

val primalSlideOutHorizontallyToEnd = slideOutHorizontally(animationSpec = tween(), targetOffsetX = { it })

val primalScaleIn = scaleIn(animationSpec = tween(), initialScale = 0.9f)

val primalScaleOut = scaleOut(animationSpec = tween(), targetScale = 0.9f)
