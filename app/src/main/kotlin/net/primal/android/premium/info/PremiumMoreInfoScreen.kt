package net.primal.android.premium.info

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.info.ui.FAQTabContent
import net.primal.android.premium.info.ui.FeaturesTabContent
import net.primal.android.premium.info.ui.MoreInfoTabs
import net.primal.android.premium.info.ui.WhyPremiumTabContent
import net.primal.android.theme.AppTheme

internal const val MORE_INFO_TAB_COUNT = 3

const val PREMIUM_MORE_INFO_WHY_TAB_INDEX = 0
const val PREMIUM_MORE_INFO_FEATURES_TAB_INDEX = 1
const val PREMIUM_MORE_INFO_FAQ_TAB_INDEX = 2

private val tabsRange = PREMIUM_MORE_INFO_WHY_TAB_INDEX..PREMIUM_MORE_INFO_FAQ_TAB_INDEX

@Composable
fun PremiumMoreInfoScreen(initialTabIndex: Int = PREMIUM_MORE_INFO_WHY_TAB_INDEX, onClose: () -> Unit) {
    val pagerState = rememberPagerState(
        initialPage = if (initialTabIndex in tabsRange) initialTabIndex else PREMIUM_MORE_INFO_WHY_TAB_INDEX,
        pageCount = { MORE_INFO_TAB_COUNT },
    )

    Scaffold(
        topBar = {
            MoreInfoTopAppBar(
                pagerState = pagerState,
                onClose = onClose,
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .background(AppTheme.colorScheme.surfaceVariant)
                    .fillMaxWidth(),
            ) {
                PrimalFilledButton(
                    modifier = Modifier
                        .padding(vertical = 24.dp, horizontal = 36.dp)
                        .navigationBarsPadding()
                        .fillMaxWidth(),
                    onClick = onClose,
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                    contentColor = AppTheme.colorScheme.onBackground,
                ) {
                    Text(
                        text = stringResource(id = R.string.premium_more_info_close_button),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        },
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.background(AppTheme.colorScheme.surfaceVariant),
        ) { currentPage ->
            when (currentPage) {
                PREMIUM_MORE_INFO_WHY_TAB_INDEX -> {
                    WhyPremiumTabContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp),
                    )
                }

                PREMIUM_MORE_INFO_FEATURES_TAB_INDEX -> {
                    FeaturesTabContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentPadding = paddingValues,
                    )
                }

                PREMIUM_MORE_INFO_FAQ_TAB_INDEX -> {
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
        title = stringResource(id = R.string.premium_more_info_title),
        navigationIcon = PrimalIcons.ArrowBack,
        onNavigationIconClick = onClose,
        showDivider = true,
        footer = {
            MoreInfoTabs(
                modifier = Modifier.padding(top = 8.dp),
                selectedTabIndex = pagerState.currentPage,
                onWhyPremiumTabClick = { uiScope.launch { pagerState.scrollToPage(PREMIUM_MORE_INFO_WHY_TAB_INDEX) } },
                onFeaturesTabClick = {
                    uiScope.launch {
                        pagerState.scrollToPage(
                            PREMIUM_MORE_INFO_FEATURES_TAB_INDEX,
                        )
                    }
                },
                onFAQTabClick = { uiScope.launch { pagerState.scrollToPage(PREMIUM_MORE_INFO_FAQ_TAB_INDEX) } },
            )
        },
    )
}
