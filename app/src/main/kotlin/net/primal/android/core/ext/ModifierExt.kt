package net.primal.android.core.ext

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Dp
import kotlin.collections.plusAssign

fun Modifier.onFocusSelectAll(textFieldValueState: MutableState<TextFieldValue>): Modifier =
    composed(
        inspectorInfo = debugInspectorInfo {
            name = "textFieldValueState"
            properties["textFieldValueState"] = textFieldValueState
        },
    ) {
        var triggerEffect by remember {
            mutableStateOf<Boolean?>(null)
        }
        if (triggerEffect != null) {
            LaunchedEffect(triggerEffect) {
                val tfv = textFieldValueState.value
                textFieldValueState.value = tfv.copy(selection = TextRange(0, tfv.text.length))
            }
        }
        onFocusChanged { focusState ->
            if (focusState.isFocused) {
                triggerEffect = triggerEffect?.let { bool ->
                    !bool
                } ?: true
            }
        }
    }

fun Modifier.onDragDownBeyond(threshold: Dp, onTriggered: () -> Unit): Modifier =
    composed {
        val latestOnTriggered by rememberUpdatedState(onTriggered)
        pointerInput(threshold) {
            val thresholdPx = threshold.toPx()
            var acc = 0f
            var fired = false

            detectDragGestures(
                onDragStart = {
                    acc = 0f
                    fired = false
                },
                onDragCancel = {
                    acc = 0f
                    fired = false
                },
                onDragEnd = {
                    acc = 0f
                    fired = false
                },
            ) { _, drag ->
                acc += drag.y
                if (!fired && acc >= thresholdPx) {
                    fired = true
                    latestOnTriggered()
                }
            }
        }
    }
