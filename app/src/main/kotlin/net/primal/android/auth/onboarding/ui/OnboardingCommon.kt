package net.primal.android.auth.onboarding.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.primal.android.R
import net.primal.android.auth.compose.OnboardingButton
import net.primal.android.auth.onboarding.OnboardingStep
import net.primal.android.core.compose.HorizontalPagerIndicator
import net.primal.android.theme.AppTheme

@Composable
fun onboardingTextHintTypography() =
    AppTheme.typography.bodyMedium.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
    )

@Composable
fun OnboardingStep.backgroundPainter(): Painter {
    return when (this) {
        OnboardingStep.Details -> painterResource(id = R.drawable.onboarding_spot2)
        OnboardingStep.Interests -> painterResource(id = R.drawable.onboarding_spot3)
        OnboardingStep.Preview -> painterResource(id = R.drawable.onboarding_spot4)
        OnboardingStep.WalletActivation -> painterResource(id = R.drawable.onboarding_spot5)
    }
}

@Composable
fun OnboardingStepBottomBar(
    buttonText: String,
    onButtonClick: () -> Unit,
    buttonEnabled: Boolean = true,
    buttonLoading: Boolean = false,
    footer: @Composable ColumnScope.() -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        OnboardingButton(
            text = buttonText,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(alignment = Alignment.CenterHorizontally),
            onClick = {
                keyboardController?.hide()
                onButtonClick()
            },
            enabled = buttonEnabled,
            loading = buttonLoading,
        )

        footer()
    }
}

@Composable
fun OnboardingStepsIndicator(currentPage: Int) {
    HorizontalPagerIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        pagesCount = OnboardingStep.entries.size,
        successorsColor = Color.White.copy(alpha = 0.4f),
        currentPage = currentPage,
        currentColor = Color.White,
    )
}
