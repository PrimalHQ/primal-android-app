package net.primal.android.premium.legend.custimization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.AvatarThumbnailCustomBorder
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

    LaunchedEffect(viewModel, onClose) {
        viewModel.effect.collect {
            when (it) {
                LegendaryProfileCustomizationContract.SideEffect.CustomizationSaved -> onClose()
            }
        }
    }

    LegendaryProfileCustomizationScreen(
        state = uiState,
        eventPublisher = viewModel::setEvent,
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegendaryProfileCustomizationScreen(
    state: LegendaryProfileCustomizationContract.UiState,
    eventPublisher: (LegendaryProfileCustomizationContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var customBadge by remember(state.customBadge) { mutableStateOf(state.customBadge) }
    var avatarGlow by remember(state.avatarGlow) { mutableStateOf(state.avatarGlow) }
    var selectedProfile by remember(state.legendaryProfile) { mutableStateOf(state.legendaryProfile) }

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
                    eventPublisher(
                        LegendaryProfileCustomizationContract.UiEvent.ApplyCustomization(
                            customBadge = customBadge,
                            avatarGlow = avatarGlow,
                            style = selectedProfile,
                        ),
                    )
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
                AvatarThumbnailCustomBorder(
                    avatarCdnImage = state.avatarCdnImage,
                    avatarSize = 80.dp,
                    hasBorder = true,
                    borderBrush = if (avatarGlow) {
                        selectedProfile.brush
                    } else {
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    },
                )
                Spacer(modifier = Modifier.height(16.dp))
                val primalName = state.membership?.premiumName ?: ""
                NostrUserText(
                    modifier = Modifier.padding(start = 8.dp),
                    displayName = primalName,
                    internetIdentifier = "$primalName@primal.net",
                    internetIdentifierBadgeSize = 24.dp,
                    customBadge = if (customBadge) selectedProfile else null,
                    fontSize = 20.sp,
                )
            }

            if (state.membership != null) {
                PremiumBadge(
                    firstCohort = state.membership.cohort1,
                    secondCohort = state.membership.cohort2,
                    membershipExpired = state.membership.isExpired(),
                    legendaryProfile = selectedProfile,
                )

                PrimalDivider(modifier = Modifier.padding(top = 16.dp))

                LegendaryColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    activeLegendaryProfile = selectedProfile,
                    onProfileChanged = { selectedProfile = it },
                )

                SwitchSettings(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    avatarRing = avatarGlow,
                    onAvatarRingChanged = { avatarGlow = it },
                    customBadge = customBadge,
                    onCustomBadgeChanged = { customBadge = it },
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
private fun LegendaryColorPicker(
    modifier: Modifier,
    activeLegendaryProfile: LegendaryProfile,
    onProfileChanged: (LegendaryProfile) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LegendProfileColorBox(
                previewProfile = LegendaryProfile.NO_CUSTOMIZATION,
                activeProfile = activeLegendaryProfile,
                onClick = onProfileChanged,
            )

            LegendProfileColorBox(
                previewProfile = LegendaryProfile.GOLD,
                activeProfile = activeLegendaryProfile,
                onClick = onProfileChanged,
            )

            LegendProfileColorBox(
                previewProfile = LegendaryProfile.AQUA,
                activeProfile = activeLegendaryProfile,
                onClick = onProfileChanged,
            )

            LegendProfileColorBox(
                previewProfile = LegendaryProfile.SILVER,
                activeProfile = activeLegendaryProfile,
                onClick = onProfileChanged,
            )

            LegendProfileColorBox(
                previewProfile = LegendaryProfile.PURPLE,
                activeProfile = activeLegendaryProfile,
                onClick = onProfileChanged,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LegendProfileColorBox(
                previewProfile = LegendaryProfile.PURPLE_HAZE,
                activeProfile = activeLegendaryProfile,
                onClick = onProfileChanged,
            )

            LegendProfileColorBox(
                previewProfile = LegendaryProfile.TEAL,
                activeProfile = activeLegendaryProfile,
                onClick = onProfileChanged,
            )

            LegendProfileColorBox(
                previewProfile = LegendaryProfile.BROWN,
                activeProfile = activeLegendaryProfile,
                onClick = onProfileChanged,
            )

            LegendProfileColorBox(
                previewProfile = LegendaryProfile.BLUE,
                activeProfile = activeLegendaryProfile,
                onClick = onProfileChanged,
            )

            LegendProfileColorBox(
                previewProfile = LegendaryProfile.SUN_FIRE,
                activeProfile = activeLegendaryProfile,
                onClick = onProfileChanged,
            )
        }
    }
}

@Composable
private fun LegendProfileColorBox(
    previewProfile: LegendaryProfile,
    activeProfile: LegendaryProfile,
    onClick: (LegendaryProfile) -> Unit,
) {
    Box(
        modifier = Modifier
            .clickable { onClick(previewProfile) }
            .border(
                width = 3.dp,
                color = if (previewProfile == activeProfile) {
                    AppTheme.colorScheme.onSurface
                } else {
                    Color.Transparent
                },
                shape = CircleShape,
            )
            .size(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .background(brush = previewProfile.brush, shape = CircleShape)
                .size(36.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (previewProfile == LegendaryProfile.NO_CUSTOMIZATION) {
                Icon(
                    imageVector = PrimalIcons.LegendaryProfileNoCustomization,
                    contentDescription = null,
                    tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                )
            }
        }
    }
}

@Composable
private fun SwitchSettings(
    modifier: Modifier,
    customBadge: Boolean,
    onCustomBadgeChanged: (Boolean) -> Unit,
    avatarRing: Boolean,
    onAvatarRingChanged: (Boolean) -> Unit,
) {
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
                checked = customBadge,
                onCheckedChange = onCustomBadgeChanged,
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
                checked = avatarRing,
                onCheckedChange = onAvatarRingChanged,
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
