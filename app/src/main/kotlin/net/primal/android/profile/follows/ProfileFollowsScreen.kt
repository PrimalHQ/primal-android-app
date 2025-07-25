package net.primal.android.profile.follows

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.ListNoContent
import net.primal.android.core.compose.PrimalDivider
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.heightAdjustableLoadingLazyListPlaceholder
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.profile.approvals.FollowsApprovalAlertDialog
import net.primal.android.core.errors.resolveUiErrorMessage
import net.primal.android.explore.search.ui.FollowUnfollowVisibility
import net.primal.android.explore.search.ui.UserProfileListItem
import net.primal.android.profile.domain.ProfileFollowsType

@Composable
fun ProfileFollowsScreen(viewModel: ProfileFollowsViewModel, callbacks: ProfileFollowsContract.ScreenCallbacks) {
    val uiState = viewModel.state.collectAsState()

    ProfileFollowsScreen(
        state = uiState.value,
        callbacks = callbacks,
        eventPublisher = { viewModel.setEvent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileFollowsScreen(
    state: ProfileFollowsContract.UiState,
    callbacks: ProfileFollowsContract.ScreenCallbacks,
    eventPublisher: (ProfileFollowsContract.UiEvent) -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    SnackbarErrorHandler(
        error = state.uiError,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = { it.resolveUiErrorMessage(context = context) },
        onErrorDismiss = { eventPublisher(ProfileFollowsContract.UiEvent.DismissError) },
    )

    Scaffold(
        modifier = Modifier,
        topBar = {
            PrimalTopAppBar(
                title = state.profileName?.let {
                    when (state.followsType) {
                        ProfileFollowsType.Following -> stringResource(id = R.string.profile_following_title, it)
                        ProfileFollowsType.Followers -> stringResource(id = R.string.profile_followers_title, it)
                    }
                } ?: "",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = callbacks.onClose,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
            )
        },
        content = { paddingValues ->
            ProfileFollowsContent(
                state = state,
                eventPublisher = eventPublisher,
                paddingValues = paddingValues,
                onProfileClick = callbacks.onProfileClick,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
private fun ProfileFollowsContent(
    state: ProfileFollowsContract.UiState,
    eventPublisher: (ProfileFollowsContract.UiEvent) -> Unit,
    paddingValues: PaddingValues,
    onProfileClick: (String) -> Unit,
) {
    if (state.shouldApproveProfileAction != null) {
        FollowsApprovalAlertDialog(
            followsApproval = state.shouldApproveProfileAction,
            onFollowsActionsApproved = {
                eventPublisher(ProfileFollowsContract.UiEvent.ApproveFollowsActions(it.actions))
            },
            onClose = { eventPublisher(ProfileFollowsContract.UiEvent.DismissConfirmFollowUnfollowAlertDialog) },
        )
    }

    FollowsLazyColumn(
        paddingValues = paddingValues,
        state = state,
        onProfileClick = onProfileClick,
        onFollowProfileClick = { eventPublisher(ProfileFollowsContract.UiEvent.FollowProfile(profileId = it)) },
        onUnfollowProfileClick = { eventPublisher(ProfileFollowsContract.UiEvent.UnfollowProfile(profileId = it)) },
        onRefreshClick = { eventPublisher(ProfileFollowsContract.UiEvent.ReloadData) },
    )
}

@Composable
private fun FollowsLazyColumn(
    paddingValues: PaddingValues,
    state: ProfileFollowsContract.UiState,
    onProfileClick: (String) -> Unit,
    onFollowProfileClick: (String) -> Unit,
    onUnfollowProfileClick: (String) -> Unit,
    onRefreshClick: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .imePadding(),
    ) {
        items(
            items = state.users,
            key = { it.profileId },
            contentType = { "ProfileListItem" },
        ) { user ->
            UserProfileListItem(
                data = user,
                onClick = { item -> onProfileClick(item.profileId) },
                followUnfollowVisibility = if (user.profileId != state.userId) {
                    FollowUnfollowVisibility.Visible
                } else {
                    FollowUnfollowVisibility.Invisible
                },
                isFollowed = state.userFollowing.contains(user.profileId),
                onFollowUnfollowClick = {
                    if (state.userFollowing.contains(user.profileId)) {
                        onUnfollowProfileClick(user.profileId)
                    } else {
                        onFollowProfileClick(user.profileId)
                    }
                },
            )
            PrimalDivider()
        }

        if (state.users.isEmpty()) {
            when (state.loading) {
                true -> heightAdjustableLoadingLazyListPlaceholder(
                    height = 48.dp,
                    showDivider = true,
                    itemPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                )

                false -> item(contentType = "NoContent") {
                    ListNoContent(
                        modifier = Modifier.fillParentMaxSize(),
                        noContentText = when (state.followsType) {
                            ProfileFollowsType.Following -> stringResource(
                                id = R.string.profile_following_no_content,
                            )

                            ProfileFollowsType.Followers -> stringResource(
                                id = R.string.profile_followers_no_content,
                            )
                        },
                        refreshButtonVisible = true,
                        onRefresh = onRefreshClick,
                    )
                }
            }
        }
    }
}
