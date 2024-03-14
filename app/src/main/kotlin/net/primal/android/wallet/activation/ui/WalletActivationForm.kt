package net.primal.android.wallet.activation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import net.primal.android.R
import net.primal.android.core.compose.DatePickerModalBottomSheet
import net.primal.android.core.compose.PrimalDefaults
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.WalletPrimalActivation
import net.primal.android.theme.AppTheme
import net.primal.android.wallet.activation.domain.WalletActivationData
import net.primal.android.wallet.activation.regions.Country
import net.primal.android.wallet.activation.regions.RegionSelectionBottomSheet
import net.primal.android.wallet.activation.regions.State

@ExperimentalMaterial3Api
@Composable
fun WalletActivationForm(
    modifier: Modifier,
    allCountries: List<Country>,
    availableStates: List<State>,
    isHeaderIconVisible: Boolean = true,
    data: WalletActivationData,
    onDataChange: (WalletActivationData) -> Unit,
    colors: TextFieldColors = PrimalDefaults.outlinedTextFieldColors(),
) {
    var datePickerVisible by remember { mutableStateOf(false) }
    if (datePickerVisible) {
        WalletDatePicker(
            value = data.dateOfBirth,
            onValueChange = { onDataChange(data.copy(dateOfBirth = it)) },
            onDismiss = { datePickerVisible = false },
        )
    }

    var countrySelectionVisible by remember { mutableStateOf(false) }
    if (countrySelectionVisible) {
        RegionSelectionBottomSheet(
            regions = allCountries,
            title = stringResource(id = R.string.wallet_activation_country_picker_title),
            onRegionClick = {
                onDataChange(data.copy(country = it))
            },
            onDismissRequest = { countrySelectionVisible = false },
        )
    }

    var stateSelectionVisible by remember { mutableStateOf(false) }
    if (stateSelectionVisible) {
        RegionSelectionBottomSheet(
            regions = allCountries.find { it.code == data.country?.code }?.states ?: emptyList(),
            title = stringResource(id = R.string.wallet_activation_state_picker_title),
            onRegionClick = {
                onDataChange(data.copy(state = it))
            },
            onDismissRequest = { stateSelectionVisible = false },
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WalletActivationFormHeader(
            iconVisible = isHeaderIconVisible,
        )

        WalletActivationFormInputFields(
            data = data,
            availableStates = availableStates,
            onDataChange = onDataChange,
            onDateOfBirthClick = { datePickerVisible = true },
            onCountryClick = { countrySelectionVisible = true },
            onStateClick = { stateSelectionVisible = true },
            colors = colors,
        )
    }
}

@Composable
private fun WalletActivationFormInputFields(
    data: WalletActivationData,
    availableStates: List<State>,
    onDataChange: (WalletActivationData) -> Unit,
    onDateOfBirthClick: () -> Unit,
    onCountryClick: () -> Unit,
    onStateClick: () -> Unit,
    colors: TextFieldColors = PrimalDefaults.outlinedTextFieldColors(),
) {
    Column {
        WalletOutlinedTextField(
            modifier = Modifier.fillMaxWidth(fraction = 0.8f),
            value = data.firstName,
            onValueChange = { onDataChange(data.copy(firstName = it)) },
            placeholderText = stringResource(id = R.string.wallet_activation_first_name),
            colors = colors,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        WalletOutlinedTextField(
            modifier = Modifier.fillMaxWidth(fraction = 0.8f),
            value = data.lastName,
            onValueChange = { onDataChange(data.copy(lastName = it)) },
            placeholderText = stringResource(id = R.string.wallet_activation_last_name),
            colors = colors,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        WalletOutlinedTextField(
            modifier = Modifier.fillMaxWidth(fraction = 0.8f),
            value = data.email,
            onValueChange = { onDataChange(data.copy(email = it.trim())) },
            placeholderText = stringResource(id = R.string.wallet_activation_email_address),
            colors = colors,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
        )

        Spacer(modifier = Modifier.height(16.dp))

        WalletOutlinedTextField(
            modifier = Modifier.fillMaxWidth(fraction = 0.8f),
            value = data.dateOfBirth.toDateFormat(),
            onClick = onDateOfBirthClick,
            onValueChange = { },
            readOnly = true,
            colors = colors,
            placeholderText = stringResource(id = R.string.wallet_activation_date_of_birth),
        )

        Spacer(modifier = Modifier.height(16.dp))

        CountryAndStateTextFields(
            data = data,
            availableStates = availableStates,
            onCountryClick = onCountryClick,
            onStateClick = onStateClick,
            colors = colors,
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CountryAndStateTextFields(
    data: WalletActivationData,
    availableStates: List<State>,
    onCountryClick: () -> Unit,
    onStateClick: () -> Unit,
    colors: TextFieldColors = PrimalDefaults.outlinedTextFieldColors(),
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(fraction = 0.8f)
            .animateContentSize(),
    ) {
        WalletOutlinedTextField(
            modifier = Modifier.weight(weight = 0.75f),
            onClick = onCountryClick,
            value = data.country?.name ?: "",
            onValueChange = {},
            readOnly = true,
            colors = colors,
            placeholderText = stringResource(id = R.string.wallet_activation_country_of_residence),
        )

        if (availableStates.isNotEmpty()) {
            Spacer(modifier = Modifier.width(16.dp))

            WalletOutlinedTextField(
                modifier = Modifier.weight(weight = 0.25f),
                onClick = onStateClick,
                value = data.state?.code?.split("-")?.last() ?: "",
                onValueChange = {},
                readOnly = true,
                colors = colors,
                placeholderText = stringResource(id = R.string.wallet_activation_state),
            )
        }
    }
}

@Composable
private fun WalletActivationFormHeader(iconVisible: Boolean = true, textVisible: Boolean = true) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AnimatedVisibility(
            visible = iconVisible,
            enter = slideInVertically(initialOffsetY = { -it }),
            exit = ExitTransition.None,
        ) {
            Image(
                modifier = Modifier.padding(vertical = 16.dp),
                imageVector = PrimalIcons.WalletPrimalActivation,
                contentDescription = null,
                colorFilter = ColorFilter.tint(color = AppTheme.colorScheme.onSurface),
            )
        }

        AnimatedVisibility(visible = textVisible) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .padding(bottom = 32.dp, top = if (iconVisible) 32.dp else 0.dp),
                text = stringResource(id = R.string.wallet_activation_pending_data_hint),
                textAlign = TextAlign.Center,
                color = AppTheme.colorScheme.onSurface,
                style = AppTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
    }
}

private const val MIN_AGE_FOR_WALLET = 18
private const val MAX_DATE_OF_BIRTH = 1900

@Suppress("MagicNumber")
@ExperimentalMaterial3Api
@Composable
private fun WalletDatePicker(
    value: Long?,
    onValueChange: (Long?) -> Unit,
    onDismiss: () -> Unit,
) {
    val maxDate = Instant.now().minus(
        Duration.ofDays(MIN_AGE_FOR_WALLET * 365L) +
            Duration.ofHours(MIN_AGE_FOR_WALLET / 4 * 24L),
    )
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = value,
        initialDisplayedMonthMillis = value ?: maxDate.toEpochMilli(),
        yearRange = IntRange(MAX_DATE_OF_BIRTH, LocalDate.now().year - MIN_AGE_FOR_WALLET),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= maxDate.toEpochMilli()
            }
        },
    )
    LaunchedEffect(datePickerState.selectedDateMillis) {
        onValueChange(datePickerState.selectedDateMillis)
    }

    DatePickerModalBottomSheet(
        state = datePickerState,
        onDismissRequest = onDismiss,
    )
}

private fun Long?.toDateFormat(): String {
    if (this == null) return ""

    return LocalDate.ofEpochDay(this / Duration.ofDays(1).toMillis())
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
}

@Composable
private fun WalletOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    colors: TextFieldColors = PrimalDefaults.outlinedTextFieldColors(),
    onClick: (() -> Unit)? = null,
) {
    val interactionSource = remember {
        if (onClick != null) {
            object : MutableInteractionSource {
                override val interactions = MutableSharedFlow<Interaction>(
                    extraBufferCapacity = 16,
                    onBufferOverflow = BufferOverflow.DROP_OLDEST,
                )

                override suspend fun emit(interaction: Interaction) {
                    if (interaction is PressInteraction.Release) {
                        onClick()
                    }
                    interactions.emit(interaction)
                }

                override fun tryEmit(interaction: Interaction): Boolean {
                    return interactions.tryEmit(interaction)
                }
            }
        } else {
            MutableInteractionSource()
        }
    }

    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        colors = colors,
        shape = AppTheme.shapes.large,
        singleLine = true,
        enabled = enabled,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        placeholder = {
            Text(
                text = placeholderText.lowercase(),
                color = AppTheme.extraColorScheme.onSurfaceVariantAlt3,
                style = AppTheme.typography.bodyLarge,
            )
        },
        interactionSource = interactionSource,
    )
}
