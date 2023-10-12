package net.primal.android.messages.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.PrimalTopLevelDestination
import net.primal.android.core.compose.foundation.brandBackground
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.AvatarDefault
import net.primal.android.core.compose.icons.primaliconpack.Message
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalBottomBarHeightDp
import net.primal.android.drawer.PrimalDrawerScaffold
import net.primal.android.theme.AppTheme

@Composable
fun MessageListScreen(
    viewModel: MessageListViewModel,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerScreenClick: (DrawerScreenDestination) -> Unit,
    onChatClick: (String) -> Unit,
    onNewMessageClick: () -> Unit,
) {

    val uiState = viewModel.state.collectAsState()

    MessageListScreen(
        state = uiState.value,
        onPrimaryDestinationChanged = onTopLevelDestinationChanged,
        onDrawerDestinationClick = onDrawerScreenClick,
        onChatClick = onChatClick,
        onNewMessageClick = onNewMessageClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageListScreen(
    state: MessageListContract.UiState,
    onPrimaryDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
    onChatClick: (String) -> Unit,
    onNewMessageClick: () -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)
    val listState = rememberLazyListState()

    val bottomBarHeight = PrimalBottomBarHeightDp
    var bottomBarOffsetHeightPx by remember { mutableFloatStateOf(0f) }
    val focusMode by remember { derivedStateOf { bottomBarOffsetHeightPx < 0f } }

    PrimalDrawerScaffold(
        drawerState = drawerState,
        activeDestination = PrimalTopLevelDestination.Messages,
        onActiveDestinationClick = { uiScope.launch { listState.animateScrollToItem(0) } },
        onPrimaryDestinationChanged = onPrimaryDestinationChanged,
        onDrawerDestinationClick = onDrawerDestinationClick,
        badges = state.badges,
        bottomBarHeight = bottomBarHeight,
        onBottomBarOffsetChange = { bottomBarOffsetHeightPx = it },
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.messages_title),
                avatarUrl = state.activeAccountAvatarUrl,
                navigationIcon = PrimalIcons.AvatarDefault,
                onNavigationIconClick = {
                    uiScope.launch { drawerState.open() }
                },
                scrollBehavior = it,
                footer = {
                    MessagesTabs(
                        onFollowsTabClick = {

                        },
                        onOtherTabClick = {

                        },
                        onMarkAllRead = {

                        },
                    )
                }
            )
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .background(color = AppTheme.colorScheme.surfaceVariant)
                    .fillMaxSize(),
                contentPadding = paddingValues,
                state = listState,
            ) {
                items(count = 50) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .padding(horizontal = 32.dp),
                        textAlign = TextAlign.Start,
                        text = "Coming soon... (${it + 1})",
                    )
                }

            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !focusMode,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                FloatingActionButton(
                    onClick = onNewMessageClick,
                    modifier = Modifier
                        .size(bottomBarHeight)
                        .clip(CircleShape)
                        .brandBackground(shape = CircleShape),
                    elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
                    containerColor = Color.Unspecified,
                    content = {
                        Icon(
                            imageVector = PrimalIcons.Message,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    },
                )
            }
        }
    )
}

@Composable
private fun MessagesTabs(
    onFollowsTabClick: () -> Unit,
    onOtherTabClick: () -> Unit,
    onMarkAllRead: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var tabIndex by remember { mutableIntStateOf(0) }

        var followsTabWidth by remember { mutableIntStateOf(0) }
        var otherTabWidth by remember { mutableIntStateOf(0) }
        val tabsSpaceWidth = 16.dp

        val onFollowsClick = {
            tabIndex = 0
            onFollowsTabClick()
        }
        val onOtherClick = {
            tabIndex = 1
            onOtherTabClick()
        }

        Column(
            modifier = Modifier.wrapContentWidth(),
        ) {
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MessagesTab(
                    text = stringResource(id = R.string.messages_follows_tab_title).uppercase(),
                    onSizeChanged = { size -> followsTabWidth = size.width },
                    onClick = onFollowsClick,
                )

                Spacer(modifier = Modifier.width(tabsSpaceWidth))

                MessagesTab(
                    text = stringResource(id = R.string.messages_other_tab_title).uppercase(),
                    onSizeChanged = { size -> otherTabWidth = size.width },
                    onClick = onOtherClick,
                )
            }

            with(LocalDensity.current) {
                Box(
                    modifier = Modifier
                        .height(4.dp)
                        .width(
                            animateIntAsState(
                                targetValue = when (tabIndex) {
                                    0 -> followsTabWidth
                                    1 -> otherTabWidth
                                    else -> 0
                                },
                                label = "indicatorWidth"
                            ).value.toDp()
                        )
                        .offset(
                            y = (-4).dp,
                            x = animateIntAsState(
                                targetValue = when (tabIndex) {
                                    1 -> followsTabWidth + tabsSpaceWidth
                                        .toPx()
                                        .toInt()

                                    else -> 0
                                },
                                label = "indicatorOffsetX"
                            ).value.toDp()
                        )
                        .background(
                            color = AppTheme.colorScheme.primary,
                            shape = AppTheme.shapes.small,
                        )
                )
            }
        }

        Text(
            modifier = Modifier
                .defaultMinSize(minHeight = 32.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onMarkAllRead,
                    role = Role.Button,
                ),
            text = stringResource(id = R.string.messages_mark_all_read_button),
            textAlign = TextAlign.End,
            color = AppTheme.colorScheme.primary,
            style = AppTheme.typography.bodySmall,
        )
    }
}

@Composable
fun MessagesTab(
    text: String,
    onSizeChanged: (IntSize) -> Unit,
    onClick: () -> Unit,
) {
    Text(
        modifier = Modifier
            .wrapContentWidth()
            .onSizeChanged { onSizeChanged(it) }
            .defaultMinSize(minHeight = 32.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
                role = Role.Button,
            ),
        text = text,
        style = AppTheme.typography.bodySmall,
        color = AppTheme.colorScheme.onSurface,
    )
}
