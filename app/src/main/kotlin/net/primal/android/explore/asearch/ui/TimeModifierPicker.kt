package net.primal.android.explore.asearch.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.format.FormatStyle
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.utils.formatToDefaultDateFormat
import net.primal.android.explore.asearch.AdvancedSearchContract
import net.primal.android.explore.asearch.toDisplayName
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeModifierPicker(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onItemSelected: (AdvancedSearchContract.TimeModifier) -> Unit,
    selectedItem: AdvancedSearchContract.TimeModifier? = null,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
) {
    var selectState by remember { mutableStateOf(TimeModifierPickerState.All) }

    ModalBottomSheet(
        tonalElevation = 0.dp,
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = if (selectState.isCustom()) {
                        stringResource(id = R.string.asearch_custom_time_posted_label)
                    } else {
                        stringResource(id = R.string.asearch_time_posted_label)
                    },
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
                )
            },
            navigationIcon = {
                if (selectState.isCustom()) {
                    IconButton(
                        onClick = { selectState = TimeModifierPickerState.All },
                    ) {
                        Icon(imageVector = PrimalIcons.ArrowBack, contentDescription = null)
                    }
                }
            },
        )
        AnimatedContent(
            targetState = selectState,
            transitionSpec = { transitionSpecBetweenStages() },
            label = "TimeModifierPicker",
        ) {
            when (it) {
                TimeModifierPickerState.Custom -> {
                    CustomPickerStateContent(
                        sheetState = sheetState,
                        onDismissRequest = onDismissRequest,
                        onCancelClick = { selectState = TimeModifierPickerState.All },
                        onCustomTimePicked = onItemSelected,
                        selectedItem = selectedItem,
                    )
                }

                TimeModifierPickerState.All -> {
                    AllPickerStateContent(
                        sheetState = sheetState,
                        onDismissRequest = onDismissRequest,
                        onItemSelected = onItemSelected,
                        selectedItem = selectedItem,
                        onCustomItemClicked = { selectState = TimeModifierPickerState.Custom },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AllPickerStateContent(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onItemSelected: (AdvancedSearchContract.TimeModifier) -> Unit,
    onCustomItemClicked: () -> Unit,
    selectedItem: AdvancedSearchContract.TimeModifier?,
) {
    val scope = rememberCoroutineScope()
    val timeModifiers: List<AdvancedSearchContract.TimeModifier> = listOf(
        AdvancedSearchContract.TimeModifier.Anytime,
        AdvancedSearchContract.TimeModifier.Today,
        AdvancedSearchContract.TimeModifier.Week,
        AdvancedSearchContract.TimeModifier.Month,
        AdvancedSearchContract.TimeModifier.Year,
    )
    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        timeModifiers.forEach { timeModifier ->
            TimeModifierListItem(
                onClick = {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismissRequest()
                            }
                            onItemSelected(timeModifier)
                        }
                },
                itemDisplayName = timeModifier.toDisplayName(),
                isSelected = selectedItem == timeModifier,
            )
        }
        TimeModifierListItem(
            onClick = onCustomItemClicked,
            itemDisplayName = stringResource(id = R.string.asearch_time_posted_custom),
            isSelected = selectedItem is AdvancedSearchContract.TimeModifier.Custom,
        )
        if (selectedItem is AdvancedSearchContract.TimeModifier.Custom) {
            ListItem(
                headlineContent = {
                    Text(
                        text = "${
                            selectedItem.startDate.formatToDefaultDateFormat(
                                FormatStyle.LONG,
                            )
                        } - ${selectedItem.endDate.formatToDefaultDateFormat(FormatStyle.LONG)}",
                        color = AppTheme.colorScheme.secondary,
                    )
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomPickerStateContent(
    sheetState: SheetState,
    onDismissRequest: () -> Unit,
    onCancelClick: () -> Unit,
    onCustomTimePicked: (AdvancedSearchContract.TimeModifier) -> Unit,
    selectedItem: AdvancedSearchContract.TimeModifier?,
) {
    val scope = rememberCoroutineScope()
    CustomTimePicker(
        onTimePicked = { start, end ->
            scope.launch { sheetState.hide() }
                .invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        onDismissRequest()
                    }
                    onCustomTimePicked(
                        AdvancedSearchContract.TimeModifier.Custom(
                            startDate = Instant.ofEpochMilli(start),
                            endDate = Instant.ofEpochMilli(end),
                        ),
                    )
                }
        },
        onDismissRequest = onCancelClick,
        selectedItem = selectedItem,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomTimePicker(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onTimePicked: (startMillis: Long, endMillis: Long) -> Unit,
    selectedItem: AdvancedSearchContract.TimeModifier?,
) {
    val dateRangePickerState = rememberDateRangePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return Instant.ofEpochMilli(utcTimeMillis) < Instant.now()
            }
        },
    )

    if (selectedItem is AdvancedSearchContract.TimeModifier.Custom) {
        dateRangePickerState.setSelection(
            startDateMillis = selectedItem.startDate.toEpochMilli(),
            endDateMillis = selectedItem.endDate.toEpochMilli(),
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.Top),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                PrimalLoadingButton(
                    enabled = dateRangePickerState.selectedStartDateMillis != null &&
                        dateRangePickerState.selectedEndDateMillis != null,
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(id = R.string.asearch_custom_time_posted_confirm),
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            onTimePicked(start, end)
                        }
                    },
                )
                TextButton(
                    modifier = Modifier
                        .height(58.dp)
                        .fillMaxWidth(),
                    onClick = { onDismissRequest() },
                ) {
                    Text(
                        text = stringResource(id = R.string.asearch_custom_time_posted_cancel),
                        style = AppTheme.typography.bodyLarge,
                    )
                }
            }
        },
    ) { paddingValues ->
        DateRangePicker(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            colors = DatePickerDefaults.colors(
                selectedDayContentColor = AppTheme.colorScheme.surfaceVariant,
            ),
            title = null,
            state = dateRangePickerState,
            showModeToggle = true,
        )
    }
}

@Composable
private fun TimeModifierListItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    itemDisplayName: String,
    isSelected: Boolean,
) {
    ListItem(
        modifier = modifier
            .clickable { onClick() },
        headlineContent = {
            Text(
                text = itemDisplayName,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt1,
            )
        },
        trailingContent = {
            if (isSelected) {
                Icon(imageVector = Icons.Filled.Check, contentDescription = null)
            }
        },
    )
    PrimalDivider()
}

private enum class TimeModifierPickerState {
    Custom,
    All,
    ;

    fun isCustom() = this == Custom
    fun isAll() = this == All
}

private fun AnimatedContentTransitionScope<TimeModifierPickerState>.transitionSpecBetweenStages() =
    when (initialState) {
        TimeModifierPickerState.Custom -> {
            slideInHorizontally(initialOffsetX = { -it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { it }))
        }

        TimeModifierPickerState.All -> {
            slideInHorizontally(initialOffsetX = { it })
                .togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
        }
    }
