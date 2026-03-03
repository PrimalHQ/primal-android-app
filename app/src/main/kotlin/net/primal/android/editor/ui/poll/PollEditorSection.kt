package net.primal.android.editor.ui.poll

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.foundation.keyboardVisibilityAsState
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.editor.NoteEditorContract
import net.primal.android.editor.NoteEditorContract.PollType
import net.primal.android.editor.NoteEditorViewModel.Companion.MAX_POLL_CHOICES
import net.primal.android.editor.NoteEditorViewModel.Companion.MIN_POLL_CHOICES
import net.primal.android.theme.AppTheme
import net.primal.android.theme.domain.PrimalTheme

private val choiceFieldShape = PollEditorDefaults.choiceFieldShape
private val startPadding = PollEditorDefaults.startPadding
private val endPadding = PollEditorDefaults.endPadding

private const val MAX_POLL_DAYS = 30
private const val MAX_POLL_HOURS = 23
private const val MAX_POLL_MINUTES = 59
private const val KEYBOARD_SETTLE_DELAY_MS = 200L

@Composable
fun PollEditorSection(
    pollState: NoteEditorContract.PollEditorState,
    eventPublisher: (NoteEditorContract.UiEvent) -> Unit,
    footerHeightPx: Int,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val keyboardVisible by keyboardVisibilityAsState()

    LaunchedEffect(keyboardVisible) {
        if (!keyboardVisible) {
            focusManager.clearFocus()
        }
    }

    var showPollTypeDialog by remember { mutableStateOf(false) }
    var showPollLengthPicker by remember { mutableStateOf(false) }
    var focusedChoiceId by remember { mutableStateOf<UUID?>(null) }

    if (showPollTypeDialog) {
        PollTypeDialog(
            selectedType = pollState.pollType,
            onTypeSelected = { type ->
                eventPublisher(NoteEditorContract.UiEvent.UpdatePollType(type))
                showPollTypeDialog = false
            },
            onDismiss = { showPollTypeDialog = false },
        )
    }

    Column(modifier = modifier) {
        PollChoicesSection(
            pollState = pollState,
            focusedChoiceId = focusedChoiceId,
            onFocusedChoiceIdChange = { focusedChoiceId = it },
            eventPublisher = eventPublisher,
        )

        Spacer(modifier = Modifier.height(8.dp))

        HorizontalDivider(
            modifier = Modifier.padding(start = startPadding, end = endPadding),
            color = AppTheme.colorScheme.outline,
        )

        Spacer(modifier = Modifier.height(8.dp))

        PollSettingsSection(
            pollState = pollState,
            showPollLengthPicker = showPollLengthPicker,
            onTogglePollLengthPicker = { showPollLengthPicker = !showPollLengthPicker },
            onShowPollTypeDialog = { showPollTypeDialog = true },
            eventPublisher = eventPublisher,
            footerHeightPx = footerHeightPx,
        )
    }
}

@Composable
private fun PollChoicesSection(
    pollState: NoteEditorContract.PollEditorState,
    focusedChoiceId: UUID?,
    onFocusedChoiceIdChange: (UUID?) -> Unit,
    eventPublisher: (NoteEditorContract.UiEvent) -> Unit,
) {
    pollState.choices.forEachIndexed { index, choice ->
        key(choice.id) {
            PollChoiceField(
                choice = choice,
                index = index,
                isFocused = focusedChoiceId == choice.id,
                canRemove = pollState.choices.size > MIN_POLL_CHOICES,
                onTextChange = { text ->
                    eventPublisher(NoteEditorContract.UiEvent.UpdatePollChoice(choice.id, text))
                },
                onFocusChange = { focused ->
                    if (focused) onFocusedChoiceIdChange(choice.id)
                },
                onRemove = {
                    eventPublisher(NoteEditorContract.UiEvent.RemovePollChoice(choice.id))
                },
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (pollState.choices.size < MAX_POLL_CHOICES) {
        Row(
            modifier = Modifier
                .padding(start = startPadding, end = endPadding)
                .clickable { eventPublisher(NoteEditorContract.UiEvent.AddPollChoice) }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(id = R.string.accessibility_poll_add_choice),
                modifier = Modifier.size(20.dp),
                tint = AppTheme.colorScheme.secondary,
            )
            Text(
                text = stringResource(id = R.string.poll_editor_add_choice),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.secondary,
            )
        }
    }
}

@Composable
private fun PollSettingsSection(
    pollState: NoteEditorContract.PollEditorState,
    showPollLengthPicker: Boolean,
    onTogglePollLengthPicker: () -> Unit,
    onShowPollTypeDialog: () -> Unit,
    eventPublisher: (NoteEditorContract.UiEvent) -> Unit,
    footerHeightPx: Int,
) {
    PollSettingRow(
        label = stringResource(id = R.string.poll_editor_poll_type),
        onClick = onShowPollTypeDialog,
        trailingContent = {
            PollTypeLabel(pollType = pollState.pollType)
        },
    )

    HorizontalDivider(
        modifier = Modifier.padding(
            start = startPadding,
            end = endPadding,
            top = 4.dp,
            bottom = 4.dp,
        ),
        color = AppTheme.colorScheme.outline,
    )

    PollSettingRow(
        label = stringResource(id = R.string.poll_editor_poll_length),
        onClick = onTogglePollLengthPicker,
        trailingContent = {
            Text(
                text = formatPollLength(
                    days = pollState.pollLengthDays,
                    hours = pollState.pollLengthHours,
                    minutes = pollState.pollLengthMinutes,
                ),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colorScheme.onSurface,
            )
        },
    )

    AnimatedVisibility(visible = showPollLengthPicker) {
        PollLengthPicker(
            days = pollState.pollLengthDays,
            hours = pollState.pollLengthHours,
            minutes = pollState.pollLengthMinutes,
            onLengthChanged = { days, hours, minutes ->
                eventPublisher(NoteEditorContract.UiEvent.UpdatePollLength(days, hours, minutes))
            },
        )
    }

    if (pollState.pollType == PollType.ZapPoll) {
        HorizontalDivider(
            modifier = Modifier.padding(
                start = startPadding,
                end = endPadding,
                top = 4.dp,
                bottom = 4.dp,
            ),
            color = AppTheme.colorScheme.outline,
        )

        ZapAmountRow(
            label = stringResource(id = R.string.poll_editor_min_zap),
            amountInSats = pollState.minZapAmountInSats,
            onAmountChanged = { amount ->
                eventPublisher(NoteEditorContract.UiEvent.UpdateMinZapAmount(amount))
            },
            footerHeightPx = footerHeightPx,
        )

        HorizontalDivider(
            modifier = Modifier.padding(
                start = startPadding,
                end = endPadding,
                top = 4.dp,
                bottom = 4.dp,
            ),
            color = AppTheme.colorScheme.outline,
        )

        ZapAmountRow(
            label = stringResource(id = R.string.poll_editor_max_zap),
            amountInSats = pollState.maxZapAmountInSats,
            onAmountChanged = { amount ->
                eventPublisher(NoteEditorContract.UiEvent.UpdateMaxZapAmount(amount))
            },
            footerHeightPx = footerHeightPx,
        )
    }
}

@Composable
private fun PollSettingRow(
    label: String,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            )
            .padding(start = startPadding, end = endPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        if (trailingContent != null) {
            trailingContent()
        }
    }
}

@Composable
private fun PollTypeLabel(pollType: PollType) {
    Text(
        text = when (pollType) {
            PollType.UserPoll -> stringResource(id = R.string.poll_editor_user_poll)
            PollType.ZapPoll -> stringResource(id = R.string.poll_editor_zap_poll)
        },
        style = AppTheme.typography.bodyMedium,
        color = AppTheme.colorScheme.onSurface,
    )
}

@Composable
private fun PollLengthPicker(
    days: Int,
    hours: Int,
    minutes: Int,
    onLengthChanged: (days: Int, hours: Int, minutes: Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding, end = endPadding, top = 4.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PollNumericField(
            value = days.toString(),
            onValueChange = { text ->
                val parsed = text.filter { it.isDigit() }.toIntOrNull() ?: 0
                onLengthChanged(parsed.coerceIn(0, MAX_POLL_DAYS), hours, minutes)
            },
            suffix = pluralStringResource(id = R.plurals.poll_editor_day_suffix, count = days),
            modifier = Modifier.weight(1f),
        )
        PollNumericField(
            value = hours.toString(),
            onValueChange = { text ->
                val parsed = text.filter { it.isDigit() }.toIntOrNull() ?: 0
                onLengthChanged(days, parsed.coerceIn(0, MAX_POLL_HOURS), minutes)
            },
            suffix = pluralStringResource(id = R.plurals.poll_editor_hrs_suffix, count = hours),
            modifier = Modifier.weight(1f),
        )
        PollNumericField(
            value = minutes.toString(),
            onValueChange = { text ->
                val parsed = text.filter { it.isDigit() }.toIntOrNull() ?: 0
                onLengthChanged(days, hours, parsed.coerceIn(0, MAX_POLL_MINUTES))
            },
            suffix = pluralStringResource(id = R.plurals.poll_editor_min_suffix, count = minutes),
            modifier = Modifier.weight(1f),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PollNumericField(
    value: String,
    onValueChange: (String) -> Unit,
    suffix: String,
    modifier: Modifier = Modifier,
    focusModifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val colors = PrimalDefaults.outlinedTextFieldColors(
        focusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        unfocusedContainerColor = AppTheme.extraColorScheme.surfaceVariantAlt2,
        focusedBorderColor = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
        unfocusedBorderColor = AppTheme.colorScheme.outline,
    )

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(44.dp)
            .then(focusModifier),
        singleLine = true,
        textStyle = AppTheme.typography.bodySmall.copy(
            textAlign = TextAlign.Start,
            color = AppTheme.colorScheme.onSurface,
        ),
        cursorBrush = SolidColor(AppTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        interactionSource = interactionSource,
        decorationBox = { innerTextField ->
            OutlinedTextFieldDefaults.DecorationBox(
                value = value,
                innerTextField = innerTextField,
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                contentPadding = PaddingValues(
                    start = 10.dp,
                    end = 10.dp,
                    top = 6.dp,
                    bottom = 6.dp,
                ),
                container = {
                    OutlinedTextFieldDefaults.Container(
                        enabled = true,
                        isError = false,
                        interactionSource = interactionSource,
                        colors = colors,
                        shape = choiceFieldShape,
                    )
                },
                suffix = {
                    Text(
                        text = suffix,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                    )
                },
            )
        },
    )
}

@Composable
private fun ZapAmountRow(
    label: String,
    amountInSats: Long?,
    onAmountChanged: (Long?) -> Unit,
    footerHeightPx: Int,
) {
    var isEditing by remember { mutableStateOf(false) }

    if (!isEditing) {
        ZapAmountDisplayRow(
            label = label,
            amountInSats = amountInSats,
            onStartEditing = { isEditing = true },
        )
    } else {
        ZapAmountEditRow(
            label = label,
            amountInSats = amountInSats,
            onAmountChanged = onAmountChanged,
            footerHeightPx = footerHeightPx,
            onStopEditing = { isEditing = false },
        )
    }
}

@Composable
private fun ZapAmountDisplayRow(
    label: String,
    amountInSats: Long?,
    onStartEditing: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable { onStartEditing() }
            .padding(start = startPadding, end = endPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = amountInSats?.toString() ?: "—",
                style = AppTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = AppTheme.colorScheme.onSurface,
            )
            Text(
                text = stringResource(id = R.string.poll_editor_sats_suffix),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
            )
        }
    }
}

@Composable
private fun ZapAmountEditRow(
    label: String,
    amountInSats: Long?,
    onAmountChanged: (Long?) -> Unit,
    footerHeightPx: Int,
    onStopEditing: () -> Unit,
) {
    var wasFocused by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    var rowSize by remember { mutableStateOf(IntSize.Zero) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .bringIntoViewRequester(bringIntoViewRequester)
            .onSizeChanged { rowSize = it }
            .padding(start = startPadding, end = endPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
        )

        PollNumericField(
            value = amountInSats?.toString() ?: "",
            onValueChange = { text ->
                val parsed = text.filter { it.isDigit() }.toLongOrNull()
                onAmountChanged(parsed)
            },
            suffix = stringResource(id = R.string.poll_editor_sats_suffix),
            modifier = Modifier.width(96.dp),
            focusModifier = Modifier
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused) {
                        wasFocused = true
                        coroutineScope.launch {
                            /*
                            NOTE: Delay is important to let the keyboard settle
                             and properly bring this field into view.
                             */
                            delay(KEYBOARD_SETTLE_DELAY_MS)
                            bringIntoViewRequester.bringIntoView(
                                rect = Rect(
                                    left = 0f,
                                    top = 0f,
                                    right = rowSize.width.toFloat(),
                                    bottom = rowSize.height.toFloat() + footerHeightPx.toFloat(),
                                ),
                            )
                        }
                    } else if (wasFocused) {
                        onStopEditing()
                        wasFocused = false
                    }
                },
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Composable
private fun formatPollLength(
    days: Int,
    hours: Int,
    minutes: Int,
): String {
    val parts = mutableListOf<String>()
    if (days > 0) parts.add("$days ${pluralStringResource(id = R.plurals.poll_editor_day_suffix, count = days)}")
    if (hours > 0) parts.add("$hours ${pluralStringResource(id = R.plurals.poll_editor_hrs_suffix, count = hours)}")
    if (minutes > 0) {
        parts.add(
            "$minutes ${pluralStringResource(id = R.plurals.poll_editor_min_suffix, count = minutes)}",
        )
    }
    return parts.joinToString(
        " ",
    ).ifEmpty { "0 ${pluralStringResource(id = R.plurals.poll_editor_min_suffix, count = 0)}" }
}

@Preview
@Composable
private fun PollEditorSectionPreview() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        PollEditorSection(
            pollState = NoteEditorContract.PollEditorState(
                choices = listOf(
                    NoteEditorContract.PollChoice(text = "Option A"),
                    NoteEditorContract.PollChoice(text = "Option B"),
                    NoteEditorContract.PollChoice(text = ""),
                ),
                pollLengthDays = 1,
                pollLengthHours = 12,
                pollLengthMinutes = 0,
            ),
            eventPublisher = {},
            footerHeightPx = 0,
        )
    }
}

@Preview
@Composable
private fun PollChoiceFieldPreview() {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        PollChoiceField(
            choice = NoteEditorContract.PollChoice(text = "Sample choice"),
            index = 0,
            isFocused = false,
            canRemove = true,
            onTextChange = {},
            onFocusChange = {},
            onRemove = {},
        )
    }
}
