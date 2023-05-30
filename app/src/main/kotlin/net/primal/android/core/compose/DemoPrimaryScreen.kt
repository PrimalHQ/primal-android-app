package net.primal.android.core.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import net.primal.android.drawer.DrawerScreenDestination
import net.primal.android.drawer.PrimalDrawer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DemoPrimaryScreen(
    title: String,
    description: String,
    primaryDestination: PrimalTopLevelDestination,
    onTopLevelDestinationChanged: (PrimalTopLevelDestination) -> Unit,
    onDrawerDestinationClick: (DrawerScreenDestination) -> Unit,
) {
    val uiScope = rememberCoroutineScope()
    val drawerState: DrawerState = rememberDrawerState(DrawerValue.Closed)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PrimalDrawer(
                drawerState = drawerState,
                onDrawerDestinationClick = onDrawerDestinationClick,
            )
        },
        content = {
            Scaffold(
                topBar = {
                    PrimalTopAppBar(
                        title = title,
                        onNavigationIconClick = {
                            uiScope.launch { drawerState.open() }
                        },
                    )
                },
                content = { paddingValues ->
                    Box(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = description,
                        )
                    }
                },
                bottomBar = {
                    PrimalNavigationBar(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .height(64.dp),
                        activeDestination = primaryDestination,
                        onTopLevelDestinationChanged = onTopLevelDestinationChanged
                    )
                }
            )
        }
    )

}