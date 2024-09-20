package net.primal.android.profile.domain

enum class ProfileFeedSpec {
    AuthoredNotes,
    AuthoredReplies,
    AuthoredArticles,
    AuthoredMedia,
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

            AuthoredArticles -> {
                "{\"kind\":\"reads\"," +
                    "\"notes\":\"authored\"," +
                    "\"pubkey\":\"$profileId\"}"
            }

            AuthoredMedia -> {
                "{\"id\":\"feed\"," +
                    "\"kind\":\"notes\"," +
                    "\"notes\":\"user_media_thumbnails\"," +
                    "\"pubkey\":\"$profileId\"}"
            }
        }
    }
}
