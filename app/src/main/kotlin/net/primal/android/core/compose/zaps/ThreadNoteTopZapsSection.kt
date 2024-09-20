package net.primal.android.core.compose.zaps

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ContextualFlowRowOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.note.ui.EventZapUiModel
import net.primal.android.theme.AppTheme

@ExperimentalLayoutApi
@Composable
fun ThreadNoteTopZapsSection(zaps: List<EventZapUiModel>, onClick: () -> Unit) {
    if (zaps.size > 3) {
        TwoLineTopZapsSection(
            modifier = Modifier
                .animateContentSize()
                .padding(horizontal = 10.dp),
            zaps = zaps,
            onClick = onClick,
        )
    } else {
        SingleLineTopZapsSection(
            modifier = Modifier
                .animateContentSize()
                .padding(horizontal = 10.dp),
            zaps = zaps,
            onClick = onClick,
        )
    }
}

@ExperimentalLayoutApi
@Composable
private fun TwoLineTopZapsSection(
    modifier: Modifier,
    zaps: List<EventZapUiModel>,
    onClick: () -> Unit,
) {
    val topZap = zaps.firstOrNull()
    val otherZaps = zaps.drop(n = 1)

    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(12.dp))

        if (topZap != null) {
            EventZapItem(
                noteZap = topZap,
                showMessage = true,
                onClick = onClick,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (otherZaps.isNotEmpty()) {
            ContextualZapsFlowRow(
                modifier = Modifier.fillMaxWidth(),
                zaps = otherZaps,
                onClick = onClick,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
    }
}

@ExperimentalLayoutApi
@Composable
private fun ContextualZapsFlowRow(
    modifier: Modifier,
    zaps: List<EventZapUiModel>,
    onClick: () -> Unit,
) {
    ContextualFlowRow(
        modifier = modifier,
        itemCount = zaps.size,
        verticalArrangement = Arrangement.Center,
        maxLines = 1,
        overflow = ContextualFlowRowOverflow.expandIndicator(
            content = {
                Icon(
                    modifier = Modifier
                        .background(
                            color = AppTheme.extraColorScheme.surfaceVariantAlt1,
                            shape = CircleShape,
                        )
                        .size(26.dp)
                        .padding(horizontal = 4.dp)
                        .clickable { onClick() },
                    imageVector = PrimalIcons.More,
                    contentDescription = null,
                )
            },
        ),
    ) {
        val zap = zaps[it]
        key(zap.id) {
            EventZapItem(
                modifier = Modifier.padding(end = 6.dp),
                noteZap = zap,
                showMessage = false,
                onClick = onClick,
            )
        }
    }
}

@ExperimentalLayoutApi
@Composable
private fun SingleLineTopZapsSection(
    modifier: Modifier,
    zaps: List<EventZapUiModel>,
    onClick: () -> Unit,
) {
    val topZap = zaps.firstOrNull()
    val otherZaps = zaps.drop(n = 1)

    Row(modifier = modifier) {
        if (topZap != null) {
            EventZapItem(
                modifier = Modifier.padding(end = 6.dp),
                noteZap = topZap,
                showMessage = true,
                onClick = onClick,
            )
        }

        if (otherZaps.isNotEmpty()) {
            ContextualZapsFlowRow(
                modifier = Modifier.fillMaxWidth(),
                zaps = otherZaps,
                onClick = onClick,
            )
        }
    }
}
