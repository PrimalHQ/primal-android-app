package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension

@Composable
fun WrappedContentWithSuffix(wrappedContent: @Composable () -> Unit, suffixFixedContent: @Composable () -> Unit) {
    ConstraintLayout(
        modifier = Modifier.wrapContentWidth(),
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
                }
                .padding(top = 2.dp),
        ) {
            suffixFixedContent()
        }
    }
}
