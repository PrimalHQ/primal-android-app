package net.primal.android.premium.info

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import net.primal.android.core.compose.PrimalSingleTab
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

internal const val WHY_PREMIUM_TAB_INDEX = 0
internal const val FEATURES_TAB_INDEX = 1
internal const val FAQ_TAB_INDEX = 2

@Composable
fun PremiumMoreInfoScreen(onClose: () -> Unit) {
    val pagerState = rememberPagerState { MORE_INFO_TAB_COUNT }

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
                    Text(text = "Close", fontWeight = FontWeight.Bold)
                }
            }
        },
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.background(AppTheme.colorScheme.surfaceVariant),
        ) { currentPage ->
            when (currentPage) {
                WHY_PREMIUM_TAB_INDEX -> {
                    WhyPremiumTabContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp),
                    )
                }

                FEATURES_TAB_INDEX -> {
                    FeaturesTabContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        contentPadding = paddingValues,
                    )
                }

                FAQ_TAB_INDEX -> {
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

    Column(
        modifier = Modifier.background(AppTheme.colorScheme.surface),
    ) {
        PrimalTopAppBar(
            title = "Primal Premium",
            navigationIcon = PrimalIcons.ArrowBack,
            onNavigationIconClick = onClose,
            showDivider = false,
        )
        MoreInfoTabs(
            modifier = Modifier.padding(top = 8.dp),
            selectedTabIndex = pagerState.currentPage,
            onWhyPremiumTabClick = { uiScope.launch { pagerState.scrollToPage(WHY_PREMIUM_TAB_INDEX) } },
            onFeaturesTabClick = { uiScope.launch { pagerState.scrollToPage(FEATURES_TAB_INDEX) } },
            onFAQTabClick = { uiScope.launch { pagerState.scrollToPage(FAQ_TAB_INDEX) } },
        )
    }
}
