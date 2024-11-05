package net.primal.android.premium.purchase.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.OtpTextField
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalFilledButton
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumPromoCodeBottomSheet(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    isCheckingPromoCodeValidity: Boolean,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    onCodeCodeConfirmed: (String) -> Unit,
    promoCodeValidity: Boolean? = null,
) {
    val uiScope = rememberCoroutineScope()
    var promoCode by remember { mutableStateOf("") }
    var isFieldDirty by remember { mutableStateOf(false) }
    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = AppTheme.colorScheme.surfaceVariant,
    ) {
        Scaffold(
            topBar = {
                PrimalTopAppBar(
                    title = stringResource(id = R.string.premium_promo_code_title),
                    showDivider = false,
                )
            },
            bottomBar = {
                BottomBarButtonsColumn(
                    onCancelClick = {
                        uiScope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismissRequest()
                            }
                        }
                    },
                    onApplyClick = {
                        isFieldDirty = false
                        onCodeCodeConfirmed(promoCode)
                    },
                    isCheckingPromoCodeValidity = isCheckingPromoCodeValidity,
                )
            },
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(paddingValues),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
            ) {
                Text(
                    text = stringResource(id = R.string.premium_promo_code_enter_code_below),
                    color = AppTheme.extraColorScheme.onSurfaceVariantAlt2,
                    style = AppTheme.typography.bodyLarge,
                )
                OtpTextField(
                    otpText = promoCode,
                    onOtpTextChange = {
                        isFieldDirty = true
                        promoCode = it.uppercase()
                    },
                    otpCount = 8,
                    charSpacing = 4.dp,
                    charWidth = null,
                    keyboardType = KeyboardType.Text,
                    onCodeConfirmed = {
                        isFieldDirty = false
                        onCodeCodeConfirmed(it)
                    },
                )
                AnimatedVisibility(
                    visible = promoCodeValidity == false && !isFieldDirty,
                ) {
                    Text(
                        text = stringResource(id = R.string.premium_promo_code_invalid_code_message),
                        color = AppTheme.colorScheme.error,
                        style = AppTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
fun BottomBarButtonsColumn(
    modifier: Modifier = Modifier,
    onApplyClick: () -> Unit,
    onCancelClick: () -> Unit,
    isCheckingPromoCodeValidity: Boolean,
) {
    Column(
        modifier = modifier
            .navigationBarsPadding()
            .padding(vertical = 24.dp, horizontal = 36.dp)
            .imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PrimalLoadingButton(
            loading = isCheckingPromoCodeValidity,
            modifier = Modifier.fillMaxWidth(),
            onClick = onApplyClick,
            text = stringResource(id = R.string.premium_promo_code_apply_button),
            contentColor = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
        PrimalFilledButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCancelClick,
            containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
            contentColor = AppTheme.colorScheme.onBackground,
        ) {
            Text(
                text = stringResource(id = R.string.premium_promo_code_cancel_button),
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
