package net.primal.android.premium.buying.purchase

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Year
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.isGoogleBuild
import net.primal.android.premium.buying.PremiumBuyingContract
import net.primal.android.premium.buying.home.PRO_ORANGE
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.LegendaryStyle
import net.primal.android.premium.ui.PremiumBadge
import net.primal.android.premium.ui.PremiumPrimalNameTable
import net.primal.android.premium.ui.toGetSubscriptionString
import net.primal.android.premium.ui.toPricingString
import net.primal.android.premium.utils.isPremiumTier
import net.primal.android.premium.utils.isProTier
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.store.domain.SubscriptionProduct
import net.primal.android.wallet.store.domain.SubscriptionTier

@ExperimentalMaterial3Api
@Composable
fun PremiumPurchaseStage(
    state: PremiumBuyingContract.UiState,
    onBack: () -> Unit,
    onLearnMoreClick: (SubscriptionTier) -> Unit,
    eventPublisher: (PremiumBuyingContract.UiEvent) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val activity = LocalActivity.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var promoCodeBottomSheetVisibility by remember { mutableStateOf(false) }
    LaunchedEffect(state.promoCodeValidity) {
        if (state.promoCodeValidity == true) {
            uiScope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    promoCodeBottomSheetVisibility = false
                }
            }
        }
    }

    if (promoCodeBottomSheetVisibility) {
        PremiumPromoCodeBottomSheet(
            sheetState = sheetState,
            promoCodeValidity = state.promoCodeValidity,
            onDismissRequest = {
                promoCodeBottomSheetVisibility = false
                eventPublisher(PremiumBuyingContract.UiEvent.ClearPromoCodeValidity)
            },
            onCodeCodeConfirmed = {
                eventPublisher(PremiumBuyingContract.UiEvent.ApplyPromoCode(it))
            },
            isCheckingPromoCodeValidity = state.isCheckingPromoCodeValidity,
        )
    }
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = if (state.isExtendingPremium) {
                    stringResource(id = R.string.premium_extend_subscription_title)
                } else {
                    stringResource(id = R.string.premium_purchase_title)
                },
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onBack,
                showDivider = false,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .navigationBarsPadding()
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(paddingValues)
                .verticalScroll(state = rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        ) {
            if (state.profile != null && state.primalName != null) {
                UniversalAvatarThumbnail(
                    avatarCdnImage = state.profile.avatarCdnImage,
                    avatarSize = 80.dp,
                    legendaryCustomization = if (state.subscriptionTier.isProTier()) {
                        LegendaryCustomization(
                            avatarGlow = true,
                            legendaryStyle = LegendaryStyle.GOLD,
                        )
                    } else {
                        state.profile.premiumDetails?.legendaryCustomization
                    },
                )
                NostrUserText(
                    displayName = state.primalName,
                    internetIdentifier = "${state.primalName}@primal.net",
                    internetIdentifierBadgeSize = 24.dp,
                    fontSize = 20.sp,
                    legendaryCustomization = if (state.subscriptionTier.isProTier()) {
                        LegendaryCustomization(
                            customBadge = true,
                            legendaryStyle = LegendaryStyle.GOLD,
                        )
                    } else {
                        state.profile.premiumDetails?.legendaryCustomization
                    },
                )

                if (state.subscriptionTier.isProTier()) {
                    PremiumBadge(
                        firstCohort = "Legend",
                        secondCohort = Year.now().value.toString(),
                        membershipExpired = false,
                        legendaryStyle = LegendaryStyle.GOLD,
                    )
                }

                if (!state.isExtendingPremium) {
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        text = if (state.isUpgradingToPro) {
                            stringResource(id = R.string.premium_purchase_your_legend_status)
                        } else {
                            stringResource(id = R.string.premium_purchase_primal_name_available)
                        },
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.extraColorScheme.successBright,
                    )
                }
                PremiumPrimalNameTable(
                    primalName = state.primalName,
                )
            }

            if (state.isExtendingPremium) {
                Text(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    text = stringResource(id = R.string.premium_extend_subscription_notice),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    textAlign = TextAlign.Center,
                )
            }

            MoreInfoPromoCodeRow(
                modifier = Modifier.padding(vertical = 8.dp),
                subscriptionTier = state.subscriptionTier,
                onLearnMoreClick = onLearnMoreClick,
                onPromoCodeClick = { promoCodeBottomSheetVisibility = true },
            )

            val currentSubscriptions = remember(state.subscriptions, state.subscriptionTier) {
                state.subscriptions.filter { it.tier == state.subscriptionTier }
            }

            if (activity != null) {
                BuyPremiumButtons(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    loading = state.loading,
                    activeSubscriptionProductId = state.activeSubscriptionProductId,
                    subscriptions = currentSubscriptions,
                    onBuySubscription = { subscription ->
                        eventPublisher(
                            PremiumBuyingContract.UiEvent.RequestPurchase(
                                activity = activity,
                                subscriptionProduct = subscription,
                            ),
                        )
                    },
                    onRestoreSubscription = {
                        eventPublisher(PremiumBuyingContract.UiEvent.RestoreSubscription)
                    },
                    subscriptionTier = state.subscriptionTier,
                    isUpgradingToPro = state.isUpgradingToPro,
                )
                TOSNotice(subscriptionTier = state.subscriptionTier)
            }
        }
    }
}

@Suppress("unused")
@Composable
private fun MoreInfoPromoCodeRow(
    modifier: Modifier = Modifier,
    subscriptionTier: SubscriptionTier,
    onLearnMoreClick: (SubscriptionTier) -> Unit,
    onPromoCodeClick: () -> Unit,
) {
    val learnAboutText = stringResource(
        if (subscriptionTier.isPremiumTier()) {
            R.string.subscription_learn_about_premium
        } else {
            R.string.subscription_learn_about_pro
        },
    )
    val color = if (subscriptionTier.isPremiumTier()) AppTheme.colorScheme.primary else PRO_ORANGE

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
        Text(
            modifier = Modifier.clickable {
                onLearnMoreClick(
                    if (subscriptionTier.isPremiumTier()) {
                        SubscriptionTier.PREMIUM
                    } else {
                        SubscriptionTier.PRO
                    },
                )
            },
            text = learnAboutText,
            color = color,
            style = AppTheme.typography.bodyMedium,
        )
//        VerticalDivider(
//            modifier = Modifier.height(20.dp),
//            thickness = 1.dp,
//            color = AppTheme.colorScheme.outline,
//        )
//        Text(
//            modifier = Modifier.clickable { onPromoCodeClick() },
//            text = stringResource(id = R.string.premium_purchase_promo_code),
//            color = AppTheme.colorScheme.secondary,
//            style = AppTheme.typography.bodyMedium,
//        )
    }
}

@Composable
fun BuyPremiumButtons(
    modifier: Modifier,
    activeSubscriptionProductId: String?,
    isUpgradingToPro: Boolean,
    loading: Boolean,
    subscriptions: List<SubscriptionProduct>,
    onBuySubscription: (SubscriptionProduct) -> Unit,
    onRestoreSubscription: () -> Unit,
    subscriptionTier: SubscriptionTier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (activeSubscriptionProductId != null && !isUpgradingToPro) {
            Text(
                modifier = Modifier.padding(horizontal = 32.dp),
                text = stringResource(R.string.premium_purchase_restore_subscription_explanation),
                textAlign = TextAlign.Center,
                style = AppTheme.typography.bodySmall,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )

            PrimalFilledButton(
                modifier = Modifier.fillMaxWidth(),
                height = 64.dp,
                shape = RoundedCornerShape(percent = 100),
                onClick = onRestoreSubscription,
            ) {
                Text(
                    text = stringResource(R.string.premium_purchase_restore_subscription),
                    style = AppTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                )
            }
        } else {
            when {
                subscriptions.isEmpty() -> {
                    Box(
                        modifier = Modifier.height(144.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (loading) {
                            PrimalLoadingSpinner(size = 32.dp)
                        } else {
                            Text(
                                modifier = Modifier
                                    .wrapContentSize()
                                    .padding(horizontal = 16.dp),
                                text = when {
                                    isGoogleBuild() -> stringResource(id = R.string.premium_google_play_not_available)
                                    else -> stringResource(id = R.string.premium_google_play_not_available_aosp)
                                },
                                textAlign = TextAlign.Center,
                                style = AppTheme.typography.bodySmall,
                                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                            )
                        }
                    }
                }
                else -> {
                    val minFontSize = subscriptions.minOf { it.toPricingString().resolveFontSize().value }.sp

                    subscriptions.forEach {
                        BuyPremiumButton(
                            startText = it.toGetSubscriptionString(),
                            endText = it.toPricingString(),
                            subscriptionTier = subscriptionTier,
                            fontSize = minFontSize,
                            onClick = { onBuySubscription(it) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BuyPremiumButton(
    startText: String,
    endText: String,
    subscriptionTier: SubscriptionTier,
    fontSize: TextUnit,
    onClick: () -> Unit,
) {
    PrimalFilledButton(
        modifier = Modifier.fillMaxWidth(),
        height = 64.dp,
        shape = RoundedCornerShape(percent = 100),
        onClick = onClick,
        containerColor = if (subscriptionTier.isPremiumTier()) {
            AppTheme.colorScheme.primary
        } else {
            PRO_ORANGE
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 6.dp),
                text = startText,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.wrapContentWidth(unbounded = true),
                text = endText,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = fontSize,
                maxLines = 1,
            )
        }
    }
}

@Suppress("MagicNumber")
private fun String.resolveFontSize() =
    when (this.length) {
        in 0..10 -> 19.sp
        in 11..16 -> 17.sp
        else -> 13.sp
    }

@Composable
private fun TOSNotice(subscriptionTier: SubscriptionTier) {
    Text(
        text = buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    fontSize = 14.sp,
                ),
            ) {
                append(stringResource(id = R.string.premium_purchase_tos_notice))
            }
            withLink(link = LinkAnnotation.Url("https://primal.net/terms")) {
                withStyle(
                    style = SpanStyle(
                        color = if (subscriptionTier.isPremiumTier()) {
                            AppTheme.colorScheme.secondary
                        } else {
                            PRO_ORANGE
                        },
                        fontSize = 14.sp,
                    ),
                ) {
                    append(" " + stringResource(id = R.string.premium_purchase_tos))
                }
            }
        },
        textAlign = TextAlign.Center,
    )
}
