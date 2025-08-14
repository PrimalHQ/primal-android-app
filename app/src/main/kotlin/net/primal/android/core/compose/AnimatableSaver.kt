package net.primal.android.core.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.saveable.Saver

fun animatableSaver(): Saver<Animatable<Float, AnimationVector1D>, *> =
    Saver(
        save = { it.value },
        restore = { Animatable(it) },
    )
