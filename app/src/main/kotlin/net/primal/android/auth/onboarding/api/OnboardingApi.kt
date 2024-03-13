package net.primal.android.auth.onboarding.api

import retrofit2.http.GET

interface OnboardingApi {

    @GET("https://media.primal.net/api/suggestions")
    suspend fun getFollowSuggestions(): FollowSuggestionsResponse
}
