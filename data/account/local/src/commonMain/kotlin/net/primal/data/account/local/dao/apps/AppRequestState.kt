package net.primal.data.account.local.dao.apps

enum class AppRequestState {
    PendingUserAction,
    PendingResponse,
    Approved,
    Rejected,
}
