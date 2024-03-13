package net.primal.android.auth.onboarding.account

enum class OnboardingStep(val index: Int) {
    Details(index = 0),
    Interests(index = 1),
    Preview(index = 2),
    ;

    companion object {
        fun fromIndex(index: Int): OnboardingStep {
            return OnboardingStep.entries.find { it.index == index }
                ?: throw IllegalArgumentException("Invalid index.")
        }
    }
}
