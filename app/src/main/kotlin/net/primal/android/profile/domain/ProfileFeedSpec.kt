package net.primal.android.profile.domain

enum class ProfileFeedSpec {
    AuthoredNotes,
    AuthoredReplies,
    ;

    fun buildSpec(profileId: String): String {
        return when (this) {
            AuthoredNotes -> {
                "{\"id\":\"feed\"," +
                    "\"kind\":\"notes\"," +
                    "\"notes\":\"authored\"," +
                    "\"pubkey\":\"$profileId\"}"
            }

            AuthoredReplies -> {
                "{\"id\":\"feed\"," +
                    "\"include_replies\":true," +
                    "\"kind\":\"notes\"," +
                    "\"notes\":\"authored\"," +
                    "\"pubkey\":\"$profileId\"}"
            }
        }
    }
}
