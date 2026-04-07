package net.primal.android.main.explore.people

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import net.primal.android.main.explore.people.model.FollowPackUi

interface ExplorePeopleContract {
    data class UiState(
        val followPacks: Flow<PagingData<FollowPackUi>>,
    )
}
