package net.primal.android.explore.home.people

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.explore.home.people.model.FollowPackUi

interface ExplorePeopleContract {
    data class UiState(
        val followPacks: Flow<PagingData<FollowPackUi>>,
    )
}
