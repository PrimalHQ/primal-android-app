package net.primal.android.premium.info

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.util.TableInfo
import kotlinx.coroutines.launch
import net.primal.android.core.compose.PrimalSingleTab
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme


internal const val MORE_INFO_TAB_COUNT = 3

internal const val WHY_PREMIUM_TAB_INDEX = 0
internal const val FEATURES_TAB_INDEX = 1
internal const val FAQ_TAB_INDEX = 2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumMoreInfoScreen(
    onClose: () -> Unit,
) {
    val pagerState = rememberPagerState { MORE_INFO_TAB_COUNT }

    Scaffold(
        topBar = {
            MoreInfoTopAppBar(
                pagerState = pagerState,
                onClose = onClose,
            )
        },
        bottomBar = {
            PrimalFilledButton(
                modifier = Modifier
                    .padding(vertical = 24.dp)
                    .padding(horizontal = 36.dp)
                    .systemBarsPadding()
                    .fillMaxWidth(),
                onClick = onClose,
                containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                contentColor = AppTheme.colorScheme.onBackground,
            ) {
                Text(text = "Close", fontWeight = FontWeight.Bold)
            }
        },
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            contentPadding = paddingValues,
            modifier = Modifier.background(AppTheme.colorScheme.surfaceVariant),
        ) { currentPage ->
            when (currentPage) {
                WHY_PREMIUM_TAB_INDEX -> {
                    WhyPremiumTabContent(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                    )
                }

                FEATURES_TAB_INDEX -> {

                }

                FAQ_TAB_INDEX -> {

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoreInfoTopAppBar(
    pagerState: PagerState,
    onClose: () -> Unit,
) {
    val uiScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.background(AppTheme.colorScheme.surface)
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

@Composable
private fun WhyPremiumTabContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(26.dp),
    ) {
        Text(
            text = "Why Get Primal Premium?",
            fontWeight = FontWeight.Bold,
            style = AppTheme.typography.bodyLarge,
            fontSize = 18.sp,
        )
        Text(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            style = AppTheme.typography.bodyLarge,
            lineHeight = 24.sp,
            fontSize = 18.sp,
            textAlign = TextAlign.Justify,
            text = """
            Become a Nostr power user and help shape the future! Open protocols like Nostr give us the opportunity to regain control over our online lives. 
            
            At Primal, we don’t rely on advertising. We don’t monetize user data. Our users are our customers. Our sole focus is to make the best possible product for our users. We open source all our work to help the Nostr ecosystem flourish. By signing up for Primal Premium, you are enabling us to continue building for Nostr. 
            
            Be the change you want to see in the world. If you don’t want to be the product, consider being the customer. 
        """.trimIndent(),
        )
    }
}

@Composable
private fun MoreInfoTabs(
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
            text = "Why Premium".uppercase(),
            onClick = onWhyPremiumTabClick,
            textStyle = AppTheme.typography.bodyMedium,
        )
        PrimalSingleTab(
            selected = selectedTabIndex == FEATURES_TAB_INDEX,
            text = "Features".uppercase(),
            onClick = onFeaturesTabClick,
            textStyle = AppTheme.typography.bodyMedium,
        )
        PrimalSingleTab(
            selected = selectedTabIndex == FAQ_TAB_INDEX,
            text = "FAQ".uppercase(),
            onClick = onFAQTabClick,
            textStyle = AppTheme.typography.bodyMedium,
        )

    }

}
