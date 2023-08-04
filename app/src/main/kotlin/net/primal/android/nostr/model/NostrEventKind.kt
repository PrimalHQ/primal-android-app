package net.primal.android.nostr.model

import kotlinx.serialization.Serializable

@Serializable
enum class NostrEventKind(val value: Int) {
    Metadata(value = 0),
    ShortTextNote(value = 1),
    RecommendRelay(value = 2),
    Contacts(value = 3),
    EncryptedDirectMessages(value = 4),
    EventDeletion(value = 5),
    Reposts(value = 6),
    Reaction(value = 7),
    BadgeAward(value = 8),
    ChannelCreation(value = 40),
    ChannelMetadata(value = 41),
    ChannelMessage(value = 42),
    ChannelHideMessage(value = 43),
    ChannelMuteUser(value = 44),
    FileMetadata(value = 1063),
    Reporting(value = 1984),
    ZapRequest(value = 9734),
    Zap(value = 9735),
    MuteList(value = 10_000),
    PinList(value = 10_001),
    RelayListMetadata(value = 10_002),
    WalletInfo(value = 13_194),
    ClientAuthentication(value = 22_242),
    WalletRequest(value = 23_194),
    WalletResponse(value = 23_195),
    NostrConnect(value = 24_133),
    CategorizedPeopleList(value = 30_000),
    CategorizedBookmarkList(value = 30_001),
    ProfileBadges(value = 30_008),
    BadgeDefinition(value = 30_009),
    LongFormContent(value = 30_023),
    ApplicationSpecificData(value = 30_078),
    PrimalEventStats(value = 10_000_100),
    PrimalNetStats(value = 10_000_101),
    PrimalExploreLegendCounts(value = 10_000_102),
    PrimalDefaultSettings(value = 10_000_103),
    PrimalUserProfileStats(value = 10_000_105),
    PrimalReferencedEvent(value = 10_000_107),
    PrimalUserScores(value = 10_000_108),
    PrimalRelays(value = 10_000_109),
    PrimalNotification(value = 10_000_110),
    PrimalNotificationsSeenUntil(value = 10_000_111),
    PrimalNotificationsSummary(value = 10_000_112),
    PrimalPaging(value = 10_000_113),
    PrimalMediaMapping(value = 10_000_114),
    PrimalEventUserStats(value = 10_000_115),
    PrimalTrendingHashtags(value = 10_000_116),
    PrimalEventResources(value = 10_000_119),
    PrimalAppState(value = 10_000_999),
    Unknown(value = -1);

    companion object {
        fun valueOf(value: Int): NostrEventKind =
            enumValues<NostrEventKind>().find { it.value == value } ?: Unknown
    }

}
