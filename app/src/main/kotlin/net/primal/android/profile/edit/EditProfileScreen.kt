package net.primal.android.profile.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.profile.InputField
import net.primal.android.core.compose.profile.ProfileHero
import net.primal.android.theme.PrimalTheme

@Composable
fun EditProfileScreen(
    viewModel: EditProfileViewModel,
    onClose: () -> Unit
) {
    val state = viewModel.state.collectAsState()

    EditProfileScreen(
        state = state.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    state: EditProfileContract.UiState,
    eventPublisher: (EditProfileContract.UiEvent) -> Unit,
    onClose: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ErrorHandler(error = state.error, snackbarHostState = snackbarHostState)

    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = "Edit Profile",
                navigationIcon = PrimalIcons.ArrowBack,
                onNavigationIconClick = {
                    onClose()
                },
            )
        },
        content = { paddingValues ->
            EditProfileContent(
                state = state,
                eventPublisher = eventPublisher,
                paddingValues = paddingValues
            )
        }
    )
}

@Composable
fun EditProfileContent(
    state: EditProfileContract.UiState,
    eventPublisher: (EditProfileContract.UiEvent) -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(paddingValues = paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(
                    rememberScrollState()
                )
                .fillMaxHeight()
                .weight(weight = 1f, fill = true),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            ProfileHero(
                avatarUri = state.avatarUri,
                bannerUri = state.bannerUri,
                onBannerUriChange = {
                    eventPublisher(EditProfileContract.UiEvent.BannerUriChangedEvent(bannerUri = it))
                }, onAvatarUriChange = {
                    eventPublisher(EditProfileContract.UiEvent.AvatarUriChangedEvent(avatarUri = it))
                })
            Spacer(modifier = Modifier.height(32.dp))
            InputField(
                header = stringResource(id = R.string.create_input_header_display_name),
                value = state.displayName,
                onValueChange = {
                    eventPublisher(
                        EditProfileContract.UiEvent.DisplayNameChangedEvent(
                            it.trim()
                        )
                    )
                },
                isRequired = true
            )
            Spacer(modifier = Modifier.height(12.dp))
            InputField(
                header = stringResource(id = R.string.create_input_header_handle),
                value = state.name,
                onValueChange = { eventPublisher(EditProfileContract.UiEvent.NameChangedEvent(it.trim())) },
                isRequired = true,
                prefix = "@"
            )
            Spacer(modifier = Modifier.height(12.dp))
            InputField(header = stringResource(id = R.string.create_input_header_website),
                value = state.website,
                onValueChange = { eventPublisher(EditProfileContract.UiEvent.WebsiteChangedEvent(it.trim())) })
            Spacer(modifier = Modifier.height(12.dp))
            InputField(header = stringResource(id = R.string.create_input_header_about_me),
                value = state.aboutMe,
                isMultiline = true,
                onValueChange = { eventPublisher(EditProfileContract.UiEvent.AboutMeChangedEvent(it)) })
            Spacer(modifier = Modifier.height(12.dp))
            InputField(header = stringResource(id = R.string.create_input_header_bitcoin_lightning_address),
                value = state.lightningAddress,
                onValueChange = {
                    eventPublisher(
                        EditProfileContract.UiEvent.LightningAddressChangedEvent(
                            it
                        )
                    )
                })
            Spacer(modifier = Modifier.height(12.dp))
            InputField(header = stringResource(id = R.string.create_input_header_nip_05),
                value = state.nip05Identifier,
                onValueChange = {
                    eventPublisher(
                        EditProfileContract.UiEvent.Nip05IdentifierChangedEvent(
                            it
                        )
                    )
                })
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .padding(horizontal = 32.dp)
        ) {
            PrimalLoadingButton(
                text = stringResource(id = R.string.profile_save_profile_button),
                enabled = !state.loading,
                loading = state.loading,
                onClick = {
                    eventPublisher(EditProfileContract.UiEvent.SaveProfileEvent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
            )
        }
    }
}

@Composable
private fun ErrorHandler(
    error: EditProfileContract.UiState.EditProfileError?,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    LaunchedEffect(key1 = error ?: true) {
        val errorMessage = when (error) {
            is EditProfileContract.UiState.EditProfileError.FailedToPublishMetadata -> context.getString(R.string.profile_failed_to_publish_metadata)
            is EditProfileContract.UiState.EditProfileError.MissingRelaysConfiguration -> context.getString(R.string.app_missing_relays_config)
            is EditProfileContract.UiState.EditProfileError.FailedToUploadImage -> context.getString(R.string.app_failed_to_upload_image)
            null -> return@LaunchedEffect
        }

        snackbarHostState.showSnackbar(
            message = errorMessage,
            duration = SnackbarDuration.Short
        )
    }
}

@Preview
@Composable
fun PreviewEditProfileScreen() {
    PrimalTheme(primalTheme = PrimalTheme.Sunset) {
        EditProfileScreen(state = EditProfileContract.UiState(
            name = "Tralala Handle",
            displayName = "Tralala Display Name",
            aboutMe = "About me",
            website = "http://www.example.com",
            lightningAddress = "tralala@getalby.com",
            nip05Identifier = "tralala@getalby.com"
        ), eventPublisher = {}, onClose = {})
    }
}