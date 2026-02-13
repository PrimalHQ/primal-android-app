package net.primal.android.core.compose

import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.widget.ImageView
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.viewinterop.AndroidView

@RequiresApi(Build.VERSION_CODES.P)
@Composable
fun AnimatedRawResImage(
    @RawRes resId: Int,
    repeatCount: Int,
    modifier: Modifier = Modifier,
    onAnimationEnd: (() -> Unit)? = null,
) {
    val resources = LocalResources.current
    val currentOnAnimationEnd = rememberUpdatedState(onAnimationEnd)

    val drawable = remember(resId) {
        val source = ImageDecoder.createSource(resources, resId)
        ImageDecoder.decodeDrawable(source) as AnimatedImageDrawable
    }

    DisposableEffect(resId) {
        drawable.repeatCount = repeatCount
        var disposed = false

        val callback = object : android.graphics.drawable.Animatable2.AnimationCallback() {
            override fun onAnimationEnd(d: android.graphics.drawable.Drawable?) {
                if (!disposed) {
                    currentOnAnimationEnd.value?.invoke()
                }
            }
        }
        drawable.registerAnimationCallback(callback)
        drawable.start()

        onDispose {
            disposed = true
            drawable.stop()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
        },
        update = { imageView ->
            imageView.setImageDrawable(drawable)
        },
    )
}
