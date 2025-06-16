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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.Check
import net.primal.android.core.compose.icons.primaliconpack.PrimalPremium
import net.primal.android.premium.ui.toPricingString
import net.primal.android.premium.utils.isPremiumTier
import net.primal.android.premium.utils.isProTier
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.store.domain.SubscriptionBillingPeriod
import net.primal.android.wallet.store.domain.SubscriptionProduct
import net.primal.android.wallet.store.domain.SubscriptionTier

internal val PREMIUM_TINT_DARK = Color(0xFFDDDDDD)
internal val PREMIUM_TINT_LIGHT = Color(0xFF222222)
internal val PREMIUM_PINK = Color(0xFFCA077C)
internal val PRO_ORANGE = Color(0xFFE47C00)

@ExperimentalMaterial3Api
@Composable
fun PremiumBuyingHomeStage(
    subscriptionTier: SubscriptionTier,
    loading: Boolean,
    isPremiumBadgeOrigin: Boolean,
    subscriptions: List<SubscriptionProduct>,
    onClose: () -> Unit,
    onLearnMoreClick: (SubscriptionTier) -> Unit,
    onPurchaseSubscription: (SubscriptionTier) -> Unit,
) {
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

            PrimalSubscriptionTitle()

            SubscriptionOfferSelector(
                initialSubscriptionTier = subscriptionTier,
                loading = loading,
                subscriptions = subscriptions,
                onPurchaseSubscription = onPurchaseSubscription,
                onLearnMoreClick = onLearnMoreClick,
            )

            TOSNotice()
        }
    }
}

@Composable
private fun SubscriptionOfferSelector(
    initialSubscriptionTier: SubscriptionTier,
    loading: Boolean,
    subscriptions: List<SubscriptionProduct>,
    onPurchaseSubscription: (SubscriptionTier) -> Unit,
    onLearnMoreClick: (SubscriptionTier) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = initialSubscriptionTier.ordinal,
        pageCount = { 2 },
    )

    Column {
        OfferPager(
            pagerState = pagerState,
            loading = loading,
            subscriptions = subscriptions,
            onPurchaseSubscription = onPurchaseSubscription,
        )
        OfferPagerIndicators(pagerState = pagerState)
        LearnMoreLink(
            pagerState = pagerState,
            onLearnMoreClick = onLearnMoreClick,
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun OfferPager(
    pagerState: PagerState,
    subscriptions: List<SubscriptionProduct>,
    loading: Boolean,
    onPurchaseSubscription: (SubscriptionTier) -> Unit,
) {
    val pageSize = 300.dp

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val horizontalPadding = (maxWidth - pageSize) / 2
            .coerceAtLeast(0)

        val premiumSubscription = remember(subscriptions) {
            subscriptions.filter { it.tier.isPremiumTier() }
        }
        val proSubscriptions = remember(subscriptions) {
            subscriptions.filter { it.tier.isProTier() }
        }

        val premiumMonthly = remember(premiumSubscription) {
            premiumSubscription.find { it.billingPeriod == SubscriptionBillingPeriod.Monthly }
        }
        val premiumYearly = remember(premiumSubscription) {
            premiumSubscription.find { it.billingPeriod == SubscriptionBillingPeriod.Yearly }
        }

        val proMonthly = remember(proSubscriptions) {
            proSubscriptions.find { it.billingPeriod == SubscriptionBillingPeriod.Monthly }
        }
        val proYearly = remember(proSubscriptions) {
            proSubscriptions.find { it.billingPeriod == SubscriptionBillingPeriod.Yearly }
        }

        val premiumDiscountPercent = remember(premiumMonthly, premiumYearly) {
            calculateDiscountPercent(premiumMonthly, premiumYearly)
        }
        val proDiscountPercent = remember(proMonthly, proYearly) {
            calculateDiscountPercent(proMonthly, proYearly)
        }

        val premiumBadgeText = if (!loading) {
            premiumDiscountPercent?.let {
                stringResource(R.string.subscription_save_percent, it)
            }
        } else {
            null
        }

        val proBadgeText = if (!loading) {
            proDiscountPercent?.let {
                stringResource(R.string.subscription_save_percent, it)
            }
        } else {
            null
        }

        HorizontalPager(
            state = pagerState,
            pageSpacing = 16.dp,
            pageSize = PageSize.Fixed(pageSize),
            contentPadding = PaddingValues(horizontal = horizontalPadding),
        ) { page ->
            when (page) {
                0 -> OfferCard(
                    titleSuffix = stringResource(R.string.subscription_primal_premium_title),
                    titleColor = PREMIUM_PINK,
                    priceText = if (loading) "---" else premiumMonthly?.toPricingString() ?: "---",
                    billingText = stringResource(
                        R.string.subscription_billed_annually,
                        if (loading) "---" else premiumYearly?.toPricingString() ?: "---",
                    ),

                    badgeColor = PREMIUM_PINK,
                    descriptionItems = listOf(
                        stringResource(R.string.subscription_primal_premium_perk_verified_nostr_address),
                        stringResource(R.string.subscription_primal_premium_perk_custom_lightning_address),
                        stringResource(R.string.subscription_primal_premium_perk_vip_profile),
                        stringResource(R.string.subscription_primal_premium_perk_advanced_nostr_search),
                        stringResource(R.string.subscription_primal_premium_perk_paid_relay),
                        stringResource(R.string.subscription_primal_premium_perk_10gb_media_storage),
                        stringResource(R.string.subscription_primal_premium_perk_1gb_max_file_size),
                    ),
                    buttonText = stringResource(R.string.subscription_primal_buy_premium),
                    onBuyOfferClick = { onPurchaseSubscription(SubscriptionTier.PREMIUM) },
                    badgeText = premiumBadgeText,
                )
                1 -> OfferCard(
                    titleSuffix = stringResource(R.string.subscription_primal_pro_title),
                    titleColor = PRO_ORANGE,
                    priceText = if (loading) "---" else proMonthly?.toPricingString() ?: "---",
                    billingText = stringResource(
                        R.string.subscription_billed_annually,
                        if (loading) "---" else proYearly?.toPricingString() ?: "---",
                    ),
                    badgeColor = PRO_ORANGE,
                    descriptionItems = listOf(
                        stringResource(R.string.subscription_primal_pro_perk_everything_in_premium),
                        stringResource(R.string.subscription_primal_pro_perk_studio),
                        stringResource(R.string.subscription_primal_pro_perk_legend_status),
                        stringResource(R.string.subscription_primal_pro_perk_100gb_storage),
                        stringResource(R.string.subscription_primal_pro_perk_10gb_max_file),
                    ),
                    hideFirstBullet = true,
                    buttonText = stringResource(R.string.subscription_primal_buy_pro),
                    onBuyOfferClick = { onPurchaseSubscription(SubscriptionTier.PRO) },
                    buttonColor = PRO_ORANGE,
                    badgeText = proBadgeText,
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
private fun LearnMoreLink(pagerState: PagerState, onLearnMoreClick: (SubscriptionTier) -> Unit) {
    val isPremium = pagerState.currentPage == 0
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
                append(stringResource(id = R.string.subscription_tos_title))
            }
            withLink(link = LinkAnnotation.Url("https://primal.net/terms")) {
                withStyle(
                    style = textStyle.copy(textDecoration = TextDecoration.Underline),
                ) {
                    append("\n")
                    append(stringResource(id = R.string.subscription_tos_subtitle))
                }
            }
        },
        textAlign = TextAlign.Center,
        lineHeight = 22.sp,
    )
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

@Composable
private fun OfferCard(
    modifier: Modifier = Modifier,
    titleSuffix: String,
    titleColor: Color,
    priceText: String,
    billingText: String,
    badgeColor: Color,
    descriptionItems: List<String>,
    hideFirstBullet: Boolean = false,
    buttonText: String,
    onBuyOfferClick: () -> Unit,
    badgeText: String?,
    buttonColor: Color = AppTheme.colorScheme.primary,
) {
    Column(
        modifier = modifier
            .height(430.dp)
            .width(300.dp)
            .clip(AppTheme.shapes.medium)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt3)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            OfferTitle(
                titleSuffix = titleSuffix,
                titleColor = titleColor,
            )
            Spacer(Modifier.height(16.dp))

            OfferPrice(priceText = priceText)
            Spacer(Modifier.height(4.dp))

            BillingInfo(
                billingText = billingText,
                badgeColor = badgeColor,
                badgeText = badgeText,
            )
            Spacer(Modifier.height(16.dp))

            DescriptionSection(
                bulletPoints = descriptionItems,
                showFirstWithoutCheckmark = hideFirstBullet,
            )
        }

        PrimalFilledButton(
            modifier = Modifier.fillMaxWidth(),
            height = 48.dp,
            containerColor = buttonColor,
            onClick = onBuyOfferClick,
        ) {
            Text(
                text = buttonText,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun OfferTitle(titleSuffix: String, titleColor: Color) {
    Row {
        Text(
            text = "Primal",
            fontSize = 24.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colorScheme.onSurface,
        )
        Text(
            text = " $titleSuffix",
            fontSize = 24.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Bold,
            color = titleColor,
        )
    }
}

@Composable
private fun OfferPrice(priceText: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            text = priceText,
            fontSize = 44.sp,
            lineHeight = 44.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = stringResource(R.string.subscription_price_per_month),
            fontSize = 20.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.Normal,
            color = AppTheme.colorScheme.onSurface,
        )
    }
}

private const val BadgeCornerRadiusPercent = 50

@Composable
private fun BillingInfo(
    billingText: String,
    badgeColor: Color,
    badgeText: String?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = billingText,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
        Spacer(Modifier.width(4.dp))

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .background(
                        color = badgeColor.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(BadgeCornerRadiusPercent),
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(
                    text = badgeText,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

@Composable
private fun DescriptionSection(showFirstWithoutCheckmark: Boolean = false, bulletPoints: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.Start,
    ) {
        if (showFirstWithoutCheckmark) {
            Text(
                text = bulletPoints[0],
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                fontSize = 16.sp,
                lineHeight = 20.sp,
            )
            bulletPoints.drop(1)
        }
        if (showFirstWithoutCheckmark) {
            bulletPoints.drop(1)
        } else {
            bulletPoints
        }.onEach {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(10.dp),
                    imageVector = PrimalIcons.Check,
                    contentDescription = null,
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = it,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                )
            }
        }
    }
}

private fun calculateDiscountPercent(monthly: SubscriptionProduct?, yearly: SubscriptionProduct?): Int? {
    val monthlyMicros = monthly?.takeIf { it.billingPeriod == SubscriptionBillingPeriod.Monthly }?.priceAmountMicros
    val yearlyMicros = yearly?.takeIf { it.billingPeriod == SubscriptionBillingPeriod.Yearly }?.priceAmountMicros

    if (monthlyMicros == null || yearlyMicros == null) return null

    val monthlyPrice = monthlyMicros / MICROS_IN_UNIT
    val yearlyPrice = yearlyMicros / MICROS_IN_UNIT

    val discount = if (monthlyPrice > 0.0 && yearlyPrice > 0.0) {
        ((1 - yearlyPrice / (monthlyPrice * MONTHS_IN_YEAR)) * PERCENT_CONVERSION).toInt()
    } else {
        null
    }

    return discount?.takeIf { it > 0 }
}

private const val MICROS_IN_UNIT = 1_000_000.0
private const val MONTHS_IN_YEAR = 12
private const val PERCENT_CONVERSION = 100
