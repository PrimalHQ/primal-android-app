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
                    "\"kinds\":[1]," +
                    "\"notes\":\"authored\"," +
                    "\"pubkey\":\"$profileId\"}"
            }

            AuthoredReplies -> {
                "{\"id\":\"feed\"," +
                    "\"include_replies\":true," +
                    "\"kinds\":[1]," +
                    "\"notes\":\"authored\"," +
                    "\"pubkey\":\"$profileId\"}"
            }

            AuthoredArticles -> {
                "{\"kinds\":[20,30023]," +
                    "\"notes\":\"authored\"," +
                    "\"pubkey\":\"$profileId\"}"
            }

            AuthoredMedia -> {
                "{\"id\":\"feed\"," +
                    "\"kinds\":[1]," +
                    "\"notes\":\"user_media_thumbnails\"," +
                    "\"pubkey\":\"$profileId\"}"
            }
        }
    }
}
