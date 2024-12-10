package net.primal.android.profile.details.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.core.utils.shortened
import net.primal.android.profile.details.MEDIA_TAB_INDEX
import net.primal.android.profile.details.NOTES_TAB_INDEX
import net.primal.android.profile.details.READS_TAB_INDEX
import net.primal.android.profile.details.REPLIES_TAB_INDEX
import net.primal.android.theme.AppTheme

internal const val PROFILE_TAB_COUNT = 4

@Composable
fun ProfileTabs(
    selectedTabIndex: Int,
    notesCount: Int?,
    onNotesCountClick: () -> Unit,
    repliesCount: Int?,
    onRepliesCountClick: () -> Unit,
    readsCount: Int?,
    onReadsCountClick: () -> Unit,
    mediaCount: Int?,
    onMediaCountClick: () -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "-",
) {
    TabRow(
        modifier = modifier,
        selectedTabIndex = selectedTabIndex,
        containerColor = AppTheme.colorScheme.surfaceVariant,
        divider = { },
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    height = 4.dp,
                    color = AppTheme.colorScheme.tertiary,
                )
            }
        },
    ) {
        CustomTab(
            modifier = Modifier.fillMaxWidth(),
            selected = selectedTabIndex == NOTES_TAB_INDEX,
            onClick = onNotesCountClick,
            text = notesCount?.asTabText() ?: placeholderText,
            label = stringResource(id = R.string.profile_notes_stat),
        )

        CustomTab(
            modifier = Modifier.fillMaxWidth(),
            selected = selectedTabIndex == REPLIES_TAB_INDEX,
            onClick = onRepliesCountClick,
            text = repliesCount?.asTabText() ?: placeholderText,
            label = stringResource(id = R.string.profile_replies_stat),
        )

        CustomTab(
            selected = selectedTabIndex == READS_TAB_INDEX,
            onClick = onReadsCountClick,
            text = readsCount?.asTabText() ?: placeholderText,
            label = stringResource(id = R.string.profile_reads_stat),
        )

        CustomTab(
            selected = selectedTabIndex == MEDIA_TAB_INDEX,
            onClick = onMediaCountClick,
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
