package net.primal.android.core.compose.pulltorefresh

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonRestartableComposable
import androidx.paging.LoadState
import androidx.paging.LoadStates
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

@Composable
@NonRestartableComposable
@ExperimentalMaterial3Api
fun LaunchedPullToRefreshEndingEffect(mediatorLoadStates: LoadStates?, onRefreshEnd: () -> Unit) {
    LaunchedEffect(mediatorLoadStates) {
        val refresh = mediatorLoadStates?.refresh
        val prepend = mediatorLoadStates?.prepend
        val append = mediatorLoadStates?.append
        if (refresh != LoadState.Loading && prepend != LoadState.Loading && append != LoadState.Loading) {
            delay(0.21.seconds)
            onRefreshEnd()
        }
    }
}
