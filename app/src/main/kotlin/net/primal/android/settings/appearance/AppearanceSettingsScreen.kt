package net.primal.android.settings.appearance

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme
import net.primal.android.theme.PrimalTheme
import net.primal.android.theme.domain.PrimalTheme

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
    eventPublisher: (AppearanceSettingsContract.UiEvent) -> Unit,
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = "Appearance",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = onClose,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .imePadding(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
            ) {
                ThemeSection(state = state, eventPublisher = eventPublisher)
                PrimalDivider()
            }
        },
    )
}

@Composable
private fun ThemeSection(
    state: AppearanceSettingsContract.UiState,
    eventPublisher: (AppearanceSettingsContract.UiEvent) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(
                id = R.string.settings_appearance_theme_section_title,
            ).uppercase(),
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
            state.themes.forEach { primalTheme ->
                ThemeBox(
                    primalTheme = primalTheme,
                    state = state,
                    eventPublisher = eventPublisher,
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ThemeBox(
    primalTheme: PrimalTheme,
    state: AppearanceSettingsContract.UiState,
    eventPublisher: (AppearanceSettingsContract.UiEvent) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val selected = primalTheme.themeName == state.selectedThemeName
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
                .clickable {
                    eventPublisher(
                        AppearanceSettingsContract.UiEvent.SelectedThemeChanged(
                            themeName = primalTheme.themeName,
                        ),
                    )
                }
                .background(color = if (primalTheme.isDarkTheme) Color.Black else Color.White)
                .size(72.dp),
        ) {
            Image(
                modifier = Modifier.align(alignment = Alignment.Center),
                painter = painterResource(id = primalTheme.accent.logoId),
                contentDescription = null,
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
            text = primalTheme.themeName,
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 16.sp,
            textAlign = TextAlign.Center,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
    }
}

class AppearanceSettingsUiStateProvider :
    PreviewParameterProvider<AppearanceSettingsContract.UiState> {
    override val values: Sequence<AppearanceSettingsContract.UiState>
        get() = PrimalTheme.values().map {
            return@map AppearanceSettingsContract.UiState(
                selectedThemeName = it.themeName,
                themes = PrimalTheme.values().toList(),
            )
        }.asSequence()
}

@Preview
@Composable
fun PreviewAppearanceSettingsScreen(
    @PreviewParameter(AppearanceSettingsUiStateProvider::class)
    state: AppearanceSettingsContract.UiState,
) {
    PrimalTheme(primalTheme = PrimalTheme.valueOf(themeName = state.selectedThemeName)!!) {
        AppearanceSettingsScreen(
            state = state,
            onClose = {},
            eventPublisher = {},
        )
    }
}
