package net.primal.android.profile.mention

import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import net.primal.android.core.compose.profile.model.mapAsUserProfileUi
import net.primal.android.user.repository.UserRepository
import net.primal.domain.common.exception.NetworkException
import net.primal.domain.explore.ExploreRepository
import timber.log.Timber

@OptIn(FlowPreview::class)
class UserMentionHandler(
    private val scope: CoroutineScope,
    private val userId: String,
    private val exploreRepository: ExploreRepository,
    private val userRepository: UserRepository,
) {
    private val _state = MutableStateFlow(UserTaggingState())
    val state = _state.asStateFlow()
    private fun setState(reducer: UserTaggingState.() -> UserTaggingState) = _state.getAndUpdate(reducer)

    private val searchQueryFlow = MutableStateFlow("")

    init {
        scope.launch {
            searchQueryFlow
                .debounce(0.42.seconds)
                .distinctUntilChanged()
                .collect { query ->
                    searchUsers(query)
                }
        }

        fetchInitialUsers()
    }

    private fun fetchInitialUsers() {
        scope.launch {
            try {
                val popular = exploreRepository.fetchPopularUsers()
                setState { copy(popularUsers = popular.map { it.mapAsUserProfileUi() }) }
            } catch (error: NetworkException) {
                Timber.w(error)
            }
        }
        scope.launch {
            userRepository.observeRecentUsers(ownerId = userId)
                .distinctUntilChanged()
                .collect { user ->
                    setState { copy(recentUsers = user.map { it.mapAsUserProfileUi() }) }
                }
        }
    }

    private suspend fun searchUsers(query: String) {
        if (query.isNotEmpty()) {
            try {
                val result = exploreRepository.searchUsers(query = query, limit = 10)
                setState { copy(searchResults = result.map { it.mapAsUserProfileUi() }) }
            } catch (error: NetworkException) {
                Timber.w(error)
                setState { copy(searchResults = emptyList()) }
            }
        } else {
            setState { copy(searchResults = emptyList()) }
        }
        setState { copy(isSearching = false) }
    }

    fun search(query: String) {
        setState { copy(isSearching = true, userTaggingQuery = query) }
        searchQueryFlow.value = query
    }

    fun toggleSearch(enabled: Boolean) {
        if (enabled) {
            search(query = "")
        } else {
            setState { copy(userTaggingQuery = null) }
        }
    }

    fun markUserAsMentioned(profileId: String) {
        scope.launch {
            userRepository.markAsInteracted(profileId = profileId, ownerId = userId)
        }
    }
}
