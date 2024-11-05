package net.primal.android.premium.purchase

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.findActivity
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.purchase.ui.PremiumPromoCodeBottomSheet
import net.primal.android.premium.ui.PremiumPrimalNameTable
import net.primal.android.premium.ui.toGetSubscriptionString
import net.primal.android.premium.ui.toPricingString
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.store.domain.SubscriptionProduct

@Composable
fun PremiumPurchaseStage(
    primalName: String,
    subscriptions: List<SubscriptionProduct>,
    onBack: () -> Unit,
    onLearnMoreClick: () -> Unit,
) {
    val viewModel = hiltViewModel<PremiumPurchaseViewModel>()
    val uiState = viewModel.state.collectAsState()

    PremiumPurchaseStage(
        state = uiState.value,
        subscriptions = subscriptions,
        eventPublisher = viewModel::setEvent,
        primalName = primalName,
        onBack = onBack,
        onLearnMoreClick = onLearnMoreClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumPurchaseStage(
    primalName: String,
    subscriptions: List<SubscriptionProduct>,
    state: PremiumPurchaseContract.UiState,
    onBack: () -> Unit,
    onLearnMoreClick: () -> Unit,
    eventPublisher: (PremiumPurchaseContract.UiEvent) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val activity = LocalContext.current.findActivity()
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
                eventPublisher(PremiumPurchaseContract.UiEvent.ClearPromoCodeValidity)
            },
            onCodeCodeConfirmed = {
                eventPublisher(PremiumPurchaseContract.UiEvent.ApplyPromoCode(it))
            },
            isCheckingPromoCodeValidity = state.isCheckingPromoCodeValidity,
        )
    }
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_purchase_title),
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
            state.profile?.let {
                AvatarThumbnail(
                    avatarCdnImage = it.avatarCdnImage,
                    avatarSize = 80.dp,
                )
                NostrUserText(
                    displayName = it.authorDisplayName,
                    internetIdentifier = "$primalName@primal.net",
                    internetIdentifierBadgeSize = 24.dp,
                    fontSize = 20.sp,
                )
            }
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = stringResource(id = R.string.premium_purchase_primal_name_available),
                style = AppTheme.typography.bodyLarge,
                color = AppTheme.extraColorScheme.successBright,
            )
            PremiumPrimalNameTable(
                primalName = primalName,
            )
            MoreInfoPromoCodeRow(
                modifier = Modifier.padding(vertical = 8.dp),
                onLearnMoreClick = onLearnMoreClick,
                onPromoCodeClick = { promoCodeBottomSheetVisibility = true },
            )

            if (activity != null) {
                BuyPremiumButtons(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    subscriptions = subscriptions,
                    onClick = { subscription ->
                        eventPublisher(
                            PremiumPurchaseContract.UiEvent.RequestPurchase(
                                activity = activity,
                                subscriptionProduct = subscription,
                            ),
                        )
                    },
                )
                TOSNotice()
            }
        }
    }
}

@Composable
private fun MoreInfoPromoCodeRow(
    modifier: Modifier = Modifier,
    onLearnMoreClick: () -> Unit,
    onPromoCodeClick: () -> Unit,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
    ) {
        Text(
            modifier = Modifier.clickable { onLearnMoreClick() },
            text = stringResource(id = R.string.premium_purchase_learn_more),
            color = AppTheme.colorScheme.secondary,
            style = AppTheme.typography.bodyMedium,
        )
        VerticalDivider(
            modifier = Modifier.height(20.dp),
            thickness = 1.dp,
            color = AppTheme.colorScheme.outline,
        )
        Text(
            modifier = Modifier.clickable { onPromoCodeClick() },
            text = stringResource(id = R.string.premium_purchase_promo_code),
            color = AppTheme.colorScheme.secondary,
            style = AppTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun BuyPremiumButtons(
    modifier: Modifier = Modifier,
    subscriptions: List<SubscriptionProduct>,
    onClick: (SubscriptionProduct) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        subscriptions.forEach {
            BuyPremiumButton(
                startText = it.toGetSubscriptionString(),
                endText = it.toPricingString(),
                onClick = { onClick(it) },
            )
        }
    }
}

@Composable
fun BuyPremiumButton(
    startText: String,
    endText: String,
    onClick: () -> Unit,
) {
    PrimalFilledButton(
        modifier = Modifier.fillMaxWidth(),
        height = 64.dp,
        shape = RoundedCornerShape(percent = 100),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = startText,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            )
            Text(
                text = endText,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            )
        }
    }
}

@Composable
private fun TOSNotice() {
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
                        color = AppTheme.colorScheme.secondary,
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
