package net.primal.android.premium.buying.home

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.LocalPrimalTheme
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.PrimalPremium
import net.primal.android.premium.buying.ui.PrimalPremiumOfferCard
import net.primal.android.premium.buying.ui.PrimalProOfferCard
import net.primal.android.premium.utils.isPremiumTier
import net.primal.android.premium.utils.isProTier
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.store.domain.SubscriptionProduct
import net.primal.android.wallet.store.domain.SubscriptionTier

internal val PREMIUM_TINT_DARK = Color(0xFFDDDDDD)
internal val PREMIUM_TINT_LIGHT = Color(0xFF222222)
internal val PREMIUM_PINK = Color(0xFFCA077C)
internal val PRO_ORANGE = Color(0xFFE47C00)

private const val PAGE_PREMIUM = 0
private const val PAGE_PRO = 1

@ExperimentalMaterial3Api
@Composable
fun PremiumBuyingHomeStage(
    subscriptionTier: SubscriptionTier,
    loading: Boolean,
    isUpgradingToPrimalPro: Boolean,
    isPremiumBadgeOrigin: Boolean,
    subscriptions: List<SubscriptionProduct>,
    onClose: () -> Unit,
    onLearnMoreClick: (SubscriptionTier) -> Unit,
    onPurchaseSubscription: (SubscriptionTier) -> Unit,
) {
    val premiumSubscriptions = remember(subscriptions) {
        subscriptions.filter { it.tier.isPremiumTier() }
    }
    val proSubscriptions = remember(subscriptions) {
        subscriptions.filter { it.tier.isProTier() }
    }

    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = if (isPremiumBadgeOrigin) stringResource(R.string.premium_primal_og_title) else "",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                showDivider = false,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(bottom = 24.dp)
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (isPremiumBadgeOrigin) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.premium_primal_og_description),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.onSurface,
                )
            }

            if (isUpgradingToPrimalPro) {
                UpgradeToPrimalProTitle()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 24.dp)
                        .padding(horizontal = 32.dp)
                        .fillMaxSize(),
                ) {
                    PrimalProOfferCard(
                        loading = loading,
                        proSubscriptions = proSubscriptions,
                        onPurchaseSubscription = onPurchaseSubscription,
                        paddingValues = PaddingValues(24.dp),
                        autoSizePrice = true,
                        priceFontSizeResolved = {},
                    )
                }
                LearnMoreLink(
                    isPremium = false,
                    onLearnMoreClick = onLearnMoreClick,
                )
            } else {
                PrimalSubscriptionTitle()

                SubscriptionOfferSelector(
                    subscriptionTier = subscriptionTier,
                    loading = loading,
                    proSubscriptions = proSubscriptions,
                    premiumSubscriptions = premiumSubscriptions,
                    onPurchaseSubscription = onPurchaseSubscription,
                    onLearnMoreClick = onLearnMoreClick,
                )
            }

            TOSNotice()
        }
    }
}

@Composable
private fun SubscriptionOfferSelector(
    subscriptionTier: SubscriptionTier,
    loading: Boolean,
    premiumSubscriptions: List<SubscriptionProduct>,
    proSubscriptions: List<SubscriptionProduct>,
    onPurchaseSubscription: (SubscriptionTier) -> Unit,
    onLearnMoreClick: (SubscriptionTier) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = subscriptionTier.ordinal,
        pageCount = { 2 },
    )

    Column {
        OfferPager(
            pagerState = pagerState,
            loading = loading,
            premiumSubscriptions = premiumSubscriptions,
            proSubscriptions = proSubscriptions,
            onPurchaseSubscription = onPurchaseSubscription,
        )
        OfferPagerIndicators(pagerState = pagerState)
        LearnMoreLink(
            isPremium = pagerState.currentPage == 0,
            onLearnMoreClick = onLearnMoreClick,
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun OfferPager(
    pagerState: PagerState,
    premiumSubscriptions: List<SubscriptionProduct>,
    proSubscriptions: List<SubscriptionProduct>,
    loading: Boolean,
    onPurchaseSubscription: (SubscriptionTier) -> Unit,
) {
    val pageSize = 300.dp
    var minFontSize by remember { mutableStateOf(44.sp) }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val horizontalPadding = ((maxWidth - pageSize) / 2).coerceAtLeast(0.dp)

        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            pageSize = PageSize.Fixed(pageSize),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
        ) { page ->
            when (page) {
                PAGE_PREMIUM -> PrimalPremiumOfferCard(
                    modifier = Modifier
                        .height(430.dp)
                        .width(300.dp),
                    loading = loading,
                    premiumSubscriptions = premiumSubscriptions,
                    onPurchaseSubscription = onPurchaseSubscription,
                    priceFontSize = minFontSize,
                    paddingValues = PaddingValues(16.dp),
                )

                PAGE_PRO -> PrimalProOfferCard(
                    modifier = Modifier
                        .height(430.dp)
                        .width(300.dp),
                    loading = loading,
                    proSubscriptions = proSubscriptions,
                    autoSizePrice = true,
                    priceFontSizeResolved = { minFontSize = it },
                    onPurchaseSubscription = onPurchaseSubscription,
                    paddingValues = PaddingValues(16.dp),
                )
            }
        }
    }
}

@Composable
private fun OfferPagerIndicators(pagerState: PagerState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(2) { index ->
            val selected = pagerState.currentPage == index
            val color = when {
                selected && pagerState.currentPage == 0 -> PREMIUM_PINK
                selected -> PRO_ORANGE
                else -> AppTheme.extraColorScheme.onSurfaceVariantAlt2
            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}

@Composable
private fun LearnMoreLink(isPremium: Boolean, onLearnMoreClick: (SubscriptionTier) -> Unit) {
    val learnAboutText = stringResource(
        if (isPremium) {
            R.string.subscription_learn_about_premium
        } else {
            R.string.subscription_learn_about_pro
        },
    )
    val color = if (isPremium) AppTheme.colorScheme.primary else PRO_ORANGE

    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp, top = 4.dp)
            .clickable {
                if (isPremium) {
                    onLearnMoreClick(SubscriptionTier.PREMIUM)
                } else {
                    onLearnMoreClick(SubscriptionTier.PRO)
                }
            },
        text = learnAboutText,
        fontSize = 18.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
        color = color,
    )
}

@Composable
private fun TOSNotice() {
    Text(
        modifier = Modifier.padding(horizontal = 35.dp, vertical = 8.dp),
        text = buildAnnotatedString {
            val textStyle = SpanStyle(
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                fontSize = 15.sp,
            )
            withStyle(style = textStyle) {
                appendLine(stringResource(id = R.string.subscription_tos_title))
            }
            withLink(link = LinkAnnotation.Url("https://primal.net/terms")) {
                withStyle(
                    style = textStyle.copy(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                    ),
                ) {
                    append(stringResource(id = R.string.subscription_tos))
                }
                withStyle(textStyle) {
                    append(" " + stringResource(id = R.string.subscription_and) + " ")
                }
                withStyle(
                    style = textStyle.copy(
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Bold,
                    ),
                ) {
                    append(stringResource(id = R.string.subscription_pp))
                }
            }
        },
        textAlign = TextAlign.Center,
        lineHeight = 22.sp,
    )
}

@Composable
fun UpgradeToPrimalProTitle(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = R.string.premium_buying_home_primal_pro),
            fontSize = 36.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = AppTheme.colorScheme.onSurface,
        )

        Text(
            text = stringResource(id = R.string.premium_buying_home_primal_pro_description),
            fontSize = 15.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
    }
}

@Composable
private fun PrimalSubscriptionTitle() {
    Column(
        modifier = Modifier.padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.subscription_primal_title_primary),
            fontSize = 36.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = AppTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.subscription_primal_title_secondary),
            fontSize = 36.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = AppTheme.colorScheme.onSurface,
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.subscription_primal_subtitle),
            fontSize = 18.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
    }
}

@Composable
fun PrimalPremiumLogoHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(id = R.drawable.primal_wave_logo_summer),
            contentDescription = null,
            modifier = Modifier
                .clip(CircleShape)
                .background(AppTheme.colorScheme.background)
                .size(46.dp),
        )
        Icon(
            imageVector = PrimalIcons.PrimalPremium,
            contentDescription = null,
            tint = if (LocalPrimalTheme.current.isDarkTheme) {
                PREMIUM_TINT_DARK
            } else {
                PREMIUM_TINT_LIGHT
            },
        )
    }
}
