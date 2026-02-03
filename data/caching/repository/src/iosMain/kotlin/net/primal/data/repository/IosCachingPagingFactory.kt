package net.primal.data.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.IosPagingFactory
import net.primal.IosPagingPresenter
import net.primal.IosPagingSnapshot
import net.primal.domain.events.EventZap
import net.primal.domain.explore.FollowPack
import net.primal.domain.messages.DMConversation
import net.primal.domain.messages.DirectMessage
import net.primal.domain.notifications.Notification
import net.primal.domain.posts.FeedPost
import net.primal.domain.reads.Article

object IosCachingPagingFactory {

    // FeedPost
    fun createFeedPostPresenter(pagingFlow: Flow<PagingData<FeedPost>>): IosPagingPresenter<FeedPost> {
        return IosPagingFactory.createPresenter(pagingFlow)
    }

    fun createFeedPostSnapshot(pagingFlow: Flow<PagingData<FeedPost>>): IosPagingSnapshot<FeedPost> {
        return IosPagingFactory.createSnapshot(pagingFlow)
    }

    // Notification
    fun createNotificationPresenter(pagingFlow: Flow<PagingData<Notification>>): IosPagingPresenter<Notification> {
        return IosPagingFactory.createPresenter(pagingFlow)
    }

    fun createNotificationSnapshot(pagingFlow: Flow<PagingData<Notification>>): IosPagingSnapshot<Notification> {
        return IosPagingFactory.createSnapshot(pagingFlow)
    }

    // EventZap
    fun createEventZapPresenter(pagingFlow: Flow<PagingData<EventZap>>): IosPagingPresenter<EventZap> {
        return IosPagingFactory.createPresenter(pagingFlow)
    }

    fun createEventZapSnapshot(pagingFlow: Flow<PagingData<EventZap>>): IosPagingSnapshot<EventZap> {
        return IosPagingFactory.createSnapshot(pagingFlow)
    }

    // Article
    fun createArticlePresenter(pagingFlow: Flow<PagingData<Article>>): IosPagingPresenter<Article> {
        return IosPagingFactory.createPresenter(pagingFlow)
    }

    fun createArticleSnapshot(pagingFlow: Flow<PagingData<Article>>): IosPagingSnapshot<Article> {
        return IosPagingFactory.createSnapshot(pagingFlow)
    }

    // FollowPack
    fun createFollowPackPresenter(pagingFlow: Flow<PagingData<FollowPack>>): IosPagingPresenter<FollowPack> {
        return IosPagingFactory.createPresenter(pagingFlow)
    }

    fun createFollowPackSnapshot(pagingFlow: Flow<PagingData<FollowPack>>): IosPagingSnapshot<FollowPack> {
        return IosPagingFactory.createSnapshot(pagingFlow)
    }

    // DMConversation
    fun createDMConversationPresenter(
        pagingFlow: Flow<PagingData<DMConversation>>,
    ): IosPagingPresenter<DMConversation> {
        return IosPagingFactory.createPresenter(pagingFlow)
    }

    fun createDMConversationSnapshot(pagingFlow: Flow<PagingData<DMConversation>>): IosPagingSnapshot<DMConversation> {
        return IosPagingFactory.createSnapshot(pagingFlow)
    }

    // DirectMessage
    fun createDirectMessagePresenter(pagingFlow: Flow<PagingData<DirectMessage>>): IosPagingPresenter<DirectMessage> {
        return IosPagingFactory.createPresenter(pagingFlow)
    }

    fun createDirectMessageSnapshot(pagingFlow: Flow<PagingData<DirectMessage>>): IosPagingSnapshot<DirectMessage> {
        return IosPagingFactory.createSnapshot(pagingFlow)
    }
}
