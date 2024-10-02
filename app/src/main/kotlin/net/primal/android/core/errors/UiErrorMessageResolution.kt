package net.primal.android.core.errors

import android.content.Context
import net.primal.android.R

fun UiError.resolveUiErrorMessage(context: Context): String {
    return when (this) {
        UiError.InvalidNaddr -> context.getString(
            R.string.app_error_invalid_naddr,
        )

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

        is UiError.FailedToPublishRepostEvent -> context.getString(
            R.string.post_action_repost_failed,
        )

        is UiError.FailedToMuteUser -> context.getString(
            R.string.app_error_muting_user,
        )

        is UiError.MissingRelaysConfiguration -> context.getString(
            R.string.app_missing_relays_config,
        )

        is UiError.GenericError -> context.getString(R.string.app_generic_error)
    }
}
