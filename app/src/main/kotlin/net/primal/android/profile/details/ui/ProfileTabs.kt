package net.primal.android.profile.details.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.utils.shortened
import net.primal.android.profile.domain.ProfileFeedSpec
import net.primal.android.theme.AppTheme
import timber.log.Timber

@Composable
fun ProfileTabs(
    notesCount: Int?,
    onNotesCountClick: () -> Unit,
    repliesCount: Int?,
    onRepliesCountClick: () -> Unit,
    readsCount: Int?,
    onReadsCountClick: () -> Unit,
    mediaCount: Int?,
    onMediaCountClick: () -> Unit,
    modifier: Modifier = Modifier,
    pagerState: PagerState = rememberPagerState { 4 },
    placeholderText: String = "-",
) {
    val scope = rememberCoroutineScope()

    TabRow(
        modifier = modifier,
        selectedTabIndex = pagerState.currentPage,
        containerColor = Color.Transparent,
        divider = { },
        indicator = { tabPositions ->
            if (pagerState.currentPage < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    height = 4.dp,
                    color = AppTheme.colorScheme.tertiary,
                )
            }
        },
    ) {
        CustomTab(
            modifier = Modifier.fillMaxWidth(),
            selected = pagerState.currentPage == 0,
            onClick = {
                onNotesCountClick()
                scope.launch { pagerState.animateScrollToPage(0) }
            },
            text = notesCount?.asTabText() ?: placeholderText,
            label = stringResource(id = R.string.profile_notes_stat),
        )

        CustomTab(
            modifier = Modifier.fillMaxWidth(),
            selected = pagerState.currentPage == 1,
            onClick = {
                onRepliesCountClick()
                scope.launch { pagerState.animateScrollToPage(1) }
            },
            text = repliesCount?.asTabText() ?: placeholderText,
            label = stringResource(id = R.string.profile_replies_stat),
        )

        CustomTab(
            selected = pagerState.currentPage == 2,
            onClick = {
                onReadsCountClick()
                scope.launch { pagerState.animateScrollToPage(2) }
            },
            text = readsCount?.asTabText() ?: placeholderText,
            label = stringResource(id = R.string.profile_reads_stat),
        )

        CustomTab(
            selected = pagerState.currentPage == 3,
            onClick = {
                onMediaCountClick()
                scope.launch { pagerState.animateScrollToPage(3) }
            },
            text = mediaCount?.asTabText() ?: placeholderText,
            label = stringResource(id = R.string.profile_media_stat),
        )
    }
}

private const val MAX_ORIGINAL_COUNT = 9999

@Composable
private fun Int.asTabText(): String {
    val numberFormat = remember { NumberFormat.getNumberInstance() }
    val formattedInt = numberFormat.format(this)
    return if (this > MAX_ORIGINAL_COUNT) this.shortened() else formattedInt
}

@Composable
private fun CustomTab(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    label: String,
) {
    Tab(
        modifier = modifier,
        selected = selected,
        onClick = onClick,
        selectedContentColor = Color.Unspecified,
        content = {
            Column(
                modifier = Modifier.padding(vertical = 12.dp),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = text,
                    textAlign = TextAlign.Center,
                    style = AppTheme.typography.bodyLarge.copy(
                        fontSize = 26.sp,
                    ),
                    color = AppTheme.colorScheme.onPrimary,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = label,
                    style = AppTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                )
            }
        },
    )
}
