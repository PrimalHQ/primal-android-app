package net.primal.android.core.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun FeedsErrorColumn(
    modifier: Modifier = Modifier,
    text: String,
    onRefresh: () -> Unit,
    onRestoreDefaultFeeds: () -> Unit,
) {
    val visible = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500.milliseconds)
        visible.value = true
    }

    AnimatedVisibility(
        visible = visible.value,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 16.dp),
                text = text,
                textAlign = TextAlign.Center,
            )

            TextButton(onClick = onRefresh) {
                Text(
                    text = stringResource(id = R.string.feed_refresh_button).uppercase(),
                )
            }

            Text(
                text = stringResource(R.string.feeds_or),
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )

            TextButton(onClick = onRestoreDefaultFeeds) {
                Text(
                    text = stringResource(id = R.string.feed_restore_default_feeds_button).uppercase(),
                )
            }
        }
    }
}
