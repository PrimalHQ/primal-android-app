package net.primal.android.user.handler

import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import net.primal.android.core.utils.batchOnInactivity
import net.primal.android.user.repository.UserRepository
import net.primal.core.utils.coroutines.DispatcherProvider

class ProfileFollowsHandler @Inject constructor(
    private val userRepository: UserRepository,
    dispatcherProvider: DispatcherProvider,
) {

    private val scope = CoroutineScope(dispatcherProvider.io())

    private val actions = MutableSharedFlow<Action>()
    private fun setAction(action: Action) = scope.launch { actions.emit(action) }

    private val results = MutableSharedFlow<ActionResult>()
    private fun setResult(result: ActionResult) = scope.launch { results.emit(result) }

    init {
        observeActions()
    }

    fun followDelayed(userId: String, profileId: String) =
        setAction(action = Action.Follow(userId = userId, profileId = profileId))

    fun unfollowDelayed(userId: String, profileId: String) =
        setAction(action = Action.Unfollow(userId = userId, profileId = profileId))

    suspend fun follow(userId: String, profileId: String) =
        processActions(actions = listOf(Action.Follow(userId = userId, profileId = profileId)), forceUpdate = false)

    suspend fun unfollow(userId: String, profileId: String) =
        processActions(actions = listOf(Action.Unfollow(userId = userId, profileId = profileId)), forceUpdate = false)

    fun forceUpdateList(actions: List<Action>) = scope.launch { processActions(actions = actions, forceUpdate = true) }

    fun observeResults() = results.distinctUntilChanged()

    @OptIn(FlowPreview::class)
    private fun observeActions() =
        scope.launch {
            actions.batchOnInactivity(inactivityTimeout = ACTIONS_DELAY)
                .collect { actions ->
                    processActions(actions = actions, forceUpdate = false)
                }
        }

    private suspend fun processActions(actions: List<Action>, forceUpdate: Boolean) {
        if (actions.isEmpty()) return

        runCatching {
            userRepository.updateFollowList(
                userId = actions.first().userId,
                reducer = { foldActions(actions = actions) },
                forceUpdate = forceUpdate,
            )
        }.onFailure { error ->
            setResult(result = ActionResult.Error(actions = actions, error = error))
        }.onSuccess {
            setResult(ActionResult.Success)
        }
    }

    sealed class Action(open val userId: String, open val profileId: String) {
        data class Follow(override val userId: String, override val profileId: String) : Action(userId, profileId)
        data class Unfollow(override val userId: String, override val profileId: String) : Action(userId, profileId)

        fun flip() =
            when (this) {
                is Follow -> Unfollow(userId, profileId)
                is Unfollow -> Follow(userId, profileId)
            }
    }

    companion object {
        private val ACTIONS_DELAY = 2.seconds

        fun Set<String>.foldActions(actions: List<Action>) =
            actions.fold(initial = this) { acc, value ->
                when (value) {
                    is Action.Follow -> acc + value.profileId
                    is Action.Unfollow -> acc - value.profileId
                }
            }
    }

    sealed class ActionResult {
        data class Error(val actions: List<Action>, val error: Throwable?) : ActionResult()
        data object Success : ActionResult()
    }
}
