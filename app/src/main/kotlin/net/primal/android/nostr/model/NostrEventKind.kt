package net.primal.android.nostr.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class NostrEventKind(val value: Int? = null) {
    @SerialName("-1") Unknown(value = -1),

    @SerialName("0") Metadata(value = 0),
    @SerialName("1") ShortTextNote(value = 1),
    @SerialName("2") RecommendRelay(value = 2),
    @SerialName("3") Contacts(value = 3),
    @SerialName("4") EncryptedDirectMessages(value = 4),
    @SerialName("5") EventDeletion(value = 5),
    @SerialName("6") Reposts(value = 6),
    @SerialName("7") Reaction(value = 7),
    @SerialName("8") BadgeAward(value = 8),
    @SerialName("40") ChannelCreation(value = 40),
    @SerialName("41") ChannelMetadata(value = 41),
    @SerialName("42") ChannelMessage(value = 42),
    @SerialName("43") ChannelHideMessage(value = 43),
    @SerialName("44") ChannelMuteUser(value = 44),
    @SerialName("1063") FileMetadata(value = 1063),
    @SerialName("1984") Reporting(value = 1984),
    @SerialName("9734") ZapRequest(value = 9734),
    @SerialName("9735") Zap(value = 9735),
    @SerialName("10000") MuteList(value = 10000),
    @SerialName("10001") PinList(value = 10001),
    @SerialName("10002") RelayListMetadata(value = 10002),
    @SerialName("13194") WalletInfo(value = 13194),
    @SerialName("22242") ClientAuthentication(value = 22242),
    @SerialName("23194") WalletRequest(value = 23194),
    @SerialName("23195") WalletResponse(value = 23195),
    @SerialName("24133") NostrConnect(value = 24133),
    @SerialName("30000") CategorizedPeopleList(value = 30000),
    @SerialName("30001") CategorizedBookmarkList(value = 30001),
    @SerialName("30008") ProfileBadges(value = 30008),
    @SerialName("30009") BadgeDefinition(value = 30009),
    @SerialName("30023") LongFormContent(value = 30023),
    @SerialName("30078") ApplicationSpecificData(value = 30078),

    // Primal specific open-sourced
    @SerialName("10000100") PrimalEventStats(value = 10000100),
    @SerialName("10000101") PrimalNetStats(value = 10000101),
    @SerialName("10000105") PrimalUserProfile(value = 10000105),
    @SerialName("10000107") PrimalReferencedEvent(value = 10000107),

    // Primal specific internal
    @SerialName("10000102") PrimalExploreLegendCounts(value = 10000102),
    @SerialName("10000103") PrimalPrimalSettings(value = 10000103),
    @SerialName("10000108") PrimalUserScores(value = 10000108),
    @SerialName("10000109") PrimalRelays(value = 10000109),
    @SerialName("10000110") PrimalNotification(value = 10000110),
    @SerialName("10000111") PrimalNotificationsSeenUntil(value = 10000111),
    @SerialName("10000112") PrimalNotificationsSummary(value = 10000112),
    @SerialName("10000113") PrimalPaging(value = 10000113),
    @SerialName("10000114") PrimalMediaMapping(value = 10000114),
    @SerialName("10000999") PrimalAppState(value = 10000999),
}