package net.primal.android.premium.legend.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.premium.legend.LegendaryStyle
import net.primal.android.profile.details.ui.ProfilePremiumBadge
import net.primal.android.profile.details.ui.model.shouldShowPremiumBadge
import net.primal.android.theme.AppTheme

private val TOP_ICON_COLOR = Color(0xFF1E1E1E)

@Composable
fun LegendCardScreen(
    viewModel: LegendCardViewModel,
    onBackClick: () -> Unit,
    onSeeOtherLegendsClick: () -> Unit,
    onBecomeLegendClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    LegendCardScreen(
        state = uiState.value,
        onBackClick = onBackClick,
        onSeeOtherLegendsClick = onSeeOtherLegendsClick,
        onBecomeLegendClick = onBecomeLegendClick,
    )
}

@Composable
fun LegendCardScreen(
    state: LegendCardContract.UiState,
    onBackClick: () -> Unit,
    onSeeOtherLegendsClick: () -> Unit,
    onBecomeLegendClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .clip(AppTheme.shapes.medium)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt1)
            .drawBehind {
                val topStart = Path().apply {
                    moveTo(0f, size.height * 0.30f)
                    lineTo(size.height * 0.30f, 0f)
                    lineTo(-10f, 0f)
                    close()
                }

                val bottomStart = Path().apply {
                    moveTo(0f, size.height * 0.70f)
                    lineTo(0f, size.height)
                    lineTo(size.height * 0.30f, size.height)
                    close()
                }

                val topEnd = Path().apply {
                    moveTo(-10f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height * 0.45f)
                    close()
                }

                state.profile?.premiumDetails?.legendaryCustomization?.legendaryStyle?.simpleBrush?.let { brush ->
                    drawPath(
                        alpha = 0.25f,
                        path = topStart,
                        brush = brush,
                    )
                    drawPath(
                        alpha = 0.25f,
                        path = bottomStart,
                        brush = brush,
                    )
                    drawPath(
                        path = topEnd,
                        brush = brush,
                    )
                }
            }
            .padding(bottom = 16.dp)
            .padding(4.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterVertically),
        ) {
            OptionsMenu(onDismissRequest = onBackClick)

            state.profile?.let { profile ->
                ProfileSummary(
                    profile = profile,
                )

                LegendDescription(
                    modifier = Modifier.padding(vertical = 16.dp),
                    profile = profile,
                )
            }

            state.profile?.premiumDetails?.legendaryCustomization?.let { legendaryCustomization ->
                ButtonsColumn(
                    legendaryCustomization = legendaryCustomization,
                    onSeeOtherLegendsClick = onSeeOtherLegendsClick,
                    onBecomeLegendClick = onBecomeLegendClick,
                )
            }
        }
    }
}

@Composable
private fun ButtonsColumn(
    modifier: Modifier = Modifier,
    onSeeOtherLegendsClick: () -> Unit,
    onBecomeLegendClick: () -> Unit,
    legendaryCustomization: LegendaryCustomization,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ) {
        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onSeeOtherLegendsClick,
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            Text(
                text = "See other Legends",
                style = AppTheme.typography.bodyMedium,
                color = legendaryCustomization.legendaryStyle.resolveNoCustomizationAndNull(),
                fontSize = 16.sp,
            )
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBecomeLegendClick,
            contentPadding = PaddingValues(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = legendaryCustomization.legendaryStyle.resolveNoCustomizationAndNull(),
                contentColor = legendaryCustomization.legendaryStyle.resolveButtonColor(),
            ),
        ) {
            Text(
                text = "Become a Legend",
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
        }
    }
}

private fun LegendaryStyle?.resolveNoCustomizationAndNull(): Color =
    run { if (this == null || this == LegendaryStyle.NO_CUSTOMIZATION) Color.Unspecified else this.color }

private fun LegendaryStyle?.resolveButtonColor(): Color =
    when (this) {
        LegendaryStyle.NO_CUSTOMIZATION, LegendaryStyle.GOLD, LegendaryStyle.AQUA,
        LegendaryStyle.SILVER, LegendaryStyle.TEAL, LegendaryStyle.BROWN, null,
        -> Color.Black

        LegendaryStyle.PURPLE, LegendaryStyle.PURPLE_HAZE,
        LegendaryStyle.BLUE, LegendaryStyle.SUN_FIRE,
        -> Color.White
    }

@Composable
private fun LegendDescription(modifier: Modifier = Modifier, profile: ProfileDetailsUi) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = "Legend since December 21, 2024",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colorScheme.onPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Legend status is awarded to users who\nmade a significant contribution to\nNostr or Primal",
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ProfileSummary(modifier: Modifier = Modifier, profile: ProfileDetailsUi) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ) {
        UniversalAvatarThumbnail(
            avatarSize = 100.dp,
            avatarCdnImage = profile.avatarCdnImage,
            legendaryCustomization = profile.premiumDetails?.legendaryCustomization,
        )
        Spacer(modifier = Modifier.height(10.dp))
        NostrUserText(
            displayName = profile.authorDisplayName,
            internetIdentifier = profile.internetIdentifier,
            internetIdentifierBadgeSize = 26.dp,
            internetIdentifierBadgeAlign = PlaceholderVerticalAlign.Center,
            style = AppTheme.typography.titleLarge.copy(
                fontSize = 22.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold,
            ),
            legendaryCustomization = profile.premiumDetails?.legendaryCustomization,
        )

        profile.internetIdentifier?.let { internetIdentifier ->
            Text(
                modifier = Modifier,
                text = internetIdentifier.formatNip05Identifier(),
                style = AppTheme.typography.bodyMedium.copy(
                    lineHeight = 12.sp,
                ),
                color = AppTheme.colorScheme.onPrimary,
            )
        }
        if (profile.premiumDetails?.shouldShowPremiumBadge() == true) {
            ProfilePremiumBadge(
                firstCohort = profile.premiumDetails.cohort1 ?: "",
                secondCohort = profile.premiumDetails.cohort2 ?: "",
                legendaryStyle = profile.premiumDetails.legendaryCustomization?.legendaryStyle,
            )
        }
    }
}

@Composable
private fun OptionsMenu(modifier: Modifier = Modifier, onDismissRequest: () -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = onDismissRequest,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = TOP_ICON_COLOR,
            )
        }
    }
}
