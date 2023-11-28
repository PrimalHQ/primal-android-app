package net.primal.android.auth.create.ui.steps

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.auth.create.CreateAccountContract
import net.primal.android.core.compose.profile.PrimalOutlinedTextField
import net.primal.android.core.compose.profile.ProfileHero
import net.primal.android.core.utils.isValidUsername

@Composable
fun CreateAccountStep(state: CreateAccountContract.UiState, eventPublisher: (CreateAccountContract.UiEvent) -> Unit) {
    ProfileHero(
        avatarUri = state.avatarUri,
        bannerUri = state.bannerUri,
        onBannerUriChange = {
            eventPublisher(CreateAccountContract.UiEvent.BannerUriChangedEvent(bannerUri = it))
        },
        onAvatarUriChange = {
            eventPublisher(CreateAccountContract.UiEvent.AvatarUriChangedEvent(avatarUri = it))
        },
    )
    Spacer(modifier = Modifier.height(32.dp))
    PrimalOutlinedTextField(
        header = stringResource(id = R.string.create_input_header_handle).uppercase(),
        value = state.username,
        onValueChange = {
            if (it.isValidUsername()) {
                eventPublisher(CreateAccountContract.UiEvent.UsernameChangedEvent(it.trim()))
            }
        },
        isRequired = true,
        prefix = "@",
    )
    Spacer(modifier = Modifier.height(12.dp))
    PrimalOutlinedTextField(
        header = stringResource(id = R.string.create_input_header_display_name).uppercase(),
        value = state.displayName,
        onValueChange = {
            eventPublisher(CreateAccountContract.UiEvent.DisplayNameChangedEvent(it))
        },
    )
    Spacer(modifier = Modifier.height(12.dp))
    PrimalOutlinedTextField(
        header = stringResource(id = R.string.create_input_header_website).uppercase(),
        value = state.website,
        onValueChange = {
            eventPublisher(
                CreateAccountContract.UiEvent.WebsiteChangedEvent(it.trim()),
            )
        },
    )
    Spacer(modifier = Modifier.height(12.dp))
    PrimalOutlinedTextField(
        header = stringResource(id = R.string.create_input_header_about_me).uppercase(),
        value = state.aboutMe,
        isMultiline = true,
        onValueChange = { eventPublisher(CreateAccountContract.UiEvent.AboutMeChangedEvent(it)) },
    )
    Spacer(modifier = Modifier.height(12.dp))
    PrimalOutlinedTextField(
        header = stringResource(
            id = R.string.create_input_header_bitcoin_lightning_address,
        ).uppercase(),
        value = state.lightningAddress,
        onValueChange = {
            eventPublisher(
                CreateAccountContract.UiEvent.LightningAddressChangedEvent(it.trim()),
            )
        },
    )
    Spacer(modifier = Modifier.height(12.dp))
    PrimalOutlinedTextField(
        header = stringResource(id = R.string.create_input_header_nip_05).uppercase(),
        value = state.nip05Identifier,
        onValueChange = {
            eventPublisher(
                CreateAccountContract.UiEvent.Nip05IdentifierChangedEvent(it.trim()),
            )
        },
    )
}
