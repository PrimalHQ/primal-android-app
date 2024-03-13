package net.primal.android.auth.onboarding.account.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.primal.android.auth.onboarding.account.OnboardingStep
import net.primal.android.core.compose.HorizontalPagerIndicator

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
