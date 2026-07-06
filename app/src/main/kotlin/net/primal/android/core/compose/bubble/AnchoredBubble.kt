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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.Close
import net.primal.android.theme.AppTheme

fun Modifier.anchor(handle: AnchorHandle) =
    onGloballyPositioned {
        handle.rectInRoot = it.boundsInRoot()
        handle.rectInParent = it.boundsInParent()
        handle.anchorSize = it.size
    }

enum class BubblePlacement {
    Below,
    Above,
}

@Composable
fun AnchoredBubble(
    anchor: AnchorHandle,
    text: String,
    visible: Boolean,
    onDismiss: () -> Unit,
    placement: BubblePlacement = BubblePlacement.Below,
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(delayMillis = 300)),
        exit = fadeOut(animationSpec = tween(durationMillis = 50)),
    ) {
        val density = LocalDensity.current
        var bubbleSize by remember { mutableStateOf(IntSize.Zero) }
        val rootPadding = with(density) { 8.dp.toPx() }

        val parentW = LocalWindowInfo.current.containerSize.width.toFloat()
        val bubbleW = bubbleSize.width.toFloat().coerceAtLeast(1f)

        val targetCx = anchor.rectInRoot?.center?.x ?: 0f
        val maxLeft = (parentW - rootPadding - bubbleW).coerceAtLeast(rootPadding)
        val bubbleLeft = (targetCx - bubbleW / 2f).coerceIn(rootPadding, maxLeft)

        val pointerWidth = 20.dp
        val pointerHeight = 8.dp
        val shape = BubbleWithPointerShape(
            cornerRadius = 8.dp,
            pointerWidth = pointerWidth,
            pointerHeight = pointerHeight,
            pointerOffset = with(density) { (targetCx - bubbleLeft).toDp() - pointerWidth / 2 },
            placement = placement,
        )

        Popup(
            popupPositionProvider = bubblePositionProvider(anchor, placement, bubbleLeft.toInt()),
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
                        .padding(
                            top = if (placement == BubblePlacement.Above) 0.dp else pointerHeight,
                            bottom = if (placement == BubblePlacement.Above) pointerHeight else 0.dp,
                        )
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

private const val ANCHOR_OVERLAP_PX = 16f
private const val ABOVE_LIFT_PX = 8f

private fun bubblePositionProvider(
    anchor: AnchorHandle,
    placement: BubblePlacement,
    bubbleLeft: Int,
): PopupPositionProvider =
    object : PopupPositionProvider {
        override fun calculatePosition(
            anchorBounds: IntRect,
            windowSize: IntSize,
            layoutDirection: LayoutDirection,
            popupContentSize: IntSize,
        ): IntOffset {
            val target = anchor.rectInRoot ?: return IntOffset.Zero
            val y = when (placement) {
                BubblePlacement.Below -> target.bottom - ANCHOR_OVERLAP_PX
                BubblePlacement.Above -> target.top - popupContentSize.height + ANCHOR_OVERLAP_PX - ABOVE_LIFT_PX
            }
            return IntOffset(x = bubbleLeft, y = y.toInt())
        }
    }
