package net.primal.data.repository.mappers.local

import net.primal.data.local.dao.bookmarks.PublicBookmark as PublicBookmarkPO
import net.primal.domain.model.PublicBookmark as PublicBookmarkDO

fun PublicBookmarkPO.asPublicBookmark(): PublicBookmarkDO {
    return PublicBookmarkDO(
        tagValue = this.tagValue,
        tagType = this.tagType,
        bookmarkType = this.bookmarkType,
        ownerId = this.ownerId,
    )
}
