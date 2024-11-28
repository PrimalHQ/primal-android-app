package net.primal.android.auth.onboarding.account.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.auth.compose.OnboardingBottomBar
import net.primal.android.auth.compose.onboardingTextHintTypography
import net.primal.android.auth.onboarding.account.OnboardingContract
import net.primal.android.auth.onboarding.account.OnboardingStep
import net.primal.android.auth.onboarding.account.ui.model.FollowGroup
import net.primal.android.core.compose.PrimalLoadingSpinner
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.theme.AppTheme

@ExperimentalLayoutApi
@ExperimentalMaterial3Api
@Composable
fun OnboardingProfileInterestsScreen(
    state: OnboardingContract.UiState,
    eventPublisher: (OnboardingContract.UiEvent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            PrimalTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
                title = stringResource(id = R.string.onboarding_title_your_interests),
                textColor = Color.White,
                showDivider = false,
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconTintColor = Color.White,
                onNavigationIconClick = onBack,
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 32.dp),
                    text = stringResource(id = R.string.onboarding_profile_interests_hint),
                    textAlign = TextAlign.Center,
                    style = onboardingTextHintTypography(),
                )

                InterestsContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    allSuggestions = state.allSuggestions,
                    selectedSuggestions = state.selectedSuggestions,
                    onSuggestionSelected = {
                        eventPublisher(OnboardingContract.UiEvent.InterestSelected(groupName = it.name))
                    },
                    onSuggestionUnselected = {
                        eventPublisher(OnboardingContract.UiEvent.InterestUnselected(groupName = it.name))
                    },
                )

                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    text = pluralStringResource(
                        id = R.plurals.onboarding_profile_interests_selection_count,
                        state.selectedSuggestions.size,
                        state.selectedSuggestions.size,
                    ),
                    textAlign = TextAlign.Center,
                    style = onboardingTextHintTypography(),
                )
            }
        },
        bottomBar = {
            OnboardingBottomBar(
                buttonText = stringResource(id = R.string.onboarding_button_next),
                buttonEnabled = state.selectedSuggestions.isNotEmpty(),
                onButtonClick = { eventPublisher(OnboardingContract.UiEvent.RequestNextStep) },
                footer = { OnboardingStepsIndicator(currentPage = OnboardingStep.Interests.index) },
            )
        },
    )
}

@ExperimentalLayoutApi
@Composable
private fun InterestsContent(
    modifier: Modifier,
    allSuggestions: List<FollowGroup>,
    selectedSuggestions: List<FollowGroup>,
    onSuggestionSelected: (FollowGroup) -> Unit,
    onSuggestionUnselected: (FollowGroup) -> Unit,
) {
    if (allSuggestions.isEmpty()) {
        Box(modifier = modifier) {
            PrimalLoadingSpinner()
        }
    } else {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.Top,
        ) {
            allSuggestions.forEach { group ->
                val isSelected = selectedSuggestions.find { it.name == group.name } != null
                SuggestionChip(
                    modifier = Modifier
                        .height(48.dp)
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    shape = AppTheme.shapes.extraLarge,
                    colors = if (isSelected) suggestionSelectedChipColors() else suggestionUnselectedChipColors(),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = isSelected,
                        borderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                    ),
                    label = {
                        Text(
                            modifier = Modifier.padding(horizontal = 2.dp),
                            text = group.name.lowercase(),
                            style = AppTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    onClick = { if (isSelected) onSuggestionUnselected(group) else onSuggestionSelected(group) },
                )
            }
        }
    }
}

@Composable
fun suggestionSelectedChipColors() =
    SuggestionChipDefaults.suggestionChipColors(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1,
        labelColor = AppTheme.colorScheme.onSurface,
        iconContentColor = AppTheme.colorScheme.onSurface,
    )

@Composable
fun suggestionUnselectedChipColors() =
    SuggestionChipDefaults.suggestionChipColors(
        containerColor = AppTheme.extraColorScheme.surfaceVariantAlt1.copy(alpha = 0.4f),
        labelColor = AppTheme.colorScheme.onSurface,
        iconContentColor = AppTheme.colorScheme.onSurface,
    )
