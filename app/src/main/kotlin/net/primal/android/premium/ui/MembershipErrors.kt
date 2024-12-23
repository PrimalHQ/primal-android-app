package net.primal.android.premium.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import net.primal.android.R
import net.primal.android.premium.domain.MembershipError

@Composable
fun MembershipError.toHumanReadableString(): String {
    return when (this) {
        is MembershipError.FailedToCancelSubscription ->
            stringResource(R.string.premium_error_failed_to_cancel_subscription)

        is MembershipError.FailedToProcessSubscriptionPurchase ->
            stringResource(R.string.premium_error_failed_to_process_subscription_purchase)

        MembershipError.PlaySubscriptionPurchaseNotFound ->
            stringResource(R.string.premium_error_play_subscription_purchase_not_found)

        is MembershipError.FailedToApplyNostrAddress ->
            stringResource(id = R.string.app_error_unable_to_set_nostr_address)

        MembershipError.FailedToApplyLightningAddress ->
            stringResource(id = R.string.app_error_unable_to_set_lightning_address)

        MembershipError.ProfileMetadataNotFound ->
            stringResource(id = R.string.app_generic_error)
    }
}
