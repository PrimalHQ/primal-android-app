package net.primal.android.core.compose.profile

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.theme.AppTheme

@Composable
fun PrimalOutlinedTextField(
    header: String,
    value: String,
    onValueChange: (String) -> Unit,
    isRequired: Boolean = false,
    prefix: String? = null,
    isMultiline: Boolean = false,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = header.uppercase(),
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt4,
            )
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
                            append(stringResource(id = R.string.create_account_required_hint))
                        }
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            colors = PrimalDefaults.outlinedTextFieldColors(),
            shape = AppTheme.shapes.medium,
            singleLine = !isMultiline,
            minLines = if (isMultiline) 6 else 0,
            value = value,
            onValueChange = onValueChange,
            textStyle = AppTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
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
