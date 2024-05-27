package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Composable
fun WrappedContentWithSuffix(
    modifier: Modifier = Modifier,
    wrappedContent: @Composable () -> Unit,
    suffixFixedContent: @Composable () -> Unit,
) {
    ConstraintLayout(
        modifier = modifier.wrapContentWidth(),
    ) {
        val (mainRef, endRef) = createRefs()

        Box(
            modifier = Modifier
                .constrainAs(mainRef) {
                    start.linkTo(parent.start)
                    end.linkTo(endRef.start)
                    width = Dimension.preferredWrapContent
                },
        ) {
            wrappedContent()
        }

        Box(
            modifier = Modifier
                .constrainAs(endRef) {
                    end.linkTo(parent.end)
                    start.linkTo(mainRef.end)
                    top.linkTo(mainRef.top)
                    bottom.linkTo(mainRef.bottom)
                    height = Dimension.fillToConstraints
                },
            contentAlignment = Alignment.Center,
        ) {
            suffixFixedContent()
        }
    }
}
