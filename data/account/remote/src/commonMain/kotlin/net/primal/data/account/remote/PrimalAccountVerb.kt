package net.primal.data.account.remote

enum class PrimalAccountVerb(val id: String) {
    RECOMMENDED_BLOSSOM_SERVERS("get_recommended_blossom_servers"),
    UPDATE_PUSH_NOTIFICATION_TOKEN("update_push_notification_token"),
    UPDATE_PUSH_NOTIFICATION_TOKEN_FOR_NIP46("update_push_notification_token_for_nip46"),
}
