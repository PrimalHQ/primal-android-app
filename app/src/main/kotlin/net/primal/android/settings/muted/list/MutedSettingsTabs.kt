package net.primal.android.settings.muted.list

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalSingleTab
import net.primal.android.theme.AppTheme

internal const val USERS_INDEX = 0
internal const val WORDS_INDEX = 1
internal const val HASHTAGS_INDEX = 2
internal const val THREADS_INDEX = 3

@Composable
fun MutedSettingsTabs(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    onUsersTabClick: () -> Unit,
    onWordsTabClick: () -> Unit,
    onHashtagsTabClick: () -> Unit,
    onThreadsTabClick: () -> Unit,
) {
    TabRow(
        modifier = modifier.padding(vertical = 4.dp),
        selectedTabIndex = selectedTabIndex,
        containerColor = AppTheme.colorScheme.background,
        divider = {},
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .padding(horizontal = 10.dp)
                        .clip(RoundedCornerShape(percent = 100)),
                    height = 4.dp,
                    color = AppTheme.colorScheme.tertiary,
                )
            }
        },
    ) {
        PrimalSingleTab(
            selected = selectedTabIndex == USERS_INDEX,
            onClick = onUsersTabClick,
            text = stringResource(id = R.string.settings_muted_users_tab).uppercase(),
        )
        PrimalSingleTab(
            selected = selectedTabIndex == WORDS_INDEX,
            onClick = onWordsTabClick,
            text = stringResource(id = R.string.settings_muted_words_tab).uppercase(),
        )
        PrimalSingleTab(
            selected = selectedTabIndex == HASHTAGS_INDEX,
            onClick = onHashtagsTabClick,
            text = stringResource(id = R.string.settings_muted_hashtags_tab).uppercase(),
        )
        PrimalSingleTab(
            selected = selectedTabIndex == THREADS_INDEX,
            onClick = onThreadsTabClick,
            text = stringResource(id = R.string.settings_muted_threads_tab).uppercase(),
        )
    }
}
