package net.primal.android.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically

val primalSlideInHorizontallyFromEnd = slideInHorizontally(animationSpec = tween(), initialOffsetX = { it })

val primalSlideOutHorizontallyToEnd = slideOutHorizontally(animationSpec = tween(), targetOffsetX = { it })

val primalSlideInVerticallyFromBottom = slideInVertically(initialOffsetY = { it })

val primalSlideOutVerticallyToBottom = slideOutVertically(targetOffsetY = { it })

val primalScaleIn = scaleIn(animationSpec = tween(), initialScale = 0.9f)

val primalScaleOut = scaleOut(animationSpec = tween(), targetScale = 0.9f)
