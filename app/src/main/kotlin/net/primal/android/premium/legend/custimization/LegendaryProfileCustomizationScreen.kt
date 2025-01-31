package net.primal.android.premium.legend.custimization

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.NostrUserText
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.UniversalAvatarThumbnail
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.LegendaryProfileNoCustomization
import net.primal.android.premium.legend.LegendaryCustomization
import net.primal.android.premium.legend.LegendaryStyle
import net.primal.android.premium.legend.custimization.LegendaryProfileCustomizationContract.UiEvent
import net.primal.android.premium.ui.PremiumBadge
import net.primal.android.theme.AppTheme

const val SHOUTOUT_CHAR_LIMIT = 140

@Composable
fun LegendaryProfileCustomizationScreen(viewModel: LegendaryProfileCustomizationViewModel, onClose: () -> Unit) {
    val uiState by viewModel.state.collectAsState()

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
    eventPublisher: (UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var customBadge by remember(state.avatarLegendaryCustomization?.customBadge) {
        mutableStateOf(state.avatarLegendaryCustomization?.customBadge)
    }
    var avatarGlow by remember(state.avatarLegendaryCustomization?.avatarGlow) {
        mutableStateOf(state.avatarLegendaryCustomization?.avatarGlow)
    }
    var selectedStyle by remember(state.avatarLegendaryCustomization?.legendaryStyle) {
        mutableStateOf(state.avatarLegendaryCustomization?.legendaryStyle)
    }
    var inLeaderboard by remember(state.avatarLegendaryCustomization?.inLeaderboard) {
        mutableStateOf(state.avatarLegendaryCustomization?.inLeaderboard)
    }
    var shoutout by remember(state.membership?.editedShoutout, state.avatarLegendaryCustomization?.currentShoutout) {
        mutableStateOf(state.computeShoutout())
    }

    var editsInReview by remember(state.membership?.editedShoutout) {
        mutableStateOf(state.membership?.editedShoutout != null)
    }

    var editMode by remember { mutableStateOf(false) }
    val textFieldState =
        rememberSaveable(
            state.membership?.editedShoutout,
            state.avatarLegendaryCustomization?.currentShoutout,
            saver = TextFieldState.Saver,
        ) {
            TextFieldState(initialText = state.computeShoutout())
        }

    fun cancelEditMode() {
        editMode = false
        shoutout = state.computeShoutout()
        textFieldState.edit { replace(0, length, shoutout) }
    }

    BackHandler(enabled = editMode) {
        cancelEditMode()
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.premium_legend_profile_customization),
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        bottomBar = {
            if (editMode) {
                BottomBarButtons(
                    onSendClick = {
                        editMode = false
                        editsInReview = true
                        eventPublisher(UiEvent.ApplyCustomization(editedShoutout = shoutout))
                    },
                    onCancelClick = { cancelEditMode() },
                    shoutoutLength = shoutout.length,
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
                UniversalAvatarThumbnail(
                    avatarCdnImage = state.avatarCdnImage,
                    avatarSize = 80.dp,
                    legendaryCustomization = LegendaryCustomization(
                        avatarGlow = avatarGlow == true,
                        customBadge = customBadge == true,
                        legendaryStyle = selectedStyle,
                    ),
                )
                Spacer(modifier = Modifier.height(16.dp))
                val primalName = state.membership?.premiumName ?: ""
                NostrUserText(
                    modifier = Modifier.padding(start = 8.dp),
                    displayName = primalName,
                    internetIdentifier = "$primalName@primal.net",
                    internetIdentifierBadgeSize = 24.dp,
                    legendaryCustomization = LegendaryCustomization(
                        customBadge = customBadge == true,
                        legendaryStyle = selectedStyle,
                    ),
                    fontSize = 20.sp,
                )
            }

            if (state.membership != null) {
                PremiumBadge(
                    firstCohort = state.membership.cohort1,
                    secondCohort = state.membership.cohort2,
                    membershipExpired = state.membership.isExpired(),
                    legendaryStyle = selectedStyle ?: LegendaryStyle.NO_CUSTOMIZATION,
                )

                PrimalDivider(modifier = Modifier.padding(top = 16.dp))

                LegendaryColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 8.dp),
                    activeLegendaryStyle = selectedStyle ?: LegendaryStyle.NO_CUSTOMIZATION,
                    onStyleChanged = {
                        selectedStyle = it
                        eventPublisher(UiEvent.ApplyCustomization(style = it))
                    },
                )

                SwitchSettings(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    avatarRing = avatarGlow == true,
                    onAvatarRingChanged = {
                        avatarGlow = it
                        eventPublisher(UiEvent.ApplyCustomization(avatarGlow = it))
                    },
                    customBadge = customBadge == true,
                    onCustomBadgeChanged = {
                        customBadge = it
                        eventPublisher(UiEvent.ApplyCustomization(customBadge = it))
                    },
                    appearInLeaderboard = inLeaderboard == true,
                    onAppearInLeaderboardChanged = {
                        inLeaderboard = it
                        eventPublisher(UiEvent.ApplyCustomization(inLeaderboard = it))
                    },
                )

                Spacer(modifier = Modifier.height(24.dp))
                LegendCardShoutout(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onShoutoutChanged = { shoutout = it },
                    editMode = editMode,
                    onEditModeChange = { editMode = it },
                    textFieldState = textFieldState,
                )
                if (editsInReview) {
                    EditsInReviewBadge(modifier = Modifier.padding(bottom = 16.dp))
                }

                Text(
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    text = if (editsInReview) {
                        stringResource(id = R.string.premium_legend_profile_customization_notice_in_review)
                    } else {
                        stringResource(id = R.string.premium_legend_profile_customization_notice)
                    },
                    style = AppTheme.typography.bodySmall,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun LegendCardShoutout(
    modifier: Modifier = Modifier,
    textFieldState: TextFieldState,
    onShoutoutChanged: (String) -> Unit,
    editMode: Boolean,
    onEditModeChange: (Boolean) -> Unit,
) {
    Text(
        text = stringResource(id = R.string.premium_legend_profile_customization_card_shoutout).uppercase(),
        style = AppTheme.typography.bodyMedium,
        fontSize = 14.sp,
    )
    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text }.collect {
            if (it.length > SHOUTOUT_CHAR_LIMIT) {
                textFieldState.setTextAndPlaceCursorAtEnd(it.take(SHOUTOUT_CHAR_LIMIT).toString())
            } else {
                onShoutoutChanged(it.toString())
            }
        }
    }

    val bottomPadding = if (editMode) 28.dp else 12.dp

    BasicTextField(
        modifier = modifier
            .clip(AppTheme.shapes.medium)
            .background(AppTheme.extraColorScheme.surfaceVariantAlt1),
        readOnly = !editMode,
        state = textFieldState,
        textStyle = AppTheme.typography.bodyMedium.copy(
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            textAlign = TextAlign.Center,
        ),
        decorator = { innerTextField ->
            Box(
                modifier = modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 28.dp, bottom = bottomPadding)
                    .clip(AppTheme.shapes.medium)
                    .background(AppTheme.extraColorScheme.surfaceVariantAlt1),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
                ) {
                    innerTextField()

                    AnimatedVisibility(visible = !editMode) {
                        TextButton(
                            onClick = { onEditModeChange(true) },
                        ) {
                            Text(
                                text = stringResource(id = R.string.premium_legend_profile_customization_suggest_edits),
                                color = AppTheme.colorScheme.secondary,
                                style = AppTheme.typography.bodySmall,
                                fontSize = 14.sp,
                            )
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun EditsInReviewBadge(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier
            .background(AppTheme.extraColorScheme.surfaceVariantAlt2)
            .padding(vertical = 4.dp),
        text = stringResource(id = R.string.premium_legend_profile_customization_edits_under_review).uppercase(),
        color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        style = AppTheme.typography.bodyMedium,
        fontWeight = FontWeight.ExtraBold,
        textAlign = TextAlign.Center,
        fontSize = 13.sp,
    )
}

@Composable
private fun LegendaryColorPicker(
    modifier: Modifier,
    activeLegendaryStyle: LegendaryStyle,
    onStyleChanged: (LegendaryStyle) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LegendaryStyleColorBox(
                previewStyle = LegendaryStyle.NO_CUSTOMIZATION,
                activeStyle = activeLegendaryStyle,
                onClick = onStyleChanged,
            )

            LegendaryStyleColorBox(
                previewStyle = LegendaryStyle.GOLD,
                activeStyle = activeLegendaryStyle,
                onClick = onStyleChanged,
            )

            LegendaryStyleColorBox(
                previewStyle = LegendaryStyle.AQUA,
                activeStyle = activeLegendaryStyle,
                onClick = onStyleChanged,
            )

            LegendaryStyleColorBox(
                previewStyle = LegendaryStyle.SILVER,
                activeStyle = activeLegendaryStyle,
                onClick = onStyleChanged,
            )

            LegendaryStyleColorBox(
                previewStyle = LegendaryStyle.PURPLE,
                activeStyle = activeLegendaryStyle,
                onClick = onStyleChanged,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LegendaryStyleColorBox(
                previewStyle = LegendaryStyle.PURPLE_HAZE,
                activeStyle = activeLegendaryStyle,
                onClick = onStyleChanged,
            )

            LegendaryStyleColorBox(
                previewStyle = LegendaryStyle.TEAL,
                activeStyle = activeLegendaryStyle,
                onClick = onStyleChanged,
            )

            LegendaryStyleColorBox(
                previewStyle = LegendaryStyle.BROWN,
                activeStyle = activeLegendaryStyle,
                onClick = onStyleChanged,
            )

            LegendaryStyleColorBox(
                previewStyle = LegendaryStyle.BLUE,
                activeStyle = activeLegendaryStyle,
                onClick = onStyleChanged,
            )

            LegendaryStyleColorBox(
                previewStyle = LegendaryStyle.SUN_FIRE,
                activeStyle = activeLegendaryStyle,
                onClick = onStyleChanged,
            )
        }
    }
}

@Composable
private fun LegendaryStyleColorBox(
    previewStyle: LegendaryStyle,
    activeStyle: LegendaryStyle,
    onClick: (LegendaryStyle) -> Unit,
) {
    Box(
        modifier = Modifier
            .clickable { onClick(previewStyle) }
            .border(
                width = 3.dp,
                color = if (previewStyle == activeStyle) {
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
                .background(brush = previewStyle.primaryBrush, shape = CircleShape)
                .size(36.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (previewStyle == LegendaryStyle.NO_CUSTOMIZATION) {
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
    appearInLeaderboard: Boolean,
    onAppearInLeaderboardChanged: (Boolean) -> Unit,
) {
    Column(
        modifier = modifier.background(
            color = AppTheme.extraColorScheme.surfaceVariantAlt3,
            shape = AppTheme.shapes.large,
        ),
    ) {
        SwitchSettingsSingleRow(
            text = stringResource(R.string.premium_legend_profile_custom_badge),
            checked = customBadge,
            onCheckedChange = onCustomBadgeChanged,
        )

        PrimalDivider()

        SwitchSettingsSingleRow(
            text = stringResource(R.string.premium_legend_profile_avatar_ring),
            checked = avatarRing,
            onCheckedChange = onAvatarRingChanged,
        )

        PrimalDivider()

        SwitchSettingsSingleRow(
            text = stringResource(R.string.premium_legend_profile_appear_in_leaderboard),
            checked = appearInLeaderboard,
            onCheckedChange = onAppearInLeaderboardChanged,
        )
    }
}

@Composable
private fun SwitchSettingsSingleRow(
    modifier: Modifier = Modifier,
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = text,
            fontSize = 16.sp,
            style = AppTheme.typography.bodyMedium,
        )
        PrimalSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun BottomBarButtons(
    onSendClick: () -> Unit,
    onCancelClick: () -> Unit,
    shoutoutLength: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 4.dp)
            .background(AppTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "$shoutoutLength/$SHOUTOUT_CHAR_LIMIT",
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            style = AppTheme.typography.bodySmall,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onCancelClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 2.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.premium_legend_profile_customization_cancel),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colorScheme.onPrimary,
                )
            }
            Button(
                onClick = onSendClick,
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 2.dp),
            ) {
                Text(
                    text = stringResource(id = R.string.premium_legend_profile_customization_send),
                    style = AppTheme.typography.bodyMedium,
                    color = Color.White,
                )
            }
        }
    }
}
