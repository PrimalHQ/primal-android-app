package net.primal.android.auth.onboarding.account.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.primal.android.core.compose.HorizontalPagerIndicator
import net.primal.android.core.compose.PrimalDarkTextColor

private const val TOTAL_ONBOARDING_STEPS = 5

@Composable
fun OnboardingStepsIndicator(currentPage: Int) {
    HorizontalPagerIndicator(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        pagesCount = TOTAL_ONBOARDING_STEPS,
        predecessorsColor = PrimalDarkTextColor,
        currentColor = PrimalDarkTextColor,
        successorsColor = PrimalDarkTextColor.copy(alpha = 0.25f),
        currentPage = currentPage,
    )
}
