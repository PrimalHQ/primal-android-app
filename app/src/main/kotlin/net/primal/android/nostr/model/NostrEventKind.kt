package net.primal.android.nostr.model

import kotlinx.serialization.Serializable

@Serializable
enum class NostrEventKind(val value: Int? = null) {
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
    MuteList(value = 10000),
    PinList(value = 10001),
    RelayListMetadata(value = 10002),
    WalletInfo(value = 13194),
    ClientAuthentication(value = 22242),
    WalletRequest(value = 23194),
    WalletResponse(value = 23195),
    NostrConnect(value = 24133),
    CategorizedPeopleList(value = 30000),
    CategorizedBookmarkList(value = 30001),
    ProfileBadges(value = 30008),
    BadgeDefinition(value = 30009),
    LongFormContent(value = 30023),
    ApplicationSpecificData(value = 30078),
    PrimalEventStats(value = 10000100),
    PrimalNetStats(value = 10000101),
    PrimalExploreLegendCounts(value = 10000102),
    PrimalDefaultSettings(value = 10000103),
    PrimalUserProfile(value = 10000105),
    PrimalReferencedEvent(value = 10000107),
    PrimalUserScores(value = 10000108),
    PrimalRelays(value = 10000109),
    PrimalNotification(value = 10000110),
    PrimalNotificationsSeenUntil(value = 10000111),
    PrimalNotificationsSummary(value = 10000112),
    PrimalPaging(value = 10000113),
    PrimalMediaMapping(value = 10000114),
    PrimalAppState(value = 10000999),
    Unknown(value = -1);

    companion object {
        fun valueOf(value: Int): NostrEventKind =
            enumValues<NostrEventKind>().find { it.value == value } ?: Unknown
    }

}