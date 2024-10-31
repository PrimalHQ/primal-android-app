package net.primal.android.profile.editor.ui

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import net.primal.android.R
import net.primal.android.core.compose.PrimalOutlinedTextField
import net.primal.android.core.compose.PrimalTopAppBar
import net.primal.android.core.compose.SnackbarErrorHandler
import net.primal.android.core.compose.button.PrimalLoadingButton
import net.primal.android.core.compose.icons.PrimalIcons
import net.primal.android.core.compose.icons.primaliconpack.ArrowBack
import net.primal.android.core.compose.preview.PrimalPreview
import net.primal.android.core.utils.isValidEmail
import net.primal.android.core.utils.isValidUsername
import net.primal.android.profile.editor.ProfileEditorContract
import net.primal.android.profile.editor.ProfileEditorViewModel
import net.primal.android.theme.domain.PrimalTheme

@Composable
fun ProfileEditorScreen(viewModel: ProfileEditorViewModel, onClose: () -> Unit) {
    LaunchedEffect(viewModel, onClose) {
        viewModel.effect.collect {
            when (it) {
                is ProfileEditorContract.SideEffect.AccountSuccessfulyEdited -> onClose()
            }
        }
    }
    val state = viewModel.state.collectAsState()

    ProfileEditorScreen(
        state = state.value,
        eventPublisher = { viewModel.setEvent(it) },
        onClose = onClose,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditorScreen(
    state: ProfileEditorContract.UiState,
    eventPublisher: (ProfileEditorContract.UiEvent) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    SnackbarErrorHandler(
        error = state.error,
        snackbarHostState = snackbarHostState,
        errorMessageResolver = {
            when (it) {
                is ProfileEditorContract.UiState.EditProfileError.FailedToPublishMetadata ->
                    context.getString(R.string.profile_failed_to_publish_metadata)

                is ProfileEditorContract.UiState.EditProfileError.MissingRelaysConfiguration ->
                    context.getString(R.string.app_missing_relays_config)

                is ProfileEditorContract.UiState.EditProfileError.FailedToUploadImage ->
                    context.getString(R.string.app_failed_to_upload_image)

                is ProfileEditorContract.UiState.EditProfileError.InvalidLightningAddress -> {
                    val isValidLud16Format = it.lud16.isValidEmail()
                    val lud16Parts = it.lud16.split("@")
                    if (isValidLud16Format && lud16Parts.size == 2) {
                        context.getString(
                            R.string.app_invalid_lud16_address_user_not_found,
                            lud16Parts[0],
                            lud16Parts[1],
                        )
                    } else {
                        context.getString(R.string.app_invalid_lud16_address)
                    }
                }

                is ProfileEditorContract.UiState.EditProfileError.InvalidNostrVerificationAddress -> {
                    context.getString(R.string.app_invalid_nip05_address)
                }
            }
        },
        onErrorDismiss = { eventPublisher(ProfileEditorContract.UiEvent.DismissError) },
    )

    Scaffold(
        topBar = {
            PrimalTopAppBar(
                title = stringResource(id = R.string.profile_edit_profile_title),
                navigationIcon = PrimalIcons.ArrowBack,
                navigationIconContentDescription = stringResource(id = R.string.accessibility_back_button),
                onNavigationIconClick = {
                    onClose()
                },
            )
        },
        content = { paddingValues ->
            ProfileEditorContent(
                state = state,
                eventPublisher = eventPublisher,
                paddingValues = paddingValues,
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    )
}

@Composable
fun ProfileEditorContent(
    state: ProfileEditorContract.UiState,
    eventPublisher: (ProfileEditorContract.UiEvent) -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .padding(paddingValues = paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ProfileEditorForm(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxHeight()
                .weight(weight = 1f, fill = true),
            state = state,
            eventPublisher = eventPublisher,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .padding(horizontal = 32.dp),
        ) {
            PrimalLoadingButton(
                text = stringResource(id = R.string.profile_save_profile_button),
                enabled = !state.loading,
                loading = state.loading,
                onClick = {
                    keyboardController?.hide()
                    eventPublisher(ProfileEditorContract.UiEvent.SaveProfileEvent)
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
private fun ProfileEditorForm(
    modifier: Modifier,
    state: ProfileEditorContract.UiState,
    eventPublisher: (ProfileEditorContract.UiEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        ProfileHero(
            avatarUri = state.remoteAvatarUrl?.toUri() ?: state.localAvatarUri,
            bannerUri = state.remoteBannerUrl?.toUri() ?: state.localBannerUri,
            onBannerUriChange = {
                eventPublisher(ProfileEditorContract.UiEvent.BannerUriChangedEvent(bannerUri = it))
            },
            onAvatarUriChange = {
                eventPublisher(ProfileEditorContract.UiEvent.AvatarUriChangedEvent(avatarUri = it))
            },
        )
        Spacer(modifier = Modifier.height(32.dp))
        PrimalOutlinedTextField(
            header = stringResource(id = R.string.profile_editor_input_header_handle).uppercase(),
            value = state.username,
            onValueChange = {
                if (it.isValidUsername()) {
                    eventPublisher(ProfileEditorContract.UiEvent.UsernameChangedEvent(it))
                }
            },
            prefix = "@",
        )
        Spacer(modifier = Modifier.height(12.dp))
        PrimalOutlinedTextField(
            header = stringResource(id = R.string.profile_editor_input_header_display_name),
            value = state.displayName,
            onValueChange = {
                eventPublisher(ProfileEditorContract.UiEvent.DisplayNameChangedEvent(it))
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        PrimalOutlinedTextField(
            header = stringResource(id = R.string.profile_editor_input_header_website).uppercase(),
            value = state.website,
            onValueChange = {
                eventPublisher(ProfileEditorContract.UiEvent.WebsiteChangedEvent(it.trim()))
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        PrimalOutlinedTextField(
            header = stringResource(id = R.string.profile_editor_input_header_about_me).uppercase(),
            value = state.aboutMe,
            isMultiline = true,
            onValueChange = {
                eventPublisher(ProfileEditorContract.UiEvent.AboutMeChangedEvent(it))
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        PrimalOutlinedTextField(
            header = stringResource(
                id = R.string.profile_editor_input_header_bitcoin_lightning_address,
            ).uppercase(),
            value = state.lightningAddress,
            onValueChange = {
                eventPublisher(ProfileEditorContract.UiEvent.LightningAddressChangedEvent(it.trim()))
            },
        )
        Spacer(modifier = Modifier.height(12.dp))
        PrimalOutlinedTextField(
            header = stringResource(id = R.string.profile_editor_input_header_nip_05).uppercase(),
            value = state.nip05Identifier,
            onValueChange = {
                eventPublisher(ProfileEditorContract.UiEvent.Nip05IdentifierChangedEvent(it.trim()))
            },
        )
    }
}

@Preview
@Composable
fun PreviewEditProfileScreen() {
    PrimalPreview(primalTheme = PrimalTheme.Sunset) {
        ProfileEditorScreen(
            state = ProfileEditorContract.UiState(
                username = "Random Handle",
                displayName = "Random Display Name",
                aboutMe = "About me",
                website = "http://www.example.com",
                lightningAddress = "tralala@getalby.com",
                nip05Identifier = "tralala@getalby.com",
            ),
            eventPublisher = {},
            onClose = {},
        )
    }
}
