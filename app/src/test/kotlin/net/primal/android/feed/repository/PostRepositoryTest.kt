package net.primal.android.feed.repository

import kotlinx.coroutines.test.runTest
import org.junit.Test

class PostRepositoryTest {

    @Test
    fun `likePost updates like stats in database if reaction was published`() = runTest {

    }

    @Test
    fun `likePost reverts like stats in database if reaction was not published`() = runTest {

    }

    @Test
    fun `likePost throws exception if reaction was not published`() = runTest {

    }

    @Test
    fun `likePost does not update like stats in database if post was liked by user`() = runTest {

    }

    @Test
    fun `repostPost updates repost stats in database if reaction was published`() = runTest {

    }

    @Test
    fun `repostPost reverts repost stats in database if reaction was not published`() = runTest {

    }

    @Test
    fun `repostPost throws exception if reaction was not published`() = runTest {

    }

    @Test
    fun `publishShortTextNote completes if post was published`() = runTest {

    }

    @Test
    fun `publishShortTextNote throws exception if post was not published`() = runTest {

    }
}
