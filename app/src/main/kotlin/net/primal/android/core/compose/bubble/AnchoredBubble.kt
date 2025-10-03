package net.primal.android.core.compose.bubble

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import kotlin.math.absoluteValue
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Close
import net.primal.android.theme.AppTheme

fun Modifier.anchor(handle: AnchorHandle) =
    onGloballyPositioned {
        handle.rectInRoot = it.boundsInRoot()
        handle.rectInParent = it.boundsInParent()
        handle.anchorSize = it.size
    }

@Composable
fun AnchoredBubble(
    anchor: AnchorHandle,
    text: String,
    visible: Boolean,
    onDismiss: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(delayMillis = 300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 50)),
    ) {
        val density = LocalDensity.current
        var bubbleSize by remember { mutableStateOf(IntSize.Zero) }
        val rootPadding = with(density) { 8.dp.toPx() }

        val parentW = with(LocalDensity.current) { (LocalConfiguration.current.screenWidthDp.dp).toPx() }
        val bubbleW = bubbleSize.width.toFloat().coerceAtLeast(1f)

        val targetCx = anchor.rectInRoot?.center?.x ?: 0f
        val minLeft = rootPadding
        val maxLeft = parentW - bubbleW - rootPadding

        val bubbleLeft = when {
            maxLeft < minLeft -> (parentW - bubbleW) / 2f
            else -> (targetCx - bubbleW / 2f).coerceIn(minLeft, maxLeft)
        }

        val pointerHeight = 8.dp
        val pointerOffsetPx = (targetCx - bubbleLeft)
        val shape = BubbleWithPointerShape(
            cornerRadius = 8.dp,
            pointerWidth = 20.dp,
            pointerHeight = pointerHeight,
            pointerOffset = with(density) { (pointerOffsetPx).toDp() - 20.dp - (-rootPadding).toDp() },
        )

        Popup(
            alignment = Alignment.TopEnd,
            offset = IntOffset(
                y = ((anchor.rectInParent?.top?.absoluteValue ?: 0f) + (anchor.anchorSize?.height ?: 0) - 16f)
                    .coerceAtLeast(0f).toInt(),
                x = -rootPadding.toInt(),
            ),
            onDismissRequest = onDismiss,
        ) {
            Surface(
                shape = shape,
                color = AppTheme.colorScheme.onSurface,
                modifier = Modifier.onGloballyPositioned { bubbleSize = it.size },
            ) {
                Row(
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(top = pointerHeight)
                        .padding(start = 2.dp)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.Top,
                ) {
                    Text(
                        modifier = Modifier.padding(4.dp),
                        text = text,
                        color = AppTheme.extraColorScheme.surfaceVariantAlt2,
                        style = AppTheme.typography.bodyLarge,
                        fontSize = 16.sp,
                    )

                    IconButton(
                        modifier = Modifier.size(18.dp),
                        onClick = onDismiss,
                    ) {
                        Icon(
                            imageVector = PrimalIcons.Close,
                            contentDescription = null,
                            tint = AppTheme.extraColorScheme.surfaceVariantAlt2,
                        )
                    }
                }
            }
        }
    }
}
