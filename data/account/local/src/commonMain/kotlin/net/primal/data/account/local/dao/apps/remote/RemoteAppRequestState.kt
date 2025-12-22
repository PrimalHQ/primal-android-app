package net.primal.data.account.local.dao.apps.remote

enum class RemoteAppRequestState {
    PendingUserAction,
    PendingResponse,
    Approved,
    Rejected,
}
