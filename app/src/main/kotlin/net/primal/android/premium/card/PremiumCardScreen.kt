package net.primal.android.premium.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.EaseOutSine
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.format.FormatStyle
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.core.compose.dropdown.DropdownPrimalMenuItem
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.More
import net.primal.android.core.compose.icons.primaliconpack.Settings
import net.primal.android.core.compose.profile.model.ProfileDetailsUi
import net.primal.android.core.utils.formatNip05Identifier
import net.primal.android.core.utils.formatToDefaultDateFormat
import net.primal.android.premium.legend.domain.LegendaryCustomization
import net.primal.android.premium.legend.domain.LegendaryStyle
import net.primal.android.profile.details.ui.ProfilePremiumBadge
import net.primal.android.profile.details.ui.model.PremiumProfileDataUi
import net.primal.android.profile.details.ui.model.shouldShowPremiumBadge
import net.primal.android.theme.AppTheme

private val TOP_ICON_COLOR = Color(0xFF1E1E1E)
private val GLOW_RECT_COLOR = Color(0xFFCCCCCC)
private val CARD_BACKGROUND_COLOR = Color(0xFF222222)
private val DROPDOWN_BACKGROUND_COLOR = Color(0xFF282828)
private val PRIMARY_TEXT_COLOR = Color(0xFFFFFFFF)
private val SECONDARY_TEXT_COLOR = Color(0xFFAAAAAA)
private val FALLBACK_BACKGROUND_ELEMENT_COLOR = Color(0xFF444444)

private const val PRIMAL_2_0_RELEASE_DATE_IN_SECONDS = 1732147200L

private const val AVATAR_START_ROTATION = -45f
private const val AVATAR_END_ROTATION = 0f

@Composable
fun PremiumCardScreen(
    viewModel: PremiumCardViewModel,
    onClose: () -> Unit,
    onLegendSettingsClick: () -> Unit,
    onSeeOtherLegendsClick: () -> Unit,
    onSeeOtherPrimalOGsClick: () -> Unit,
    onBecomeLegendClick: () -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    PremiumCardScreen(
        state = uiState.value,
        onClose = onClose,
        onLegendSettingsClick = onLegendSettingsClick,
        onSeeOtherLegendsClick = onSeeOtherLegendsClick,
        onSeeOtherPrimalOGsClick = onSeeOtherPrimalOGsClick,
        onBecomeLegendClick = onBecomeLegendClick,
    )
}

@Composable
private fun PremiumCardScreen(
    state: PremiumCardContract.UiState,
    onClose: () -> Unit,
    onSeeOtherPrimalOGsClick: () -> Unit,
    onLegendSettingsClick: () -> Unit,
    onSeeOtherLegendsClick: () -> Unit,
    onBecomeLegendClick: () -> Unit,
) {
    val animationProgress = remember { AnimationState(initialValue = 0f) }
    val glowProgress = remember { AnimationState(initialValue = 0f) }
    LaunchedEffect(Unit) {
        launch { animationProgress.startAnimation(delayMillis = 250, easing = EaseInOutQuart) }
        launch { glowProgress.startAnimation(delayMillis = 750, easing = EaseOutSine) }
    }

    Box(
        modifier = Modifier
            .padding(20.dp)
            .fillMaxWidth()
            .clip(AppTheme.shapes.medium)
            .background(CARD_BACKGROUND_COLOR)
            .drawAnimatedBackgroundAndGlow(
                brush = state.profile?.premiumDetails?.legendaryCustomization?.legendaryStyle?.secondaryBrush
                    ?: SolidColor(FALLBACK_BACKGROUND_ELEMENT_COLOR),
                backgroundProgress = animationProgress,
                glowProgress = glowProgress,
            )
            .padding(bottom = 16.dp)
            .padding(4.dp)
            .aspectRatio(ratio = 0.56f),
    ) {
        when {
            state.isPrimalLegend -> {
                LegendCardLayout(
                    state = state,
                    onClose = onClose,
                    onLegendSettingsClick = onLegendSettingsClick,
                    onSeeOtherLegendsClick = onSeeOtherLegendsClick,
                    onBecomeLegendClick = onBecomeLegendClick,
                )
            }

            else -> {
                PrimalOGLayout(
                    state = state,
                    onClose = onClose,
                    onSeeOtherPrimalOGsClick = onSeeOtherPrimalOGsClick,
                )
            }
        }
    }
}

@Composable
private fun PrimalOGLayout(
    onClose: () -> Unit,
    onSeeOtherPrimalOGsClick: () -> Unit,
    state: PremiumCardContract.UiState,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(48.dp, Alignment.CenterVertically),
        ) {
            CloseButtonRow(onDismissRequest = onClose)

            state.profile?.let { profile ->
                ProfileSummary(profile = profile)
                PrimalOGDescription(profile = profile)
            }
        }
        AnimatedButtonsColumn(
            primaryButtonText = stringResource(id = R.string.premium_card_button_see_other_ogs),
            onPrimaryButtonClick = onSeeOtherPrimalOGsClick,
            isSecondaryButtonVisible = false,
        )
    }
}

@Composable
private fun LegendCardLayout(
    state: PremiumCardContract.UiState,
    onClose: () -> Unit,
    onLegendSettingsClick: () -> Unit,
    onSeeOtherLegendsClick: () -> Unit,
    onBecomeLegendClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        if (state.isActiveAccountCard) {
            OptionsDropdownMenu(
                onBackClick = onClose,
                onLegendSettingsClick = onLegendSettingsClick,
            )
        } else {
            CloseButtonRow(onDismissRequest = onClose)
        }

        state.profile?.let { profile ->
            ProfileSummary(profile = profile)
            LegendDescription(
                modifier = Modifier.padding(vertical = 16.dp),
                profile = profile,
            )
        }

        state.profile?.premiumDetails?.legendaryCustomization?.let { legendaryCustomization ->
            AnimatedButtonsColumn(
                primaryButtonText = stringResource(id = R.string.premium_card_button_see_other_legends),
                onPrimaryButtonClick = onSeeOtherLegendsClick,
                secondaryButtonText = stringResource(id = R.string.premium_card_button_become_a_legend),
                onSecondaryButtonClick = onBecomeLegendClick,
                isSecondaryButtonVisible = !state.isActiveAccountLegend,
                legendaryCustomization = legendaryCustomization,
            )
        }
    }
}

private suspend fun AnimationState<Float, AnimationVector1D>.startAnimation(delayMillis: Int, easing: Easing) =
    animateTo(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = 667,
            delayMillis = delayMillis,
            easing = easing,
        ),
    )

private fun Modifier.drawAnimatedBackgroundAndGlow(
    brush: Brush,
    backgroundProgress: AnimationState<Float, AnimationVector1D>,
    glowProgress: AnimationState<Float, AnimationVector1D>,
) = drawWithContent {
    val (topStart, bottomStart, topEnd) = makeEdgePaths(animationProgress = backgroundProgress)

    drawPath(alpha = 0.25f, path = topStart, brush = brush)
    drawPath(alpha = 0.15f, path = bottomStart, brush = brush)
    drawPath(path = topEnd, brush = brush)

    drawContent()
    drawGlowRectangle(glowProgress = glowProgress)
}

@Composable
private fun AnimatedButtonsColumn(
    modifier: Modifier = Modifier,
    primaryButtonText: String,
    secondaryButtonText: String = "",
    onPrimaryButtonClick: () -> Unit,
    onSecondaryButtonClick: () -> Unit = {},
    isSecondaryButtonVisible: Boolean = true,
    legendaryCustomization: LegendaryCustomization? = null,
) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }
    val secondaryButtonAlpha = if (isSecondaryButtonVisible) 1f else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = makeEnterTransition(delayMillis = 583),
        ) {
            TextButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onPrimaryButtonClick,
                contentPadding = PaddingValues(vertical = 16.dp),
            ) {
                Text(
                    text = primaryButtonText,
                    style = AppTheme.typography.bodyMedium,
                    color = legendaryCustomization?.legendaryStyle.resolveNoCustomizationAndNull(),
                    fontSize = 16.sp,
                )
            }
        }
        AnimatedVisibility(
            visible = showContent,
            enter = makeEnterTransition(delayMillis = 667),
        ) {
            Button(
                modifier = Modifier
                    .alpha(secondaryButtonAlpha)
                    .fillMaxWidth(),
                enabled = isSecondaryButtonVisible,
                onClick = onSecondaryButtonClick,
                contentPadding = PaddingValues(vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = legendaryCustomization?.legendaryStyle.resolveNoCustomizationAndNull(),
                    contentColor = legendaryCustomization?.legendaryStyle.resolveButtonColor(),
                ),
            ) {
                Text(
                    text = secondaryButtonText,
                    style = AppTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

private fun ContentDrawScope.drawGlowRectangle(glowProgress: AnimationState<Float, AnimationVector1D>) =
    rotate(degrees = 45f) {
        drawRect(
            topLeft = Offset(x = -440f, y = 1840f - glowProgress.value * 2280f),
            alpha = .05f + glowProgress.value * .25f,
            color = GLOW_RECT_COLOR,
            size = Size(width = 2000f, height = 300f),
        )
    }

@Suppress("MagicNumber")
private fun DrawScope.makeEdgePaths(
    animationProgress: AnimationState<Float, AnimationVector1D>,
): Triple<Path, Path, Path> {
    val topStart = Path().apply {
        moveTo(0f, animationProgress.value * size.height * .31f)
        lineTo(animationProgress.value * size.width * .6f, 0f)
        lineTo(-10f, 0f)
        close()
    }

    val bottomStart = Path().apply {
        moveTo(
            x = 0f,
            y = size.height * 0.745f + (1 - animationProgress.value) * size.height * .255f,
        )
        lineTo(0f, size.height)
        lineTo(animationProgress.value * size.width * 0.49f, size.height)
        close()
    }

    val topEnd = Path().apply {
        moveTo(size.width - 10f - animationProgress.value * size.width, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, animationProgress.value * size.height * .5f)
        close()
    }

    return Triple(topStart, bottomStart, topEnd)
}

private fun LegendaryStyle?.resolveNoCustomizationAndNull(): Color =
    run {
        if (this == null || this == LegendaryStyle.NO_CUSTOMIZATION) {
            Color.Unspecified
        } else {
            this.color
        }
    }

private fun LegendaryStyle?.resolveButtonColor(): Color =
    when (this) {
        LegendaryStyle.GOLD, LegendaryStyle.AQUA,
        LegendaryStyle.SILVER, LegendaryStyle.TEAL, LegendaryStyle.BROWN, null,
        -> Color.Black

        LegendaryStyle.NO_CUSTOMIZATION, LegendaryStyle.PURPLE, LegendaryStyle.PURPLE_HAZE,
        LegendaryStyle.BLUE, LegendaryStyle.SUN_FIRE,
        -> Color.White
    }

@Composable
private fun PrimalOGDescription(modifier: Modifier = Modifier, profile: ProfileDetailsUi) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }

    val ogSince = profile.premiumDetails?.premiumSince?.let {
        Instant.ofEpochSecond(it)
    } ?: Instant.ofEpochSecond(PRIMAL_2_0_RELEASE_DATE_IN_SECONDS)

    AnimatedVisibility(
        visible = showContent,
        enter = makeEnterTransition(delayMillis = 583),
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                        append(stringResource(id = R.string.premium_card_og_since))
                        append(" ")
                    }
                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                        append(ogSince.formatToDefaultDateFormat(FormatStyle.MEDIUM))
                    }
                },
                style = AppTheme.typography.bodyMedium,
                color = PRIMARY_TEXT_COLOR,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun LegendDescription(modifier: Modifier = Modifier, profile: ProfileDetailsUi) {
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }

    val legendSince = profile.premiumDetails?.legendSince?.let {
        Instant.ofEpochSecond(it)
    } ?: Instant.ofEpochSecond(PRIMAL_2_0_RELEASE_DATE_IN_SECONDS)

    AnimatedVisibility(
        visible = showContent,
        enter = makeEnterTransition(delayMillis = 583),
    ) {
        Column(
            modifier = modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        ) {
            Text(
                text = stringResource(id = R.string.premium_card_legend_since) + " " +
                    legendSince.formatToDefaultDateFormat(FormatStyle.LONG),
                style = AppTheme.typography.bodyMedium,
                color = PRIMARY_TEXT_COLOR,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = profile.premiumDetails?.legendaryCustomization?.currentShoutout ?: "",
                style = AppTheme.typography.bodyMedium,
                color = SECONDARY_TEXT_COLOR,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ProfileSummary(modifier: Modifier = Modifier, profile: ProfileDetailsUi) {
    val avatarRotation = remember { Animatable(AVATAR_START_ROTATION) }
    val avatarSizeAndAlphaProgress = remember { Animatable(0f) }
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        launch {
            avatarRotation.animateTo(
                targetValue = AVATAR_END_ROTATION,
                animationSpec = tween(
                    durationMillis = 650,
                    delayMillis = 250,
                    easing = EaseInOutQuart,
                ),
            )
        }
        launch {
            avatarSizeAndAlphaProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 650,
                    delayMillis = 250,
                    easing = EaseInOutQuart,
                ),
            )
        }
        showContent = true
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
    ) {
        Box(
            modifier = Modifier.size(145.dp),
            contentAlignment = Alignment.Center,
        ) {
            UniversalAvatarThumbnail(
                modifier = Modifier
                    .alpha(avatarSizeAndAlphaProgress.value)
                    .rotate(avatarRotation.value)
                    .scale(avatarSizeAndAlphaProgress.value.coerceAtLeast(minimumValue = 0.01f)),
                avatarSize = 145.dp,
                avatarCdnImage = profile.avatarCdnImage,
                legendaryCustomization = profile.premiumDetails?.legendaryCustomization,
                hasInnerBorderOverride = false,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            AnimatedDisplayName(showContent = showContent, profile = profile)
        }

        profile.internetIdentifier?.let { internetIdentifier ->
            AnimatedInternetIdentifier(
                showContent = showContent,
                internetIdentifier = internetIdentifier,
            )
        }

        if (profile.premiumDetails?.shouldShowPremiumBadge() == true) {
            AnimatedPremiumBadge(showContent = showContent, premiumDetails = profile.premiumDetails)
        }
    }
}

@Composable
private fun ColumnScope.AnimatedPremiumBadge(showContent: Boolean, premiumDetails: PremiumProfileDataUi) {
    AnimatedVisibility(
        visible = showContent,
        enter = makeEnterTransition(delayMillis = 500),
    ) {
        ProfilePremiumBadge(
            firstCohort = premiumDetails.cohort1 ?: "",
            secondCohort = premiumDetails.cohort2 ?: "",
            legendaryStyle = premiumDetails.legendaryCustomization?.legendaryStyle,
            firstCohortFontSize = 14.sp,
            secondCohortFontSize = 14.sp,
        )
    }
}

@Composable
private fun ColumnScope.AnimatedInternetIdentifier(showContent: Boolean, internetIdentifier: String) {
    AnimatedVisibility(
        visible = showContent,
        enter = makeEnterTransition(delayMillis = 416),
    ) {
        Text(
            modifier = Modifier,
            text = internetIdentifier.formatNip05Identifier(),
            style = AppTheme.typography.bodyMedium.copy(
                lineHeight = 12.sp,
            ),
            color = PRIMARY_TEXT_COLOR,
        )
    }
}

@Composable
private fun AnimatedDisplayName(showContent: Boolean, profile: ProfileDetailsUi) {
    AnimatedVisibility(
        visible = showContent,
        enter = makeEnterTransition(delayMillis = 333),
    ) {
        Box {
            NostrUserText(
                autoResizeToFit = true,
                displayName = profile.authorDisplayName,
                displayNameColor = PRIMARY_TEXT_COLOR,
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
        }
    }
}

private fun makeEnterTransition(delayMillis: Int, durationMillis: Int = 667) =
    fadeIn(
        animationSpec = tween(
            durationMillis = durationMillis,
            delayMillis = delayMillis,
            easing = EaseInOutQuart,
        ),
    ) + slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(
            durationMillis = durationMillis,
            delayMillis = delayMillis,
            easing = EaseInOutQuart,
        ),
    )

@Composable
private fun OptionsDropdownMenu(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onLegendSettingsClick: () -> Unit,
) {
    var menuVisible by remember { mutableStateOf(false) }
    val itemColors = MenuDefaults.itemColors(
        textColor = PRIMARY_TEXT_COLOR,
        trailingIconColor = PRIMARY_TEXT_COLOR,
    )
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(align = Alignment.CenterEnd),
    ) {
        IconButton(
            onClick = { menuVisible = !menuVisible },
        ) {
            Icon(
                imageVector = PrimalIcons.More,
                contentDescription = null,
                tint = TOP_ICON_COLOR,
            )
        }
        DropdownPrimalMenu(
            expanded = menuVisible,
            onDismissRequest = { menuVisible = false },
            backgroundColor = DROPDOWN_BACKGROUND_COLOR,
        ) {
            DropdownPrimalMenuItem(
                trailingIconVector = Icons.Default.Close,
                text = stringResource(id = R.string.premium_card_dropdown_close),
                onClick = onBackClick,
                colors = itemColors,
            )
            DropdownPrimalMenuItem(
                trailingIconVector = PrimalIcons.Settings,
                text = stringResource(id = R.string.premium_card_dropdown_legend_settings),
                onClick = onLegendSettingsClick,
                colors = itemColors,
            )
        }
    }
}

@Composable
private fun CloseButtonRow(modifier: Modifier = Modifier, onDismissRequest: () -> Unit) {
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
