package net.primal.android.premium.legend.custimization

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnail
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.LegendaryProfileNoCustomization
import net.primal.android.premium.legend.LegendaryProfile
import net.primal.android.premium.ui.PremiumBadge
import net.primal.android.theme.AppTheme

@Composable
fun LegendaryProfileCustomizationScreen(viewModel: LegendaryProfileCustomizationViewModel, onClose: () -> Unit) {
    val uiState by viewModel.state.collectAsState()

    LegendaryProfileCustomizationScreen(
        state = uiState,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegendaryProfileCustomizationScreen(state: LegendaryProfileCustomizationContract.UiState, onClose: () -> Unit) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_legend_profile_customization),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        bottomBar = {
            BottomBarButton(
                text = stringResource(id = R.string.premium_legend_profile_apply_button),
                onClick = {

                },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 16.dp)
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
                val primalName = state.membership?.premiumName ?: ""
                NostrUserText(
                    modifier = Modifier.padding(start = 8.dp),
                    displayName = primalName,
                    internetIdentifier = "$primalName@primal.net",
                    internetIdentifierBadgeSize = 24.dp,
                    fontSize = 20.sp,
                )
            }

            if (state.membership != null) {
                PremiumBadge(
                    firstCohort = state.membership.cohort1,
                    secondCohort = state.membership.cohort2,
                    membershipExpired = state.membership.isExpired(),
                    topColor = AppTheme.colorScheme.primary,
                )

                PrimalDivider(modifier = Modifier.padding(top = 16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        LegendProfileColorBox(profile = LegendaryProfile.NO_CUSTOMIZATION)

                        LegendProfileColorBox(profile = LegendaryProfile.GOLD)

                        LegendProfileColorBox(profile = LegendaryProfile.AQUA)

                        LegendProfileColorBox(profile = LegendaryProfile.SILVER)

                        LegendProfileColorBox(profile = LegendaryProfile.PURPLE)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        LegendProfileColorBox(profile = LegendaryProfile.PURPLE_HAZE)

                        LegendProfileColorBox(profile = LegendaryProfile.TEAL)

                        LegendProfileColorBox(profile = LegendaryProfile.BROWN)

                        LegendProfileColorBox(profile = LegendaryProfile.BLUE)

                        LegendProfileColorBox(profile = LegendaryProfile.SUN_FIRE)
                    }
                }

                SwitchSettings(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                )

                Text(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    text = stringResource(id = R.string.premium_legend_profile_customization_notice),
                    style = AppTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun LegendProfileColorBox(profile: LegendaryProfile) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(brush = profile.brush, shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (profile == LegendaryProfile.NO_CUSTOMIZATION) {
            Icon(
                imageVector = PrimalIcons.LegendaryProfileNoCustomization,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }
    }
}

@Composable
private fun SwitchSettings(modifier: Modifier) {
    Column(
        modifier = modifier.background(
            color = AppTheme.extraColorScheme.surfaceVariantAlt3,
            shape = AppTheme.shapes.large,
        ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.premium_legend_profile_custom_badge),
                fontSize = 16.sp,
                style = AppTheme.typography.bodyMedium,
            )
            PrimalSwitch(
                checked = false,
                onCheckedChange = {},
            )
        }

        PrimalDivider()

        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = stringResource(R.string.premium_legend_profile_avatar_ring),
                fontSize = 16.sp,
                style = AppTheme.typography.bodyMedium,
            )
            PrimalSwitch(
                checked = true,
                onCheckedChange = {},
            )
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
            .padding(bottom = 36.dp)
            .padding(horizontal = 36.dp),
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
