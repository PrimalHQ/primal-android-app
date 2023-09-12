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
import kotlinx.coroutines.delay
import net.primal.android.R

@Composable
fun ListNoContent(
    modifier: Modifier,
    noContentText: String,
    refreshButtonVisible: Boolean = true,
    onRefresh: () -> Unit = {},
) {
    val visible = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500L)
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
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 32.dp),
                text = noContentText,
                textAlign = TextAlign.Center,
            )

            if (refreshButtonVisible) {
                TextButton(
                    modifier = Modifier.padding(vertical = 8.dp),
                    onClick = onRefresh,
                ) {
                    Text(
                        text = stringResource(id = R.string.feed_refresh_button).uppercase(),
                    )
                }
            }
        }
    }
}
