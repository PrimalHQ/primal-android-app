package net.primal.android.explore.asearch.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults.Container
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalSliderThumb
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.dropdown.DropdownPrimalMenu
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.ext.onFocusSelectAll
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
        modifier = modifier.statusBarsPadding(),
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppTheme.extraColorScheme.surfaceVariantAlt2),
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.asearch_filter_sheet_title),
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
                ),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                if (searchKind.isReads()) {
                    SliderColumn(
                        label = stringResource(id = R.string.asearch_filter_min_read_time),
                        value = filterState.minReadTime,
                        onValueChange = { filterState = filterState.copy(minReadTime = it) },
                        maxValue = 20,
                    )
                    SliderColumn(
                        label = stringResource(id = R.string.asearch_filter_max_read_time),
                        value = filterState.maxReadTime,
                        onValueChange = { filterState = filterState.copy(maxReadTime = it) },
                        maxValue = 20,
                    )
                }
                if (searchKind.isImages() || searchKind.isVideos()) {
                    OrientationRow(
                        orientation = filterState.orientation,
                        onOrientationSelected = { filterState = filterState.copy(orientation = it) },
                    )
                }
                if (searchKind.isVideos() || searchKind.isSound()) {
                    SliderColumn(
                        label = stringResource(id = R.string.asearch_filter_min_duration),
                        value = filterState.minDuration,
                        onValueChange = { filterState = filterState.copy(minDuration = it) },
                        maxValue = 600,
                    )
                    SliderColumn(
                        label = stringResource(id = R.string.asearch_filter_max_duration),
                        value = filterState.maxDuration,
                        onValueChange = { filterState = filterState.copy(maxDuration = it) },
                        maxValue = 600,
                    )
                }
                CommonSliders(
                    filterState = filterState,
                    onFilterStateChange = { reducer -> filterState = filterState.reducer() },
                )
            }

            FilterPickerBottomBar(
                sheetState = sheetState,
                onDismissRequest = onDismissRequest,
                onFiltersSelected = { filterSelected(filterState) },
            )
        }
    }
}

@Composable
private fun CommonSliders(
    filterState: AdvancedSearchContract.SearchFilter,
    onFilterStateChange: (AdvancedSearchContract.SearchFilter.() -> AdvancedSearchContract.SearchFilter) -> Unit,
) {
    Column {
        SliderColumn(
            label = stringResource(id = R.string.asearch_filter_min_content_score),
            value = filterState.minContentScore,
            onValueChange = { onFilterStateChange { copy(minContentScore = it) } },
            maxValue = 100,
        )
        SliderColumn(
            label = stringResource(id = R.string.asearch_filter_min_interactions),
            value = filterState.minInteractions,
            onValueChange = { onFilterStateChange { copy(minInteractions = it) } },
            maxValue = 100,
        )
        SliderColumn(
            label = stringResource(id = R.string.asearch_filter_min_likes),
            value = filterState.minLikes,
            onValueChange = { onFilterStateChange { copy(minLikes = it) } },
            maxValue = 20,
        )
        SliderColumn(
            label = stringResource(id = R.string.asearch_filter_min_zaps),
            value = filterState.minZaps,
            onValueChange = { onFilterStateChange { copy(minZaps = it) } },
            maxValue = 20,
        )
        SliderColumn(
            label = stringResource(id = R.string.asearch_filter_min_replies),
            value = filterState.minReplies,
            onValueChange = { onFilterStateChange { copy(minReplies = it) } },
            maxValue = 20,
        )
        SliderColumn(
            label = stringResource(id = R.string.asearch_filter_min_reposts),
            value = filterState.minReposts,
            onValueChange = { onFilterStateChange { copy(minReposts = it) } },
            maxValue = 20,
        )
    }
}

@Composable
private fun OrientationRow(
    orientation: AdvancedSearchContract.Orientation?,
    onOrientationSelected: (AdvancedSearchContract.Orientation) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
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
        modifier = Modifier.background(AppTheme.extraColorScheme.surfaceVariantAlt2),
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
        modifier = modifier.fillMaxWidth(),
    ) {
        Text(
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
                modifier = Modifier.weight(1.0f),
                interactionSource = interactionSource,
                colors = sliderColors,
                track = {
                    SliderDefaults.Track(
                        sliderState = it,
                        modifier = Modifier.scale(scaleX = 1f, scaleY = 0.5f),
                        colors = sliderColors,
                    )
                },
                thumb = {
                    PrimalSliderThumb(
                        interactionSource = interactionSource,
                        colors = sliderColors,
                    )
                },
                value = value.toFloat(),
                onValueChange = { onValueChange(it.toInt()) },
                steps = maxValue,
                valueRange = minValue.toFloat()..maxValue.toFloat(),
            )
            Spacer(modifier = Modifier.width(10.dp))

            SliderIndicatorField(
                interactionSource = interactionSource,
                value = value,
                onValueChange = onValueChange,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SliderIndicatorField(
    interactionSource: MutableInteractionSource,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    val basicTextFieldValue = remember { mutableStateOf(TextFieldValue(text = value.toString())) }

    LaunchedEffect(value) {
        basicTextFieldValue.value = basicTextFieldValue.value.copy(text = value.toString())
    }

    val keyboardVisibility by keyboardVisibilityAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(keyboardVisibility) {
        if (!keyboardVisibility) {
            focusManager.clearFocus()
        }
    }

    val textColor = if (value == 0) {
        AppTheme.extraColorScheme.onSurfaceVariantAlt4
    } else {
        AppTheme.colorScheme.onPrimary
    }
    val containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1

    val colors = TextFieldDefaults.colors(
        focusedIndicatorColor = containerColor,
        unfocusedIndicatorColor = containerColor,
        focusedContainerColor = containerColor,
        unfocusedContainerColor = containerColor,
        disabledContainerColor = containerColor,
        focusedTextColor = textColor,
        unfocusedTextColor = textColor,
    )

    BasicTextField(
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                colors = colors,
                singleLine = true,
                enabled = true,
                innerTextField = innerTextField,
                interactionSource = interactionSource,
                value = basicTextFieldValue.value.text,
                visualTransformation = VisualTransformation.None,
                contentPadding = PaddingValues(start = 10.dp, end = 16.dp),
                container = {
                    Container(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = colors,
                        shape = RoundedCornerShape(percent = 50),
                        focusedBorderThickness = 0.dp,
                        unfocusedBorderThickness = 0.dp,
                    )
                },
            )
        },
        textStyle = AppTheme.typography.bodyLarge.copy(color = textColor, textAlign = TextAlign.End),
        modifier = Modifier
            .height(42.dp)
            .width(72.dp)
            .focusRequester(focusRequester)
            .onFocusSelectAll(basicTextFieldValue),
        value = basicTextFieldValue.value,
        singleLine = true,
        onValueChange = {
            if (!it.text.isDigitsOnly()) return@BasicTextField

            basicTextFieldValue.value = it
            runCatching {
                onValueChange(
                    it.text
                        .ifEmpty { "0" }
                        .toInt()
                        .coerceAtLeast(0),
                )
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
        keyboardActions = KeyboardActions(
            onDone = { keyboardController?.hide() },
        ),
    )
}

@Composable
private fun OrientationDropDownMenu(
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
