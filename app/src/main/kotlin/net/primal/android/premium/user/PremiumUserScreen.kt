package net.primal.android.premium.user

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.premium.ui.PremiumBadge
import net.primal.android.premium.ui.PrimalPremiumTable
import net.primal.android.premium.utils.isPremiumFree
import net.primal.android.premium.utils.isPrimalLegend
import net.primal.android.theme.AppTheme

@Composable
fun PremiumUserScreen(
    viewModel: PremiumUserViewModel,
    onClose: () -> Unit,
    onManagePremium: () -> Unit,
    onSupportPrimal: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    PremiumUserScreen(
        onClose = onClose,
        onManagePremium = onManagePremium,
        state = uiState.value,
        onSupportPrimal = onSupportPrimal,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumUserScreen(
    state: PremiumUserContract.UiState,
    onClose: () -> Unit,
    onManagePremium: () -> Unit,
    onSupportPrimal: () -> Unit,
) {
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
            PremiumUserBottomBar(
                onClick = onManagePremium,
            )
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
                AvatarThumbnail(
                    avatarCdnImage = state.avatarCdnImage,
                    avatarSize = 80.dp,
                )
                Spacer(modifier = Modifier.height(16.dp))
                NostrUserText(
                    modifier = Modifier.padding(start = 8.dp),
                    displayName = state.displayName,
                    internetIdentifier = state.profileNostrAddress,
                    internetIdentifierBadgeSize = 24.dp,
                    fontSize = 20.sp,
                )
            }

            state.membership?.let {
                PremiumBadge(
                    firstCohort = it.cohort1,
                    secondCohort = it.cohort2,
                    topColor = AppTheme.colorScheme.primary,
                )
                if (it.cohort2.isPremiumFree()) {
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
                    premiumMembership = it,
                )
                if (!it.cohort1.isPrimalLegend()) {
                    SupportUsNotice(
                        onSupportPrimal = onSupportPrimal,
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumUserBottomBar(modifier: Modifier = Modifier, onClick: () -> Unit) {
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
                text = stringResource(id = R.string.premium_manage_premium_button),
                style = AppTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun SupportUsNotice(onSupportPrimal: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            text = stringResource(id = R.string.premium_enjoying_primal),
            style = AppTheme.typography.bodyMedium,
        )
        Row {
            Text(
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                text = stringResource(id = R.string.premium_enjoying_primal_if_so) + " ",
                style = AppTheme.typography.bodyMedium,
            )
            Text(
                modifier = Modifier.clickable { onSupportPrimal() },
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
