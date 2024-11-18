package net.primal.android.core.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.theme.AppTheme

@Composable
fun PrimalOutlinedTextField(
    header: String?,
    value: String,
    onValueChange: (String) -> Unit,
    forceFocus: Boolean = false,
    isRequired: Boolean = false,
    prefix: String? = null,
    isMultiline: Boolean = false,
    fontSize: TextUnit = 16.sp,
    textAlign: TextAlign = TextAlign.Start,
    isError: Boolean = false,
) {
    val focusRequester: FocusRequester = remember { FocusRequester() }
    var focusRequested by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(focusRequester) {
        if (!focusRequested && forceFocus) {
            focusRequester.requestFocus()
            focusRequested = true
        }
    }

    var textFieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(
            TextFieldValue(text = value, selection = TextRange(start = value.length, end = value.length)),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            header?.let {
                Text(
                    text = header.uppercase(),
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                )
            }
            if (isRequired) {
                Text(
                    buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                color = Color.Red,
                                fontWeight = FontWeight.W400,
                                fontSize = 16.sp,
                            ),
                        ) {
                            append("*")
                        }
                        withStyle(
                            style = SpanStyle(
                                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                                fontWeight = FontWeight.W400,
                                fontSize = 16.sp,
                            ),
                        ) {
                            append(" ")
                            append(stringResource(id = R.string.profile_editor_required_field_hint))
                        }
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth(),
            colors = PrimalDefaults.outlinedTextFieldColors(),
            shape = AppTheme.shapes.medium,
            isError = isError,
            singleLine = !isMultiline,
            minLines = if (isMultiline) 6 else 0,
            value = textFieldValue,
            onValueChange = {
                textFieldValue = it
                onValueChange(it.text)
            },
            textStyle = AppTheme.typography.bodyLarge.copy(
                fontSize = fontSize,
                textAlign = textAlign,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            leadingIcon = if (prefix != null) {
                {
                    Text(
                        modifier = Modifier
                            .align(alignment = Alignment.CenterHorizontally)
                            .padding(bottom = 6.dp),
                        text = prefix,
                        fontWeight = FontWeight.W500,
                        fontSize = 18.sp,
                        color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
                    )
                }
            } else {
                null
            },
        )
    }
}
