package net.primal.android.auth.onboarding.account

enum class OnboardingStep(val index: Int) {
    Details(index = 0),
    Interests(index = 1),
    Follows(index = 2),
    Preview(index = 3),
    ;

    companion object {
        fun fromIndex(index: Int): OnboardingStep {
            return OnboardingStep.entries.find { it.index == index }
                ?: throw IllegalArgumentException("Invalid index.")
        }
    }
}

enum class AccountCreationStep {
    AccountPreview,
    AccountCreated,
    ZapsIntroduction,
}
