package net.primal.android.core.errors

import android.content.Context
import net.primal.android.R

@Suppress("CyclomaticComplexMethod")
fun UiError.resolveUiErrorMessage(context: Context): String {
    return when (this) {
        UiError.InvalidNaddr -> context.getString(
            R.string.app_error_invalid_naddr,
        )

        UiError.MissingPrivateKey -> context.getString(R.string.app_error_missing_private_key)

        is UiError.InvalidZapRequest -> context.getString(
            R.string.post_action_invalid_zap_request,
        )

        is UiError.MissingLightningAddress -> context.getString(
            R.string.post_action_missing_lightning_address,
        )

        is UiError.FailedToPublishZapEvent -> context.getString(
            R.string.post_action_zap_failed,
        )

        is UiError.FailedToPublishLikeEvent -> context.getString(
            R.string.post_action_like_failed,
        )

        is UiError.FailedToPublishDeleteEvent -> context.getString(R.string.post_action_delete_failed)

        is UiError.FailedToPublishRepostEvent -> context.getString(
            R.string.post_action_repost_failed,
        )

        is UiError.FailedToMuteUser -> context.getString(R.string.app_error_muting_user)

        is UiError.FailedToUnmuteUser -> context.getString(R.string.app_error_unmuting_user)

        is UiError.FailedToFollowUser -> context.getString(R.string.app_error_unable_to_follow_profile)

        is UiError.FailedToUnfollowUser -> context.getString(R.string.app_error_unable_to_unfollow_profile)

        is UiError.MissingRelaysConfiguration -> context.getString(
            R.string.app_missing_relays_config,
        )

        is UiError.FailedToAddToFeed -> context.getString(R.string.app_error_adding_to_feed)

        is UiError.GenericError -> context.getString(R.string.app_generic_error)

        is UiError.FailedToRemoveFeed -> context.getString(R.string.app_error_removing_feed)

        UiError.NostrSignUnauthorized -> context.getString(R.string.app_error_sign_unauthorized)

        is UiError.FailedToRestoreDefaultBlossomServer -> context.getString(
            R.string.app_error_restore_default_blossom_server,
        )

        is UiError.FailedToUpdateBlossomServer -> context.getString(
            R.string.app_error_unable_to_update_blossom_server_list,
        )

        is UiError.FailedToMuteThread -> context.getString(R.string.app_error_unmuting_thread)

        is UiError.FailedToUnmuteThread -> context.getString(R.string.app_error_unmuting_thread)

        is UiError.FailedToMuteHashtag -> context.getString(R.string.app_error_muting_hashtag)

        is UiError.FailedToMuteWord -> context.getString(R.string.app_error_muting_word)

        is UiError.FailedToUnmuteHashtag -> context.getString(R.string.app_error_unmuting_hashtag)

        is UiError.FailedToUnmuteWord -> context.getString(R.string.app_error_unmuting_word)

        is UiError.NetworkError -> context.getString(R.string.app_error_network)

        is UiError.SignatureError -> when (this.error) {
            SignatureUiError.SigningKeyNotFound -> context.getString(R.string.app_npub_login_error)
            SignatureUiError.SigningRejected -> context.getString(R.string.app_error_sign_unauthorized)
        }

        is UiError.FailedToFetchMuteList -> context.getString(R.string.app_error_fetching_mute_list)

        is UiError.FailedToFetchNote -> context.getString(R.string.app_error_fetching_note)

        is UiError.FailedToFetchTopNoteZaps -> context.getString(R.string.app_error_fetching_top_note_zaps)
    }
}
