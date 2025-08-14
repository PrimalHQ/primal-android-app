package net.primal.android.premium.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import net.primal.android.core.compose.PrimalScaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.runtime.DisposableLifecycleObserverEffect
import net.primal.android.premium.buying.home.PRO_ORANGE
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.LegendaryStyle
import net.primal.android.premium.ui.PremiumBadge
import net.primal.android.premium.ui.PrimalPremiumTable
import net.primal.android.premium.ui.toHumanReadableString
import net.primal.android.premium.utils.isPremiumFreeTier
import net.primal.android.premium.utils.isPrimalLegendTier
import net.primal.android.theme.AppTheme
import net.primal.domain.links.CdnImage

@Composable
fun PremiumHomeScreen(viewModel: PremiumHomeViewModel, callbacks: PremiumHomeContract.ScreenCallbacks) {
    val uiState = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> viewModel.setEvent(PremiumHomeContract.UiEvent.RequestMembershipUpdate)
            else -> Unit
        }
    }

    PremiumHomeScreen(
        state = uiState.value,
        callbacks = callbacks,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumHomeScreen(
    state: PremiumHomeContract.UiState,
    callbacks: PremiumHomeContract.ScreenCallbacks,
    eventPublisher: (PremiumHomeContract.UiEvent) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.toHumanReadableString() },
        onErrorDismiss = {
            eventPublisher(PremiumHomeContract.UiEvent.DismissError)
        },
    )

    PrimalScaffold(
        topBar = {
            PrimalTopAppBar(
                title = if (state.membership.isPrimalLegendTier()) {
                    stringResource(id = R.string.premium_home_member_pro_title)
                } else {
                    stringResource(id = R.string.premium_home_member_premium_title)
                },
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = callbacks.onClose,
                showDivider = false,
            )
        },
        bottomBar = {
            if (state.membership?.isExpired() == true) {
                BottomBarButton(
                    text = stringResource(id = R.string.premium_home_renew_subscription),
                    legendaryCustomization = state.avatarLegendaryCustomization,
                    onClick = {
                        callbacks.onRenewSubscription(state.membership.premiumName)
                    },
                )
            } else {
                BottomBarButton(
                    text = if (state.membership.isPrimalLegendTier()) {
                        stringResource(id = R.string.premium_home_pro_button)
                    } else {
                        stringResource(id = R.string.premium_home_premium_button)
                    },
                    legendaryCustomization = state.avatarLegendaryCustomization,
                    onClick = callbacks.onManagePremium,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        PremiumHomeContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(paddingValues)
                .navigationBarsPadding(),
            state = state,
            eventPublisher = eventPublisher,
            onLegendCardClick = callbacks.onLegendCardClick,
            onContributePrimal = callbacks.onContributePrimal,
            onSupportPrimal = callbacks.onSupportPrimal,
            onUpgradeToProClick = callbacks.onUpgradeToProClick,
        )
    }
}

@Suppress("UnusedParameter")
@Composable
private fun PremiumHomeContent(
    modifier: Modifier,
    state: PremiumHomeContract.UiState,
    eventPublisher: (PremiumHomeContract.UiEvent) -> Unit,
    onLegendCardClick: (String) -> Unit,
    onContributePrimal: () -> Unit,
    onSupportPrimal: () -> Unit,
    onUpgradeToProClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
    ) {
        PremiumAvatarHeader(
            primalName = state.membership?.premiumName ?: "",
            avatarCdnImage = state.avatarCdnImage,
            avatarLegendaryCustomization = state.avatarLegendaryCustomization,
        )

        if (state.membership != null) {
            PremiumBadge(
                modifier = Modifier.clickable(enabled = state.membership.isPrimalLegendTier()) {
                    state.profileId?.let { onLegendCardClick(it) }
                },
                firstCohort = state.membership.cohort1,
                secondCohort = state.membership.cohort2,
                membershipExpired = state.membership.isExpired(),
                legendaryStyle = state.avatarLegendaryCustomization?.legendaryStyle
                    ?: LegendaryStyle.NO_CUSTOMIZATION,
            )

            if (state.membership.isPremiumFreeTier()) {
                Text(
                    modifier = Modifier.padding(horizontal = 36.dp),
                    text = stringResource(id = R.string.premium_home_member_early_primal_user),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                )
            }

            PrimalPremiumTable(
                profileNostrAddress = state.profileNostrAddress,
                profileLightningAddress = state.profileLightningAddress,
                premiumMembership = state.membership,
                onApplyPrimalNostrAddress = { eventPublisher(PremiumHomeContract.UiEvent.ApplyPrimalNostrAddress) },
                onApplyPrimalLightningAddress = {
                    eventPublisher(PremiumHomeContract.UiEvent.ApplyPrimalLightningAddress)
                },
            )

            when {
                state.membership.isExpired() -> {
                    Text(
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        text = stringResource(id = R.string.premium_home_expired_subscription_notice),
                        style = AppTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }

                !state.membership.isPrimalLegendTier() -> {
                    UpgradeToPrimalProNotice(onUpgradeToProClick = onUpgradeToProClick)
                }

                else -> {
                    if (state.showSupportUsNotice) {
                        if (!state.membership.isPrimalLegendTier()) {
                            SupportUsNoticePremium(
                                onSupportPrimal = onSupportPrimal,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UpgradeToPrimalProNotice(onUpgradeToProClick: () -> Unit) {
    Text(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onUpgradeToProClick,
            ),
        text = buildAnnotatedString {
            appendLine(stringResource(id = R.string.premium_home_primal_pro_want_to))
            append(stringResource(id = R.string.premium_home_primal_pro_check_out))
            append(" ")
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = PRO_ORANGE)) {
                append(stringResource(id = R.string.premium_home_primal_pro))
            }
            append(".")
        },
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        style = AppTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun PremiumAvatarHeader(
    primalName: String,
    avatarCdnImage: CdnImage? = null,
    avatarLegendaryCustomization: LegendaryCustomization? = null,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        UniversalAvatarThumbnail(
            avatarCdnImage = avatarCdnImage,
            avatarSize = 80.dp,
            legendaryCustomization = avatarLegendaryCustomization,
        )
        Spacer(modifier = Modifier.height(16.dp))
        NostrUserText(
            modifier = Modifier.padding(start = 8.dp),
            displayName = primalName,
            internetIdentifier = "$primalName@primal.net",
            internetIdentifierBadgeSize = 24.dp,
            fontSize = 20.sp,
            legendaryCustomization = avatarLegendaryCustomization,
        )
    }
}

@Composable
private fun BottomBarButton(
    text: String,
    legendaryCustomization: LegendaryCustomization?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val legendaryBrush = if (
        legendaryCustomization?.legendaryStyle != null &&
        legendaryCustomization.legendaryStyle != LegendaryStyle.NO_CUSTOMIZATION
    ) {
        legendaryCustomization.legendaryStyle.primaryBrush
    } else {
        Brush.linearGradient(listOf(AppTheme.colorScheme.tertiary, AppTheme.colorScheme.tertiary))
    }

    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(36.dp)
            .clip(AppTheme.shapes.extraLarge)
            .background(legendaryBrush),
    ) {
        PrimalFilledButton(
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent,
            onClick = onClick,
        ) {
            Text(
                modifier = Modifier.padding(vertical = 8.dp),
                text = text,
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SupportUsNoticePremium(onSupportPrimal: () -> Unit) {
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            text = stringResource(id = R.string.premium_home_enjoying_primal),
            style = AppTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Row {
            Text(
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                text = stringResource(id = R.string.premium_home_enjoying_primal_if_so) + " ",
                style = AppTheme.typography.bodyMedium,
            )
            Text(
                modifier = Modifier.clickable(onClick = onSupportPrimal),
                style = AppTheme.typography.bodyMedium,
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = AppTheme.colorScheme.secondary,
                        ),
                    ) {
                        append(stringResource(id = R.string.premium_home_support_us))
                    }
                    withStyle(
                        style = SpanStyle(
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                        ),
                    ) {
                        append(".")
                    }
                },
            )
        }
    }
}

@Suppress("UnusedPrivateMember")
@Composable
private fun SupportUsNoticeLegend(
    visible: Boolean,
    donatedSats: Long,
    onContributePrimal: () -> Unit,
) {
    Column(
        modifier = Modifier.alpha(if (visible) 1.0f else 0.0f),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row {
            Text(
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                text = stringResource(id = R.string.premium_home_legend_contribution_title) + " ",
                style = AppTheme.typography.bodyMedium,
            )
            Text(
                modifier = Modifier.clickable(onClick = onContributePrimal),
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = AppTheme.colorScheme.onBackground,
                        ),
                    ) {
                        append(donatedSats.let { "%,d sats".format(it) })
                    }
                },
            )
        }
        if (donatedSats > 0L) {
            Text(
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                text = stringResource(id = R.string.premium_home_legend_support_appreciation),
                style = AppTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            modifier = Modifier
                .clickable(
                    onClick = onContributePrimal,
                ),
            color = AppTheme.colorScheme.secondary,
            text = stringResource(id = R.string.premium_home_legend_contribute_more),
            style = AppTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}
