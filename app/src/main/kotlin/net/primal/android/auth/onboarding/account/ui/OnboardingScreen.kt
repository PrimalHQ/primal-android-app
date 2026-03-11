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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import net.primal.android.auth.onboarding.account.OnboardingContract
import net.primal.android.auth.onboarding.account.OnboardingStep
import net.primal.android.auth.onboarding.account.OnboardingViewModel
import net.primal.android.auth.onboarding.account.ui.model.FollowGroup
import net.primal.android.core.compose.ColumnWithBackground
import net.primal.android.core.compose.PrimalGradientAlpha
import net.primal.android.core.compose.PrimalGradientBackgroundColor
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.compose.primalGradientBrush
import net.primal.android.stream.player.hideStreamMiniPlayer
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel, callbacks: OnboardingContract.ScreenCallbacks) {
    val uiState = viewModel.state.collectAsState()

    fun handleBackEvent() {
        when (uiState.value.currentStep) {
            OnboardingStep.Details -> callbacks.onClose()
            else -> viewModel.setEvent(OnboardingContract.UiEvent.RequestPreviousStep)
        }
    }

    BackHandler { handleBackEvent() }

    hideStreamMiniPlayer()
    OnboardingScreen(
        state = uiState.value,
        eventPublisher = { viewModel.setEvent(it) },
        onBack = { handleBackEvent() },
        callbacks = callbacks,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun OnboardingScreen(
    state: OnboardingContract.UiState,
    eventPublisher: (OnboardingContract.UiEvent) -> Unit,
    onBack: () -> Unit,
    callbacks: OnboardingContract.ScreenCallbacks,
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
            backgroundBrushProvider = ::primalGradientBrush,
            brushAlpha = PrimalGradientAlpha,
            backgroundColor = PrimalGradientBackgroundColor,
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

                OnboardingStep.Follows -> OnboardingProfileFollowsScreen(
                    state = state,
                    eventPublisher = eventPublisher,
                    onBack = onBack,
                )

                OnboardingStep.Preview -> OnboardingProfilePreviewScreen(
                    state = state,
                    eventPublisher = eventPublisher,
                    onBack = onBack,
                    onOnboarded = callbacks.onOnboarded,
                )
            }
        }
    }
}

private class UiStateProvider(
    override val values: Sequence<OnboardingContract.UiState> = sequenceOf(
        OnboardingContract.UiState(
            currentStep = OnboardingStep.Details,
        ),
        OnboardingContract.UiState(
            currentStep = OnboardingStep.Interests,
            allSuggestions = listOf(
                FollowGroup(name = "art", members = emptyList()),
                FollowGroup(name = "bitcoin", members = emptyList()),
                FollowGroup(name = "memes", members = emptyList()),
                FollowGroup(name = "primal", members = emptyList()),
                FollowGroup(name = "android", members = emptyList()),
                FollowGroup(name = "nostr", members = emptyList()),
                FollowGroup(name = "developers", members = emptyList()),
                FollowGroup(name = "designers", members = emptyList()),
                FollowGroup(name = "human rights", members = emptyList()),
            ),
            selectedSuggestions = listOf(
                FollowGroup(name = "bitcoin", members = emptyList()),
                FollowGroup(name = "memes", members = emptyList()),
            ),
        ),
        OnboardingContract.UiState(
            currentStep = OnboardingStep.Follows,
        ),
        OnboardingContract.UiState(
            currentStep = OnboardingStep.Preview,
            profileDisplayName = "Alex",
            profileAboutYou = "Primal Lead Android Developer",
        ),
        OnboardingContract.UiState(
            currentStep = OnboardingStep.Preview,
            profileDisplayName = "Alex",
            profileAboutYou = "Primal Lead Android Developer",
            accountCreated = true,
        ),
    ),
) : PreviewParameterProvider<OnboardingContract.UiState>

@Preview
@Composable
private fun PreviewOnboarding(
    @PreviewParameter(provider = UiStateProvider::class) uiState: OnboardingContract.UiState,
) {
    PrimalPreview(primalTheme = PrimalTheme.Midnight) {
        OnboardingScreen(
            state = uiState,
            eventPublisher = {},
            onBack = {},
            callbacks = OnboardingContract.ScreenCallbacks(
                onClose = {},
                onOnboarded = {},
            ),
        )
    }
}
