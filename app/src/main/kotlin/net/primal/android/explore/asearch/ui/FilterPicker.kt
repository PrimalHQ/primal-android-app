package net.primal.android.explore.asearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.explore.asearch.AdvancedSearchContract
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterPicker(
    modifier: Modifier = Modifier,
    searchKind: AdvancedSearchContract.SearchKind,
    onDismissRequest: () -> Unit,
    filterSelected: (AdvancedSearchContract.SearchFilter) -> Unit,
    startState: AdvancedSearchContract.SearchFilter,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    var filterState by remember { mutableStateOf(startState) }

    ModalBottomSheet(
        tonalElevation = 0.dp,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = stringResource(id = R.string.asearch_filter_sheet_title),
                            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                        )
                    },
                )
            },
            bottomBar = {
                FilterPickerBottomBar(
                    sheetState = sheetState,
                    onDismissRequest = onDismissRequest,
                    onFiltersSelected = { filterSelected(filterState) },
                )
            },

        ) { paddingValues ->
            val scrollState = rememberScrollState()
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(scrollState)
                    .padding(paddingValues = paddingValues),
            ) {
                if (searchKind.isImages() || searchKind.isVideos()) {
                    OrientationDropDownMenu(
                        orientation = filterState.orientation,
                        onOrientationSelected = { filterState = filterState.copy(orientation = it) },
                    )
                }
                if (searchKind.isVideos() || searchKind.isSound()) {
                    SliderColumn(
                        label = stringResource(id = R.string.asearch_filter_min_duration),
                        value = filterState.minDuration,
                        onValueChange = { filterState = filterState.copy(minDuration = it) },
                    )
                    SliderColumn(
                        label = stringResource(id = R.string.asearch_filter_max_duration),
                        value = filterState.maxDuration,
                        onValueChange = { filterState = filterState.copy(maxDuration = it) },
                    )
                }
                CommonSliders(
                    filterState = filterState,
                    onFilterStateChange = { reducer -> filterState = filterState.reducer() },
                )
            }
        }
    }
}

@Composable
private fun CommonSliders(
    filterState: AdvancedSearchContract.SearchFilter,
    onFilterStateChange: (AdvancedSearchContract.SearchFilter.() -> AdvancedSearchContract.SearchFilter) -> Unit,
) {
    SliderColumn(
        label = stringResource(id = R.string.asearch_filter_min_content_score),
        value = filterState.minContentScore,
        onValueChange = { onFilterStateChange { copy(minContentScore = it) } },
    )
    SliderColumn(
        label = stringResource(id = R.string.asearch_filter_min_interactions),
        value = filterState.minInteractions,
        onValueChange = { onFilterStateChange { copy(minInteractions = it) } },
    )
    SliderColumn(
        label = stringResource(id = R.string.asearch_filter_min_likes),
        value = filterState.minLikes,
        onValueChange = { onFilterStateChange { copy(minLikes = it) } },
    )
    SliderColumn(
        label = stringResource(id = R.string.asearch_filter_min_zaps),
        value = filterState.minZaps,
        onValueChange = { onFilterStateChange { copy(minZaps = it) } },
    )
    SliderColumn(
        label = stringResource(id = R.string.asearch_filter_min_replies),
        value = filterState.minReplies,
        onValueChange = { onFilterStateChange { copy(minReplies = it) } },
    )
    SliderColumn(
        label = stringResource(id = R.string.asearch_filter_min_reposts),
        value = filterState.minReposts,
        onValueChange = { onFilterStateChange { copy(minReposts = it) } },
    )
}

@Composable
private fun OrientationDropDownMenu(
    orientation: AdvancedSearchContract.Orientation?,
    onOrientationSelected: (AdvancedSearchContract.Orientation) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(id = R.string.asearch_filter_orientation),
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            style = AppTheme.typography.bodyMedium,
        )
        OrientationDropDownMenu(
            currentOrientation = orientation ?: AdvancedSearchContract.Orientation.Any,
            onOrientationSelected = onOrientationSelected,
        )
    }
    PrimalDivider()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterPickerBottomBar(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onFiltersSelected: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier.background(AppTheme.colorScheme.background),
    ) {
        PrimalLoadingButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = stringResource(id = R.string.asearch_filter_apply_button),
            onClick = {
                scope.launch { sheetState.hide() }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onDismissRequest()
                        }
                        onFiltersSelected()
                    }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SliderColumn(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = 0,
    maxValue: Int = 1000,
) {
    val interactionSource = remember { MutableInteractionSource() }

    val sliderColors = SliderDefaults.colors(
        thumbColor = if (value == 0) {
            AppTheme.colorScheme.outline
        } else {
            AppTheme.extraColorScheme.onSurfaceVariantAlt2
        },
        activeTrackColor = AppTheme.colorScheme.tertiary,
        activeTickColor = AppTheme.colorScheme.tertiary,
        inactiveTrackColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        inactiveTickColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
    )

    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        Text(
            modifier = Modifier.padding(start = 8.dp),
            text = label,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            style = AppTheme.typography.bodyMedium,
        )
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Slider(
                modifier = Modifier.fillMaxWidth(fraction = 0.8f),
                interactionSource = interactionSource,
                colors = sliderColors,
                track = {
                    SliderDefaults.Track(
                        sliderState = it,
                        modifier = Modifier.scale(scaleX = 1f, scaleY = 1.5f),
                        colors = sliderColors,
                    )
                },
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                steps = maxValue,
                valueRange = minValue.toFloat()..maxValue.toFloat(),
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .width(72.dp)
                    .height(42.dp)
                    .background(AppTheme.extraColorScheme.surfaceVariantAlt1),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Text(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .padding(vertical = 8.dp),
                    maxLines = 1,
                    text = value.toString(),
                    color = if (value == 0) {
                        AppTheme.extraColorScheme.onSurfaceVariantAlt4
                    } else {
                        AppTheme.colorScheme.onPrimary
                    },
                )
            }
        }
    }
}

@Composable
fun OrientationDropDownMenu(
    modifier: Modifier = Modifier,
    currentOrientation: AdvancedSearchContract.Orientation,
    onOrientationSelected: (AdvancedSearchContract.Orientation) -> Unit,
) {
    var menuVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { menuVisible = true },
        ),
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .height(42.dp)
                .background(AppTheme.extraColorScheme.surfaceVariantAlt1),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                maxLines = 1,
                text = currentOrientation.toString(),
                color = AppTheme.colorScheme.onPrimary,
            )
        }

        DropdownPrimalMenu(
            offset = DpOffset(x = 0.dp, y = 4.dp),
            expanded = menuVisible,
            onDismissRequest = { menuVisible = false },
        ) {
            OrientationMenuItem(
                text = stringResource(id = R.string.asearch_filter_orientation_any),
                onClick = {
                    onOrientationSelected(AdvancedSearchContract.Orientation.Any)
                    menuVisible = false
                },
            )
            OrientationMenuItem(
                text = stringResource(id = R.string.asearch_filter_orientation_horizontal),
                onClick = {
                    onOrientationSelected(AdvancedSearchContract.Orientation.Horizontal)
                    menuVisible = false
                },
            )
            OrientationMenuItem(
                text = stringResource(id = R.string.asearch_filter_orientation_vertical),
                onClick = {
                    onOrientationSelected(AdvancedSearchContract.Orientation.Vertical)
                    menuVisible = false
                },
            )
        }
    }
}

@Composable
fun OrientationMenuItem(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = AppTheme.colorScheme.onPrimary,
    onClick: () -> Unit,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = PaddingValues(
        horizontal = 16.dp,
        vertical = 0.dp,
    ),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    DropdownMenuItem(
        modifier = modifier,
        text = {
            Text(
                modifier = Modifier
                    .padding(start = 8.dp),
                text = text,
                color = textColor,
                style = AppTheme.typography.bodyMedium,
            )
        },
        onClick = onClick,
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
    )
}
