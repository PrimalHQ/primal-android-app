package net.primal.android.settings.appearance

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import kotlin.time.Duration.Companion.minutes
import net.primal.android.LocalContentDisplaySettings
import net.primal.android.R
import net.primal.android.attachments.domain.CdnImage
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSwitch
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.feed.model.EventStatsUi
import net.primal.android.core.compose.feed.model.FeedPostUi
import net.primal.android.core.compose.feed.note.FeedNoteCard
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.icons.primaliconpack.FontSize
import net.primal.android.core.compose.settings.SettingsItem
import net.primal.android.settings.appearance.AppearanceSettingsContract.UiEvent
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme
import net.primal.android.user.domain.ContentDisplaySettings
import net.primal.android.user.domain.NoteAppearance

@Composable
fun AppearanceSettingsScreen(viewModel: AppearanceSettingsViewModel, onClose: () -> Unit) {
    val uiState = viewModel.state.collectAsState()
    AppearanceSettingsScreen(
        state = uiState.value,
        onClose = onClose,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    state: AppearanceSettingsContract.UiState,
    onClose: () -> Unit,
    eventPublisher: (UiEvent) -> Unit,
) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.settings_appearance_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                ThemeSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    themes = state.themes,
                    selectedThemeName = state.selectedThemeName,
                    onThemeChange = {
                        eventPublisher(UiEvent.ChangeTheme(themeName = it))
                    },
                )

                SettingsItem(
                    headlineText = stringResource(id = R.string.settings_appearance_auto_adjust_dark_mode),
                    supportText = stringResource(id = R.string.settings_appearance_auto_adjust_dark_mode_hint),
                    trailingContent = {
                        PrimalSwitch(
                            checked = state.selectedThemeName.isNullOrEmpty(),
                            onCheckedChange = {
                                eventPublisher(
                                    UiEvent.ToggleAutoAdjustDarkTheme(
                                        enabled = it,
                                        isSystemInDarkTheme = isSystemInDarkTheme,
                                    ),
                                )
                            },
                        )
                    },
                )

                PrimalDivider(modifier = Modifier.padding(horizontal = 8.dp))

                FontSizeSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    onNoteAppearanceChanged = {
                        eventPublisher(UiEvent.ChangeNoteAppearance(noteAppearance = it))
                    },
                )

                PrimalDivider(modifier = Modifier.padding(horizontal = 8.dp))

                NotePreviewSection(
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
    )
}

@Composable
private fun ThemeSection(
    modifier: Modifier = Modifier,
    themes: List<PrimalTheme>,
    selectedThemeName: String?,
    onThemeChange: (String) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(id = R.string.settings_appearance_theme_section_title).uppercase(),
            fontWeight = FontWeight.W500,
            fontSize = 14.sp,
            lineHeight = 16.sp,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            themes.forEach { primalTheme ->
                ThemeBox(
                    primalTheme = primalTheme,
                    selectedThemeName = selectedThemeName,
                    onThemeChange = onThemeChange,
                )
            }
        }
    }
}

@Composable
private fun ThemeBox(
    primalTheme: PrimalTheme,
    selectedThemeName: String?,
    onThemeChange: (String) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val selected = primalTheme.themeName == selectedThemeName
        val borderBrush = if (selected) {
            Brush.linearGradient(
                colors = listOf(
                    primalTheme.colorScheme.primary,
                    primalTheme.colorScheme.primary,
                ),
            )
        } else {
            if (primalTheme.isDarkTheme) {
                Brush.linearGradient(
                    colors = listOf(
                        primalTheme.colorScheme.outline,
                        primalTheme.colorScheme.outline,
                    ),
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        primalTheme.colorScheme.outline,
                        primalTheme.colorScheme.outline,
                    ),
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(AppTheme.shapes.small)
                .border(
                    width = 1.dp,
                    brush = borderBrush,
                    shape = AppTheme.shapes.small,
                )
                .clickable { onThemeChange(primalTheme.themeName) }
                .background(color = if (primalTheme.isDarkTheme) Color.Black else Color.White)
                .size(72.dp),
        ) {
            Image(
                modifier = Modifier.align(alignment = Alignment.Center),
                painter = painterResource(id = primalTheme.accent.logoId),
                contentDescription = primalTheme.name,
            )

            if (selected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(topStart = 8.dp))
                        .background(color = AppTheme.colorScheme.primary)
                        .size(16.dp)
                        .align(alignment = Alignment.BottomEnd),
                ) {
                    Icon(
                        modifier = Modifier
                            .size(14.dp)
                            .align(alignment = Alignment.Center),
                        imageVector = Icons.Default.Check,
                        tint = Color.White,
                        contentDescription = null,
                    )
                }
            }
        }

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = primalTheme.themeName,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontSizeSection(modifier: Modifier, onNoteAppearanceChanged: (NoteAppearance) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val contentDisplaySettings = LocalContentDisplaySettings.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(id = R.string.settings_appearance_font_section_title).uppercase(),
            fontWeight = FontWeight.W500,
            fontSize = 14.sp,
            lineHeight = 16.sp,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(22.dp),
                imageVector = PrimalIcons.FontSize,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
            FontSizeSlider(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                initialNoteAppearance = contentDisplaySettings.noteAppearance,
                onNoteAppearanceChanged = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNoteAppearanceChanged(it)
                },
            )
            Icon(
                modifier = Modifier.size(26.dp),
                imageVector = PrimalIcons.FontSize,
                contentDescription = null,
                tint = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }
    }
}

@ExperimentalMaterial3Api
@Composable
private fun FontSizeSlider(
    modifier: Modifier,
    initialNoteAppearance: NoteAppearance,
    onNoteAppearanceChanged: (NoteAppearance) -> Unit,
) {
    val state by remember {
        mutableStateOf(
            SliderState(
                value = initialNoteAppearance.asFloat(),
                steps = 2,
            ),
        )
    }

    var initialValueChange by remember { mutableStateOf(true) }

    LaunchedEffect(state.value) {
        if (!initialValueChange) {
            val noteAppearance = state.value.toNoteAppearance()
            onNoteAppearanceChanged(noteAppearance)
        } else {
            initialValueChange = false
        }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val colors = SliderDefaults.colors(
        activeTrackColor = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
        inactiveTrackColor = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
    )

    Slider(
        modifier = modifier,
        state = state,
        colors = colors,
        interactionSource = interactionSource,
        thumb = {
            SliderDefaults.Thumb(
                interactionSource = interactionSource,
                colors = colors,
                enabled = true,
            )
        },
        track = { FontSizeTrack() },
    )
}

private val TrackHeight = 1.dp
private val TickSize = 6.dp
private const val FractionSmallFontSize = 0.00f
private const val FractionDefaultFontSize = 0.33f
private const val FractionLargeFontSize = 0.66f
private const val FractionExtraLargeFontSize = 1.00f
private const val FractionThresholdValue = 0.10f

@Composable
@ExperimentalMaterial3Api
private fun FontSizeTrack() {
    val trackColor = AppTheme.extraColorScheme.onSurfaceVariantAlt4
    Canvas(modifier = Modifier.fillMaxWidth()) {
        drawTrack(
            tickFractions = floatArrayOf(
                FractionSmallFontSize,
                FractionDefaultFontSize,
                FractionLargeFontSize,
                FractionExtraLargeFontSize,
            ),
            color = trackColor,
        )
    }
}

private fun NoteAppearance.asFloat(): Float {
    return when (this) {
        NoteAppearance.Small -> FractionSmallFontSize
        NoteAppearance.Default -> FractionDefaultFontSize
        NoteAppearance.Large -> FractionLargeFontSize
        NoteAppearance.ExtraLarge -> FractionExtraLargeFontSize
    }
}

private fun Float.aroundFraction(range: Float = FractionThresholdValue): ClosedFloatingPointRange<Float> {
    return (this - range)..(this + range)
}

private fun Float.toNoteAppearance(): NoteAppearance {
    return when (this) {
        in FractionSmallFontSize.aroundFraction() -> NoteAppearance.Small
        in FractionDefaultFontSize.aroundFraction() -> NoteAppearance.Default
        in FractionLargeFontSize.aroundFraction() -> NoteAppearance.Large
        else -> NoteAppearance.ExtraLarge
    }
}

private fun DrawScope.drawTrack(tickFractions: FloatArray, color: Color) {
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val sliderLeft = Offset(0f, center.y)
    val sliderRight = Offset(size.width, center.y)
    val sliderStart = if (isRtl) sliderRight else sliderLeft
    val sliderEnd = if (isRtl) sliderLeft else sliderRight
    val tickSize = TickSize.toPx()
    val trackStrokeWidth = TrackHeight.toPx()
    drawLine(
        color,
        sliderStart,
        sliderEnd,
        trackStrokeWidth,
        StrokeCap.Round,
    )
    val sliderValueEnd = Offset(
        sliderStart.x +
            (sliderEnd.x - sliderStart.x) * 1.0f,
        center.y,
    )

    val sliderValueStart = Offset(
        sliderStart.x +
            (sliderEnd.x - sliderStart.x) * 0.0f,
        center.y,
    )

    drawLine(
        color,
        sliderValueStart,
        sliderValueEnd,
        trackStrokeWidth,
        StrokeCap.Round,
    )

    for (tick in tickFractions) {
        drawCircle(
            color = color,
            center = Offset(lerp(sliderStart, sliderEnd, tick).x, center.y),
            radius = tickSize / 2f,
        )
    }
}

@Composable
private fun NotePreviewSection(modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            text = stringResource(id = R.string.settings_appearance_note_preview_section_title).uppercase(),
            fontWeight = FontWeight.W500,
            fontSize = 14.sp,
            lineHeight = 16.sp,
        )

        FeedNoteCard(
            modifier = Modifier.padding(horizontal = 4.dp),
            data = NotePreviewTemplate,
            noteOptionsMenuEnabled = false,
            onArticleClick = {},
        )
    }
}

private val NotePreviewTemplate = FeedPostUi(
    postId = "random",
    authorId = "author",
    authorHandle = "",
    authorName = "preston",
    authorInternetIdentifier = "preston@primal.net",
    authorAvatarCdnImage = CdnImage(
        sourceUrl = "https://primal.b-cdn.net/media-cache?s=o&a=1&u=https%3A%2F%2Fi.imgur.com%2FXf8iV9G.gif",
    ),
    content = "Welcome to #Nostr! A magical place where you can speak " +
        "freely and truly own your account, content, and followers. ✨",
    hashtags = listOf("#Nostr"),
    stats = EventStatsUi(
        repliesCount = 21,
        satsZapped = 441,
        userZapped = true,
        likesCount = 63,
        userLiked = true,
        repostsCount = 42,
        userReposted = true,
    ),
    timestamp = Instant.now().minusSeconds(18.minutes.inWholeSeconds),
    rawNostrEventJson = "",
)

class AppearanceSettingsUiStateProvider :
    PreviewParameterProvider<AppearanceSettingsContract.UiState> {
    override val values: Sequence<AppearanceSettingsContract.UiState>
        get() = PrimalTheme.entries.map {
            return@map AppearanceSettingsContract.UiState(
                selectedThemeName = it.themeName,
                themes = PrimalTheme.entries,
            )
        }.asSequence()
}

@Preview
@Composable
fun PreviewAppearanceSettingsScreen(
    @PreviewParameter(AppearanceSettingsUiStateProvider::class)
    state: AppearanceSettingsContract.UiState,
) {
    CompositionLocalProvider(
        LocalContentDisplaySettings provides ContentDisplaySettings(),
    ) {
        checkNotNull(state.selectedThemeName)
        PrimalTheme(primalTheme = PrimalTheme.valueOf(themeName = state.selectedThemeName)!!) {
            AppearanceSettingsScreen(
                state = state,
                onClose = {},
                eventPublisher = {},
            )
        }
    }
}
