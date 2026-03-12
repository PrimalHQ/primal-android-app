package net.primal.android.auth.onboarding.account.api

import retrofit2.http.GET

interface OnboardingApi {

    @GET("https://media.primal.net/api/suggestions_2")
    suspend fun getFollowSuggestions(): FollowSuggestionsResponse
}
