package net.primal.android.auth.onboarding.account.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import net.primal.android.R
import net.primal.android.auth.compose.ColumnWithBackground
import net.primal.android.auth.onboarding.account.OnboardingContract
import net.primal.android.auth.onboarding.account.OnboardingStep
import net.primal.android.auth.onboarding.account.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onClose: () -> Unit,
    onOnboarded: () -> Unit,
    onActivateWallet: () -> Unit,
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
        onActivateWallet = onActivateWallet,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun OnboardingScreen(
    state: OnboardingContract.UiState,
    eventPublisher: (OnboardingContract.UiEvent) -> Unit,
    onBack: () -> Unit,
    onOnboarded: () -> Unit,
    onActivateWallet: () -> Unit,
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
                    onActivateWallet = onActivateWallet,
                )
            }
        }
    }
}

@Composable
private fun OnboardingStep.backgroundPainter(): Painter {
    return when (this) {
        OnboardingStep.Details -> painterResource(id = R.drawable.onboarding_spot2)
        OnboardingStep.Interests -> painterResource(id = R.drawable.onboarding_spot3)
        OnboardingStep.Preview -> painterResource(id = R.drawable.onboarding_spot4)
    }
}
