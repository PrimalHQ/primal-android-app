package net.primal.android.auth.create.ui.steps

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.primal.android.R
import net.primal.android.auth.create.CreateContract
import net.primal.android.core.compose.profile.InputField
import net.primal.android.core.compose.profile.ProfileHero

@Composable
fun CreateAccountStep(
    state: CreateContract.UiState,
    eventPublisher: (CreateContract.UiEvent) -> Unit,
) {
    ProfileHero(onBannerUriChange = {
        eventPublisher(CreateContract.UiEvent.BannerUriChangedEvent(bannerUri = it))
    }, onAvatarUriChange = {
        eventPublisher(CreateContract.UiEvent.AvatarUriChangedEvent(avatarUri = it))
    })
    Spacer(modifier = Modifier.height(32.dp))
    InputField(
        header = stringResource(id = R.string.create_input_header_display_name),
        value = state.name,
        onValueChange = { eventPublisher(CreateContract.UiEvent.NameChangedEvent(it.trim())) },
        isRequired = true
    )
    Spacer(modifier = Modifier.height(12.dp))
    InputField(
        header = stringResource(id = R.string.create_input_header_handle),
        value = state.handle,
        onValueChange = { eventPublisher(CreateContract.UiEvent.HandleChangedEvent(it.trim())) },
        isRequired = true,
        prefix = "@"
    )
    Spacer(modifier = Modifier.height(12.dp))
    InputField(header = stringResource(id = R.string.create_input_header_website),
        value = state.website,
        onValueChange = { eventPublisher(CreateContract.UiEvent.WebsiteChangedEvent(it.trim())) })
    Spacer(modifier = Modifier.height(12.dp))
    InputField(header = stringResource(id = R.string.create_input_header_about_me),
        value = state.aboutMe,
        isMultiline = true,
        onValueChange = { eventPublisher(CreateContract.UiEvent.AboutMeChangedEvent(it)) })
    Spacer(modifier = Modifier.height(12.dp))
    InputField(header = stringResource(id = R.string.create_input_header_bitcoin_lightning_address),
        value = state.lightningAddress,
        onValueChange = { eventPublisher(CreateContract.UiEvent.LightningAddressChangedEvent(it)) })
    Spacer(modifier = Modifier.height(12.dp))
    InputField(header = stringResource(id = R.string.create_input_header_nip_05),
        value = state.nip05Identifier,
        onValueChange = { eventPublisher(CreateContract.UiEvent.Nip05IdentifierChangedEvent(it)) })
}