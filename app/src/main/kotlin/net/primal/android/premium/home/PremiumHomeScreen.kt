package net.primal.android.premium.home

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import net.primal.android.premium.legend.domain.LegendaryStyle
import net.primal.android.premium.ui.PremiumBadge
import net.primal.android.premium.ui.PrimalPremiumTable
import net.primal.android.premium.ui.toHumanReadableString
import net.primal.android.premium.utils.isPremiumFreeTier
import net.primal.android.theme.AppTheme

@Composable
fun PremiumHomeScreen(
    viewModel: PremiumHomeViewModel,
    onClose: () -> Unit,
    onRenewSubscription: (primalName: String) -> Unit,
    onManagePremium: () -> Unit,
    onSupportPrimal: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    DisposableLifecycleObserverEffect(viewModel) {
        when (it) {
            Lifecycle.Event.ON_START -> viewModel.setEvent(PremiumHomeContract.UiEvent.RequestMembershipUpdate)
            else -> Unit
        }
    }

    PremiumHomeScreen(
        state = uiState.value,
        onClose = onClose,
        onRenewSubscription = onRenewSubscription,
        onManagePremium = onManagePremium,
        onSupportPrimal = onSupportPrimal,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumHomeScreen(
    state: PremiumHomeContract.UiState,
    onClose: () -> Unit,
    onRenewSubscription: (primalName: String) -> Unit,
    onManagePremium: () -> Unit,
    onSupportPrimal: () -> Unit,
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

    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_member_title),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
                showDivider = false,
            )
        },
        bottomBar = {
            if (state.membership?.isExpired() == true) {
                BottomBarButton(
                    text = stringResource(id = R.string.premium_renew_subscription),
                    onClick = {
                        onRenewSubscription(state.membership.premiumName)
                    },
                )
            } else {
                BottomBarButton(
                    text = stringResource(id = R.string.premium_manage_premium_button),
                    onClick = onManagePremium,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(paddingValues)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                UniversalAvatarThumbnail(
                    avatarCdnImage = state.avatarCdnImage,
                    avatarSize = 80.dp,
                    legendaryCustomization = state.avatarLegendaryCustomization,
                )
                Spacer(modifier = Modifier.height(16.dp))
                val primalName = state.membership?.premiumName ?: ""
                NostrUserText(
                    modifier = Modifier.padding(start = 8.dp),
                    displayName = primalName,
                    internetIdentifier = "$primalName@primal.net",
                    internetIdentifierBadgeSize = 24.dp,
                    fontSize = 20.sp,
                    legendaryCustomization = state.avatarLegendaryCustomization,
                )
            }

            if (state.membership != null) {
                PremiumBadge(
                    firstCohort = state.membership.cohort1,
                    secondCohort = state.membership.cohort2,
                    membershipExpired = state.membership.isExpired(),
                    legendaryStyle = state.avatarLegendaryCustomization?.legendaryStyle
                        ?: LegendaryStyle.NO_CUSTOMIZATION,

                )

                if (state.membership.isPremiumFreeTier()) {
                    Text(
                        modifier = Modifier.padding(horizontal = 36.dp),
                        text = stringResource(id = R.string.premium_member_early_primal_user),
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
                            text = stringResource(id = R.string.premium_expired_subscription_notice),
                            style = AppTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                        )
                    }

                    else -> {
                        SupportUsNotice(
                            visible = state.showSupportUsNotice,
                            onSupportPrimal = onSupportPrimal,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomBarButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(36.dp),
    ) {
        PrimalFilledButton(
            modifier = Modifier.fillMaxWidth(),
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
private fun SupportUsNotice(visible: Boolean, onSupportPrimal: () -> Unit) {
    Column(
        modifier = Modifier.alpha(if (visible) 1.0f else 0.0f),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            text = stringResource(id = R.string.premium_enjoying_primal),
            style = AppTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Row {
            Text(
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                text = stringResource(id = R.string.premium_enjoying_primal_if_so) + " ",
                style = AppTheme.typography.bodyMedium,
            )
            Text(
                modifier = Modifier
                    .clickable(
                        enabled = visible,
                        onClick = onSupportPrimal,
                    ),
                style = AppTheme.typography.bodyMedium,
                text = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            color = AppTheme.colorScheme.secondary,
                        ),
                    ) {
                        append(stringResource(id = R.string.premium_support_us))
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
