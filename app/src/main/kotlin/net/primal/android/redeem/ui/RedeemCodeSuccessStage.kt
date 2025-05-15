package net.primal.android.redeem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import net.primal.android.R
import net.primal.android.auth.compose.OnboardingButton
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.LightningBoltFilled
import net.primal.android.core.compose.icons.primaliconpack.VerifiedFilled
import net.primal.android.redeem.RedeemCodeContract
import net.primal.android.theme.AppTheme

private val LIGHTNING_COLOR = Color(0xFFFF9F2F)
private val MUTED_TEXT_COLOR = Color(0xFF808080)
private val DIVIDER_COLOR = Color(0x26111111)

@Composable
internal fun RedeemCodeSuccessStage(
    modifier: Modifier = Modifier,
    title: String,
    userState: RedeemCodeContract.UserState,
    requiresPrimalWallet: Boolean,
    isLoading: Boolean,
    benefits: List<RedeemCodeContract.PromoCodeBenefit>,
    onOnboardToPrimalClick: () -> Unit,
    onActivateWalletClick: () -> Unit,
    onApplyCodeClick: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                text = title,
                color = Color.White,
                style = AppTheme.typography.bodyLarge,
            )
            PromoCodeBenefitsList(
                modifier = Modifier.fillMaxWidth(),
                benefits = benefits,
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            when {
                userState == RedeemCodeContract.UserState.NoUser ->
                    NoUserContent(
                        isLoading = isLoading,
                        onOnboardToPrimalClick = onOnboardToPrimalClick,
                    )

                userState == RedeemCodeContract.UserState.UserWithoutPrimalWallet && requiresPrimalWallet ->
                    UserWithoutWalletContent(
                        isLoading = isLoading,
                        onActivateWalletClick = onActivateWalletClick,
                    )

                else ->
                    UserWithWalletContent(
                        isLoading = isLoading,
                        onApplyCodeClick = onApplyCodeClick,
                    )
            }
        }
    }
}

@Composable
fun NoUserContent(isLoading: Boolean, onOnboardToPrimalClick: () -> Unit) {
    Text(
        text = stringResource(id = R.string.redeem_code_to_redeem_code_notice),
        style = AppTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = Color.White,
    )

    OnboardingButton(
        loading = isLoading,
        text = stringResource(id = R.string.redeem_code_onboard_to_primal_button),
        onClick = onOnboardToPrimalClick,
    )
}

@Composable
fun UserWithWalletContent(isLoading: Boolean, onApplyCodeClick: () -> Unit) {
    OnboardingButton(
        loading = isLoading,
        text = stringResource(id = R.string.redeem_code_redeem_code_button),
        onClick = onApplyCodeClick,
    )
}

@Composable
fun UserWithoutWalletContent(isLoading: Boolean, onActivateWalletClick: () -> Unit) {
    Text(
        text = stringResource(id = R.string.redeem_code_no_wallet_notice),
        style = AppTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = Color.White,
    )

    OnboardingButton(
        loading = isLoading,
        text = stringResource(id = R.string.redeem_code_active_wallet_button),
        onClick = onActivateWalletClick,
    )
}

@Composable
fun PromoCodeBenefitsList(modifier: Modifier = Modifier, benefits: List<RedeemCodeContract.PromoCodeBenefit>) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .clip(AppTheme.shapes.medium)
            .background(Color.White.copy(alpha = 0.8f))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
    ) {
        Text(
            text = stringResource(id = R.string.redeem_code_this_code_is_loaded),
            color = MUTED_TEXT_COLOR,
            style = AppTheme.typography.bodyMedium,
        )
        benefits.mapIndexed { index, item ->
            when (item) {
                is RedeemCodeContract.PromoCodeBenefit.PrimalPremium -> {
                    PrimalPremiumBenefit(
                        modifier = Modifier.padding(top = 16.dp),
                        benefit = item,
                    )
                }

                is RedeemCodeContract.PromoCodeBenefit.WalletBalance -> {
                    WalletBalanceBenefit(
                        modifier = Modifier.padding(top = 16.dp),
                        benefit = item,
                    )
                }
            }

            if (index < benefits.lastIndex) {
                PrimalDivider(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(fraction = 0.75f),
                    color = DIVIDER_COLOR,
                )
            }
        }
    }
}

@Composable
fun PrimalPremiumBenefit(modifier: Modifier = Modifier, benefit: RedeemCodeContract.PromoCodeBenefit.PrimalPremium) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            Icon(
                imageVector = PrimalIcons.VerifiedFilled,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(32.dp),
            )
            Text(
                text = stringResource(id = R.string.redeem_code_primal_premium),
                style = AppTheme.typography.headlineSmall,
            )
        }

        Text(
            text = pluralStringResource(id = R.plurals.redeem_code_premium_months, benefit.durationInMonths),
            color = MUTED_TEXT_COLOR,
            style = AppTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun WalletBalanceBenefit(modifier: Modifier = Modifier, benefit: RedeemCodeContract.PromoCodeBenefit.WalletBalance) {
    val numberFormat = remember { NumberFormat.getNumberInstance() }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            Icon(
                imageVector = PrimalIcons.LightningBoltFilled,
                contentDescription = null,
                tint = LIGHTNING_COLOR,
                modifier = Modifier.size(24.dp),
            )

            Text(
                text = numberFormat.format(benefit.sats),
                style = AppTheme.typography.headlineLarge,
            )
            Text(
                text = stringResource(id = R.string.redeem_code_sats),
                style = AppTheme.typography.headlineLarge,
                fontWeight = FontWeight.Normal,
            )
        }

        Text(
            text = stringResource(id = R.string.redeem_code_get_zapping),
            color = MUTED_TEXT_COLOR,
            style = AppTheme.typography.bodyMedium,
        )
    }
}
