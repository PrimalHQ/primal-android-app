package net.primal.android.premium.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.info.ui.FAQTabContent
import net.primal.android.premium.info.ui.FeaturesTabContent
import net.primal.android.premium.info.ui.MoreInfoTabs
import net.primal.android.premium.info.ui.PremiumTabContent
import net.primal.android.premium.info.ui.ProTabContent
import net.primal.android.theme.AppTheme

internal const val MORE_INFO_TAB_COUNT = 4

const val MORE_INFO_WHY_PREMIUM_TAB_INDEX = 0
const val MORE_INFO_WHY_PRO_TAB_INDEX = 1
const val MORE_INFO_FEATURES_TAB_INDEX = 2
const val MORE_INFO_FAQ_TAB_INDEX = 3

private val tabsRange = MORE_INFO_WHY_PREMIUM_TAB_INDEX..MORE_INFO_FAQ_TAB_INDEX

@Composable
fun PremiumMoreInfoScreen(initialTabIndex: Int = MORE_INFO_WHY_PREMIUM_TAB_INDEX, onClose: () -> Unit) {
    val pagerState = rememberPagerState(
        initialPage = if (initialTabIndex in tabsRange) initialTabIndex else MORE_INFO_WHY_PREMIUM_TAB_INDEX,
        pageCount = { MORE_INFO_TAB_COUNT },
    )

    Scaffold(
        topBar = {
            MoreInfoTopAppBar(
                pagerState = pagerState,
                onClose = onClose,
            )
        },
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.background(AppTheme.colorScheme.surfaceVariant),
        ) { currentPage ->
            when (currentPage) {
                MORE_INFO_WHY_PREMIUM_TAB_INDEX -> {
                    PremiumTabContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp),
                    )
                }

                MORE_INFO_WHY_PRO_TAB_INDEX -> {
                    ProTabContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp),
                    )
                }

                MORE_INFO_FEATURES_TAB_INDEX -> {
                    FeaturesTabContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentPadding = paddingValues,
                    )
                }

                MORE_INFO_FAQ_TAB_INDEX -> {
                    FAQTabContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentPadding = paddingValues,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreInfoTopAppBar(pagerState: PagerState, onClose: () -> Unit) {
    val uiScope = rememberCoroutineScope()
    PrimalTopAppBar(
        title = resolveTopBarTitle(pagerState.currentPage),
        navigationIcon = PrimalIcons.ArrowBack,
        onNavigationIconClick = onClose,
        showDivider = true,
        footer = {
            MoreInfoTabs(
                modifier = Modifier.padding(top = 8.dp),
                selectedTabIndex = pagerState.currentPage,
                onWhyPremiumTabClick = {
                    uiScope.launch {
                        pagerState.scrollToPage(
                            MORE_INFO_WHY_PREMIUM_TAB_INDEX,
                        )
                    }
                },
                onWhyProTabClick = { uiScope.launch { pagerState.scrollToPage(MORE_INFO_WHY_PRO_TAB_INDEX) } },
                onFeaturesTabClick = {
                    uiScope.launch {
                        pagerState.scrollToPage(
                            MORE_INFO_FEATURES_TAB_INDEX,
                        )
                    }
                },
                onFAQTabClick = { uiScope.launch { pagerState.scrollToPage(MORE_INFO_FAQ_TAB_INDEX) } },
            )
        },
    )
}

@Composable
private fun resolveTopBarTitle(currentPage: Int): String {
    return when (currentPage) {
        MORE_INFO_WHY_PREMIUM_TAB_INDEX -> stringResource(R.string.premium_more_info_title)
        MORE_INFO_WHY_PRO_TAB_INDEX -> stringResource(R.string.pro_more_info_title)
        MORE_INFO_FEATURES_TAB_INDEX -> stringResource(R.string.feature_more_info_title)
        MORE_INFO_FAQ_TAB_INDEX -> stringResource(R.string.faq_more_info_title)
        else -> stringResource(R.string.premium_more_info_title)
    }
}
