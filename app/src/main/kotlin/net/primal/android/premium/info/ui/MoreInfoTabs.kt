package net.primal.android.premium.info.ui

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
import net.primal.android.premium.info.FAQ_TAB_INDEX
import net.primal.android.premium.info.FEATURES_TAB_INDEX
import net.primal.android.premium.info.WHY_PREMIUM_TAB_INDEX
import net.primal.android.theme.AppTheme

@Composable
fun MoreInfoTabs(
    modifier: Modifier = Modifier,
    selectedTabIndex: Int,
    onWhyPremiumTabClick: () -> Unit,
    onFeaturesTabClick: () -> Unit,
    onFAQTabClick: () -> Unit,
) {
    TabRow(
        modifier = modifier.padding(vertical = 4.dp),
        selectedTabIndex = selectedTabIndex,
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(tabPositions[selectedTabIndex])
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 4.dp)
                        .clip(RoundedCornerShape(percent = 100)),
                    height = 4.dp,
                    color = AppTheme.colorScheme.tertiary,
                )
            }
        },
    ) {
        PrimalSingleTab(
            selected = selectedTabIndex == WHY_PREMIUM_TAB_INDEX,
            text = stringResource(id = R.string.premium_more_info_why_premium).uppercase(),
            onClick = onWhyPremiumTabClick,
            textStyle = AppTheme.typography.bodyMedium,
        )
        PrimalSingleTab(
            selected = selectedTabIndex == FEATURES_TAB_INDEX,
            text = stringResource(id = R.string.premium_more_info_features).uppercase(),
            onClick = onFeaturesTabClick,
            textStyle = AppTheme.typography.bodyMedium,
        )
        PrimalSingleTab(
            selected = selectedTabIndex == FAQ_TAB_INDEX,
            text = stringResource(id = R.string.premium_more_info_faq),
            onClick = onFAQTabClick,
            textStyle = AppTheme.typography.bodyMedium,
        )
    }
}
