package net.primal.android.networking.primal

enum class PrimalVerb(val identifier: String) {
    FOLLOW_LIST("contact_list"),
    USER_INFOS("user_infos"),
    USER_PROFILE("user_profile"),
    USER_PROFILE_FOLLOWED_BY("user_profile_followed_by"),
    USER_FOLLOWERS("user_followers"),
    USER_RELAYS("get_user_relays"),
    RECOMMENDED_USERS("get_recommended_users"),
    GET_APP_SETTINGS("get_app_settings"),
    GET_DEFAULT_APP_SETTINGS("get_default_app_settings"),
    SET_APP_SETTINGS("set_app_settings"),
    THREAD_VIEW("thread_view"),
    NOTES("events"),
    USER_SEARCH("user_search"),
    IMPORT_EVENTS("import_events"),
    GET_NOTIFICATIONS("get_notifications"),
    GET_LAST_SEEN_NOTIFICATIONS("get_notifications_seen"),
    SET_LAST_SEEN_NOTIFICATIONS("set_notifications_seen"),
    NEW_NOTIFICATIONS_COUNT("notification_counts_2"),
    UPLOAD_CHUNK("upload_chunk"),
    UPLOAD_COMPLETE("upload_complete"),
    MUTE_LIST("mutelist"),
    GET_DM_CONTACTS("get_directmsg_contacts"),
    GET_DMS("get_directmsgs"),
    MARK_DM_CONVERSATION_AS_READ("reset_directmsg_count"),
    MARK_ALL_DMS_AS_READ("reset_directmsg_counts"),
    NEW_DMS_COUNT("directmsg_count_2"),
    WALLET("wallet"),
    WALLET_MONITOR("wallet_monitor_2"),
    DEFAULT_RELAYS("get_default_relays"),
    IS_USER_FOLLOWING("is_user_following"),
    GET_BOOKMARKS_LIST("get_bookmarks"),
    EVENT_ZAPS("event_zaps_by_satszapped"),
    EVENT_ACTIONS("event_actions"),
    BROADCAST_EVENTS("broadcast_events"),
    BLOG_THREAD_VIEW("long_form_content_thread_view"),
    MEGA_FEED_DIRECTIVE("mega_feed_directive"),
    GET_FEATURED_DVM_FEEDS("get_featured_dvm_feeds"),
    GET_DEFAULT_APP_SUB_SETTINGS("get_default_app_subsettings"),
    GET_APP_SUB_SETTINGS("get_app_subsettings"),
    SET_APP_SUB_SETTINGS("set_app_subsettings"),
    EXPLORE_TOPICS("explore_topics"),
    EXPLORE_PEOPLE("explore_people"),
    EXPLORE_ZAPS("explore_zaps"),
    MEMBERSHIP_NAME_AVAILABLE("membership_name_available"),
}
