package net.primal.android.auth.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.auth.onboarding.ui.OnboardingProfileDetailsScreen
import net.primal.android.auth.onboarding.ui.OnboardingProfileInterestsScreen
import net.primal.android.auth.onboarding.ui.OnboardingProfilePreviewScreen
import net.primal.android.auth.onboarding.ui.backgroundPainter

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onClose: () -> Unit,
    onOnboarded: (String) -> Unit,
) {
    val uiState = viewModel.state.collectAsState()

    fun handleBackEvent() {
        when (uiState.value.currentStep) {
            OnboardingStep.Details -> onClose()
            else -> viewModel.setEvent(OnboardingContract.UiEvent.RequestPreviousStep)
        }
    }

    BackHandler { handleBackEvent() }

    OnboardingScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onBack = { handleBackEvent() },
        onOnboarded = onOnboarded,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun OnboardingScreen(
    state: OnboardingContract.UiState,
    eventPublisher: (OnboardingContract.UiEvent) -> Unit,
    onBack: () -> Unit,
    onOnboarded: (String) -> Unit,
) {
    AnimatedContent(
        targetState = state.currentStep,
        label = "OnboardingScreenStepAnimation",
        transitionSpec = {
            if (targetState.index > initialState.index) {
                slideInHorizontally(initialOffsetX = { it }).togetherWith(slideOutHorizontally(targetOffsetX = { -it }))
            } else {
                slideInHorizontally(initialOffsetX = { -it }).togetherWith(slideOutHorizontally(targetOffsetX = { it }))
            }
        },
    ) { onboardingStep ->
        ColumnWithBackground(
            backgroundPainter = onboardingStep.backgroundPainter(),
        ) {
            when (onboardingStep) {
                OnboardingStep.Details -> OnboardingProfileDetailsScreen(
                    state = state,
                    eventPublisher = eventPublisher,
                    onBack = onBack,
                )

                OnboardingStep.Interests -> OnboardingProfileInterestsScreen(
                    state = state,
                    eventPublisher = eventPublisher,
                    onBack = onBack,
                )
                OnboardingStep.Preview -> OnboardingProfilePreviewScreen(
                    state = state,
                    eventPublisher = eventPublisher,
                    onBack = onBack,
                    onOnboarded = onOnboarded,
                )
                OnboardingStep.WalletActivation -> Unit
            }
        }
    }
}
